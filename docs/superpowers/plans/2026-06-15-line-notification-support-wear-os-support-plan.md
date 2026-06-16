# LineNotificationSupport Wear OS Support Plan

> **For agentic workers:** This is a read-only planning artifact. Do not treat it as approval to add a Wear module immediately. Complete the phone-notification hardening and wearable validation gates first.

**Goal:** Improve `LineNotificationSupport` support for current Wear OS watches without regressing the existing phone-side LINE notification interception and resend flow.

**Recommendation:** Do **not** start by building a full standalone Wear OS app. Keep the current architecture centered on bridged phone notifications, upgrade those notifications to modern conversation-grade metadata, validate them on current Wear OS builds, and only then consider a thin Wear companion for watch-native surfaces such as ongoing call status.

**Version / date context**
- As of **2026-06-15**, official Android documentation says Wear OS notifications can come from either a phone app that the system bridges to the watch or a local wearable app notification, and both paths should use `NotificationCompat.Builder` plus standard styles such as `MessagingStyle`.
- Official Wear OS documentation updated **2026-05-19** describes **Wear OS 6.1** as available on select devices and in the official emulator, based on **Android 16 / API 36.1**, with no new behavior changes for apps.
- Official Wear OS documentation updated **2026-05-20** and **2026-06-05** describes **Wear OS 7** as a preview path that should be tested with the preview emulator and, for actual Wear-targeted apps, can be targeted with **compileSdk 37 / targetSdk 37**. The same docs introduce **Wear Widgets** and recommend **Live Updates** over `OngoingActivity` on Wear OS 7 and higher.

**Why this order fits this repo**
- The repository still has only a single `:app` Android module and no watch module, so there is no current watch-local code path to upgrade ([settings.gradle](/mnt/d/project/LineNotificationSupport/settings.gradle:1)).
- The app already uses a phone-side `NotificationListenerService` to intercept LINE notifications and resend them as local notifications, which matches the bridged-notification model that Wear OS still supports ([app/src/main/java/com/mysticwind/linenotificationsupport/service/NotificationListenerService.kt](/mnt/d/project/LineNotificationSupport/app/src/main/java/com/mysticwind/linenotificationsupport/service/NotificationListenerService.kt:195)).
- The resend path already uses `NotificationCompat.MessagingStyle` plus `RemoteInput`, so the best first step is to complete missing conversation metadata instead of replacing the architecture ([app/src/main/java/com/mysticwind/linenotificationsupport/utils/MessageStyleImageSupportedNotificationPublisherAsyncTask.kt](/mnt/d/project/LineNotificationSupport/app/src/main/java/com/mysticwind/linenotificationsupport/utils/MessageStyleImageSupportedNotificationPublisherAsyncTask.kt:66), [app/src/main/java/com/mysticwind/linenotificationsupport/reply/DefaultReplyActionBuilder.kt](/mnt/d/project/LineNotificationSupport/app/src/main/java/com/mysticwind/linenotificationsupport/reply/DefaultReplyActionBuilder.kt:36)).
- The current README evidence is still anchored to **Samsung Galaxy Watch Tizen 5.5.0.1** rather than Wear OS, so the biggest gap is validation and notification semantics on modern watches, not feature sprawl ([README.md](/mnt/d/project/LineNotificationSupport/README.md:34)).

**Current technical shape**
- Build baseline: `compileSdkVersion 35`, `targetSdkVersion 35`, `minSdkVersion 26`, no separate wear target ([app/build.gradle](/mnt/d/project/LineNotificationSupport/app/build.gradle:9)).
- Manifest posture: phone app permissions and one exported notification listener service; no Wear activities, services, or watch packaging metadata ([app/src/main/AndroidManifest.xml](/mnt/d/project/LineNotificationSupport/app/src/main/AndroidManifest.xml:8), [app/src/main/AndroidManifest.xml](/mnt/d/project/LineNotificationSupport/app/src/main/AndroidManifest.xml:125)).
- Message modeling: extracted LINE fields include `line.chat.id`, `line.message.id`, sticker URL, `Person`, and action slicing by fixed indices ([app/src/main/java/com/mysticwind/linenotificationsupport/model/LineNotificationBuilder.kt](/mnt/d/project/LineNotificationSupport/app/src/main/java/com/mysticwind/linenotificationsupport/model/LineNotificationBuilder.kt:46)).
- Notification publication: per-chat notification channels, grouped channels, `MessagingStyle`, image attachment via `setData("image/", uri)`, and app-defined actions added after build ([app/src/main/java/com/mysticwind/linenotificationsupport/notificationgroup/NotificationGroupCreator.kt](/mnt/d/project/LineNotificationSupport/app/src/main/java/com/mysticwind/linenotificationsupport/notificationgroup/NotificationGroupCreator.kt:136), [app/src/main/java/com/mysticwind/linenotificationsupport/utils/MessageStyleImageSupportedNotificationPublisherAsyncTask.kt](/mnt/d/project/LineNotificationSupport/app/src/main/java/com/mysticwind/linenotificationsupport/utils/MessageStyleImageSupportedNotificationPublisherAsyncTask.kt:151)).

---

## Requirements Summary

- Current Wear OS watches must receive readable bridged notifications for one-on-one chats, groups, stickers/images, and calls.
- Quick reply from the watch must keep working through `RemoteInput`.
- The first delivery slice must avoid a full watch-app rewrite and must preserve the existing single-module phone app release flow.
- Any future watch-native module must avoid duplicate notifications with the phone bridge path.
- Validation must cover both current stable Wear OS and the next preview line before release claims are made.

## Acceptance Criteria

- On a **Wear OS 6.1** emulator and at least one real Wear OS watch, these flows work end-to-end:
  - one-on-one message mirror
  - group message mirror
  - sticker/image display fallback
  - watch reply through `RemoteInput`
  - dismiss synchronization expectations are documented and verified
  - incoming call / missed call attention path is verified
- Reposted message notifications still use `MessagingStyle`, but are also upgraded to conversation notifications with stable per-chat identity.
- Per-chat metadata is stable enough that future watch-native surfaces can reuse the same chat identity without inventing a second ID scheme.
- No watch module is added until the phone-side notification path passes the validation matrix above.

---

## Phase 0: Lock the Wear OS Baseline Before Architecture Changes

**Objective:** establish the target wearable environments and the exact repo surfaces that shape current watch behavior.

**Files to inspect or update first**
- [README.md](/mnt/d/project/LineNotificationSupport/README.md:32)
- [app/build.gradle](/mnt/d/project/LineNotificationSupport/app/build.gradle:8)
- [app/src/main/AndroidManifest.xml](/mnt/d/project/LineNotificationSupport/app/src/main/AndroidManifest.xml:1)
- [docs/superpowers/plans/2026-06-14-line-notification-support-android-upgrade.md](/mnt/d/project/LineNotificationSupport/docs/superpowers/plans/2026-06-14-line-notification-support-android-upgrade.md:392)

**Work**
- Record the supported validation baseline as:
  - current phone app target: API 35
  - stable watch validation target: Wear OS 6.1
  - forward-compatibility target: Wear OS 7 preview
- Update the repo docs to distinguish legacy Tizen validation from the new Wear OS validation matrix.
- Treat the Wear OS 7 SDK path as a **compatibility probe**, not as the first code migration target for this repo.

**Acceptance criteria**
- A repo-local doc explicitly says the project supports bridged-notification validation on Wear OS 6.1 and Wear OS 7 preview, and that Tizen-only evidence is historical.
- Future work items have a clear stable target and preview target instead of a vague “latest Wear OS” label.

---

## Phase 1: Upgrade Phone-Side Notifications to Modern Conversation Notifications

**Objective:** keep the existing bridged architecture, but make the notifications more legible and better classified on modern Wear OS.

**Primary files**
- [app/src/main/java/com/mysticwind/linenotificationsupport/model/LineNotificationBuilder.kt](/mnt/d/project/LineNotificationSupport/app/src/main/java/com/mysticwind/linenotificationsupport/model/LineNotificationBuilder.kt:46)
- [app/src/main/java/com/mysticwind/linenotificationsupport/utils/MessageStyleImageSupportedNotificationPublisherAsyncTask.kt](/mnt/d/project/LineNotificationSupport/app/src/main/java/com/mysticwind/linenotificationsupport/utils/MessageStyleImageSupportedNotificationPublisherAsyncTask.kt:57)
- [app/src/main/java/com/mysticwind/linenotificationsupport/utils/BigPictureStyleImageSupportedNotificationPublisherAsyncTask.kt](/mnt/d/project/LineNotificationSupport/app/src/main/java/com/mysticwind/linenotificationsupport/utils/BigPictureStyleImageSupportedNotificationPublisherAsyncTask.kt:56)
- [app/src/main/java/com/mysticwind/linenotificationsupport/notificationgroup/NotificationGroupCreator.kt](/mnt/d/project/LineNotificationSupport/app/src/main/java/com/mysticwind/linenotificationsupport/notificationgroup/NotificationGroupCreator.kt:136)

**Work**
- Keep `MessagingStyle` as the foundation, but add conversation identity that Android and Wear can classify consistently:
  - publish a stable long-lived shortcut per conversation
  - attach `Person` data for the other participants
  - associate the reposted notification with that shortcut using `setShortcutId()` or `setShortcutInfo()`
  - set `LocusIdCompat` for the same conversation identity
- Use the existing `chatId` as the canonical cross-surface identifier wherever possible, instead of inventing a Wear-only ID.
- Review whether message vs. call categories should be explicitly reattached on the reposted notification, rather than relying only on style and channel.
- Preserve the current per-chat notification channel behavior, but verify whether merged-message mode and conversation shortcuts need different channel naming or lifecycle rules.

**Acceptance criteria**
- One reposted message notification contains both `MessagingStyle` and stable conversation metadata.
- Group chats, one-on-one chats, and fallback chats with missing LINE IDs all have a defined identity rule.
- The implementation does not require a watch module and remains releaseable through the existing `:app` pipeline.

---

## Phase 2: Harden Actions, Dismissal, and Call Semantics for Wear

**Objective:** make the bridged notification interactions behave more predictably on current watches.

**Primary files**
- [app/src/main/java/com/mysticwind/linenotificationsupport/reply/DefaultReplyActionBuilder.kt](/mnt/d/project/LineNotificationSupport/app/src/main/java/com/mysticwind/linenotificationsupport/reply/DefaultReplyActionBuilder.kt:36)
- [app/src/main/java/com/mysticwind/linenotificationsupport/conversationstarter/StartConversationActionBuilder.kt](/mnt/d/project/LineNotificationSupport/app/src/main/java/com/mysticwind/linenotificationsupport/conversationstarter/StartConversationActionBuilder.kt:40)
- [app/src/main/java/com/mysticwind/linenotificationsupport/service/NotificationListenerService.kt](/mnt/d/project/LineNotificationSupport/app/src/main/java/com/mysticwind/linenotificationsupport/service/NotificationListenerService.kt:387)
- [app/src/main/java/com/mysticwind/linenotificationsupport/utils/MessageStyleImageSupportedNotificationPublisherAsyncTask.kt](/mnt/d/project/LineNotificationSupport/app/src/main/java/com/mysticwind/linenotificationsupport/utils/MessageStyleImageSupportedNotificationPublisherAsyncTask.kt:191)

**Work**
- Ensure reply actions are explicitly wearable-friendly:
  - generated replies allowed where appropriate
  - semantic reply action if supported by the min/target combination
  - optional fixed response choices for the most constrained watch input paths
- Evaluate `NotificationCompat.WearableExtender` for watch-only metadata that improves bridged behavior without creating a wear module. The first candidate is dismissal synchronization via `setDismissalId(chatId)`.
- Revisit the current call-notification loop, which updates timestamps to retrigger attention on the watch, and document which part is a compatibility hack versus intended product behavior.
- Do **not** replace the incoming-call path with watch-local `Live Updates` yet; that only becomes a serious candidate once a watch-local module exists.

**Acceptance criteria**
- Watch replies keep working on the validation devices after any reply metadata changes.
- Dismiss behavior is explicitly tested and documented instead of being left as OEM-dependent guesswork.
- The call alert path has a written contract for incoming, missed, and in-progress call behavior on watches.

---

## Phase 3: Build a Wear OS Validation Matrix Before Adding a Watch Module

**Objective:** convert “latest Wear OS support” from a claim into a repeatable test matrix.

**Validation targets**
- Stable: Wear OS 6.1 emulator
- Forward-looking: Wear OS 7 preview emulator
- Real hardware: at least one production Wear OS watch

**Work**
- Extend the current manual release gates to include watch-specific validation:
  - bridged message appearance
  - group-title fallback behavior
  - sticker/image rendering behavior
  - reply round-trip
  - dismiss behavior
  - call notification attention behavior
- Add targeted unit tests for the repo seams most likely to break watch behavior:
  - fixed action-index extraction in `LineNotificationBuilder`
  - notification metadata persistence in `MessageStyleImageSupportedNotificationPublisherAsyncTask`
  - channel identity and merged-channel behavior in `NotificationGroupCreator`
- If possible, add at least one Robolectric or instrumentation smoke test that asserts reposted notifications carry the expected style and extras. This will not prove real watch rendering, but it will catch obvious metadata regressions before device runs.

**Acceptance criteria**
- The validation matrix exists in repo docs and is runnable without relying on memory of prior experiments.
- At least one automated test asserts a reposted notification’s conversation metadata.
- Wear OS 7 preview is treated as an explicit compatibility lane, even if release support still centers on Wear OS 6.1-era devices.

---

## Phase 4: Optional Thin Wear Companion, Only if Product Goals Justify It

**Objective:** add a watch-native surface only where the bridged-notification model stops being sufficient.

**Do not start this phase unless Phases 0-3 are green.**

**Potential files**
- [settings.gradle](/mnt/d/project/LineNotificationSupport/settings.gradle:1)
- new `wear/` module
- phone-side bridging / launch coordination code

**When this phase becomes justified**
- You need watch-native recents / re-entry for a long-running flow such as call state.
- You want a watch-only glanceable surface, such as Wear Widgets, that cannot be expressed well through bridged notifications.
- You need local watch code for lifecycle or background behavior that the phone bridge cannot provide reliably.

**Design rules if this phase starts**
- Keep the first Wear module intentionally thin: one or two surfaces only.
- As soon as the watch app can generate its own notifications, explicitly configure bridging to avoid duplicate notifications between phone and watch.
- Prefer watch-native value-add surfaces, not a second full notification engine. Reuse the phone-side conversation identity model established in Phase 1.
- If call or progress status becomes watch-local, use `OngoingActivity` for backward compatibility on Wear OS 6 and consider `Live Updates` only for Wear OS 7 and higher.

**Acceptance criteria**
- A new wear module exists only with a specific product justification and a duplicate-notification strategy.
- The phone bridge path remains supported for users without the watch app installed.
- Watch-native surfaces reuse the same chat and action identity model as the phone module.

---

## Risks and Mitigations

- **Risk:** LINE notification internals change again, breaking action indices or extras before Wear work is complete.
  - **Mitigation:** add regression tests around action extraction and chat/message ID parsing before broadening the watch surface.
- **Risk:** conversation shortcuts add complexity without fixing the actual watch rendering problem.
  - **Mitigation:** gate the work behind device validation on Wear OS 6.1 first; if no user-visible improvement appears, stop after metadata hardening and keep the scope narrow.
- **Risk:** a premature Wear module creates duplicate notifications and doubles the test matrix too early.
  - **Mitigation:** defer the module until the bridged path is stable and the product need is explicit.
- **Risk:** call behavior remains OEM-sensitive even after notification cleanup.
  - **Mitigation:** keep call validation as a separate lane and document any OEM-specific fallback behavior rather than hiding it behind a generic support claim.

---

## Verification Steps

- `./gradlew :app:assembleDebug`
- `./gradlew :app:testDebugUnitTest`
- `./gradlew :app:lintDebug :app:lintRelease`
- Device / emulator matrix:
  - install app on phone
  - pair watch or emulator
  - enable notification listener
  - send LINE one-on-one message
  - send LINE group message
  - send sticker/image
  - trigger quick reply from watch
  - dismiss from watch and from phone
  - trigger incoming call / missed call
- Preview-only lane:
  - verify Wear OS 7 preview emulator shows no blocking regression before any claim of future compatibility

---

## Explicit Non-goals

- Do not start with a full standalone Wear chat client.
- Do not move the existing phone app directly to `compileSdk 37 / targetSdk 37` solely because Wear OS 7 preview docs exist.
- Do not rewrite the app UI in Compose as part of this Wear support effort.
- Do not replace the current LINE notification extraction heuristics and Wear support work in the same branch unless failing evidence forces it.

---

## Next Concrete Task

**Recommended first implementation task:** “Promote reposted LINE message notifications to conversation notifications with stable shortcut identity, then validate on a Wear OS 6.1 emulator.”

That task has the best leverage because it upgrades the current architecture to match modern Android and Wear notification expectations without forcing a second app module or a preview-SDK jump.
