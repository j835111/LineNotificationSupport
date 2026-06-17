package com.mysticwind.linenotificationsupport.notification

import com.mysticwind.linenotificationsupport.model.AutoIncomingCallNotificationState
import com.mysticwind.linenotificationsupport.model.LineNotification

object PublishedCallNotificationStateUpdater {

    data class Result(
        val nextState: AutoIncomingCallNotificationState?,
        val shouldSendImmediateRepeat: Boolean
    )

    @JvmStatic
    fun updateState(
        currentState: AutoIncomingCallNotificationState?,
        lineNotification: LineNotification,
        notificationId: Int,
        waitDurationInSeconds: Double,
        timeoutInSeconds: Long
    ): Result {
        if (lineNotification.callState == null) {
            return Result(currentState, false)
        }

        if (lineNotification.callState == LineNotification.CallState.INCOMING) {
            currentState?.cancel()
            val nextState = AutoIncomingCallNotificationState.builder()
                .lineNotification(lineNotification)
                .waitDurationInSeconds(waitDurationInSeconds)
                .timeoutInSeconds(timeoutInSeconds)
                .build()
            nextState.notified(notificationId)
            return Result(nextState, true)
        }

        if (currentState == null) {
            return Result(null, false)
        }

        when (lineNotification.callState) {
            LineNotification.CallState.MISSED_CALL -> currentState.setMissedCall()
            LineNotification.CallState.IN_A_CALL -> currentState.setAccepted()
            else -> { /* no-op */ }
        }
        return Result(currentState, false)
    }
}
