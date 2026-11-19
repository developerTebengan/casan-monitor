# CASAN Monitor

CASAN Monitor is a native Android app for internal engineering and operations. The Day 1 app starts at the dashboard and uses a single local mock incident repository.

## Run locally

1. Open this directory in Android Studio with AGP 9.3 support. Set the Gradle JDK to 17 and install Android SDK Platform 37.1, Build-Tools 36.0.0, and Platform-Tools from SDK Manager.
2. Use the included Gradle wrapper for sync and command-line builds.
3. Sync Gradle and run the `app` configuration on an emulator or device running Android 8.0 (API 26) or newer. Firebase configuration is optional for this step.

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

`LoginScreen.kt` includes a standalone Compose preview. Interactive Preview runs the validation, password visibility toggle, and simulated loading state.

## Project structure

```text
app/src/main/
├── AndroidManifest.xml
├── java/id/casan/monitor/
│   ├── CasanMonitorApp.kt
│   ├── MainActivity.kt
│   ├── core/          # Design system, navigation, DI bindings
│   ├── data/          # Mock incidents, repository, theme preferences
│   ├── domain/        # Incident models, repository contract, search
│   ├── feature/       # Dashboard, alerts, history, settings, login preview
│   └── notification/  # Messaging service and channels
└── res/               # Vector icons, strings, launch theme
app/src/test/          # Incident search and filter tests
gradle/                # Version catalog
```

Gradle Kotlin DSL uses AGP 9.3.1 with built-in Kotlin pinned to 2.4.21. The catalog pins Compose BOM 2026.02.01 (including Material 3), AndroidX Activity Compose 1.13.0, Lifecycle 2.11.0, Navigation Compose 2.10.2, Hilt 2.60.1, KSP 2.3.14, Firebase BOM 35.2.0, Google Services 4.6.0, and JUnit 4.13.2. The target SDK is Android 17 (API 37).

## Firebase setup for Day 3

1. In the CASAN Firebase project, register an Android app with package name `id.casan.monitor`.
2. Download the matching `google-services.json` and place it at `app/google-services.json`. The file is ignored by Git. The app module applies the Google Services plugin when the file exists.
3. Sync and rebuild. Keep the project configuration on the development machine or in protected build storage.

The manifest declares the messaging service, Android 13 notification permission, default icon, and fallback Info channel. The app creates Critical, High, and Info channels at startup. Settings opens Android's notification preferences for the app. FCM auto-init is disabled for Day 1, so adding the configuration alone leaves token generation disabled. The service logs callback events in debug builds without recording tokens or message payloads.

## Day 2

- Run a device smoke test on API 26 and API 37, including system back, tab restoration, search and filters, dark mode, and large fonts.
- Establish an Android CI build that runs unit tests and lint and retains the debug APK for internal testing.
- Confirm incident data and authentication contracts with the CASAN team before replacing the mock repository or login preview.
