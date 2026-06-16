package com.mysticwind.linenotificationsupport.notification

import android.app.Notification
import androidx.core.app.Person
import com.google.common.collect.ImmutableList
import com.mysticwind.linenotificationsupport.model.LineNotification
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock

class CallNotificationActionOrderAdjusterTest {

    @Test
    fun adjust_returnsOriginalNotificationWhenReverseDisabled() {
        val lineNotification = buildIncomingCallNotification(mock(), mock())

        val adjusted = CallNotificationActionOrderAdjuster.adjust(lineNotification, false)

        assertSame(lineNotification, adjusted)
    }

    @Test
    fun adjust_returnsOriginalNotificationWhenFewerThanTwoActions() {
        val lineNotification = buildIncomingCallNotification(mock())

        val adjusted = CallNotificationActionOrderAdjuster.adjust(lineNotification, true)

        assertSame(lineNotification, adjusted)
    }

    @Test
    fun adjust_swapsFirstTwoActionsWhenReverseEnabled() {
        val first = mock<Notification.Action>()
        val second = mock<Notification.Action>()
        val lineNotification = buildIncomingCallNotification(first, second)

        val adjusted = CallNotificationActionOrderAdjuster.adjust(lineNotification, true)

        assertEquals(ImmutableList.of(second, first), adjusted.actions)
    }

    @Test
    fun adjust_keepsAdditionalActionsAfterSwappingFirstTwo() {
        val first = mock<Notification.Action>()
        val second = mock<Notification.Action>()
        val third = mock<Notification.Action>()
        val lineNotification = buildIncomingCallNotification(first, second, third)

        val adjusted = CallNotificationActionOrderAdjuster.adjust(lineNotification, true)

        assertEquals(ImmutableList.of(second, first, third), adjusted.actions)
    }

    @Test
    fun adjust_clearsNoActionsForEmptyList() {
        val lineNotification = LineNotification.builder()
            .title("Caller")
            .sender(Person.Builder().setName("Caller").build())
            .callState(LineNotification.CallState.INCOMING)
            .build()

        val adjusted = CallNotificationActionOrderAdjuster.adjust(lineNotification, true)

        assertSame(lineNotification, adjusted)
        assertTrue(adjusted.actions.isEmpty())
    }

    private fun buildIncomingCallNotification(vararg actions: Notification.Action): LineNotification {
        val builder = LineNotification.builder()
            .title("Caller")
            .sender(Person.Builder().setName("Caller").build())
            .callState(LineNotification.CallState.INCOMING)
        for (action in actions) {
            builder.action(action)
        }
        return builder.build()
    }
}
