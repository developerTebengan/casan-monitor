# Day 9 release readiness

**RELEASE BLOCKED.** No verified approved signer, installable Day 9 artifact, deployed production configuration or authorized production FCM result was available. No deployment or distribution was performed.

| Gate | Evidence |
|---|---|
| Standalone checks | Kotlin and Compose source compilation succeeded. All 24 JVM tests passed |
| Debug and release | Both Gradle invocations failed before configuration because the local lock socket was denied |
| Signing | All approved signing inputs were absent. No Day 9 release artifact was available to verify |
| Physical install and upgrade | ADB failed to bind its local listener. No authorized device execution or approved previous release APK |
| Production config | One local Firebase package match and matching local project IDs. Production HTTPS API base and deployed sender alignment remain unverified |
| Authentication, FCM and multi-user | No authorized production smoke test performed. No confirmed admin sender in the checked-in backend |
| Distribution and updates | Procedures and offline policy module prepared. Authorized hosting, metadata source and release authorization pending |
| Security and recovery | Source audit and recovery plan completed. Release manifest, binary and runtime authorization still need verification |

## Rollout gates

- **P0** Obtain confirmed production configuration, approved original signing inputs and a host with Gradle and device access. Build and verify the exact signed artifact, and prove in-place upgrade and fresh installation.
- **P0** With explicit approval, validate production login, registration and authorized admin-only notifications on a test device. A prior Firebase Console delivery does not establish production CASAN sender delivery.
- **P1** Verify logout and account switching on two authorized accounts/devices. Address the missing unregister capability and system-managed background message limitations before broad distribution.
- **P2** Confirm the restricted distribution location, trusted update metadata contract and audience before connecting the live checker. Retain the known-good release and validate higher-code recovery.

Day 10 should start with the P0 gates and documented [release test cases](DAY9_RELEASE_TEST_CASES.md). Publish only after sign-off for the exact verified artifact.
