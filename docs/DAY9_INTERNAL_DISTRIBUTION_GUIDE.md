# Day 9 internal distribution guide

This procedure applies after release approval. The approved CASAN distribution location and Day 9 APK are currently unavailable.

## Release owner

1. Confirm the deployment HTTPS API base, production Firebase Android client/project and approved existing signing key. Configure `casanBackendUrl` and the signing environment outside source control. Use the project's existing builds and run `./gradlew clean assembleRelease`.
2. Select the actual newly generated release APK and inspect its package, version code and name, launcher, icons and merged manifest with the Android SDK. Confirm release debuggability is disabled and check the approved endpoint and Firebase project in the artifact. Verify signing and compare the certificate fingerprint to the approved internal release and upgrade baseline. [Android apksigner](https://developer.android.com/tools/apksigner)
3. Compute the checksum and record it in the restricted release listing. The listing should include the version, release notes, expected signer fingerprint, support contact and rollback notes. Use a versioned APK filename and authorized HTTPS access. Publish only after authorization and after the exact signed file passes installation, upgrade and smoke tests.

```sh
# APK is the actual generated and approved candidate selected by the release owner.
apksigner verify --verbose --print-certs "$APK"
aapt2 dump badging "$APK"
shasum -a 256 "$APK"
```

## Internal tester

1. Obtain the versioned APK and published SHA-256 from the authorized CASAN page or file service. Compare the downloaded file checksum to the published value before opening it. Contact the release owner if the checksum or signer differs.
2. If Android asks, allow installation for the approved browser or file app used for this download. Open the APK and accept the Android install prompt. Remove that source permission afterward if it is unnecessary.
3. Open CASAN Monitor, sign in with your authorized account, and grant notification permission when prompted. In Settings, verify **Device registered**, app notification permission and the Info channel. Check **About** for version name, code and Android API level.
4. To update, obtain the approved newer APK and install it over the current copy. If Android reports a conflicting signature or version, stop and contact release support. An in-place upgrade keeps local data when package, signer and version are compatible. Use a separate device/profile to test a differently signed debug build.

For release QA on an authorized device, use `adb devices -l`, then `adb install -r "$APK"` and `adb shell am start -n id.casan.monitor/.MainActivity`. Use `adb logcat -s AndroidRuntime:E` for crash evidence and omit secrets and sensitive payloads from support material. If registration fails, verify internet, deployment and permission settings, then use Settings retry if available. Escalate environment and sender problems to release support. See the [update and recovery strategy](DAY9_UPDATE_STRATEGY.md).
