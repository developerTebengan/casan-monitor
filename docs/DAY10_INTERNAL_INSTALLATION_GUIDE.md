# Day 10 internal installation guide

Follow these steps after release approval. The authorized distribution location, signed candidate and checksum are pending.

## Release Engineering

1. Confirm the production API, Firebase Android project, approved existing signer and authorized distribution location. Build the release and select the newly produced APK.
2. Verify the package, code 5, version 0.1.1, launcher, app and notification icons, production HTTPS configuration, and non-debuggable merged manifest. Compare the signer fingerprint with the approved certificate and prior internal baseline. [Android apksigner](https://developer.android.com/tools/apksigner)
3. Compute the SHA-256 and publish it with the version, release notes and support escalation details through the approved restricted channel after sign-off.

```sh
# Set APK to the actual candidate generated on the authorized build host.
apksigner verify --verbose --print-certs "$APK"
aapt2 dump badging "$APK"
shasum -a 256 "$APK"
```

## Internal tester

1. Download the approved APK from the authorized CASAN source and compare its SHA-256 with the release listing. If Android prompts you, grant installation permission to the approved browser or file app, then accept the system installer prompt. Remove that source permission afterward if it is unnecessary.
2. Open CASAN Monitor, submit your authorized admin credentials, and grant Android notification permission. Confirm **Device registered** and check the Info channel in Settings. Check **About** for version name and code.
3. For an upgrade, install the approved higher-code APK over the existing compatible package. Verify session, themes, cached alerts and registration afterward. If Android reports a signature or downgrade conflict, stop and escalate without clearing data.
4. For an authorized QA device, run `adb devices -l`, `adb install -r "$APK"`, and `adb shell am start -n id.casan.monitor/.MainActivity`. Collect crashes with `adb logcat -s AndroidRuntime:E`.

No automatic updater or silent installer is enabled. Use the manual authorized process. For recovery, release a higher-code build with the same approved signer and test upgrade. See [Engineering handover](DAY10_ENGINEERING_HANDOVER.md) for support triage.
