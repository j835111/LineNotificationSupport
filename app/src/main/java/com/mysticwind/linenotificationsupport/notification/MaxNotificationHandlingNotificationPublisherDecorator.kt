package com.mysticwind.linenotificationsupport.notification

import android.os.Handler
import android.service.notification.StatusBarNotification
import com.mysticwind.linenotificationsupport.model.LineNotification
import timber.log.Timber
import java.time.Instant
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.function.Consumer

class MaxNotificationHandlingNotificationPublisherDecorator(
    private val handler: Handler,
    private val notificationPublisher: NotificationPublisher,
    private val slotAvailabilityChecker: SlotAvailabilityChecker
) : NotificationPublisher {

    companion object {
        // without the cool down, messages may not get sent if messages of the same group was just dismissed
        const val DISMISS_COOL_DOWN_IN_MILLIS: Long = 500L
        private const val NOTIFICATION_CHECK_PERIOD_IN_MILLIS: Long = 500L

        // tracks the messages that has not been sent
        private val QUEUE_ITEMS: ConcurrentLinkedDeque<QueueItem> = ConcurrentLinkedDeque()

        // this is to make sure we have sufficient cool down for each chat after it being dismissed
        private val GROUP_TO_LAST_DISMISSED_INSTANT_MAP: MutableMap<String, Instant> = ConcurrentHashMap()
    }

    // TODO should we just have this class implemented and shared??
    inner class QueueItem(
        val lineNotification: LineNotification,
        val notificationId: Int
    )

    private val republishEventsIfSlotsAvailableRunnable = Runnable {
        republishIfSlotsAvailable()
    }

    override fun publishNotification(lineNotification: LineNotification, notificationId: Int) {
        requireNotNull(lineNotification)

        publishNotification(lineNotification, notificationId) { item -> QUEUE_ITEMS.add(item) }
    }

    override fun republishNotification(lineNotification: LineNotification, notificationId: Int) {
        requireNotNull(lineNotification)

        publishNotification(lineNotification, notificationId) { item -> QUEUE_ITEMS.addFirst(item) }
    }

    fun publishNotification(
        lineNotification: LineNotification,
        notificationId: Int,
        itemAddingFunction: Consumer<QueueItem>
    ) {
        if (!slotAvailabilityChecker.hasSlot(lineNotification.chatId!!)) {
            Timber.d("Reached maximum notifications, add to queue: $notificationId")
            QUEUE_ITEMS.add(QueueItem(lineNotification, notificationId))
            itemAddingFunction.accept(QueueItem(lineNotification, notificationId))
            return
        }
        if (QUEUE_ITEMS.isEmpty()) {
            Timber.d("Publish new notification: $notificationId")
            publish(lineNotification, notificationId)
            return
        }
        itemAddingFunction.accept(QueueItem(lineNotification, notificationId))

        val firstItem = getFirstItem()
        if (!firstItem.isPresent) {
            return
        }
        firstItem.ifPresent { item ->
            Timber.d("Publish previously queued notification: " + item.notificationId)
            publish(item.lineNotification, item.notificationId)
        }
    }

    override fun updateNotificationDismissed(statusBarNotification: StatusBarNotification) {
        // when should this actually happen??
        notificationPublisher.updateNotificationDismissed(statusBarNotification)

        val group = statusBarNotification.notification.group

        if (!slotAvailabilityChecker.hasSlot(group)) {
            return
        }

        var firstItem = getFirstItem()
        if (!firstItem.isPresent) {
            GROUP_TO_LAST_DISMISSED_INSTANT_MAP[group] = Instant.now()
            return
        }
        val delayInMillis = calculateDelayInMillis(firstItem.get().lineNotification.chatId!!)
        delayedPublish(firstItem.get(), delayInMillis)
        GROUP_TO_LAST_DISMISSED_INSTANT_MAP[group] = Instant.now().plusMillis(delayInMillis)

        // TODO why are there cases where there are available slots but the queue still has items???
        if (!QUEUE_ITEMS.isEmpty()) {
            scheduleSlotCheck(delayInMillis + NOTIFICATION_CHECK_PERIOD_IN_MILLIS)
        }
    }

    private fun delayedPublish(queueItem: QueueItem, delayInMillis: Long) {
        Timber.d("Scheduling a delayed publish for item: " + queueItem.lineNotification.message)
        handler.postDelayed(Runnable {
            Timber.d(
                "Publishing notification (after delay of %d): %s",
                delayInMillis, queueItem.lineNotification.message
            )
            publish(queueItem.lineNotification, queueItem.notificationId)
        }, delayInMillis)
    }

    private fun calculateDelayInMillis(chatId: String): Long {
        val lastDismissedTimestamp =
            GROUP_TO_LAST_DISMISSED_INSTANT_MAP.getOrDefault(chatId, Instant.now()).toEpochMilli()
        val now = Instant.now().toEpochMilli()
        return if (lastDismissedTimestamp > now) {
            lastDismissedTimestamp - now + 1
        } else {
            DISMISS_COOL_DOWN_IN_MILLIS
        }
    }

    private fun scheduleSlotCheck(delayInMillis: Long) {
        Timber.d("Cancelling scheduled slot checks ...")
        handler.removeCallbacks(republishEventsIfSlotsAvailableRunnable)

        Timber.d("Scheduling slot checks ...")
        handler.postDelayed(republishEventsIfSlotsAvailableRunnable, delayInMillis)
    }

    private fun republishIfSlotsAvailable() {
        Timber.d("Running slot checks ...")
        val item = QUEUE_ITEMS.peek() ?: return
        if (slotAvailabilityChecker.hasSlot(item.lineNotification.chatId!!)) {
            QUEUE_ITEMS.remove(item)

            val delayInMillis = calculateDelayInMillis(item.lineNotification.chatId!!)
            Timber.d(
                "Scheduled notification publishing (after delay of %d): %s",
                delayInMillis, item.lineNotification.message
            )
            delayedPublish(item, delayInMillis)

            if (!QUEUE_ITEMS.isEmpty()) {
                scheduleSlotCheck(delayInMillis + NOTIFICATION_CHECK_PERIOD_IN_MILLIS)
            }
        }
    }

    private fun getFirstItem(): Optional<QueueItem> {
        return Optional.ofNullable(QUEUE_ITEMS.poll())
    }

    private fun publish(lineNotification: LineNotification, notificationId: Int) {
        notificationPublisher.publishNotification(lineNotification, notificationId)
    }
}
