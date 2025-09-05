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
## Drug Test Checker (Android)

Small on-device Android app that checks https://drugtestcheck.com for whether a saved profile is required to test on a given day.
The project runs entirely on the device and stores profiles, logs, and snapshots locally. The user selects a device-local daily time
for the check (default: 03:10).

## Features

- User-manageable daily check time (defaults to 03:10 device-local).
- Local profile storage: 7-digit PIN and 4-letter last4 saved in the app's SharedPreferences (local only).
- CSV logging: `files/drug_test_logs.csv` with header `timestamp,pin,last4,message`.
- HTML snapshots saved under `files/html/` with an `index.txt` used by the in-app snapshot viewer.
- Actionable notifications: Acknowledge stops repeats for that day; swipe/dismiss handling can resurface required notifications.
- Debugging: unrecognized responses and short HTML snippets written to `files/dtc_debug.txt`.

## Build & install

1. Ensure Android SDK and JDK are installed and a device or emulator is connected.

2. From the repository root:

```bash
cd android-app
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Usage

1. Open the app, add a profile (enter a 7-digit PIN and the first 4 letters of the last name).
2. Tap "Set Daily Check Time" to select the device-local time for the check (dialog initializes to the saved time; default 03:10).
3. The app runs the check daily at the chosen device-local time and logs results to `files/drug_test_logs.csv`.
4. Use "View Logs" for readable timestamps and messages.
5. Use "View Snapshots" to inspect saved HTML responses (the viewer warns that external resources may be broken).

## Files written on device

- `files/drug_test_logs.csv` — CSV log (header: `timestamp,pin,last4,message`).
- `files/dtc_debug.txt` — debug dump for unrecognized responses (includes short HTML snippets and labels).
- `files/html/` — saved HTML snapshots plus `index.txt` (pipe-separated entries: `filename|timestamp|profileId|profileName|message`).

## Preferences

- `schedule_hour` (int): saved local-hour for the daily check (default: `3`).
- `schedule_minute` (int): saved minute for the daily check (default: `10`).
- Profiles and the active profile are stored in the `dtc` SharedPreferences namespace.

## Developer / testing notes

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

## Permissions & platform notes

- Notifications: On Android 13+ the app requests POST_NOTIFICATIONS; allow notifications to receive alerts.
- Exact alarms: On Android 12+ the app may prompt for SCHEDULE_EXACT_ALARM; without it alarms are best-effort and timing may vary.

## Security & privacy

All credentials and logs remain on the device. The app POSTs to the public site https://drugtestcheck.com using saved profile data.
No cloud services are used and the repository does not contain credentials.

## CI / build automation

The repo can be configured with CI (GitHub Actions) to build the debug APK on push.

## Notes and caveats

- Snapshots are static HTML files; external resources may not load when viewed from the snapshot. The in-app viewer displays a warning.
- Parsing is conservative; unrecognized responses are written to `dtc_debug.txt` for inspection and regex tuning.
- This project uses Jsoup for POST and parsing; ensure outbound HTTPS to `drugtestcheck.com` is allowed when testing.

## Contributing

This project is intended for local on-device use. If you contribute or publish, avoid adding sensitive files (keystores, local backups)
to the repo.

## License

Add your preferred license.
