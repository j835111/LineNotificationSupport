package com.mysticwind.linenotificationsupport.notification

import com.mysticwind.linenotificationsupport.model.AutoIncomingCallNotificationState
import com.mysticwind.linenotificationsupport.model.LineNotification

object IncomingCallNotificationRepeatPlanner {

    data class Decision(
        val shouldCancel: Boolean,
        val notificationIdsToCancel: Set<Int>,
        val notificationToPublish: LineNotification?,
        val notificationId: Int?
    )

    @JvmStatic
    fun planNextRepeat(
        autoIncomingCallNotificationState: AutoIncomingCallNotificationState,
        shouldCreateNewContinuousCallNotifications: Boolean,
        nextNotificationId: () -> Int,
        nowTimestampMillis: Long
    ): Decision {
        if (!autoIncomingCallNotificationState.shouldNotify()) {
            return Decision(
                shouldCancel = true,
                notificationIdsToCancel = autoIncomingCallNotificationState.getIncomingCallNotificationIds(),
                notificationToPublish = null,
                notificationId = null
            )
        }

        val lineNotificationWithUpdatedTimestamp = autoIncomingCallNotificationState.lineNotification.toBuilder()
            .timestamp(nowTimestampMillis)
            .build()

        val notificationId = if (shouldCreateNewContinuousCallNotifications) {
            nextNotificationId().also(autoIncomingCallNotificationState::notified)
        } else {
            autoIncomingCallNotificationState.getIncomingCallNotificationIds().iterator().next()
        }

        return Decision(
            shouldCancel = false,
            notificationIdsToCancel = emptySet(),
            notificationToPublish = lineNotificationWithUpdatedTimestamp,
            notificationId = notificationId
        )
    }
}
