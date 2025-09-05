# Drug Test Checker (Android)

Small on-device Android app that queries [drugtestcheck.com](https://drugtestcheck.com) for whether a saved profile is required to test on a given day.

This project runs entirely on the device. The app lets the user pick a device-local daily time to run the check (defaults to 03:10). Results, snapshots, and credentials are stored locally.

# Drug Test Checker (Android)

Small on-device Android app that checks https://drugtestcheck.com for whether a saved profile is required to test on a given day.
The project runs entirely on the device and stores profiles, logs, and snapshots locally. The user selects a device-local daily time
for the check (default: 03:10).

Status
- Core app implemented: scheduling, local profiles (7-digit PIN + last4), CSV logging, HTML snapshots, and in-app snapshot viewer.
- Actionable notifications implemented (Acknowledge / Dismiss).
- Optional automatic error reporting integrated with Sentry. Telemetry is opt-in (toggle in main UI). Manual debug-send button exists.

Quick features
- Set device-local daily check time (default: 03:10).
- Local profile storage: 7-digit PIN and 4-letter last4 (SharedPreferences).
- CSV logging: `files/drug_test_logs.csv` (header: `timestamp,pin,last4,message`).
- HTML snapshots: saved under `files/html/` with `index.txt` for in-app viewer.
- Debugging: `files/dtc_debug.txt` holds short HTML snippets and parse dumps for unrecognized responses.

Build & install (developer)
1. Ensure Android SDK and JDK are installed and a device or emulator is connected.

2. From the repository root:

```bash
cd android-app
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Usage (user)
1. Open the app, add a profile (enter a 7-digit PIN and the first 4 letters of the last name).
2. Tap "Set Daily Check Time" to pick the device-local time (dialog initializes to saved time; default 03:10).
3. The app will run the check daily at the chosen time and log results to `files/drug_test_logs.csv`.
4. Use "View Logs" to inspect recent runs and "View Snapshots" to review saved HTML snapshots.

Preferences and files (on device)
- SharedPreferences namespace: `dtc`
	- `schedule_hour`, `schedule_minute` (ints)
	- `telemetry_enabled` (boolean) — opt-in for Sentry/error reporting
	- `active_profile`, `pin`, `last4`, `dtc_profiles` (json string)

- Files (app internal storage):
	- `files/drug_test_logs.csv`
	- `files/dtc_debug.txt`
	- `files/html/` (snapshots) and `files/html/index.txt`

Developer / testing notes
- Force a debug-required run (development only):

```bash
adb shell am broadcast -n com.example.drugtestchecker/.AlarmReceiver --ez debug_force_required true
```

- View files written by the app (use `run-as` on device/emulator):

```bash
adb shell run-as com.example.drugtestchecker ls -l files
adb shell run-as com.example.drugtestchecker cat files/drug_test_logs.csv
adb shell run-as com.example.drugtestchecker ls -l files/html
adb shell run-as com.example.drugtestchecker cat files/html/index.txt
adb shell run-as com.example.drugtestchecker cat files/dtc_debug.txt
```

Telemetry & privacy
- Telemetry is opt-in. The main UI has a switch labeled "Telemetry" — enable it to allow automatic and manual error reports to Sentry.
- When enabled, the app will upload sanitized debug snippets when parse failures occur or when you use the manual "Send debug report (test)" button.
- Sensitive data redaction: before sending, the app masks 7-digit PIN-like sequences and 4-digit sequences in messages and attachments.
- The DSN is provided via the build config; ensure you do not commit secret DSNs into the repo.

Notes & caveats
- The in-app snapshots are static HTML; external resources may not load.
- Parsing is conservative; unrecognized responses are written to `dtc_debug.txt` for inspection and regex tuning.
- Some Sentry features (server-side beforeSend hooks) were avoided client-side to reduce SDK API coupling; the app sanitizes data locally before send.

Contributing
- Keep sensitive information out of commits (keystores, personal data).
- If you add CI, ensure secrets (Sentry DSN) are provided via repository secrets and not stored in code.

License
- Add your preferred license.
