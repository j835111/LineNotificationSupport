package com.mysticwind.linenotificationsupport.notification

import com.google.common.collect.ImmutableSet
import com.mysticwind.linenotificationsupport.model.AutoIncomingCallNotificationState
import com.mysticwind.linenotificationsupport.model.LineNotification
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IncomingCallNotificationRepeatPlannerTest {

    companion object {
        private const val UPDATED_TIMESTAMP = 123456789L
    }

    @Test
    fun planNextRepeat_returnsCancelDecisionWhenStateExpired() {
        val state = AutoIncomingCallNotificationState.builder()
            .lineNotification(buildIncomingCallNotification(10L))
            .timeoutInSeconds(0)
            .build()
        state.notified(100)

        val decision = IncomingCallNotificationRepeatPlanner.planNextRepeat(
            state,
            true,
            { 200 },
            UPDATED_TIMESTAMP
        )

        assertTrue(decision.shouldCancel)
        assertEquals(ImmutableSet.of(100), decision.notificationIdsToCancel)
        assertNull(decision.notificationToPublish)
        assertNull(decision.notificationId)
    }

    @Test
    fun planNextRepeat_usesNewNotificationIdWhenContinuousNotificationsCreateNewEntries() {
        val state = AutoIncomingCallNotificationState.builder()
            .lineNotification(buildIncomingCallNotification(10L))
            .timeoutInSeconds(60)
            .build()
        state.notified(100)

        val decision = IncomingCallNotificationRepeatPlanner.planNextRepeat(
            state,
            true,
            { 200 },
            UPDATED_TIMESTAMP
        )

        assertFalse(decision.shouldCancel)
        assertTrue(decision.notificationIdsToCancel.isEmpty())
        assertEquals(200, decision.notificationId)
        assertNotNull(decision.notificationToPublish)
        assertEquals(UPDATED_TIMESTAMP, decision.notificationToPublish!!.timestamp)
        assertEquals(ImmutableSet.of(100, 200), state.getIncomingCallNotificationIds())
    }

    @Test
    fun planNextRepeat_reusesFirstNotificationIdWhenContinuousNotificationsReuseExistingEntry() {
        val state = AutoIncomingCallNotificationState.builder()
            .lineNotification(buildIncomingCallNotification(10L))
            .timeoutInSeconds(60)
            .build()
        state.notified(100)

        val decision = IncomingCallNotificationRepeatPlanner.planNextRepeat(
            state,
            false,
            { 200 },
            UPDATED_TIMESTAMP
        )

        assertFalse(decision.shouldCancel)
        assertTrue(decision.notificationIdsToCancel.isEmpty())
        assertEquals(100, decision.notificationId)
        assertNotNull(decision.notificationToPublish)
        assertEquals(UPDATED_TIMESTAMP, decision.notificationToPublish!!.timestamp)
        assertEquals(ImmutableSet.of(100), state.getIncomingCallNotificationIds())
    }

    private fun buildIncomingCallNotification(timestamp: Long): LineNotification {
        return LineNotification.builder()
            .title("Caller")
            .timestamp(timestamp)
            .callState(LineNotification.CallState.INCOMING)
            .build()
    }
}
