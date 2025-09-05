package com.example.drugtestchecker

import io.sentry.Sentry
import io.sentry.SentryLevel
import java.io.File
import io.sentry.protocol.User

object ReportHelper {
    fun reportParseFailure(context: android.content.Context, message: String, csvSnippet: String?, htmlSnippet: String?): Boolean {
        try {
            fun redact(s: String?): String? {
                if (s == null) return null
                var out = s.replace(Regex("\\b\\d{7}\\b"), "*******")
                out = out.replace(Regex("\\b\\d{4}\\b"), "****")
                return out
            }

            // create temp files for attachments
            var csvFile: File? = null
            var htmlFile: File? = null
            csvSnippet?.let {
                csvFile = File.createTempFile("dtc_csv_snippet", ".txt", context.cacheDir)
                csvFile?.writeText(redact(it) ?: it)
            }
            htmlSnippet?.let {
                htmlFile = File.createTempFile("dtc_html_snippet", ".html", context.cacheDir)
                htmlFile?.writeText(redact(it) ?: it)
            }

            // attach if exists
            SentryInit.attachFileIfExists(csvFile)
            SentryInit.attachFileIfExists(htmlFile)

            // add some helpful device/app tags and user
            Sentry.configureScope { scope ->
                try {
                    val pkg = context.packageName
                    val pm = context.packageManager
                    val info = pm.getPackageInfo(pkg, 0)
                    scope.setTag("app.package", pkg)
                    scope.setTag("app.version", info.versionName ?: "unspecified")
                    scope.setTag("device.model", android.os.Build.MODEL ?: "unknown")
                    scope.setTag("device.manufacturer", android.os.Build.MANUFACTURER ?: "unknown")
                    scope.setTag("android.sdk", android.os.Build.VERSION.SDK_INT.toString())
                    val user = User()
                    user.id = "device:${android.os.Build.SERIAL ?: android.os.Build.ID}"
                    scope.user = user
                } catch (_: Exception) { }
            }

            // capture message (redact sensitive content)
            val redMessage = ("Parse failure: " + (redact(message) ?: message))
            Sentry.captureMessage(redMessage, SentryLevel.WARNING)
            return true

        } catch (e: Exception) {
            // swallow
            return false
        }
        return true
    }

    fun reportException(e: Throwable): Boolean {
        return try {
            Sentry.captureException(e)
            true
        } catch (_: Exception) { false }
    }
}
