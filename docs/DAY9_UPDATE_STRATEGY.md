# Day 9 update strategy

## Version policy

The candidate version is `0.1.1`, code `4`. Increment the numeric code for each rollout and use semantic version names for display. The checker uses the installed numeric version and the latest approved release code. A valid explicit minimum above the installed code yields Required. A newer approved release without such a minimum yields Available. Equal or older metadata yields Up to Date and never asks for a downgrade. Missing, inconsistent or unreachable metadata yields Unable to Check or Offline and must leave emergency access available. [Android app versioning](https://developer.android.com/studio/publish/versioning)

## Integration contract pending

CASAN must identify the owner and location of the authenticated HTTPS metadata source, approved version fields and minimum version policy, distribution destination, checksum and signer reference, and expected offline/error behavior. The update model is an Android policy boundary. Its test fixtures define no wire schema, backend API or domain. It has no shipped provider, network check, dialog, distribution URL, downloader, installer, or enforcement hook.

When CASAN supplies the contract, map the authorized source into the existing interface and use the running application's version code. Present the current and latest version, release notes, and an action to open the approved HTTPS internal page. Offer Later for optional updates. Require explicit policy approval before enforcing a minimum version. Validate the configured HTTPS origin and restrict any redirects to approved origins. Keep version checking and app usage independent during errors.

## Artifact and recovery policy

Use a restricted CASAN-owned distribution surface and publish version, checksum and signer evidence separately from the artifact. Prefer Android's user-confirmed package installer initiated from the approved browser or file app. If an on-device downloader is authorized later, verify SHA-256, package name and signing-certificate compatibility before handing off to the Android installer. Retain system consent. The current app requests no APK installation permission. [Android apksigner](https://developer.android.com/tools/apksigner)

Keep the last known-good signed APK, its source revision and signer reference. If code 4 causes a regression, prepare the known-good behavior or a fix as a hotfix with a version code greater than every deployed code and the same approved signer. Verify migration and in-place upgrade on a copy of the affected device state. Preserve app data. A direct downgrade is not the standard recovery path. If no in-place option exists, assess cache loss and obtain authorization before uninstalling or clearing data.

For FCM recovery, start with permission and Info-channel checks, account/endpoint verification, validated connectivity, and the existing registration retry. Avoid resetting the Firebase installation as a routine support step. If stale delivery occurs after logout, escalate to the backend/FCM owner and suspend affected recipient testing until the association is understood.
