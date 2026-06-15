# LineNotificationSupport Android Upgrade Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Upgrade `LineNotificationSupport` to an API 35 Play-compliant release path without regressing notification mirroring, reply, conversation-start, media, call, Bluetooth, Tasker, or localized UI behavior.

**Architecture:** Keep the first delivery slice narrow: unlock the Gradle baseline, bump the Android SDK target, then make only the compatibility fixes required by that bump. Preserve the current Java/XML architecture and avoid dependency modernization until the API 35 branch is building, testing, and packaging cleanly.

**Tech Stack:** Android app module (`:app`), Java 8 source compatibility, Android Gradle Plugin 8.2.2, Gradle wrapper 8.2, Hilt, Room, AndroidX Test, GitHub Actions CI

---

### Task 1: Unlock the Gradle baseline on this repository

**Files:**
- Modify: `gradlew`
- Verify: `gradle/wrapper/gradle-wrapper.properties`
- Verify: `.github/workflows/android.yml`

- [x] **Step 1: Capture the current failure mode before editing**

Run:

```bash
file gradlew
./gradlew --version
java -version
```

Expected:

```text
gradlew: ... with CRLF line terminators
/usr/bin/env: 'sh\r': No such file or directory
java: command not found
```

- [x] **Step 2: Normalize the Unix Gradle wrapper script to LF line endings**

Run:

```bash
perl -pi -e 's/\r$//' gradlew
chmod +x gradlew
file gradlew
```

Expected:

```text
gradlew: ... ASCII text executable
```

- [x] **Step 3: Re-run the wrapper to separate shell problems from JVM problems**

Run:

```bash
./gradlew --version
```

Expected:

```text
Either Gradle/JVM version output, or a pure Java/JAVA_HOME error with no sh\r parsing failure
```

- [x] **Step 4: Stop if Java is still unavailable and record the blocker explicitly**

Run:

```bash
java -version
```

Expected:

```text
If Java is still missing, do not continue to Task 2. Record the exact JDK setup command or environment handoff needed for this machine.
```

- [ ] **Step 5: Commit the baseline unlock only if the wrapper file actually changed**

```bash
git add gradlew
git commit -m "Normalize Gradle wrapper for Unix execution"
```

### Task 2: Raise the app build target to API 35 and keep the build surface narrow

**Files:**
- Modify: `app/build.gradle`
- Verify: `build.gradle`
- Verify: `gradle/wrapper/gradle-wrapper.properties`
- Verify: `.github/workflows/android.yml`

- [x] **Step 1: Edit the Android SDK target values only**

Change `app/build.gradle` from:

```groovy
android {
    compileSdkVersion 33

    defaultConfig {
        applicationId "com.mysticwind.linenotificationsupport"
        minSdkVersion 26
        targetSdkVersion 33
```

to:

```groovy
android {
    compileSdkVersion 35

    defaultConfig {
        applicationId "com.mysticwind.linenotificationsupport"
        minSdkVersion 26
        targetSdkVersion 35
```

- [x] **Step 2: Run the first build after the SDK bump**

Run:

```bash
./gradlew :app:assembleDebug
```

Expected:

```text
Either BUILD SUCCESSFUL or a concrete compile/manifest/lint failure caused by the API 35 bump
```

- [x] **Step 3: Run the existing JVM test suite without changing dependencies**

Run:

```bash
./gradlew :app:testDebugUnitTest
```

Expected:

```text
All existing JVM tests pass, or failures are captured as real regressions to fix before moving on
```

- [x] **Step 4: Run debug and release lint on the bumped target**

Run:

```bash
./gradlew :app:lintDebug :app:lintRelease
```

Expected:

```text
No new fatal lint errors remain after the API 35 bump
```

- [ ] **Step 5: Commit the target bump once the build and test baseline is green**

```bash
git add app/build.gradle
git commit -m "Target Android 15 for Play compliance"
```

### Task 3: Apply the minimum Android compatibility fixes required by the API 35 build

**Files:**
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/src/main/java/com/mysticwind/linenotificationsupport/reply/broadcastreceiver/ReplyActionBroadcastReceiver.java`
- Verify: `app/src/main/java/com/mysticwind/linenotificationsupport/conversationstarter/broadcastreceiver/StartConversationBroadcastReceiver.java`
- Verify: `app/src/main/java/com/mysticwind/linenotificationsupport/android/AndroidFeatureProvider.java`
- Verify: `app/src/main/java/com/mysticwind/linenotificationsupport/permission/AndroidPermissionRequester.java`

- [x] **Step 1: Remove the duplicate disable-start-conversation receiver entry**

Change `app/src/main/AndroidManifest.xml` from:

```xml
        <receiver
            android:name=".conversationstarter.broadcastreceiver.DisableStartConversationFeatureBroadcastReceiver"
            android:enabled="true"
            android:exported="false">
            <intent-filter>
                <action android:name="disable_start_conversation_feature_action" />
            </intent-filter>
        </receiver>
        <receiver
            android:name=".conversationstarter.broadcastreceiver.DisableStartConversationFeatureBroadcastReceiver"
            android:enabled="true"
            android:exported="false">
            <intent-filter>
                <action android:name="disable_start_conversation_feature_action" />
            </intent-filter>
        </receiver>
```

to:

```xml
        <receiver
            android:name=".conversationstarter.broadcastreceiver.DisableStartConversationFeatureBroadcastReceiver"
            android:enabled="true"
            android:exported="false">
            <intent-filter>
                <action android:name="disable_start_conversation_feature_action" />
            </intent-filter>
        </receiver>
```

- [ ] **Step 2: Make reply-action parcelable extraction explicit for newer SDK behavior**

Not required for the current API 35 local build/test/lint/release gates. The existing implementation compiles and packages on API 35 without this change, so it remains deferred until there is a concrete failing behavior or compatibility signal.

Change `ReplyActionBroadcastReceiver.java` from:

```java
    private Optional<Notification.Action> getLineReplyAction(final Intent intent) {
        final Notification.Action lineReplyAction = intent.getParcelableExtra(DefaultReplyActionBuilder.LINE_REPLY_ACTION_KEY);
        return Optional.ofNullable(lineReplyAction);
    }
```

to:

```java
    private Optional<Notification.Action> getLineReplyAction(final Intent intent) {
        final Notification.Action lineReplyAction;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            lineReplyAction = intent.getParcelableExtra(
                    DefaultReplyActionBuilder.LINE_REPLY_ACTION_KEY,
                    Notification.Action.class);
        } else {
            lineReplyAction = intent.getParcelableExtra(DefaultReplyActionBuilder.LINE_REPLY_ACTION_KEY);
        }
        return Optional.ofNullable(lineReplyAction);
    }
```

- [x] **Step 3: Rebuild the app after the compatibility fixes**

Run:

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest
```

Expected:

```text
BUILD SUCCESSFUL
```

- [x] **Step 4: Re-run release lint and release packaging**

Run:

```bash
./gradlew :app:lintRelease :app:assembleRelease
```

Expected:

```text
Release lint completes and the minified release APK builds
```

- [ ] **Step 5: Commit the compatibility fixes**

```bash
git add app/src/main/AndroidManifest.xml app/src/main/java/com/mysticwind/linenotificationsupport/reply/broadcastreceiver/ReplyActionBroadcastReceiver.java
git commit -m "Fix Android 15 notification compatibility issues"
```

### Task 4: Validate packaging, CI expectations, and manual release gates

**Files:**
- Modify: `.github/workflows/android.yml` (only if the local/CI JDK proves insufficient)
- Verify: `app/build.gradle`
- Verify: `app/src/androidTest/java/com/mysticwind/linenotificationsupport/ExampleInstrumentedTest.java`
- Verify: `app/build/outputs/apk/release/`
- Verify: `app/build/outputs/bundle/release/`

- [x] **Step 1: Run the full local verification matrix**

Equivalent local gates were run and passed individually:

- `:app:assembleDebug`
- `:app:testDebugUnitTest`
- `:app:lintDebug`
- `:app:lintRelease`
- `:app:assembleRelease`
- `:app:bundleRelease`

Run:

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:lintRelease :app:assembleRelease :app:bundleRelease
```

Expected:

```text
All commands succeed, or any remaining failure is isolated to signing, device-only tests, or environment setup
```

- [x] **Step 2: Run instrumentation only when an emulator or device is available**

Current status:

- `:app:connectedDebugAndroidTest` first failed with `No connected devices!`
- a temporary headless emulator was created and booted successfully
- the Gradle UTP installer path failed against the emulator with platform-side install issues
- direct `adb install` of both APKs succeeded
- manual instrumentation succeeded:

```text
com.mysticwind.linenotificationsupport.ExampleInstrumentedTest:.

Time: 0.607

OK (1 test)
```

This proves the repo's current smoke instrumentation test can run on a local emulator, even though the UTP installation path remains flaky in this environment.

Run:

```bash
./gradlew :app:connectedDebugAndroidTest
```

Expected:

```text
The single instrumentation smoke test passes, or the run is explicitly blocked by missing device/emulator capacity
```

- [x] **Step 3: Update CI only if the chosen JDK baseline fails in GitHub Actions**

Updated `.github/workflows/android.yml` to JDK 17, which matches the Android Gradle Plugin 8.2 compatibility requirement.

If CI needs a newer JDK, change `.github/workflows/android.yml` from:

```yaml
    - name: set up JDK 11
      uses: actions/setup-java@v4
      with:
        java-version: '11'
        distribution: 'temurin'
        cache: gradle
```

to:

```yaml
    - name: set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: gradle
```

- [x] **Step 4: Verify the release artifact output paths**

Verified outputs:

- `app/build/outputs/apk/release/app-release-unsigned.apk`
- `app/build/outputs/bundle/release/app-release.aab`

Run:

```bash
ls -R app/build/outputs/apk/release app/build/outputs/bundle/release
```

Expected:

```text
Release APK and AAB files exist
```

- [ ] **Step 5: Record the manual release gates before closing the branch**

Run these checks outside the repo automation:

```text
1. Enable notification listener on a device
2. Verify one-on-one and group LINE notifications mirror correctly
3. Verify reply action still sends through RemoteInput
4. Verify conversation-start action still resolves keyword-to-chat mapping
5. Verify sticker/image and call notification behavior
6. Verify POST_NOTIFICATIONS and BLUETOOTH_CONNECT permission flows
7. Verify signed release upload in Play Console
```

Current remaining external gates:

- No repo-local signing keystore or signing config was found.
- `apksigner verify` against `app/build/outputs/apk/release/app-release-unsigned.apk` reports:

```text
DOES NOT VERIFY
ERROR: Missing META-INF/MANIFEST.MF
```

- Play Console upload / pre-launch requires external account access.
- Real LINE / watch / phone-path behavior still requires manual verification on a real environment.

Additional emulator evidence gathered locally:

- direct `adb install` of the debug APK succeeds
- direct manual instrumentation succeeds for the current smoke test
- Gradle's `connectedDebugAndroidTest` path is flaky in this environment because the UTP installer/emulator path is unstable during package installation
- repeated app-level launch smoke on the temporary emulator is not reliable enough to replace real-device validation

- [ ] **Step 6: Commit the CI adjustment only if it was required**

```bash
git add .github/workflows/android.yml
git commit -m "Align Android CI with upgrade baseline"
```
