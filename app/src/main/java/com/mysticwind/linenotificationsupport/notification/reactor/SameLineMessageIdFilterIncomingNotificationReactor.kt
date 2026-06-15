package com.mysticwind.linenotificationsupport.notification.reactor

import android.service.notification.StatusBarNotification
import com.google.common.cache.CacheBuilder
import com.google.common.cache.RemovalNotification
import com.google.common.collect.ImmutableSet
import com.mysticwind.linenotificationsupport.line.Constants
import com.mysticwind.linenotificationsupport.utils.NotificationExtractor
import com.mysticwind.linenotificationsupport.utils.StatusBarNotificationPrinter
import org.apache.commons.lang3.StringUtils
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

// TODO are we supposed to do a merge or a re-read instead of ignoring?
@Singleton
class SameLineMessageIdFilterIncomingNotificationReactor @Inject constructor() : IncomingNotificationReactor {

    companion object {
        private val INTERESTED_PACKAGES: Set<String> = ImmutableSet.of(Constants.LINE_PACKAGE_NAME)
    }

    private val statusBarNotificationPrinter = StatusBarNotificationPrinter()

    private val lineMessageIdToNotificationCache = CacheBuilder.newBuilder()
        .expireAfterWrite(1, TimeUnit.HOURS)
        .removalListener { notification: RemovalNotification<String, StatusBarNotification> ->
            Timber.d(
                "LINE message ID cache removal detected: reason [%s] LINE message ID [%s], message [%s]",
                notification.cause,
                notification.key,
                NotificationExtractor.getMessage(notification.value.notification)
            )
        }
        .build<String, StatusBarNotification>()

    override fun interestedPackages(): Collection<String> = INTERESTED_PACKAGES

    override fun isInterestInNotificationGroup(): Boolean = false

    override fun reactToIncomingNotification(incomingStatusBarNotification: StatusBarNotification): Reaction {
        val lineMessageId = NotificationExtractor.getLineMessageId(incomingStatusBarNotification.notification)
        if (StringUtils.isBlank(lineMessageId)) {
            return Reaction.NONE
        }
        val cachedStatusBarNotification = lineMessageIdToNotificationCache.getIfPresent(lineMessageId)
        if (cachedStatusBarNotification == null) {
            Timber.d("Tracking LINE message ID [%s]", lineMessageId)
            lineMessageIdToNotificationCache.put(lineMessageId, incomingStatusBarNotification)
            return Reaction.NONE
        }
        val originalMessage = NotificationExtractor.getMessage(cachedStatusBarNotification.notification)
        val newMessage = NotificationExtractor.getMessage(incomingStatusBarNotification.notification)

        if (!StringUtils.equals(originalMessage, newMessage)) {
            Timber.d(
                "Detected duplicated notifications: LINE message ID [%s] original [%s] -> new [%s]",
                lineMessageId, originalMessage, newMessage
            )
            statusBarNotificationPrinter.print("Received updated notification", incomingStatusBarNotification)
            return Reaction.NONE
        }

        Timber.d(
            "[STOP] Detected duplicated notifications: LINE message ID [%s] original [%s] -> new [%s]",
            lineMessageId, originalMessage, newMessage
        )
        return Reaction.STOP_FURTHER_PROCESSING
    }
}
