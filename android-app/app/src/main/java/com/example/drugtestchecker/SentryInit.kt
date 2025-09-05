package com.example.drugtestchecker

import android.app.Application
import io.sentry.Attachment
import io.sentry.Sentry
import io.sentry.SentryOptions
import java.io.File

class SentryInit : Application() {
    override fun onCreate() {
        super.onCreate()

        val dsn = BuildConfig.SENTRY_DSN
        if (dsn.isNullOrBlank()) return

        // Minimal Sentry init to avoid API surface mismatches in beforeSend/attachments
        Sentry.init { options: SentryOptions ->
            options.dsn = dsn
            options.isDebug = false
            // no beforeSend here; we perform redaction explicitly in ReportHelper to avoid SDK API mismatches
            // set release to help grouping / traceability
            try {
                val pm = applicationContext.packageManager
                val pkg = applicationContext.packageName
                val info = pm.getPackageInfo(pkg, 0)
                val vName = info.versionName ?: "unspecified"
                val vCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) info.longVersionCode.toString() else info.versionCode.toString()
                options.release = "$pkg@$vName($vCode)"
            } catch (_: Exception) {
            }
        }
    }

    companion object {
        fun attachFileIfExists(file: File?) {
            try {
                if (file != null && file.exists()) {
                    Sentry.configureScope { scope ->
                        // use Attachment(filePath, filename) constructor which is available in this SDK
                        scope.addAttachment(Attachment(file.absolutePath, file.name))
                    }
                }
            } catch (_: Exception) {
            }
        }
    }
}
