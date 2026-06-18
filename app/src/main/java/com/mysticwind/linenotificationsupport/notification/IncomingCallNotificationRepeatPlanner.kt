package com.mysticwind.linenotificationsupport.notification

import com.mysticwind.linenotificationsupport.model.AutoIncomingCallNotificationState
import com.mysticwind.linenotificationsupport.model.LineNotification
import timber.log.Timber

object IncomingCallNotificationRepeatPlanner {

    sealed class Decision {
        data class Cancel(val notificationIdsToCancel: Set<Int>) : Decision()
        data class Repeat(
            val notificationToPublish: LineNotification,
            val notificationId: Int,
            val shouldTrackNotificationId: Boolean
        ) : Decision()
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

        val existingNotificationId = if (shouldCreateNewContinuousCallNotifications) {
            null
        } else {
            autoIncomingCallNotificationState.getIncomingCallNotificationIds().firstOrNull()
        }
        if (!shouldCreateNewContinuousCallNotifications && existingNotificationId == null) {
            Timber.w("Reuse continuous call notification requested without a tracked notification ID; falling back to a new notification ID.")
        }
        val notificationId = existingNotificationId ?: nextNotificationId()

        return Decision.Repeat(
            lineNotificationWithUpdatedTimestamp,
            notificationId,
            shouldTrackNotificationId = existingNotificationId == null
        )
    }
}
