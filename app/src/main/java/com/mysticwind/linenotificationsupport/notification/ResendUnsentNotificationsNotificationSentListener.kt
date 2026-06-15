package com.mysticwind.linenotificationsupport.notification

import android.os.Handler
import com.mysticwind.linenotificationsupport.model.LineNotification
import org.apache.commons.lang3.mutable.MutableInt
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

class ResendUnsentNotificationsNotificationSentListener(
    private val handler: Handler,
    private val notificationPublisherFactory: NotificationPublisherFactory
) : NotificationSentListener {

    companion object {
        private const val NOTIFICATION_VERIFICATION_WAIT_TIME_MILLIS: Long = 1_000L
        private const val MAX_RETRY_COUNT: Int = 3
    }

    private class Item private constructor(
        private val lineNotification: LineNotification,
        private val retryCount: MutableInt
    ) {

        companion object {
            fun newItem(lineNotification: LineNotification): Item {
                return Item(lineNotification, MutableInt(0))
            }
        }

        fun incrementRetryCount() {
            retryCount.increment()
        }

        fun reachedMaxRetry(): Boolean {
            return retryCount.toInt() > MAX_RETRY_COUNT
        }

        fun getLineNotification(): LineNotification {
            return lineNotification
        }

        fun getRetryCount(): MutableInt {
            return retryCount
        }
    }

    private val idToItemMap: MutableMap<Int, Item> = ConcurrentHashMap()

    override fun notificationSent(lineNotification: LineNotification, notificationId: Int) {
        requireNotNull(lineNotification)

        val itemInMap = idToItemMap[notificationId]

        if (itemInMap == null) {
            Timber.d("Tracking notification id [%d] message [%s] sending status", notificationId, lineNotification.message)
            idToItemMap[notificationId] = Item.newItem(lineNotification)
        } else {
            Timber.i("Notification id [%d] is already tracked", notificationId)
        }

        handler.postDelayed({
            val item = idToItemMap[notificationId]
            if (item != null) {
                if (item.reachedMaxRetry()) {
                    Timber.w("Notification [%s] reached max retry [%s]", notificationId, item.getRetryCount().toInt())
                    idToItemMap.remove(notificationId)
                } else {
                    val notification = item.getLineNotification()
                    Timber.w("Notification id [%d] message [%s] was not sent!", notificationId, notification.message)

                    item.incrementRetryCount()
                    notificationPublisherFactory.get().republishNotification(lineNotification, notificationId)
                }
            }
        }, NOTIFICATION_VERIFICATION_WAIT_TIME_MILLIS)
    }

    fun notificationReceived(notificationId: Int) {
        Timber.d("Marking notification id [%d] as sent", notificationId)

        idToItemMap.remove(notificationId)
    }
}
