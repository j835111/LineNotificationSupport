package com.mysticwind.linenotificationsupport.notification

import android.service.notification.StatusBarNotification
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.google.common.cache.RemovalListener
import com.google.common.cache.RemovalNotification
import com.mysticwind.linenotificationsupport.model.LineNotification
import org.apache.commons.lang3.StringUtils
import timber.log.Timber
import java.util.concurrent.TimeUnit

class NotificationMergingNotificationPublisherDecorator(
    private val notificationPublisher: NotificationPublisher
) : NotificationPublisher {

    private val lineMessageIdToNotificationIdCache: Cache<String, Int> = CacheBuilder.newBuilder()
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .removalListener(RemovalListener<String, Int> { notification: RemovalNotification<String, Int> ->
            Timber.d(
                "LINE message ID cache removal detected: reason [%s] LINE message ID [%s] notification ID [%s]",
                notification.cause, notification.key, notification.value
            )
        })
        .build()

    override fun publishNotification(lineNotification: LineNotification, notificationId: Int) {
        if (StringUtils.isBlank(lineNotification.lineMessageId)) {
            notificationPublisher.publishNotification(lineNotification, notificationId)
            return
        }
        val lineMessageId = lineNotification.lineMessageId!!
        val previousNotificationId = lineMessageIdToNotificationIdCache.getIfPresent(lineMessageId)
        if (previousNotificationId == null) {
            lineMessageIdToNotificationIdCache.put(lineMessageId, notificationId)
            notificationPublisher.publishNotification(lineNotification, notificationId)
        } else {
            Timber.d(
                "Detected previous published notification that should use the same notification ID previous [%d] new [%d] message [%s]",
                previousNotificationId, notificationId, lineNotification.message
            )
            notificationPublisher.publishNotification(lineNotification, previousNotificationId)
        }
    }

    override fun republishNotification(lineNotification: LineNotification, notificationId: Int) {
        // do nothing
        notificationPublisher.republishNotification(lineNotification, notificationId)
    }

    override fun updateNotificationDismissed(statusBarNotification: StatusBarNotification) {
        // do nothing
        notificationPublisher.updateNotificationDismissed(statusBarNotification)
    }
}
