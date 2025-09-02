# Drug Test Checker


Expo React Native app that checks drugtestcheck.com once/day and notifies the user.

Key features:

- Save credentials (6-digit PIN + first 4 of last name) securely via SecureStore
- Run manual check and view logs
- Background check (expo background fetch) once a day
- Local logs stored in app document directory

How to run

1. Install dependencies: npm install
2. Start expo: npm start
3. Open on Android device with Expo Go or build an APK

Notes:

- This project uses a simple HTML scraping approach and posts a form to the main site. If the site uses JavaScript-heavy rendering or CSRF tokens, we'll need a small scraping server (Python + requests/BeautifulSoup) or to mimic browser behavior.

Ejected / Android native notes

- This project has been prebuilt to a native Android project to allow precise daily scheduling using AlarmManager.
- On first run the app will schedule a daily alarm at 03:30 local time (keeps inside 03:00-04:00 window). If you need exact 03:10, we can change that but Android delivery may still vary by a few minutes.

Build APK (one-time native build):

1. Ensure Android SDK/Gradle are installed.
1. From project root run:

```bash
cd android && ./gradlew assembleRelease
```

1. Install the generated APK from `android/app/build/outputs/apk/release` onto your device.

Email placeholder

- An email entry is present in the app UI but the sending flow is a placeholder. See `email_helper.py` for an optional local SMTP helper.
