# Drug Test Checker (Android)

Small on-device Android app that checks https://drugtestcheck.com daily at 03:10 America/Boise for whether a saved profile is required to test that day.

Features
- Runs a daily exact alarm (03:10 America/Boise) when allowed; falls back to best-effort scheduling otherwise.
- Stores profiles locally (PIN and last4) in SharedPreferences.
- Logs results locally to `drug_test_logs.csv` in the app files directory.
- Sends actionable notifications (Acknowledge / Dismiss). Acknowledge marks the profile/day as seen; required-to-test results produce a persistent notification until acknowledged.
- Debugging: unrecognized responses are appended to `dtc_debug.txt` for diagnosis.

Security & privacy
- All credentials and logs stay on-device. The app does POST to the public site `drugtestcheck.com` using the saved profile to query status. No cloud services are used.

Quick build & install (developer)
1. Ensure Android SDK + a connected device (or emulator) are available.
2. From the repo root:

```bash
cd android-app
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

3. Open the app, add a profile (7-digit PIN, 4-letter last4), then schedule the daily alarm or use "Immediate Test" to schedule a debug run in ~1 minute.

Files written on device
- `files/drug_test_logs.csv` — CSV with header `timestamp,pin,last4,message`.
- `files/dtc_debug.txt` — debug dump when the parser can't recognize the response.

Testing notes
- The app uses Jsoup to POST to the site. If you see "No recognizable response" in the CSV, check `dtc_debug.txt` for label texts and an HTML snapshot to refine parsing.
- You can force a debug-required notification by broadcasting the receiver with `--ez debug_force_required true` (used during development).

Contributing / publishing
- This repository intentionally keeps all sensitive data local to the device. If you plan to publish to GitHub, double-check that you do not accidentally commit keystore files or local secrets.

License: (add your preferred license)
# Drug Test Checker

Native Android app only (Kotlin)

This repository now contains a standalone Android app under `android-app/` that performs a daily exact alarm at 03:10 America/Boise, scrapes drugtestcheck.com, logs results locally, and shows notifications.

Whats included:

- `android-app/` - full Android Studio / Gradle project (Kotlin)

How it runs reliably at 03:10am:

- The app uses `AlarmManager.setExactAndAllowWhileIdle()` and reschedules every day. This uses the device's exact alarm API to reliably trigger at the specified wall-clock time (Android only). On Android 12+ the app should request `SCHEDULE_EXACT_ALARM` permission; some OEMs may still impose background restrictions.

Quick development steps (on your machine):

1. Open `android-app/` in Android Studio.
2. Build and run on your device or emulator.
3. Use the app UI to enter a 7-digit PIN and Last4 (first 4 letters of your last name) and press "Schedule Daily 03:10 Boise" to schedule the alarm.

Notes:

- Credentials and logs are stored locally on the device (SharedPreferences + internal file storage). No external server required.
- I removed the Expo/React Native code to avoid confusion; this is now a single native Android app.

Testing and build notes:

- PIN length update: the PIN is 7 digits (was previously 6 digits in earlier drafts).

- To test immediate alarm: open the app and tap "Schedule Immediate Test (1 min)"; the app will schedule a test in one minute and you should receive a notification.
- For reliable scheduling on Android 12+, grant SCHEDULE_EXACT_ALARM permission when the app opens the settings screen after tapping "Schedule Daily 03:10 Boise".
- To build an APK from the command line (if you have Android SDK / JDK configured):

- To build an APK from the command line (if you have Android SDK / JDK configured):

    - Open a terminal in `android-app/` and run:

        ./gradlew assembleDebug

    - The APK will be in `app/build/outputs/apk/debug/app-debug.apk`.

CI build (recommended if your local environment is missing SDK/JDK):

- I added a GitHub Actions workflow `.github/workflows/android-build.yml` which will build the debug APK on push or when run manually.

To use it:

1. Commit and push your branch to GitHub.
2. Open the repository Actions tab and run the "Android CI - Build APK" workflow or wait for the push to trigger it.
3. After the workflow completes, download the `app-debug-apk` artifact which contains `app-debug.apk`.

This avoids needing to install or configure the Android SDK on your machine.


# Drug Test Checker

Native Android app only (Kotlin)

This repository now contains a standalone Android app under `android-app/` that performs a daily exact alarm at 03:10 America/Boise, scrapes drugtestcheck.com, logs results locally, and shows notifications.

What's included:

- `android-app/` - full Android Studio / Gradle project (Kotlin)

How it runs reliably at 03:10am:

- The app uses `AlarmManager.setExactAndAllowWhileIdle()` and reschedules every day. This uses the device's exact alarm API to reliably trigger at the specified wall-clock time (Android only). On Android 12+ the app should request `SCHEDULE_EXACT_ALARM` permission; some OEMs may still impose background restrictions.

Quick development steps (on your machine):

1. Open `android-app/` in Android Studio.
2. Build and run on your device or emulator.
3. Use the app UI to enter PIN and Last4 and press "Schedule Daily 03:10 Boise" to schedule the alarm.

Notes:

- Credentials and logs are stored locally on the device (SharedPreferences + internal file storage). No external server required.
- I removed the Expo/React Native code to avoid confusion; this is now a single native Android app.

Testing and build notes:

- To test immediate alarm: open the app and tap "Schedule Immediate Test (1 min)"; the app will schedule a test in one minute and you should receive a notification.
- For reliable scheduling on Android 12+, grant SCHEDULE_EXACT_ALARM permission when the app opens the settings screen after tapping "Schedule Daily 03:10 Boise".

To build an APK from the command line (if you have Android SDK / JDK configured):

- Open a terminal in `android-app/` and run:

    ./gradlew assembleDebug

- The APK will be in `app/build/outputs/apk/debug/app-debug.apk`.

CI build (recommended if your local environment is missing SDK/JDK):

- I added a GitHub Actions workflow `.github/workflows/android-build.yml` which will build the debug APK on push or when run manually.

To use it:

1. Commit and push your branch to GitHub.
2. Open the repository Actions tab and run the "Android CI - Build APK" workflow or wait for the push to trigger it.
3. After the workflow completes, download the `app-debug-apk` artifact which contains `app-debug.apk`.

This avoids needing to install or configure the Android SDK on your machine.

If you want, I can now:

- Add profile management UI (list/edit/delete saved profiles).
- Add immediate-test helper to schedule test alarm in 1 minute for debugging.
- Build an APK here (requires Android SDK/JDK in the environment) or provide Gradle commands and instructions for you to build locally.
