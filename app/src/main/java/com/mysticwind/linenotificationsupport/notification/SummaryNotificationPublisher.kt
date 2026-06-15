package com.mysticwind.linenotificationsupport.notification

import android.app.Notification
import android.content.Context
import android.os.Build
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import com.mysticwind.linenotificationsupport.R
import com.mysticwind.linenotificationsupport.module.HiltQualifiers
import com.mysticwind.linenotificationsupport.utils.GroupIdResolver
import com.mysticwind.linenotificationsupport.utils.NotificationExtractor
import com.mysticwind.linenotificationsupport.utils.StatusBarNotificationExtractor
import dagger.hilt.android.qualifiers.ApplicationContext
import org.apache.commons.lang3.StringUtils
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SummaryNotificationPublisher @Inject constructor(
    @ApplicationContext private val context: Context,
    private val androidNotificationManager: AndroidNotificationManager,
    @HiltQualifiers.PackageName private val packageName: String,
    private val groupIdResolver: GroupIdResolver
) {

    fun updateSummaryWhenNotificationsPublished(group: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return
        }
        if (StringUtils.isBlank(group)) {
            return
        }

        val notifications = androidNotificationManager.getOrderedLineNotificationSupportNotifications(
            group, NotificationFilterStrategy.EXCLUDE_SUMMARY
        )

        // summaries are only published if there are more than 2 notifications in the same group
        if (notifications.size <= 1) {
            return
        }

        val style = buildMessagingStyleFromHistory(notifications)

        val lastNotification = notifications[notifications.size - 1].notification
        Timber.d("Last notification with message: [%s]", NotificationExtractor.getMessage(lastNotification))

        val groupNotification = NotificationCompat.Builder(context, group)
            .setStyle(style)
            .setContentTitle(NotificationExtractor.getTitle(lastNotification))
            .setContentText(lastNotification.tickerText)
            .setSmallIcon(R.drawable.ic_new_message)
            .setGroup(lastNotification.group)
            .setGroupSummary(true)
            .setChannelId(lastNotification.channelId)
            .setAutoCancel(true)
            .setContentIntent(lastNotification.contentIntent)
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN)
            .build()

        groupNotification.actions = lastNotification.actions

        val groupId = groupIdResolver.resolveGroupId(group)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(groupId, groupNotification)

        Timber.d(
            "Created/Updated summary group id [%d] channel [%s] group [%s] text [%s]",
            groupId,
            groupNotification.channelId,
            groupNotification.group,
            groupNotification.tickerText
        )
    }

    private fun buildMessagingStyleFromHistory(notifications: List<StatusBarNotification>): NotificationCompat.MessagingStyle {
        val lastNotification = notifications[notifications.size - 1].notification
        val messagingStyle = NotificationCompat.MessagingStyle(buildPerson(lastNotification))
            .setConversationTitle(buildTitle(lastNotification))

        for (notification in notifications) {
            messagingStyle.addMessage(
                NotificationCompat.MessagingStyle.Message(
                    NotificationExtractor.getMessage(notification.notification),
                    notification.notification.`when`, buildPerson(notification.notification)
                )
            )
        }

        return messagingStyle
    }

    private fun buildTitle(notification: Notification): String? {
        return notification.extras.getString(Notification.EXTRA_CONVERSATION_TITLE)
    }

    private fun buildPerson(notification: Notification): Person {
        val senderName = notification.extras.getString(Notification.EXTRA_SELF_DISPLAY_NAME)
        return Person.Builder()
            .setName(senderName)
            .build()
    }

    fun updateSummaryWhenNotificationsDismissed(group: String) {
        val notifications = androidNotificationManager.getOrderedLineNotificationSupportNotifications(
            group, NotificationFilterStrategy.ALL
        )

        val nonSummaryNotificationCount = notifications.stream()
            .filter { notification -> !StatusBarNotificationExtractor.isSummary(notification) }
            .count()

        if (nonSummaryNotificationCount > 0) {
            // do nothing if the summary should still exist (we should show even there is only one notification)
            return
        }

        // cancel the group summary
        notifications.stream()
            .filter { notification -> StatusBarNotificationExtractor.isSummary(notification) }
            .forEach { notification ->
                Timber.d(
                    "Notification group [%s] remaining [%d] dismissing group [%d]",
                    group, nonSummaryNotificationCount, notification.id
                )
                androidNotificationManager.cancelNotificationById(notification.id)
            }
    }
}
