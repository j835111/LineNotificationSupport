package com.mysticwind.linenotificationsupport.model

import android.app.Notification
import androidx.core.app.Person
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.mock
import java.time.Instant

@RunWith(MockitoJUnitRunner.Silent::class)
class LineNotificationTest {

    companion object {
        private const val TITLE = "title"
        private const val MESSAGE = "message"
        private const val CHAT_ID = "chatId"
    }

    @Test
    fun testSelfResponse() {
        val lineNotification = LineNotification.builder()
            .lineMessageId(Instant.now().toEpochMilli().toString())
            .title(TITLE)
            .message(MESSAGE)
            .sender(Person.Builder().setName("You").build())
            .chatId(CHAT_ID)
            .timestamp(Instant.now().toEpochMilli())
            .isSelfResponse(true)
            .build()

        assertTrue(lineNotification.isSelfResponse)
    }

    @Test
    fun testDefaults() {
        val lineNotification = LineNotification.builder().build()

        assertEquals(emptyList<Any>(), lineNotification.messages)
        assertEquals(emptyList<Any>(), lineNotification.history)
        assertEquals(emptyList<Any>(), lineNotification.actions)
        assertNull(lineNotification.clickIntent)
        assertNull(lineNotification.message)
    }

    @Test
    fun testToBuilderCopiesValuesAndSupportsSingularActions() {
        val action1 = mock<Notification.Action>()
        val action2 = mock<Notification.Action>()

        val original = LineNotification.builder()
            .title(TITLE)
            .message(MESSAGE)
            .chatId(CHAT_ID)
            .action(action1)
            .build()

        val updated = original.toBuilder()
            .message("updated")
            .action(action2)
            .build()

        assertEquals(MESSAGE, original.message)
        assertEquals("updated", updated.message)
        assertEquals(1, original.actions.size)
        assertEquals(2, updated.actions.size)
        assertNotSame(original.actions, updated.actions)
    }
}
