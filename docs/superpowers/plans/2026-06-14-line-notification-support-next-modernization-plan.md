# LineNotificationSupport Next Modernization Plan

> **For agentic workers:** REQUIRED SUB-SKILL: use superpowers:subagent-driven-development or superpowers:executing-plans when implementing this plan.

**Goal:** Define the next safe renovation path after the Android 15 / API 35 baseline, with Kotlin migration included but sequenced behind the build and dependency blockers that would otherwise make the migration noisy and risky.

**Recommendation:** Do **not** start with a repo-wide Java-to-Kotlin conversion. First stabilize the toolchain and remove annotation / test debt, then convert the highest-value seams to Kotlin, and only then consider broader architecture or UI modernization.

**Why this order fits this repo**
- The app already builds on AGP 8.2.2 and target/compile SDK 35, but it still compiles Java 8 source and target compatibility, which is a poor base for modern Kotlin and current AndroidX upgrades ([app/build.gradle](/mnt/d/project/LineNotificationSupport/app/build.gradle:7), [app/build.gradle](/mnt/d/project/LineNotificationSupport/app/build.gradle:28)).
- The dependency graph still relies on Java annotation processors for Hilt, Room, Glide, and Lombok, so a naive Kotlin conversion would immediately create mixed kapt/ksp/lombok friction ([app/build.gradle](/mnt/d/project/LineNotificationSupport/app/build.gradle:55), [app/build.gradle](/mnt/d/project/LineNotificationSupport/app/build.gradle:74), [app/build.gradle](/mnt/d/project/LineNotificationSupport/app/build.gradle:79), [app/build.gradle](/mnt/d/project/LineNotificationSupport/app/build.gradle:84)).
- Core behavior is centered in a large notification listener service and ordered reactor pipeline, so safe migration should preserve these seams instead of combining language migration with behavioral refactors ([app/src/main/java/com/mysticwind/linenotificationsupport/service/NotificationListenerService.java](/mnt/d/project/LineNotificationSupport/app/src/main/java/com/mysticwind/linenotificationsupport/service/NotificationListenerService.java:72), [app/src/main/java/com/mysticwind/linenotificationsupport/module/ReactorModule.java](/mnt/d/project/LineNotificationSupport/app/src/main/java/com/mysticwind/linenotificationsupport/module/ReactorModule.java:33)).
- There is already architecture debt called out in TODOs around duplicated broadcast receiver logic, missing unit tests, and synchronous Room access; those should shape the Kotlin rollout order ([app/src/main/java/com/mysticwind/linenotificationsupport/reply/broadcastreceiver/ReplyActionBroadcastReceiver.java](/mnt/d/project/LineNotificationSupport/app/src/main/java/com/mysticwind/linenotificationsupport/reply/broadcastreceiver/ReplyActionBroadcastReceiver.java:33), [app/src/main/java/com/mysticwind/linenotificationsupport/notificationgroup/NotificationGroupCreator.java](/mnt/d/project/LineNotificationSupport/app/src/main/java/com/mysticwind/linenotificationsupport/notificationgroup/NotificationGroupCreator.java:143), [app/src/main/java/com/mysticwind/linenotificationsupport/conversationstarter/persistence/KeywordRoomDatabase.java](/mnt/d/project/LineNotificationSupport/app/src/main/java/com/mysticwind/linenotificationsupport/conversationstarter/persistence/KeywordRoomDatabase.java:31)).

**Current technical shape**
- App type: single Android app module with XML layouts, Fragments, Navigation, and Hilt ([app/build.gradle](/mnt/d/project/LineNotificationSupport/app/build.gradle:59), [app/src/main/res/navigation/nav_graph.xml](/mnt/d/project/LineNotificationSupport/app/src/main/res/navigation/nav_graph.xml:1), [app/src/main/java/com/mysticwind/linenotificationsupport/Application.java](/mnt/d/project/LineNotificationSupport/app/src/main/java/com/mysticwind/linenotificationsupport/Application.java:18)).
- Persistence: three Room databases with Java entities/DAO usage ([app/src/main/java/com/mysticwind/linenotificationsupport/persistence/AppDatabase.java](/mnt/d/project/LineNotificationSupport/app/src/main/java/com/mysticwind/linenotificationsupport/persistence/AppDatabase.java:9), [app/src/main/java/com/mysticwind/linenotificationsupport/persistence/ChatGroupDatabase.java](/mnt/d/project/LineNotificationSupport/app/src/main/java/com/mysticwind/linenotificationsupport/persistence/ChatGroupDatabase.java:11), [app/src/main/java/com/mysticwind/linenotificationsupport/conversationstarter/persistence/KeywordRoomDatabase.java](/mnt/d/project/LineNotificationSupport/app/src/main/java/com/mysticwind/linenotificationsupport/conversationstarter/persistence/KeywordRoomDatabase.java:15)).
- Entry points: multiple activities, receivers, and one exported notification listener service, so Android behavior is manifest-sensitive and regression-prone ([app/src/main/AndroidManifest.xml](/mnt/d/project/LineNotificationSupport/app/src/main/AndroidManifest.xml:17), [app/src/main/AndroidManifest.xml](/mnt/d/project/LineNotificationSupport/app/src/main/AndroidManifest.xml:125)).
- Test baseline: existing JVM unit tests are meaningful but still modest in breadth, which argues for adding coverage before large-scale file conversion.

---

## Phase 0: Lock the modernization baseline

**Objective:** turn the current API 35 branch into a reliable starting point for follow-up work.

**Files to inspect or update first**
- `.github/workflows/android.yml`
- `gradle.properties`
- `build.gradle`
- `app/build.gradle`

**Work**
- Standardize the required local/CI JDK version and make the Gradle/toolchain expectation explicit in docs and CI, since CI is already on JDK 17 while the module still targets Java 8 bytecode ([.github/workflows/android.yml](/mnt/d/project/LineNotificationSupport/.github/workflows/android.yml:16), [app/build.gradle](/mnt/d/project/LineNotificationSupport/app/build.gradle:28)).
- Add a documented verification matrix for every modernization phase: `assembleDebug`, `testDebugUnitTest`, and lint at minimum.
- Capture the known dirty-worktree Android upgrade items separately from this plan so modernization tasks do not get mixed with in-flight upgrade edits.

**Acceptance criteria**
- CI and local setup both document the same required JDK/Gradle expectations.
- A small “modernization baseline” checklist exists and is runnable before every later phase.

---

## Phase 1: Remove blockers before Kotlin adoption

**Objective:** shrink the number of technologies that fight Kotlin before the first `.kt` file lands.

**Priority blockers**
- Remove Lombok from production code by replacing the small set of `@Value`, `@Data`, `@Builder`, and related usages with explicit Java code or Kotlin-friendly models later ([app/build.gradle](/mnt/d/project/LineNotificationSupport/app/build.gradle:74), [app/src/main/java/com/mysticwind/linenotificationsupport/model/LineNotification.java](/mnt/d/project/LineNotificationSupport/app/src/main/java/com/mysticwind/linenotificationsupport/model/LineNotification.java:18), [app/src/main/java/com/mysticwind/linenotificationsupport/debug/history/dto/NotificationHistoryEntry.java](/mnt/d/project/LineNotificationSupport/app/src/main/java/com/mysticwind/linenotificationsupport/debug/history/dto/NotificationHistoryEntry.java:15)).
- Replace `allowMainThreadQueries()` in the keyword database path before broader refactors so later Kotlin/coroutines work does not inherit unsafe persistence patterns ([app/src/main/java/com/mysticwind/linenotificationsupport/conversationstarter/persistence/KeywordRoomDatabase.java](/mnt/d/project/LineNotificationSupport/app/src/main/java/com/mysticwind/linenotificationsupport/conversationstarter/persistence/KeywordRoomDatabase.java:29)).
- Add targeted unit tests around the most stateful notification orchestration seams that currently advertise missing coverage ([app/src/main/java/com/mysticwind/linenotificationsupport/notificationgroup/NotificationGroupCreator.java](/mnt/d/project/LineNotificationSupport/app/src/main/java/com/mysticwind/linenotificationsupport/notificationgroup/NotificationGroupCreator.java:143), [app/src/main/java/com/mysticwind/linenotificationsupport/notification/reactor/CallInProgressTrackingReactor.java](/mnt/d/project/LineNotificationSupport/app/src/main/java/com/mysticwind/linenotificationsupport/notification/reactor/CallInProgressTrackingReactor.java:100)).
- Consolidate duplicated broadcast-receiver request parsing or action handling before converting those classes, since there is already a known duplication seam between reply and conversation-start flows ([app/src/main/java/com/mysticwind/linenotificationsupport/reply/broadcastreceiver/ReplyActionBroadcastReceiver.java](/mnt/d/project/LineNotificationSupport/app/src/main/java/com/mysticwind/linenotificationsupport/reply/broadcastreceiver/ReplyActionBroadcastReceiver.java:33)).

**Acceptance criteria**
- Lombok is gone from `app/src/main/java`.
- New tests protect notification grouping, action dispatch, and at least one receiver flow.
- No main-thread Room access remains in production.

---

## Phase 2: Enable Kotlin as an additive language

**Objective:** make Kotlin available without forcing a big-bang migration.

**Files**
- `build.gradle`
- `app/build.gradle`

**Work**
- Add the Kotlin Android plugin and Kotlin stdlib/runtime path.
- Move Java/Kotlin compilation to a modern shared baseline, preferably JDK 17 toolchains, while keeping runtime behavior unchanged.
- Pick one annotation-processing path deliberately:
  - `kapt` first if the goal is minimal disruption with Hilt/Room/Glide.
  - `ksp` later only after the codebase is already mixed-language and dependencies are ready.
- Keep existing Java sources compiling unchanged; the first Kotlin phase is enablement, not conversion volume.

**Acceptance criteria**
- The app builds with both Java and Kotlin sources in the same module.
- Hilt, Room, and Glide generation still work after Kotlin is added.
- CI runs at least one Kotlin-compiling task successfully.

**Decision note**
- For this repo, `kapt` is the pragmatic first step. The build already depends on classic annotation processors, and adding Kotlin with `kapt` is lower-risk than coupling Kotlin introduction with a simultaneous KSP migration.

---

## Phase 3: First Kotlin conversion slice

**Objective:** prove the Kotlin path on low-risk, high-signal files before touching the notification core.

**Convert first**
- Small immutable model objects currently using Lombok.
- Pure utility or mapper-style classes with strong unit tests.
- ViewModel or settings-side classes that are not on the notification hot path.

**Defer initially**
- `NotificationListenerService`
- Reactor ordering / notification publisher composition
- Broadcast receivers with direct Android framework and PendingIntent behavior
- Room databases and DAO contract rewrites

**Suggested initial candidates**
- `model/NotificationHistoryEntry.java`
- `chatname/model/Chat.java`
- `conversationstarter/model/KeywordEntry.java`
- `identicalmessage/IdenticalMessageHandledResult.java`

These are small, mostly data-shaped types and give immediate value because they help eliminate Lombok while exercising mixed Java/Kotlin builds.

**Acceptance criteria**
- At least 3-5 small files convert to Kotlin with no behavior change.
- The converted files no longer rely on Lombok.
- Unit tests pass unchanged or with only mechanical fixture updates.

---

## Phase 4: Kotlin-guided modular cleanup

**Objective:** use Kotlin only where it helps the architecture, not as a cosmetic rewrite.

**Target seams**
- Extract the orchestration logic inside `NotificationListenerService` into smaller collaborators so the service becomes a thin Android shell ([app/src/main/java/com/mysticwind/linenotificationsupport/service/NotificationListenerService.java](/mnt/d/project/LineNotificationSupport/app/src/main/java/com/mysticwind/linenotificationsupport/service/NotificationListenerService.java:158)).
- Replace manual listener / state glue with clearer state-holder abstractions around preferences and notification counters.
- Convert DI modules opportunistically when touched, but only after the underlying services have test coverage.

**Acceptance criteria**
- The service class loses responsibility, not just line-for-line language changes.
- Reactor registration and ordering stay explicit and testable.
- Each extracted collaborator has focused tests.

---

## Phase 5: Optional modernization after Kotlin is established

**Only start this when Phases 0-4 are green.**

**Possible tracks**
- Dependency refresh: newer AndroidX Test, Mockito, Navigation, AppCompat, Material, Room, DataStore.
- Coroutines/Flow where async or observer code is currently awkward.
- UI modernization for selected screens, likely keeping XML first and deferring Compose unless there is a real product reason.
- Room schema export and migration discipline for future storage changes.

**Explicit non-goal for now**
- A full Compose rewrite. This repo is still XML/Fragment-based, and there is no evidence yet that UI rewrite is the highest-value next step ([app/src/main/res/layout/activity_main.xml](/mnt/d/project/LineNotificationSupport/app/src/main/res/layout/activity_main.xml:1), [app/src/main/res/layout/help_main.xml](/mnt/d/project/LineNotificationSupport/app/src/main/res/layout/help_main.xml:1)).

---

## Recommended execution order

1. Finish and verify the current Android upgrade work already in progress.
2. Create a focused “de-Lombok + test hardening” branch.
3. Enable Kotlin with `kapt`, without converting core logic yet.
4. Convert a handful of model/value classes to Kotlin.
5. Re-evaluate whether notification-core refactoring is now easier and safer.

---

## Risks and mitigations

- **Risk:** mixed migration across Kotlin, dependency upgrades, and architecture changes creates unreadable regressions.
  - **Mitigation:** one axis per phase; no simultaneous big-bang rewrite.
- **Risk:** Lombok and annotation processing produce brittle Gradle failures once Kotlin lands.
  - **Mitigation:** remove Lombok first, introduce Kotlin second, revisit processor modernization later.
- **Risk:** notification behavior regresses in subtle ordering scenarios.
  - **Mitigation:** add targeted tests around service/reactor interactions before touching those files.
- **Risk:** Room and threading issues get cemented into new Kotlin code.
  - **Mitigation:** remove `allowMainThreadQueries()` before coroutine or Flow adoption.

---

## Verification checklist for each phase

- `./gradlew :app:assembleDebug`
- `./gradlew :app:testDebugUnitTest`
- `./gradlew :app:lintDebug :app:lintRelease`
- For Kotlin-introducing phases: verify generated sources for Hilt, Room, and Glide still compile

---

## Next concrete task I would schedule

**Task:** “Remove Lombok from production models and add regression tests for notification grouping + receiver action parsing.”

That task has the best leverage because it reduces Kotlin migration friction, improves test safety, and avoids mixing toolchain work with notification-core rewrites.
