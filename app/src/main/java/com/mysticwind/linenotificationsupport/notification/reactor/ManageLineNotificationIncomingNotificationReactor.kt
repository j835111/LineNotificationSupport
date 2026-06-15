package com.mysticwind.linenotificationsupport.notification.reactor

import android.os.Handler
import android.service.notification.StatusBarNotification
import com.google.common.collect.ImmutableSet
import com.mysticwind.linenotificationsupport.line.Constants
import com.mysticwind.linenotificationsupport.line.Constants.LINE_PACKAGE_NAME
import com.mysticwind.linenotificationsupport.notification.AndroidNotificationManager
import com.mysticwind.linenotificationsupport.preference.PreferenceProvider
import com.mysticwind.linenotificationsupport.utils.NotificationExtractor
import com.mysticwind.linenotificationsupport.utils.StatusBarNotificationExtractor
import org.apache.commons.lang3.StringUtils
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ManageLineNotificationIncomingNotificationReactor @Inject constructor(
    private val preferenceProvider: PreferenceProvider,
    private val handler: Handler,
    private val androidNotificationManager: AndroidNotificationManager
) : IncomingNotificationReactor {

    companion object {
        private const val LINE_NOTIFICATION_DISMISS_RETRY_TIMEOUT = 500L
        private const val PRINT_LINE_NOTIFICATION_WAIT_TIME = 200L
        private val INTERESTED_PACKAGES: Set<String> = ImmutableSet.of(Constants.LINE_PACKAGE_NAME)
    }

    override fun interestedPackages(): Collection<String> = INTERESTED_PACKAGES

    override fun isInterestInNotificationGroup(): Boolean = false

    override fun reactToIncomingNotification(statusBarNotification: StatusBarNotification): Reaction {
        if (shouldScheduleRefetch(statusBarNotification)) {
            // Don't dismiss LINE notifications as we'll need to re-fetch them
            return Reaction.NONE
        }

        if (preferenceProvider.shouldManageLineMessageNotifications()) {
            dismissLineNotification(statusBarNotification)

            // there are situations where LINE messages are not dismissed, do this again
            handler.postDelayed({
                Timber.d(
                    "Retry dismissing LINE notifications again: key [%s] message [%s]",
                    statusBarNotification.key,
                    statusBarNotification.notification.tickerText
                )
                dismissLineNotification(statusBarNotification)
            }, LINE_NOTIFICATION_DISMISS_RETRY_TIMEOUT)
        }
        return Reaction.NONE
    }

    // TODO share this method with NotificationListenerService
    private fun shouldScheduleRefetch(statusBarNotification: StatusBarNotification): Boolean {
        // There are notifications that will not have actions and don't need to retry.
        // For example: notifications of someone added to a chat
        if (StringUtils.isBlank(NotificationExtractor.getLineMessageId(statusBarNotification.notification))) {
            return false
        }
        return StatusBarNotificationExtractor.isMessage(statusBarNotification) &&
                statusBarNotification.notification.actions == null
    }

    private fun dismissLineNotification(statusBarNotification: StatusBarNotification) {
        // we only dismiss notifications that are in the message category
        if (!StatusBarNotificationExtractor.isMessage(statusBarNotification)) {
            Timber.d(
                "LINE notification not message category but [%s]: [%s]",
                statusBarNotification.notification.category,
                statusBarNotification.notification.tickerText
            )
            return
        }

        val summaryKey = findLineNotificationSummary(statusBarNotification.notification.group)
        summaryKey?.let { key ->
            Timber.d("Cancelling LINE summary: [%s]", key)
            androidNotificationManager.cancelNotificationOfPackage(key)
        }

        Timber.d(
            "Dismiss LINE notification: key[%s] tag[%s] id[%d]",
            statusBarNotification.key,
            statusBarNotification.tag,
            statusBarNotification.id
        )
        androidNotificationManager.cancelNotificationOfPackage(statusBarNotification.key)

        handler.postDelayed(
            { printLineNotifications(statusBarNotification.notification.group) },
            PRINT_LINE_NOTIFICATION_WAIT_TIME
        )
    }

    private fun findLineNotificationSummary(group: String?): String? {
        return androidNotificationManager.getNotificationsOfPackage(LINE_PACKAGE_NAME).stream()
            .peek { notification ->
                Timber.d(
                    "LINE notification key [%s] category [%s] group [%s] isSummary [%s] title [%s] message [%s]",
                    notification.key,
                    notification.notification.category,
                    notification.notification.group,
                    StatusBarNotificationExtractor.isSummary(notification),
                    NotificationExtractor.getTitle(notification.notification),
                    NotificationExtractor.getMessage(notification.notification)
                )
            }
            .filter { notification -> StatusBarNotificationExtractor.isMessage(notification) }
            .filter { notification -> StatusBarNotificationExtractor.isSummary(notification) }
            .filter { notification -> StringUtils.equals(group, notification.notification.group) }
            .map { notification -> notification.key }
            .findFirst()
            .orElse(null)
    }

    private fun printLineNotifications(groupThatShouldBeDismissed: String?) {
        androidNotificationManager.getNotificationsOfPackage(LINE_PACKAGE_NAME)
            .forEach { notification ->
                Timber.w(
                    "%sPrint LINE notification that are not dismissed key [%s] category [%s] group [%s] isSummary [%s] isClearable [%s] title [%s] message [%s]",
                    if (StringUtils.equals(notification.notification.group, groupThatShouldBeDismissed)) "[SHOULD_DISMISS] " else "",
                    notification.key,
                    notification.notification.category,
                    notification.notification.group,
                    StatusBarNotificationExtractor.isSummary(notification),
                    notification.isClearable,
                    NotificationExtractor.getTitle(notification.notification),
                    NotificationExtractor.getMessage(notification.notification)
                )
            }
    }
}
