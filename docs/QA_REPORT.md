# QA and validation report

## Automated gates

The `android-validation.yml` workflow performs these gates on a clean Ubuntu runner with JDK 17 and Android SDK:

1. Generate and validate the pinned Gradle 8.9 wrapper.
2. Compile all Kotlin/Compose production sources and resources.
3. Run JVM unit tests (`testDebugUnitTest`).
4. Run release Android Lint with `abortOnError=true` (`lintRelease`).
5. Compile the Compose instrumentation/UI test APK (`assembleAndroidTest`).
6. Run R8 code and resource shrinking and build release (`assembleRelease`).
7. Sign the installable test release with an isolated development key.
8. Verify all APK signature schemes and certificate metadata using `apksigner`.
9. Produce and commit an SHA-256 checksum beside the APK.

Instrumentation tests are compiled in CI. They require a real Android device or emulator to execute; no claim of device execution is made when a runner has no emulator attached.

## Unit coverage

`BoostPlannerTest` verifies:

- healthy-device launch;
- missing-game blocking;
- profile-specific Severe thermal behavior;
- Critical thermal blocking for every profile;
- 45°C battery guard for Extreme;
- simultaneous low-RAM, low-battery and Battery Saver findings.

Additional domain tests verify thermal-status mapping and the allowlisted, unique regional package catalog.

## Compose checks

`A21OptimizerAppTest` verifies that:

- the primary BOOST button is reachable and invokes the preflight callback;
- the Monitor tab explicitly says FPS is unavailable instead of generating a fake number.

## Manual device checklist

Use this checklist on the target Galaxy A21 before wider distribution:

- [ ] Clean install and first launch on the phone’s current One UI build
- [ ] Persian and English layouts, RTL/LTR, font scaling 100% and 130%
- [ ] Portrait and landscape recreation without crash or lost profile
- [ ] System light and dark themes
- [ ] Permission review confirms no dangerous permissions
- [ ] Global/Garena/VNG/China package selection for the locally relevant version
- [ ] Installed game detected and official launch intent opens it
- [ ] Missing-game state and store fallback
- [ ] Returning from Settings refreshes battery/RAM state
- [ ] Low battery and Battery Saver warnings
- [ ] Warm phone produces Moderate/Severe warning when One UI exposes it
- [ ] Extreme is blocked at dangerous thermal status
- [ ] App task disappears after game launch; no optimizer notification/service remains
- [ ] Android Settings destinations work or show graceful fallback
- [ ] No ANR during repeated refresh/rotate/background/foreground cycles
- [ ] Memory inspection via Android Studio profiler on the physical A21

## Expected resource behavior

There is no scheduled WorkManager job, foreground/background service, overlay, network client, analytics SDK or periodic timer. Snapshot reads occur at first display, `onResume`, explicit Refresh, and pre-launch. The only event stream is Android’s thermal callback while the UI/ViewModel exists. Launching the game calls `finishAndRemoveTask()`, which tears down that listener with the ViewModel.
