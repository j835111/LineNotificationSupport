package com.mysticwind.linenotificationsupport.notification

import com.mysticwind.linenotificationsupport.model.AutoIncomingCallNotificationState
import com.mysticwind.linenotificationsupport.model.LineNotification

object IncomingCallNotificationRepeatPlanner {

    sealed class Decision {
        data class Cancel(val notificationIdsToCancel: Set<Int>) : Decision()
        data class Repeat(val notificationToPublish: LineNotification, val notificationId: Int) : Decision()
    }

    @JvmStatic
    fun planNextRepeat(
        autoIncomingCallNotificationState: AutoIncomingCallNotificationState,
        shouldCreateNewContinuousCallNotifications: Boolean,
        nextNotificationId: () -> Int,
        nowTimestampMillis: Long
    ): Decision {
        if (!autoIncomingCallNotificationState.shouldNotify()) {
            return Decision.Cancel(autoIncomingCallNotificationState.getIncomingCallNotificationIds())
        }

        val lineNotificationWithUpdatedTimestamp = autoIncomingCallNotificationState.lineNotification.toBuilder()
            .timestamp(nowTimestampMillis)
            .build()

        val notificationId = if (shouldCreateNewContinuousCallNotifications) {
            nextNotificationId()
        } else {
            autoIncomingCallNotificationState.getIncomingCallNotificationIds().iterator().next()
        }

        return Decision.Repeat(lineNotificationWithUpdatedTimestamp, notificationId)
    }
}
