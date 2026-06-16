# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
./gradlew :app:assembleDebug                    # build debug APK
./gradlew :app:testDebugUnitTest                # run all JVM unit tests
./gradlew :app:testDebugUnitTest --tests "com.mysticwind.linenotificationsupport.notification.HistoryProvidingNotificationPublisherDecoratorTest"  # single test class
./gradlew :app:lintDebug :app:lintRelease       # lint both variants
./gradlew build                                 # full CI verification (build + test + lint)
```

Use JDK 17 locally; CI (`.github/workflows/android.yml`) targets that baseline.

## Architecture Overview

This is a single-module Android app (Java, minSdk 26, compileSdk 35) that acts as a system `NotificationListenerService` for the LINE messaging app. The app intercepts LINE notifications, transforms them, and re-publishes them in a watch-friendly format.

### Core Flow

```
StatusBarNotification (Android)
  → NotificationListenerService.onNotificationPosted()
      → IncomingNotificationReactor[] (ordered pipeline, can STOP_FURTHER_PROCESSING)
      → LineNotificationBuilder.from()  →  LineNotification (domain model)
      → IdenticalMessageHandler (IGNORE / MERGE / SEND_AS_IS strategy)
      → NotificationPublisherFactory.get()  →  NotificationPublisher chain
          → decorated stack of NotificationPublisher implementations
```

Dismissed notifications follow the same reactor pattern via `DismissedNotificationReactor[]`.

### Reactor Pattern (`notification/reactor/`)

`ReactorModule` assembles two ordered `List<IncomingNotificationReactor>` and `List<DismissedNotificationReactor>` at startup. Each reactor declares `interestedPackages()` and can return `Reaction.STOP_FURTHER_PROCESSING` to halt the chain. The order in `ReactorModule` is load-bearing — add new reactors carefully.

### NotificationPublisher Decorator Chain (`notification/`)

`NotificationPublisherFactory` builds a decorator stack around `SimpleNotificationPublisher`. Key decorators (outermost first):
- `MaxNotificationHandlingNotificationPublisherDecorator` — enforces per-group notification caps
- `HistoryProvidingNotificationPublisherDecorator` — accumulates message history per chat
- `BigNotificationSplittingNotificationPublisherDecorator` — splits oversized messages
- `NotificationMergingNotificationPublisherDecorator` — merges into a single-notification-per-conversation mode
- `DismissActionInjectorNotificationPublisherDecorator` / `LinkActionInjectorNotificationPublisherDecorator` — injects watch action buttons

The factory rebuilds the chain when preference keys in `PREFERENCE_KEYS_THAT_TRIGGER_REBUILDING_NOTIFICATION_PUBLISHER` change.

### Dependency Injection

Hilt (`@AndroidEntryPoint`, `@Singleton`). DI wiring lives entirely in `module/`:
- `ReactorModule` — reactor lists and Bluetooth controller
- `NotificationPublisherModule` — publisher factory and decorators
- `ChatNameModule` — chat name resolution graph
- `PreferenceModule` — `PreferenceProvider` and `SharedPreferences`
- Others: `DebugModule`, `KeywordModule`, `PermissionModule`, `UiModule`

### Key Domain Types

- `LineNotification` — immutable value object built by `LineNotificationBuilder` from a raw `StatusBarNotification`; carries chat ID, sender, message, call state, actions
- `AutoIncomingCallNotificationState` — manages the periodic re-notification loop for incoming calls
- `PreferenceProvider` — single facade over `SharedPreferences` for all feature flags

### Persistence

- **Room** (`persistence/`): `ChatSenderEntry`, `KeywordEntry`, notification history — accessed via DAOs injected through Hilt
- **DataStore** (`datastore-preferences-rxjava3`): used for async preference reads in some components

### Feature Packages

| Package | Responsibility |
|---|---|
| `chatname/` | Resolve and cache chat room names (friend names, group names) |
| `conversationstarter/` | "Start conversation" persistent notification with keyword-based quick-reply |
| `reply/` | Handle reply-action broadcasts from watches |
| `identicalmessage/` | Deduplicate rapid-fire LINE notifications |
| `bluetooth/` | Toggle Bluetooth during calls (workaround for low-volume bug) |
| `debug/` | Notification history debug view; `DebugModeProvider` gate |

## Coding Conventions

- **Java only** — no Kotlin in production code; annotation processors (`annotationProcessor`) not `kapt`
- Lombok is used on several model/DTO classes; verify `@Builder`, `@Value`, `@Data` annotations before adding boilerplate
- 4-space indentation; classes named by role: `*Activity`, `*BroadcastReceiver`, `*Dao`, `*Reactor`
- Unit tests live mirror-side in `app/src/test/java/…` beside the touched package; **JUnit 4 + Mockito, written in Kotlin** (`org.mockito.kotlin:mockito-kotlin:5.2.1`)
- Notification access, reply actions, and broadcast receiver changes are regression-sensitive — run tests and describe manual verification in PRs

### Test Conventions (Kotlin)

- Use `org.mockito.kotlin` API: `any<T>()` (not `anyString()`/`anyInt()`), `argumentCaptor<T>()`, `captor.firstValue`
- Companion object constants imported as `ClassName.Companion.CONSTANT`
- Android classes unavailable in JVM tests — wrap with `mockConstruction(Intent::class.java)`
- `mockito-inline` required for static mocking (`mockStatic`) and construction mocking (`mockConstruction`)
- JVM target is 11 (matches mockito-kotlin compilation target)
