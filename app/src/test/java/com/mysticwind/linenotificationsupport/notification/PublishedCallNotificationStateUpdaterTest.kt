package com.mysticwind.linenotificationsupport.notification

import com.google.common.collect.ImmutableSet
import com.mysticwind.linenotificationsupport.model.AutoIncomingCallNotificationState
import com.mysticwind.linenotificationsupport.model.LineNotification
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class PublishedCallNotificationStateUpdaterTest {

    companion object {
        private const val NOTIFICATION_ID = 100
        private const val WAIT_DURATION_SECONDS = 3.0
        private const val TIMEOUT_SECONDS = 60L
    }

    @Test
    fun updateState_returnsExistingStateWhenNotificationIsNotACall() {
        val currentState = buildState(buildCallNotification(LineNotification.CallState.INCOMING))
        val lineNotification = LineNotification.builder().build()

        val result = PublishedCallNotificationStateUpdater.updateState(
            currentState, lineNotification, NOTIFICATION_ID, WAIT_DURATION_SECONDS, TIMEOUT_SECONDS
        )

        assertSame(currentState, result.nextState)
        assertFalse(result.shouldSendImmediateRepeat)
    }

    @Test
    fun updateState_cancelsPreviousStateAndCreatesNewRepeatStateForIncomingCalls() {
        val previousState = buildState(buildCallNotification(LineNotification.CallState.INCOMING))
        previousState.notified(1)
        val incomingCall = buildCallNotification(LineNotification.CallState.INCOMING)

        val result = PublishedCallNotificationStateUpdater.updateState(
            previousState, incomingCall, NOTIFICATION_ID, WAIT_DURATION_SECONDS, TIMEOUT_SECONDS
        )

        assertFalse(previousState.shouldNotify())
        assertTrue(result.shouldSendImmediateRepeat)
        assertNotNull(result.nextState)
        assertEquals(ImmutableSet.of(NOTIFICATION_ID), result.nextState!!.getIncomingCallNotificationIds())
        assertEquals(incomingCall, result.nextState!!.lineNotification)
        assertEquals(WAIT_DURATION_SECONDS, result.nextState!!.waitDurationInSeconds, 0.0)
    }

    @Test
    fun updateState_marksExistingStateMissedWithoutImmediateRepeat() {
        val currentState = buildState(buildCallNotification(LineNotification.CallState.INCOMING))
        val missedCall = buildCallNotification(LineNotification.CallState.MISSED_CALL)

        val result = PublishedCallNotificationStateUpdater.updateState(
            currentState, missedCall, NOTIFICATION_ID, WAIT_DURATION_SECONDS, TIMEOUT_SECONDS
        )

        assertSame(currentState, result.nextState)
        assertFalse(result.shouldSendImmediateRepeat)
        assertFalse(currentState.shouldNotify())
    }

    @Test
    fun updateState_marksExistingStateAcceptedWithoutImmediateRepeat() {
        val currentState = buildState(buildCallNotification(LineNotification.CallState.INCOMING))
        val inProgressCall = buildCallNotification(LineNotification.CallState.IN_A_CALL)

        val result = PublishedCallNotificationStateUpdater.updateState(
            currentState, inProgressCall, NOTIFICATION_ID, WAIT_DURATION_SECONDS, TIMEOUT_SECONDS
        )

        assertSame(currentState, result.nextState)
        assertFalse(result.shouldSendImmediateRepeat)
        assertFalse(currentState.shouldNotify())
    }

    @Test
    fun updateState_ignoresMissedCallWhenNoExistingState() {
        val missedCall = buildCallNotification(LineNotification.CallState.MISSED_CALL)

        val result = PublishedCallNotificationStateUpdater.updateState(
            null, missedCall, NOTIFICATION_ID, WAIT_DURATION_SECONDS, TIMEOUT_SECONDS
        )

        assertNull(result.nextState)
        assertFalse(result.shouldSendImmediateRepeat)
    }

    private fun buildState(lineNotification: LineNotification): AutoIncomingCallNotificationState {
        return AutoIncomingCallNotificationState.builder()
            .lineNotification(lineNotification)
            .waitDurationInSeconds(WAIT_DURATION_SECONDS)
            .timeoutInSeconds(TIMEOUT_SECONDS)
            .build()
    }

    private fun buildCallNotification(callState: LineNotification.CallState): LineNotification {
        return LineNotification.builder()
            .title("Caller")
            .callState(callState)
            .build()
    }
}
