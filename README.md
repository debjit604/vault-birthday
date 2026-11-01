# Vault Birthday

A small birthday vault built with plain HTML, CSS, and JavaScript. Data stays in this browser's local storage. The storage layer AES-GCM encrypts the vault using a key derived from your 4-digit PIN with PBKDF2 (100,000 iterations). A fresh random salt and IV are stored with ciphertext. The PIN is never stored. A weak PIN can still be guessed by someone who obtains the local ciphertext.

## Open and run

1. Extract the folder and open `index.html` in a modern browser. The app, forms, local reminders, and exports work directly from disk without a server.
2. Add a birthday. Notification permission is requested only after the first save, and only if supported.
3. Keep the app open for in-app alarms. While open, it checks every 30 seconds.
4. Back up from the footer to export an obfuscated JSON file. Restore replaces the current vault after confirmation.
5. Use **Export calendar** to generate an `.ics` calendar file with five alarms per birthday.

## Install on a phone

1. Copy the folder to a static HTTPS host or local network web server; service workers and PWA installation require HTTPS (localhost is also accepted by browsers).
2. Open the hosted `index.html` in Safari on iPhone or Chrome on Android.
3. Choose **Share → Add to Home Screen** on iPhone, or **Install app / Add to Home screen** from Chrome's menu on Android.
4. Open the installed app, add birthdays, and choose **Export calendar**.
5. Import the `.ics` file into Apple Calendar, Google Calendar, or another calendar app for OS-level alarms when the browser is closed.

## Privacy and options

The app makes no app-initiated network requests. The service worker caches only the app shell, never calendar exports. The 4-digit PIN derives an AES-GCM key using PBKDF2 (100,000 iterations). Wrong PINs fail authenticated decryption and open the Notes screen. Browser storage can be cleared by the user or browser; keep backups.

On first launch create a 4-digit PIN; subsequent launches require it to decrypt the vault. Wrong PIN opens a harmless Notes screen. The manifest uses a Notes-style icon and name for hidden mode. (The installed label may be cached by the OS until reinstalled.)

## Reminder behavior and limitations

- Fixed in-app stages: 7 days, 2 days, 1 day, 30 minutes, and the exact birthday time. Each stage is recorded once per year. Stages found within two hours are rung; older unhandled stages appear in the missed banner without a retroactive alarm.
- Day offsets use local `Date` calendar arithmetic, preserving wall-clock time across DST. For Feb 29 birthdays, non-leap years use Feb 28.
- `.ics` exports include five `VALARM` triggers (-P7D, -P2D, -P1D, -PT30M, PT0M), including the exact birthday moment.
- Browser alarms cannot reliably run while the app is closed, the device is asleep, browser notifications are disabled, or the browser has suspended the page. Import the calendar export for native OS calendar alerts; alarm presentation and timing depend on the calendar app and device settings.
- Direct `file://` opening is supported for the core app, but browsers block service workers and PWA installation on file URLs. Host the same folder over HTTPS for offline shell caching and home-screen installation.
- A 4-digit PIN has a limited keyspace; AES-GCM authenticates ciphertext, but device security and backups still matter.

## Android app (Capacitor)

From this folder, install Node.js 22+, Android Studio, and an Android SDK. Then run:

```sh
npm install
npm run build
npx cap add android
npx cap sync android
npx cap open android
```

The build step copies the app into `www/` and bundles the native Local Notifications bridge. It pre-schedules all five reminder stages for this year and next year on the device; app data restores after entering the PIN on return. Editing a birthday, restoring a backup, or unlocking refreshes the schedule. On first schedule, allow Android notification permission.

For exact alarms on Android 12+, add `<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />` inside `<manifest>` in `android/app/src/main/AndroidManifest.xml`. Connect a phone with USB debugging enabled or start an emulator, allow Gradle sync in Android Studio, then press **Run ▶**. After web edits run `npm run android:sync` and install/run again. Exact alarm delivery also depends on Android system and battery settings.

After sending a birthday wish, return to the app and tap **Mark wished** on that person’s card. The check is saved against the upcoming birthday year.

### Choose your Android notification sound

Android notifications use a dedicated **Birthday reminders** channel. After installing/rebuilding the app and allowing notifications, open **Settings → Apps → Vault Birthday → Notifications → Birthday reminders → Sound** and choose a ringtone or another sound offered by your phone. Some Android versions let you add a music file from the sound picker. Android owns this channel sound, so it can be changed from Settings; the bundled `vault_alarm.wav` is the initial fallback. The app creates a dedicated channel for this sound. On Android 8+, channel sound is controlled by Android and cannot be replaced by a webpage or by changing an individual notification.

When you open the app at `localhost:3000` in a browser, the browser/phone controls notification sound and does not let the page select your ringtone. The ringtone option applies to the installed Capacitor Android app. Capacitor uses an Android notification channel and packaged raw sound resource for native notifications; the OS sound picker is where users choose their ringtone. [Capacitor notification sound/channel docs](https://capacitorjs.com/docs/apis/local-notifications#configuration)

## Changelog

- Native Android birthday alarms now use a dedicated high-importance notification channel whose sound URI is selected on-device.
- Added **Choose Alarm Sound** and **Test Alarm Sound Now** in Settings.
- Redesigned the dashboard with responsive cards, animated entry, countdown flips, alarm motion, and session-only birthday confetti. Reduced-motion preferences are respected.
- Replaced the app icon with a gradient birthday cake across web and Android launcher sizes.
- The named `@capawesome/capacitor-ringtonepicker` package is not published on npm (registry returned E404); the Android system ringtone picker is implemented through a small app-local Capacitor plugin instead.

## Auto-build status
![Build APK](https://github.com/debjit604/vault-birthday/actions/workflows/build-apk.yml/badge.svg)
![Deploy Web](https://github.com/debjit604/vault-birthday/actions/workflows/deploy-web.yml/badge.svg)

- Download APK: Actions tab -> latest "Build Android APK" run -> Artifacts -> vault-birthday-apk.zip
- Live web app: https://debjit604.github.io/vault-birthday/
