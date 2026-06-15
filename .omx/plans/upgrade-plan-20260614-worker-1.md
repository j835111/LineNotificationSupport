# LineNotificationSupport Android Upgrade Plan

Date: 2026-06-14
Worker: `worker-1` / planner
Task: read-only upgrade planning; no product code changes.

## Outcome

Upgrade `LineNotificationSupport` from the current Android/Gradle baseline to a Play-compliant, testable release path while preserving the app's core promise: repackaging LINE notifications for Samsung watches without breaking notification listener, reply, grouping, stickers/images, calls, Bluetooth workaround, Tasker settings updates, persistence, or localized UI.

## Evidence inspected

Repo/context evidence:

- Context: `/mnt/d/project/LineNotificationSupport/.omx/context/upgrade-plan-20260614T060413Z.md`.
- Build stack: `build.gradle`, `app/build.gradle`, `settings.gradle`, `gradle.properties`, `gradle/wrapper/gradle-wrapper.properties`.
- Runtime surfaces: `app/src/main/AndroidManifest.xml`, `NotificationListenerService`, `AndroidFeatureProvider`, `AndroidPermissionRequester`, `LineRemoteInputReplier`, `DefaultReplyActionBuilder`, `StartConversationActionBuilder`, `NotificationGroupCreator`.
- Test/release surfaces: `.github/workflows/android.yml`, `app/src/test`, `app/src/androidTest`, `app/proguard-rules.pro`.

External policy/tooling evidence checked on 2026-06-14:

- Google Play policy: app updates must target Android 15 / API 35 or higher from 2025-08-31; existing apps below API 34 lose availability to new users on newer Android versions.
- Android Gradle Plugin release notes: AGP 9.2 supports max API 37 and requires Gradle 9.4.1, SDK Build Tools 36.0.0, and JDK 17.

## Current baseline

| Area | Observed state | Upgrade impact |
| --- | --- | --- |
| Project shape | Single Android app module `:app`; Java/XML app | Scope can remain narrow and phased. |
| App id | `com.mysticwind.linenotificationsupport` | Preserve for Play/update compatibility. |
| AGP / Gradle | AGP `8.2.2`; Gradle wrapper `8.2` | Can likely do API 35 compliance before latest-toolchain jump; AGP 9.2 requires coordinated Gradle/JDK uplift. |
| SDKs | `compileSdkVersion 33`, `targetSdkVersion 33`, `minSdkVersion 26` | Target API 33 is below current Play update policy. |
| Java | source/target Java 8 | Keep during compliance phase; validate annotation processors before any JDK/toolchain jump. |
| CI | GitHub Actions runs `./gradlew build` with JDK 11 | AGP 9.x path requires CI JDK 17. |
| Release | `release { minifyEnabled true }`; no repo signing config | R8/ProGuard and external signing must be release gates. |
| Tests | 18 local JVM test classes; 1 instrumentation smoke test | Logic tests exist, but core notification/listener/device behavior is mostly manual. |

## Recommended route

### Track A: Minimum Play-compliance route (recommended first)

Target result: produce an updateable Play release targeting API 35 without coupling that policy work to a full AGP 9.x modernization.

1. **Baseline and environment lock**
   - Normalize `gradlew` to LF line endings so Unix shell execution reaches JVM startup.
   - Install/standardize JDK for local and CI verification.
   - Capture current outputs for:
     - `./gradlew --version`
     - `./gradlew clean :app:assembleDebug :app:testDebugUnitTest :app:lintDebug`
     - `./gradlew :app:assembleRelease :app:lintRelease`
   - Record current dependency graph:
     - `./gradlew :app:dependencies --configuration releaseRuntimeClasspath`
   - Stop condition: current branch has a known green/broken baseline before any upgrade edits, and the baseline commands fail only for real build issues rather than shell/JDK bootstrapping problems.

2. **Target SDK compliance slice**
   - Raise `compileSdk` and `targetSdk` from 33 to 35.
   - Keep `minSdkVersion 26`, app id, Java source/target, and runtime behavior unchanged.
   - Re-run debug, unit, lint, and release-minified builds.
   - Fix only API/lint/build issues required by the API 35 bump.
   - Stop condition: API 35 build passes and the app can be installed on a device/emulator.

3. **Permission and notification behavior audit**
   - Recheck `POST_NOTIFICATIONS`, `BLUETOOTH_CONNECT`, notification listener access, exported components, notification channel/group migration, mutable/immutable `PendingIntent` use, and `RemoteInput` reply flows.
   - Preserve current manifest package queries for LINE.
   - Note cleanup candidates separately: duplicate `DisableStartConversationFeatureBroadcastReceiver` manifest entry, deprecated `AsyncTask`, deprecated untyped `getParcelableExtra(String)`.
   - Stop condition: no behavior-changing cleanup is mixed into the policy-only release unless tests/manual checks cover it.

4. **Release candidate validation**
   - Build `:app:bundleRelease` / `:app:assembleRelease` with minification.
   - Verify signing through the external release-signing path, because no signing config is committed.
   - Run Play Console pre-review/pre-launch checks before staged rollout.
   - Stop condition: release artifact targets API 35+, is signed, and passes automated plus manual gates below.

### Track B: Dependency modernization route (after Track A is stable)

Target result: reduce dependency/tooling risk in reviewable batches, not a single all-dependency jump.

1. **Correct concrete version mismatch first**
   - Align Glide runtime/compiler; current runtime is `glide:4.12.0` while compiler is `4.11.0`.
   - Run `dependencyInsight --dependency glide` and release-minified build.

2. **Annotation-processing batch**
   - Upgrade Hilt, FreeFair Lombok plugin, Lombok, Room runtime/compiler together or in tightly scoped pairs.
   - Validate generated code and release minification.
   - Watch for Java/JDK compatibility and annotation retention/shrinker behavior.

3. **AndroidX/UI batch**
   - AppCompat, Material, Navigation, Preference, ConstraintLayout.
   - Validate settings/help/navigation screens and localized resources.

4. **Persistence/network/util batch**
   - Room/DataStore if not already done, Guava, jsoup, commons libraries, Historian, re-retrying, Timber.
   - Validate cached chat names, keyword DB, notification history DB, sticker/image download, and retry behavior.

5. **Test tooling batch**
   - JUnit, AndroidX Test, Espresso, Mockito.
   - Expect Mockito 1.x to be a migration hotspot; tests use legacy `org.mockito.runners.MockitoJUnitRunner` and `org.mockito.Matchers`.

### Track C: Latest-toolchain route (optional, after A/B or by explicit priority)

Target result: move to AGP 9.2-era tooling only after app behavior is already green.

- Upgrade as one coordinated toolchain slice:
  - AGP `9.2.x`
  - Gradle wrapper `9.4.1`
  - CI/local JDK `17`
  - SDK Build Tools `36.0.0`
- Re-evaluate Gradle DSL compatibility, configuration cache, lint behavior, R8 behavior, and annotation processors.
- AGP 9.2 release notes call out R8 `-keepattributes` semantics changes for runtime-invisible annotations, so release-minified builds and Hilt/Room/Glide behavior need extra attention.
- Stop condition: debug + release + lint + tests pass on JDK 17 and release artifact remains Play-installable.

## Dependency and code compatibility risks

High-risk / release-blocking:

- `targetSdkVersion 33` is below Play update policy for API 35+ app updates.
- Core features depend on OS-sensitive notification behavior:
  - `NotificationListenerService`
  - notification posting/grouping/channel migration
  - `RemoteInput` reply and conversation-start actions
  - `PendingIntent` mutability and parcelable extras
  - notification dismissal and restore/reconnect behavior
- Runtime permission gates are central:
  - `POST_NOTIFICATIONS` for Android 13+
  - `BLUETOOTH_CONNECT` for Android 12+
  - notification listener access outside normal runtime permissions
- Release builds are minified; dependency bumps can fail only in `assembleRelease` / `bundleRelease`.

Medium-risk / should-fix during modernization:

- Glide runtime/compiler mismatch (`4.12.0` vs `4.11.0`).
- Old annotation-processing stack: Hilt `2.48`, Lombok plugin `8.4`, Lombok `1.18.24`, Room `2.4.3`.
- Legacy test stack: JUnit `4.12`, Mockito `1.10.19`, AndroidX Test `1.1.3`, Espresso `3.4.0`.
- Deprecated/untyped Android APIs:
  - `AsyncTask` in notification/image/history paths.
  - `getParcelableExtra(String)` for reply action extraction.
- Manifest duplicate receiver declaration for `DisableStartConversationFeatureBroadcastReceiver`.

Low-risk / preserve unless separately scoped:

- Java/XML app architecture.
- `minSdkVersion 26`.
- Existing app id and package query for LINE.
- Existing localized resource set, unless Play/App Bundle language delivery is intentionally revisited.

## Test and release verification matrix

| Gate | Command / action | Pass criteria | Manual/device required |
| --- | --- | --- | --- |
| Environment | `java -version`; `./gradlew --version` | JDK matches chosen track; Gradle starts | No |
| Dependency graph | `./gradlew :app:dependencies --configuration releaseRuntimeClasspath` | Dependency graph captured; no unexpected conflict | No |
| Unit tests | `./gradlew :app:testDebugUnitTest` or `./gradlew test` | Existing JVM tests pass | No |
| Debug build | `./gradlew :app:assembleDebug` | Debug APK builds and installs | Device/emulator install recommended |
| Lint | `./gradlew :app:lintDebug :app:lintRelease` | No new fatal lint; target SDK warnings resolved | No |
| Instrumentation | `./gradlew :app:connectedDebugAndroidTest` | App package smoke test passes | Emulator/device required |
| Release APK | `./gradlew :app:assembleRelease` | Minified release APK builds | Signing check required |
| Release bundle | `./gradlew :app:bundleRelease` | Play upload artifact builds | Play Console validation required |
| Signature | `apksigner verify --verbose --print-certs app/build/outputs/apk/release/*.apk` | Expected cert and v2/v3/v4 status | Release key access required |
| Play precheck | Play Console pre-review / pre-launch report | No policy blocker for target API | Play Console required |

Manual matrix:

| Surface | Android/API coverage | Scenarios |
| --- | --- | --- |
| Install/upgrade | Existing install -> upgraded build; fresh install | App launches; persisted settings/Room/DataStore state survives. |
| Notification listener | API 26, 31/32, 33, 35+ if available | Listener enabled, reconnects, restores existing notifications, no crash after reboot/app restart. |
| Notification posting | API 33+ especially | `POST_NOTIFICATIONS` prompt/denial/grant paths; LINE mirrored notifications appear. |
| LINE message parsing | Real LINE install where possible | One-on-one, group chat, identical messages, sender/title resolution. |
| Reply actions | Phone + watch where possible | Reply from notification/watch sends via LINE `RemoteInput`; mutable pending intents still work. |
| Sticker/image | Network available | Sticker/image downloads render; failure does not block text notification. |
| Calls | LINE call scenario | Incoming/ongoing call notifications repeat/clear as expected. |
| Channel/group migration | Toggle merge/split settings | No lost/duplicated channels; channel importance/vibration expectations preserved. |
| Bluetooth workaround | API 31/32 and API 33+ | Permission request path; disabled behavior on unsupported API; watch reconnect behavior. |
| Tasker integration | `adb shell am broadcast ...settings.update...` | Setting changes apply for both boolean paths. |
| UI/localization | en, zh-rTW, zh-rCN, zh-rHK, ja | Help/settings/history/keyword screens render expected labels. |
| Release smoke | Signed release build | Debug-only menu hidden; release-minified core flows still work. |

## Concrete implementation slices for executors

1. **Baseline branch and CI/JDK slice**
   - Ownership: CI/workflow and build environment documentation only.
   - Update CI JDK only if required by selected track.
   - Verify baseline commands before app code changes.

2. **API 35 compliance slice**
   - Ownership: `app/build.gradle` SDK fields and required compile/lint fixes only.
   - Do not batch dependency modernization unless compile requires it.

3. **Manifest/permission audit slice**
   - Ownership: manifest and permission/request code.
   - Preserve Tasker broadcast contract and notification listener declaration.
   - Treat duplicate receiver cleanup as a separate small change with manifest regression checks.

4. **Notification/reply compatibility slice**
   - Ownership: notification publisher/group/reply/listener surfaces.
   - Add targeted tests before refactoring deprecated APIs.
   - Device checks are required before release signoff.

5. **Dependency batch slices**
   - Ownership: one dependency group at a time.
   - Each batch must pass unit, lint, debug, and release-minified build before the next batch.

6. **Release hardening slice**
   - Ownership: bundle/APK production, signing verification, Play prechecks, staged rollout checklist.
   - Requires human Play Console and signing-key access.

## Open questions for leader/product owner

- Is the target horizon only Play compliance (API 35) or latest platform/tooling (API 37 + AGP 9.2)?
- Is there access to a Samsung watch or paired watch setup for the release gate?
- Where is the release signing process documented/stored, since the repo intentionally does not include signing config?
- Should the current CI remain on JDK 11 for Track A, or should the upgrade branch standardize on JDK 17 immediately?

## Planner recommendation

Proceed with Track A first: target API 35 with minimal behavior change and strict notification/manual validation. Then run Track B in small dependency batches. Defer Track C (AGP 9.2 / Gradle 9.4.1 / JDK 17) until after the Play-compliant release is green, unless the maintainers explicitly want a broader modernization release.

## Verification status for this planning task

- PASS: Read requested context from `/mnt/d/project/LineNotificationSupport/.omx/context/upgrade-plan-20260614T060413Z.md`.
- PASS: Inspected build, manifest, representative runtime, tests, CI, and release config files.
- PASS: Spawned and integrated 3 read-only planning probes:
  - `019ec4be-66a2-77c0-a7b7-334188ea8977` / Android-Gradle-Play route.
  - `019ec4be-871e-7bc0-8bec-15d8b2df90b1` / dependency and code compatibility risks.
  - `019ec4be-a45a-7521-833d-92400cb9ed15` / test and release verification matrix.
- PASS: Official Play target API and AGP 9.2 compatibility facts were checked on 2026-06-14.
- PASS: `java -version` was attempted and failed with `java: command not found`; this is captured as an environment prerequisite.
- PASS: `./gradlew --version` was attempted and failed before JVM startup because `gradlew` currently has CRLF line endings on a Unix shell (`/usr/bin/env: 'sh\\r': No such file or directory`); JDK availability remains a separate prerequisite after that wrapper issue is corrected.
- NOT RUN: Typecheck/build/test/lint are not applicable as proof of a read-only Markdown planning artifact in this environment and cannot start until Java/JDK is available.
