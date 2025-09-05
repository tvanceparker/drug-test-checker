# Drug Test Checker (Android)

Small on-device Android app that queries [drugtestcheck.com](https://drugtestcheck.com) for whether a saved profile is required to test on a given day.

This project runs entirely on the device. The app lets the user pick a device-local daily time to run the check (defaults to 03:10). Results, snapshots, and credentials are stored locally.

Features

- User-manageable daily check time (defaults to 03:10 device-local).

- Local profile storage: 7-digit PIN and 4-letter last4 in SharedPreferences.

- CSV logging: `files/drug_test_logs.csv` with header `timestamp,pin,last4,message`.

- HTML snapshots saved under `files/html/` with an `index.txt` the in-app viewer uses.

- Actionable notifications: Acknowledge stops repeats for that day; swipe/dismiss behavior can resurface required notifications.

- Debugging: unrecognized responses and short HTML snippets written to `files/dtc_debug.txt`.

Build & install

1. Ensure Android SDK and JDK are available and a device or emulator is connected.

2. From the repository root run:

```bash
cd android-app
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Usage

1. Open the app, add a profile using Add / Edit profile (enter a 7-digit PIN and the first 4 letters of the last name).

2. Tap "Set Daily Check Time" to select a device-local time for the check (the dialog initializes to the saved time; default 03:10).

3. The app will run the check daily at that device-local time and log results to `files/drug_test_logs.csv`.

4. Use "View Logs" to see readable timestamps and messages.

5. Use "View Snapshots" to inspect saved HTML responses. The viewer warns that images and external links may not work.

Files written on device

- `files/drug_test_logs.csv` — CSV log, header: `timestamp,pin,last4,message`.

- `files/dtc_debug.txt` — debug dump for unrecognized responses (includes short HTML snippets and labels).

- `files/html/` — saved HTML snapshots plus `index.txt` (pipe-separated entries: `filename|timestamp|profileId|profileName|message`).

Preferences

- `schedule_hour` (int): saved local-hour for the daily check (defaults to `3`).

- `schedule_minute` (int): saved minute for the daily check (defaults to `10`).

- Profiles and active profile are stored in the `dtc` SharedPreferences namespace.

Developer / testing notes

- Force a debug-required run (development only):

```bash
adb shell am broadcast -n com.example.drugtestchecker/.AlarmReceiver --ez debug_force_required true
```

- Inspect files written by the app:

```bash
adb shell run-as com.example.drugtestchecker ls -l files
adb shell run-as com.example.drugtestchecker cat files/drug_test_logs.csv
adb shell run-as com.example.drugtestchecker ls -l files/html
adb shell run-as com.example.drugtestchecker cat files/html/index.txt
adb shell run-as com.example.drugtestchecker cat files/dtc_debug.txt
```

Platform notes

- Notifications: Android 13+ requests `POST_NOTIFICATIONS`; allow notifications to receive alerts.

- Exact alarms: Android 12+ may require `SCHEDULE_EXACT_ALARM`; the app opens the settings screen to request this when needed. Without it, alarms are best-effort and timing may vary.

Security & privacy

All credentials and logs remain on the device. The app POSTs to the public site [drugtestcheck.com](https://drugtestcheck.com) using saved profile data. No cloud services are used.

Contributing

If you contribute or publish, avoid committing sensitive files (keystores, backups). Add a license of your choice if you want to publish.

Small on-device Android app that checks https://drugtestcheck.com daily at 03:10 America/Boise for whether a saved profile is required to test that day.

Features
- Runs a daily exact alarm (03:10 America/Boise) when allowed; falls back to best-effort scheduling otherwise.
- Stores profiles locally (PIN and last4) in SharedPreferences.
- Logs results locally to `drug_test_logs.csv` in the app files directory.
- Sends actionable notifications (Acknowledge / Dismiss). Acknowledge marks the profile/day as seen; required-to-test results produce a persistent notification until acknowledged.
- Debugging: unrecognized responses are appended to `dtc_debug.txt` for diagnosis.

# Drug Test Checker (Android)

Small on-device Android app that checks [drugtestcheck.com](https://drugtestcheck.com) for whether a saved profile is required to test on a given day.

This README reflects the current code: the app lets the user choose a daily local-device time for the check (default 03:10), saves the setting, and uses that local time for scheduling.

Features

- User-manageable daily check time (defaults to 03:10 device local). The app schedules a daily alarm at that device-local time.

- Profiles stored locally (7-digit PIN and 4-letter last4) in SharedPreferences.

- Local CSV logging: `files/drug_test_logs.csv` with header `timestamp,pin,last4,message`.

- HTML snapshots saved per run under `files/html/` and an `index.txt` used by the in-app snapshot viewer.

- Actionable notifications with Acknowledge (stops repeats for that day) and swipe/dismiss handling (resurface behavior for required days).

- Debugging: unrecognized responses and short HTML snippets are appended to `files/dtc_debug.txt`.

Security & privacy

- All credentials and logs stay on-device. The app performs POST requests to the public site [drugtestcheck.com](https://drugtestcheck.com) using saved profile data. No external cloud services are used.

Build & install (developer)

1. Ensure Android SDK, JDK, and a connected device or emulator are available.

2. From the repository root run:

```bash
cd android-app
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
Permissions & platform notes
- Notifications: On Android 13+ the app requests POST_NOTIFICATIONS. Allow notifications so you receive alerts.
- Exact alarms: On Android 12+ the app may prompt you to grant SCHEDULE_EXACT_ALARM (the settings screen is opened when the app needs it). Without it, the system will still schedule best-effort alarms but delivery time may vary.

Developer/testing notes
- To force a debug-required run (development only), broadcast to the app's `AlarmReceiver` with `--ez debug_force_required true` via adb; this will create a snapshot and a required-style notification for the active profile.

Example adb broadcast used during development:

```bash
adb shell am broadcast -n com.example.drugtestchecker/.AlarmReceiver --ez debug_force_required true
```

- To inspect files written by the app:

```bash
adb shell run-as com.example.drugtestchecker ls -l files
adb shell run-as com.example.drugtestchecker cat files/drug_test_logs.csv
adb shell run-as com.example.drugtestchecker ls -l files/html
adb shell run-as com.example.drugtestchecker cat files/html/index.txt
adb shell run-as com.example.drugtestchecker cat files/dtc_debug.txt
```

Preferences / keys
- `schedule_hour` (int): saved local-hour for the daily check (defaults to 3).
- `schedule_minute` (int): saved minute for the daily check (defaults to 10).
- Profiles and active profile are stored in the `dtc` SharedPreferences namespace. Credentials are not transmitted to any cloud service by the repository or build process.

CI / build automation
- The repo contains a GitHub Actions workflow (if present) that can build the debug APK on push. Pushes to the repo will trigger the workflow if Actions are enabled for your repository.

Notes and caveats
- Snapshots are static HTML files. External images and scripts will frequently be broken when loaded from the snapshot (the in-app viewer warns about this). This is expected.
- The parsing logic is intentionally conservative and writes unrecognized responses to `dtc_debug.txt` for inspection and regex tuning.
- This project uses Jsoup for server POST and parsing; ensure your environment allows outbound HTTPS to `drugtestcheck.com` when testing.

Contributing
- This project is intended for local on-device use. If you contribute or publish, avoid adding sensitive files (keystores, local backups) to the repo.

License
- Please add your preferred license.
How it runs reliably at 03:10am:
