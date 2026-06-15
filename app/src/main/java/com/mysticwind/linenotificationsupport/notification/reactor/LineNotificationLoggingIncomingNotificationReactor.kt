package com.mysticwind.linenotificationsupport.notification.reactor

import android.service.notification.StatusBarNotification
import com.google.common.collect.ImmutableSet
import com.mysticwind.linenotificationsupport.debug.history.manager.NotificationHistoryManager
import com.mysticwind.linenotificationsupport.line.Constants
import com.mysticwind.linenotificationsupport.line.LineAppVersionProvider
import com.mysticwind.linenotificationsupport.utils.NotificationExtractor
import com.mysticwind.linenotificationsupport.utils.StatusBarNotificationExtractor
import com.mysticwind.linenotificationsupport.utils.StatusBarNotificationPrinter
import org.apache.commons.lang3.StringUtils
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LineNotificationLoggingIncomingNotificationReactor @Inject constructor(
    private val statusBarNotificationPrinter: StatusBarNotificationPrinter,
    private val notificationHistoryManager: NotificationHistoryManager,
    private val lineAppVersionProvider: LineAppVersionProvider
) : IncomingNotificationReactor {

    override fun interestedPackages(): Collection<String> =
        ImmutableSet.of(Constants.LINE_PACKAGE_NAME)

    override fun isInterestInNotificationGroup(): Boolean = true

    override fun reactToIncomingNotification(statusBarNotification: StatusBarNotification): Reaction {
        statusBarNotificationPrinter.print("Received", statusBarNotification)

        // TODO potential waste in calling PackageManager everytime for this logging?
        val lineVersion = lineAppVersionProvider.getLineAppVersion().orElse("N/A")
        notificationHistoryManager.record(statusBarNotification, lineVersion)

        if (isNewMessageWithoutContent(statusBarNotification)) {
            Timber.d(
                "Detected potential new message without content: key [%s] title [%s] message [%s]",
                statusBarNotification.key,
                NotificationExtractor.getTitle(statusBarNotification.notification),
                statusBarNotification.notification.tickerText
            )
            // we should get a notification update for this message
        }

        return Reaction.NONE
    }

    private fun isNewMessageWithoutContent(statusBarNotification: StatusBarNotification): Boolean {
        // There are notifications that will not have actions and don't need to retry.
        // For example: notifications of someone added to a chat
        if (StringUtils.isBlank(NotificationExtractor.getLineMessageId(statusBarNotification.notification))) {
            return false
        }
        return StatusBarNotificationExtractor.isMessage(statusBarNotification) &&
                statusBarNotification.notification.actions == null
    }
}
