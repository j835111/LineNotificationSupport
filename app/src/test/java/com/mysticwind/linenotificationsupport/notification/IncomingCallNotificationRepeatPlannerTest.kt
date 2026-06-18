package com.mysticwind.linenotificationsupport.notification

import com.google.common.collect.ImmutableSet
import com.mysticwind.linenotificationsupport.model.AutoIncomingCallNotificationState
import com.mysticwind.linenotificationsupport.model.LineNotification
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mockStatic
import timber.log.Timber

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

        assertTrue(decision is IncomingCallNotificationRepeatPlanner.Decision.Cancel)
        val cancel = decision as IncomingCallNotificationRepeatPlanner.Decision.Cancel
        assertEquals(ImmutableSet.of(100), cancel.notificationIdsToCancel)
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

        assertTrue(decision is IncomingCallNotificationRepeatPlanner.Decision.Repeat)
        val repeat = decision as IncomingCallNotificationRepeatPlanner.Decision.Repeat
        assertEquals(200, repeat.notificationId)
        assertTrue(repeat.shouldTrackNotificationId)
        assertNotNull(repeat.notificationToPublish)
        assertEquals(UPDATED_TIMESTAMP, repeat.notificationToPublish.timestamp)
        // notified() is the caller's responsibility — state still only has the initial id
        assertEquals(ImmutableSet.of(100), state.getIncomingCallNotificationIds())
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

        assertTrue(decision is IncomingCallNotificationRepeatPlanner.Decision.Repeat)
        val repeat = decision as IncomingCallNotificationRepeatPlanner.Decision.Repeat
        assertEquals(100, repeat.notificationId)
        assertFalse(repeat.shouldTrackNotificationId)
        assertNotNull(repeat.notificationToPublish)
        assertEquals(UPDATED_TIMESTAMP, repeat.notificationToPublish.timestamp)
        assertEquals(ImmutableSet.of(100), state.getIncomingCallNotificationIds())
    }

    @Test
    fun planNextRepeat_fallsBackToNewNotificationIdAndMarksItForTrackingWhenReuseModeHasNoIds() {
        val state = AutoIncomingCallNotificationState.builder()
            .lineNotification(buildIncomingCallNotification(10L))
            .timeoutInSeconds(60)
            .build()

        mockStatic(Timber::class.java).use { timberMock ->
            val decision = IncomingCallNotificationRepeatPlanner.planNextRepeat(
                state,
                false,
                { 200 },
                UPDATED_TIMESTAMP
            )

            assertTrue(decision is IncomingCallNotificationRepeatPlanner.Decision.Repeat)
            val repeat = decision as IncomingCallNotificationRepeatPlanner.Decision.Repeat
            assertEquals(200, repeat.notificationId)
            assertTrue(repeat.shouldTrackNotificationId)
            assertNotNull(repeat.notificationToPublish)
            assertEquals(UPDATED_TIMESTAMP, repeat.notificationToPublish.timestamp)
            assertTrue(state.getIncomingCallNotificationIds().isEmpty())
            timberMock.verify {
                Timber.w("Reuse continuous call notification requested without a tracked notification ID; falling back to a new notification ID.")
            }
        }
    }

    private fun buildIncomingCallNotification(timestamp: Long): LineNotification {
        return LineNotification.builder()
            .title("Caller")
            .timestamp(timestamp)
            .callState(LineNotification.CallState.INCOMING)
            .build()
    }
}
