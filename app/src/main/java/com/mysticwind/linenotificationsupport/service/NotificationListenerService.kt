package com.mysticwind.linenotificationsupport.service

import android.app.Notification
import android.app.NotificationManager
import android.content.Intent
import android.content.SharedPreferences
import android.os.Handler
import android.os.IBinder
import android.service.notification.StatusBarNotification
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager
import com.github.rholder.retry.Attempt
import com.github.rholder.retry.RetryListener
import com.github.rholder.retry.RetryerBuilder
import com.github.rholder.retry.StopStrategies
import com.github.rholder.retry.WaitStrategies
import com.google.common.collect.HashMultimap
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import com.mysticwind.linenotificationsupport.android.AndroidFeatureProvider
import com.mysticwind.linenotificationsupport.conversationstarter.ConversationStarterNotificationManager
import com.mysticwind.linenotificationsupport.debug.DebugModeProvider
import com.mysticwind.linenotificationsupport.identicalmessage.AsIsIdenticalMessageHandler
import com.mysticwind.linenotificationsupport.identicalmessage.IdenticalMessageEvaluator
import com.mysticwind.linenotificationsupport.identicalmessage.IdenticalMessageHandler
import com.mysticwind.linenotificationsupport.identicalmessage.IgnoreIdenticalMessageHandler
import com.mysticwind.linenotificationsupport.identicalmessage.MergeIdenticalMessageHandler
import com.mysticwind.linenotificationsupport.line.Constants.LINE_PACKAGE_NAME
import com.mysticwind.linenotificationsupport.model.AutoIncomingCallNotificationState
import com.mysticwind.linenotificationsupport.model.IdenticalMessageHandlingStrategy
import com.mysticwind.linenotificationsupport.model.LineNotification
import com.mysticwind.linenotificationsupport.model.LineNotificationBuilder
import com.mysticwind.linenotificationsupport.notification.NotificationPublisherFactory
import com.mysticwind.linenotificationsupport.notification.impl.DefaultAndroidNotificationManager
import com.mysticwind.linenotificationsupport.notification.impl.DumbNotificationCounter
import com.mysticwind.linenotificationsupport.notification.reactor.DismissedNotificationReactor
import com.mysticwind.linenotificationsupport.notification.reactor.IncomingNotificationReactor
import com.mysticwind.linenotificationsupport.notification.reactor.Reaction
import com.mysticwind.linenotificationsupport.notificationgroup.NotificationGroupCreator
import com.mysticwind.linenotificationsupport.preference.PreferenceProvider
import com.mysticwind.linenotificationsupport.utils.NotificationExtractor
import com.mysticwind.linenotificationsupport.utils.NotificationIdGenerator
import com.mysticwind.linenotificationsupport.utils.StatusBarNotificationExtractor
import dagger.hilt.android.AndroidEntryPoint
import org.apache.commons.lang3.ArrayUtils
import org.apache.commons.lang3.tuple.Pair
import timber.log.Timber
import java.time.Instant
import java.util.Arrays
import java.util.Collections
import java.util.Optional
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors
import javax.inject.Inject

@AndroidEntryPoint
class NotificationListenerService : android.service.notification.NotificationListenerService() {

    private var autoIncomingCallNotificationState: AutoIncomingCallNotificationState? = null

    private val onSharedPreferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, preferenceKey ->
        Timber.d("onSharedPreferenceChangeListener: updated preference [%s]", preferenceKey)
        if (PREFERENCE_KEYS_THAT_TRIGGER_REBUILDING_NOTIFICATION_PUBLISHER.contains(preferenceKey)) {
            notificationPublisherFactory.notifyChange()
        }
        if (PreferenceProvider.CONVERSATION_STARTER_KEY == preferenceKey) {
            if (preferenceProvider.shouldShowConversationStarterNotification()) {
                conversationStarterNotificationManager.publishNotification()
            } else {
                conversationStarterNotificationManager.cancelNotification()
            }
        }
    }

    private var isInitialized = false
    private var isListenerConnected = false

    @Inject
    lateinit var lineNotificationBuilder: LineNotificationBuilder

    @Inject
    lateinit var incomingNotificationReactors: List<@JvmSuppressWildcards IncomingNotificationReactor>

    @Inject
    lateinit var dismissedNotificationReactors: List<@JvmSuppressWildcards DismissedNotificationReactor>

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var defaultAndroidNotificationManager: DefaultAndroidNotificationManager

    @Inject
    lateinit var handler: Handler

    @Inject
    lateinit var notificationPublisherFactory: NotificationPublisherFactory

    @Inject
    lateinit var preferenceProvider: PreferenceProvider

    @Inject
    lateinit var notificationIdGenerator: NotificationIdGenerator

    @Inject
    lateinit var dumbNotificationCounter: DumbNotificationCounter

    @Inject
    lateinit var conversationStarterNotificationManager: ConversationStarterNotificationManager

    @Inject
    lateinit var notificationGroupCreator: NotificationGroupCreator

    @Inject
    lateinit var debugModeProvider: DebugModeProvider

    override fun onCreate() {
        super.onCreate()

        Timber.d("NotificationListenerService onCreate")
    }

    override fun onBind(intent: Intent): IBinder? {
        Timber.d("NotificationListenerService onBind")
        return super.onBind(intent)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()

        isListenerConnected = true
        Timber.w("NotificationListenerService onListenerConnected")

        if (isInitialized) {
            Timber.d("NotificationListenerService has already been initialized")
            return
        }

        // setup things that are only available through NotificationListenerServices
        this.defaultAndroidNotificationManager.initialize(
            { getActiveNotificationsFromAllAppsSafely() },
            { key -> cancelNotification(key) }
        )

        // getting active notifications to restore previous state
        val existingNotifications = getActiveNotificationsFromAllAppsSafely().stream()
            .filter { notification -> notification.packageName == packageName }
            .collect(Collectors.toList())

        if (existingNotifications.isNotEmpty()) {
            val keys = existingNotifications.stream()
                .map { notification -> notification.key }
                .reduce { key1, key2 -> "$key1,$key2" }
                .orElse("N/A")
            Timber.w("Existing notifications to restore [%s]", keys)
        } else {
            Timber.d("No existing notifications to restore")
        }

        dumbNotificationCounter.updateStateFromExistingNotifications(existingNotifications)

        notificationPublisherFactory.notifyChangeWithExistingNotifications(existingNotifications)

        notificationGroupCreator.createNotificationGroups()

        sharedPreferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener)
        Timber.d("Registered onSharedPreferenceChangeListener")

        scheduleNotificationCounterCheck()

        isInitialized = true

        if (preferenceProvider.shouldShowConversationStarterNotification()) {
            conversationStarterNotificationManager.publishNotification()
        }

        Timber.d("Service completed initialization")
    }

    override fun onDestroy() {
        super.onDestroy()

        isInitialized = false

        Timber.w("NotificationListenerService onDestroy")
    }

    override fun onUnbind(intent: Intent): Boolean {
        Timber.w("NotificationListenerService onUnbind")

        return super.onUnbind(intent)
    }

    override fun onNotificationPosted(statusBarNotification: StatusBarNotification) {

        for (reactor in incomingNotificationReactors) {
            try {
                if (!reactor.interestedPackages().contains(statusBarNotification.packageName)) {
                    continue
                }
                if (StatusBarNotificationExtractor.isSummary(statusBarNotification) && !reactor.isInterestInNotificationGroup()) {
                    continue
                }

                Timber.d(
                    "Processing IncomingNotificationReactor [%s] for notification key [%s]",
                    reactor.javaClass.simpleName, statusBarNotification.key
                )
                val reaction = reactor.reactToIncomingNotification(statusBarNotification)

                // TODO how do we deal with legacy actions e.g. auto dismisses?
                if (reaction == Reaction.STOP_FURTHER_PROCESSING) {
                    Timber.d(
                        "IncomingNotificationReactor [%s] requested to [%s] after processing notification key [%s]",
                        reactor.javaClass.simpleName, reaction, statusBarNotification.key
                    )
                    return
                }
            } catch (e: Exception) {
                Timber.e(
                    e, "[ERROR] Failed to process IncomingNotificationReactor [%s]: error [%s] message [%s]",
                    reactor.javaClass.simpleName, e.javaClass.simpleName, e.message
                )
            }
        }

        try {
            onNotificationPostedUnsafe(statusBarNotification)
        } catch (e: Exception) {
            Timber.e(e, "[ERROR] onNotificationPosted failed to handle exception [%s]", e.message)
            if (debugModeProvider.isDebugMode()) {
                Toast.makeText(this, "[ERROR] LNS onNotificationPosted", Toast.LENGTH_SHORT)
            }
        }
    }

    fun onNotificationPostedUnsafe(statusBarNotification: StatusBarNotification) {
        // ignore messages from ourselves
        if (statusBarNotification.packageName.startsWith(packageName)) {
            return
        }

        if (shouldIgnoreNotification(statusBarNotification)) {
            return
        }

        sendNotification(statusBarNotification)
    }

    private fun shouldIgnoreNotification(statusBarNotification: StatusBarNotification): Boolean {
        val packageName = statusBarNotification.packageName

        // let's just focus on Line notifications for now
        if (LINE_PACKAGE_NAME != packageName) {
            return true
        }

        // ignore summaries
        if (StatusBarNotificationExtractor.isSummary(statusBarNotification)) {
            return true
        }

        return false
    }

    private fun sendNotification(notificationFromLine: StatusBarNotification) {
        val lineNotification = lineNotificationBuilder.from(notificationFromLine)

        val notificationId = notificationIdGenerator.getNextNotificationId()

        val notificationAndId = handleDuplicate(lineNotification, notificationId)

        if (!notificationAndId.isPresent) {
            // skip duplicated message
            return
        }

        val actionAdjustedLineNotification = adjustActionOrder(notificationAndId.get().left)

        sendNotification(actionAdjustedLineNotification, notificationAndId.get().right)
    }

    private fun sendNotification(lineNotification: LineNotification, notificationId: Int) {
        notificationPublisherFactory.get().publishNotification(lineNotification, notificationId)

        if (lineNotification.callState == null) {
            return
        }

        // deal with auto notifications for calls
        if (lineNotification.callState == LineNotification.CallState.INCOMING) {
            this.autoIncomingCallNotificationState?.cancel()
            this.autoIncomingCallNotificationState = AutoIncomingCallNotificationState.builder()
                .lineNotification(lineNotification)
                .waitDurationInSeconds(getWaitDurationInSeconds())
                .timeoutInSeconds(getAutoSendTimeoutInSecondsFromPreferences())
                .build()
            this.autoIncomingCallNotificationState!!.notified(notificationId)
            sendIncomingCallNotification(this.autoIncomingCallNotificationState!!)
        }

        val autoIncomingCallNotificationState = this.autoIncomingCallNotificationState ?: return

        when (lineNotification.callState) {
            LineNotification.CallState.MISSED_CALL -> autoIncomingCallNotificationState.setMissedCall()
            LineNotification.CallState.IN_A_CALL -> autoIncomingCallNotificationState.setAccepted()
            else -> { /* no-op */ }
        }
    }

    private fun adjustActionOrder(lineNotification: LineNotification): LineNotification {
        if (!shouldReverseActionOrder(lineNotification)) {
            return lineNotification
        }
        if (lineNotification.actions.size < 2) {
            return lineNotification
        }
        val actions = ArrayList(lineNotification.actions)
        val firstAction = actions[0]
        actions.add(0, actions[1])
        actions.add(1, firstAction)
        return lineNotification.toBuilder()
            .clearActions()
            .actions(actions)
            .build()
    }

    private fun handleDuplicate(lineNotification: LineNotification, notificationId: Int): Optional<Pair<LineNotification, Int>> {
        val handler = selectIdenticalMessageHandler()
        return handler.handle(lineNotification, notificationId)
    }

    private fun selectIdenticalMessageHandler(): IdenticalMessageHandler {
        val strategy = getIdenticalMessageHandlingStrategyFromPreference()
        return STRATEGY_TO_HANDLER_MAP.getOrDefault(strategy, IGNORE_IDENTICAL_MESSAGE_HANDLER)
    }

    private fun getIdenticalMessageHandlingStrategyFromPreference(): IdenticalMessageHandlingStrategy {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val stringStrategy = preferences.getString("identical_message_handling_strategy", "IGNORE")!!
        return IdenticalMessageHandlingStrategy.valueOf(stringStrategy)
    }

    private fun getWaitDurationInSeconds(): Double {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val shouldAutoNotify = preferences.getBoolean("auto_call_notifications", true)
        if (!shouldAutoNotify) {
            return 1000.0 // a random big value
        }
        val waitTimeString = preferences.getString("auto_notifications_wait", "3.0")!!
        return waitTimeString.toDouble()
    }

    private fun getAutoSendTimeoutInSecondsFromPreferences(): Long {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val shouldAutoNotify = preferences.getBoolean("auto_call_notifications", true)
        if (!shouldAutoNotify) {
            return 0L
        }
        val timeoutString = preferences.getString("auto_notifications_timeout", "-1")!!
        val timeout = parseTimeout(timeoutString)
        return if (timeout < 0) {
            // 15 min should be more than enough
            15 * 60L
        } else {
            timeout.toLong()
        }
    }

    private fun parseTimeout(timeoutString: String): Int {
        return try {
            timeoutString.toInt()
        } catch (e: Exception) {
            -1
        }
    }

    private fun shouldReverseActionOrder(lineNotification: LineNotification): Boolean {
        if (LineNotification.CallState.INCOMING != lineNotification.callState) {
            return false
        }
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        return preferences.getBoolean("call_notifications_reverse_action", false)
    }

    private fun sendIncomingCallNotification(autoIncomingCallNotificationState: AutoIncomingCallNotificationState) {
        if (!autoIncomingCallNotificationState.shouldNotify()) {
            cancelIncomingCallNotification(autoIncomingCallNotificationState.getIncomingCallNotificationIds())
            return
        }
        try {
            val lineNotificationWithUpdatedTimestamp =
                autoIncomingCallNotificationState.lineNotification.toBuilder()
                    // very interesting that the timestamp needs to be updated for the watch to vibrate
                    .timestamp(Instant.now().toEpochMilli())
                    .build()

            val notificationId: Int
            if (preferenceProvider.shouldCreateNewContinuousCallNotifications()) {
                notificationId = notificationIdGenerator.getNextNotificationId()
                autoIncomingCallNotificationState.notified(notificationId)
            } else {
                notificationId = autoIncomingCallNotificationState.getIncomingCallNotificationIds().iterator().next()
            }
            notificationPublisherFactory.get().publishNotification(lineNotificationWithUpdatedTimestamp, notificationId)
        } catch (e: Exception) {
            Timber.e(e, "Failed to send incoming call notifications: " + e.message)
        }

        scheduleNextIncomingCallNotification(autoIncomingCallNotificationState)
    }

    private fun cancelIncomingCallNotification(notificationIdsToCancel: Set<Int>) {
        val notificationManager = NotificationManagerCompat.from(this@NotificationListenerService)
        for (notificationId in notificationIdsToCancel) {
            try {
                notificationManager.cancel(notificationId)
            } catch (e: Exception) {
                Timber.w(e, "Failed to cancel notification %d: %s", notificationId, e.message)
            }
        }
    }

    private fun scheduleNextIncomingCallNotification(autoIncomingCallNotificationState: AutoIncomingCallNotificationState) {
        val delayInMillis = (autoIncomingCallNotificationState.waitDurationInSeconds * 1000).toLong()

        handler.postDelayed({
            sendIncomingCallNotification(autoIncomingCallNotificationState)
        }, delayInMillis)
    }

    private fun getActiveNotificationsFromAllAppsSafely(): List<StatusBarNotification> {
        val callable = java.util.concurrent.Callable<List<StatusBarNotification>> {
            val notifications = activeNotifications
            if (ArrayUtils.isEmpty(notifications)) {
                @Suppress("UNCHECKED_CAST")
                Collections.EMPTY_LIST as List<StatusBarNotification>
            } else {
                Arrays.asList(*notifications)
            }
        }

        val retryListener = object : RetryListener {
            override fun <V> onRetry(attempt: Attempt<V>) {
                if (attempt.hasException()) {
                    Timber.w(
                        attempt.exceptionCause,
                        "Failed to fetch active notifications attempt [%d] error [%s]",
                        attempt.attemptNumber, attempt.exceptionCause.message
                    )
                }
                if (attempt.hasResult() && attempt.attemptNumber > 1) {
                    Timber.w("Finally fetched active notifications after [%d] attempts", attempt.attemptNumber)
                }
            }
        }

        val retryer = RetryerBuilder.newBuilder<List<StatusBarNotification>>()
            .retryIfException()
            .withWaitStrategy(WaitStrategies.fixedWait(100, TimeUnit.MILLISECONDS))
            .withStopStrategy(StopStrategies.stopAfterAttempt(3))
            .withRetryListener(retryListener)
            .build()

        return try {
            retryer.call(callable)
        } catch (e: Exception) {
            Timber.w(e, "Unable to fetch active notifications after retries ... error message [%s]", e.message)
            @Suppress("UNCHECKED_CAST")
            Collections.EMPTY_LIST as List<StatusBarNotification>
        }
    }

    private fun scheduleNotificationCounterCheck() {
        handler.postDelayed(
            { checkNotificationCounter() },
            NOTIFICATION_COUNTER_CHECK_PERIOD
        )
    }

    private fun checkNotificationCounter() {
        if (!isListenerConnected) {
            Timber.w("Listener is not connected. Skipping notification counter check")
            scheduleNotificationCounterCheck()
            return
        }
        val groupToNotificationKeyMultimap = HashMultimap.create<String, String>()
        getActiveNotificationsFromAllAppsSafely().stream()
            .filter { notification -> notification.packageName == packageName }
            .forEach { notification ->
                groupToNotificationKeyMultimap.put(notification.notification.group, notification.key)
            }
        val isValid = dumbNotificationCounter.validateNotifications(groupToNotificationKeyMultimap)

        if (!isValid) {
            // TODO why would this happen outside of service being killed?
        }

        scheduleNotificationCounterCheck()
    }

    override fun onNotificationRemoved(statusBarNotification: StatusBarNotification) {

        for (reactor in dismissedNotificationReactors) {
            try {
                if (!reactor.interestedPackages().contains(statusBarNotification.packageName)) {
                    continue
                }
                if (StatusBarNotificationExtractor.isSummary(statusBarNotification) && !reactor.isInterestInNotificationGroup()) {
                    continue
                }

                Timber.d(
                    "Processing DismissedNotificationReactor [%s] for notification key [%s]",
                    reactor.javaClass.simpleName, statusBarNotification.key
                )
                val reaction = reactor.reactToDismissedNotification(statusBarNotification)

                if (reaction == Reaction.STOP_FURTHER_PROCESSING) {
                    Timber.d(
                        "DismissedNotificationReactor [%s] requested to [%s] after processing notification key [%s]",
                        reactor.javaClass.simpleName, reaction, statusBarNotification.key
                    )
                    return
                }
            } catch (e: Exception) {
                Timber.e(
                    e, "[ERROR] Failed to process DismissedNotificationReactor [%s]: error [%s] message [%s]",
                    reactor.javaClass.simpleName, e.javaClass.simpleName, e.message
                )
            }
        }

        try {
            onNotificationRemovedUnsafe(statusBarNotification)
        } catch (e: Exception) {
            Timber.e(e, "[ERROR] onNotificationRemoved failed to handle exception [%s]", e.message)
            if (debugModeProvider.isDebugMode()) {
                Toast.makeText(this, "[ERROR] LNS onNotificationRemoved", Toast.LENGTH_SHORT)
            }
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()

        isListenerConnected = false
        Timber.w("NotificationListenerService onListenerDisconnected")

        try {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener)
        } catch (e: Exception) {
            Timber.w(e, "Errors thrown when unregistering listener: [%s]", e.message)
        }
        Timber.d("Unregistered onSharedPreferenceChangeListener")

        isInitialized = false
    }

    fun onNotificationRemovedUnsafe(statusBarNotification: StatusBarNotification) {
        super.onNotificationRemoved(statusBarNotification)

        if (shouldIgnoreNotification(statusBarNotification)) {
            return
        }

        val dismissedLineNotification = lineNotificationBuilder.from(statusBarNotification)

        if (LineNotification.CallState.INCOMING == dismissedLineNotification.callState &&
            this.autoIncomingCallNotificationState != null
        ) {
            this.autoIncomingCallNotificationState!!.cancel()
        }

        if (preferenceProvider.shouldAutoDismissLineNotificationSupportNotifications()) {
            dismissedLineNotification.chatId?.let { dismissLineNotificationSupportNotifications(it) }
        }
    }

    private fun dismissLineNotificationSupportNotifications(chatId: String) {
        val notificationManager = this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val notificationIdsToCancel = Arrays.stream(notificationManager.activeNotifications)
            // we're only clearing notifications from our package
            .filter { notification -> notification.packageName == this.packageName }
            // LINE only shows the last message for a chat, we'll dismiss all of the messages in the same chat ID
            .filter { notification -> NotificationExtractor.getLineNotificationSupportChatId(notification.notification).isPresent }
            .filter { notification -> chatId == NotificationExtractor.getLineNotificationSupportChatId(notification.notification).get() }
            .map { notification -> notification.id }
            .collect(Collectors.toSet())

        for (notificationId in notificationIdsToCancel) {
            Timber.d("Cancelling notification: $notificationId")
            notificationManager.cancel(notificationId)
        }
    }

    companion object {
        const val DELETE_FRIEND_NAME_CACHE_ACTION = "com.mysticwind.linenotificationsupport.action.deletefriendnamecache"

        private const val NOTIFICATION_COUNTER_CHECK_PERIOD = 60_000L

        private val IDENTICAL_MESSAGE_EVALUATOR = IdenticalMessageEvaluator()
        private val MERGE_IDENTICAL_MESSAGE_HANDLER = MergeIdenticalMessageHandler(IDENTICAL_MESSAGE_EVALUATOR)
        private val IGNORE_IDENTICAL_MESSAGE_HANDLER = IgnoreIdenticalMessageHandler(IDENTICAL_MESSAGE_EVALUATOR)
        private val AS_IS_IDENTICAL_MESSAGE_HANDLER = AsIsIdenticalMessageHandler()

        private val STRATEGY_TO_HANDLER_MAP: Map<IdenticalMessageHandlingStrategy, IdenticalMessageHandler> = ImmutableMap.of(
            IdenticalMessageHandlingStrategy.IGNORE, IGNORE_IDENTICAL_MESSAGE_HANDLER,
            IdenticalMessageHandlingStrategy.MERGE, MERGE_IDENTICAL_MESSAGE_HANDLER,
            IdenticalMessageHandlingStrategy.SEND_AS_IS, AS_IS_IDENTICAL_MESSAGE_HANDLER
        )

        private val PREFERENCE_KEYS_THAT_TRIGGER_REBUILDING_NOTIFICATION_PUBLISHER: Set<String> = ImmutableSet.of(
            PreferenceProvider.MAX_NOTIFICATION_WORKAROUND_PREFERENCE_KEY,
            PreferenceProvider.USE_MESSAGE_SPLITTER_PREFERENCE_KEY,
            PreferenceProvider.SINGLE_NOTIFICATION_CONVERSATIONS_KEY
        )
    }
}
