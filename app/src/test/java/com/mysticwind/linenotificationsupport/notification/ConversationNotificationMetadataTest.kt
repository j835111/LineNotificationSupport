package com.mysticwind.linenotificationsupport.notification

import androidx.core.app.Person
import com.mysticwind.linenotificationsupport.model.LineNotification
import com.mysticwind.linenotificationsupport.model.LineNotificationBuilder.Companion.DEFAULT_CHAT_ID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ConversationNotificationMetadataTest {

    @Test
    fun buildShortcutId_prefersRealChatId() {
        val lineNotification = buildMessage("real-chat-id", "Group Name", "Sender")

        assertEquals(
            "line-conversation-chat-real-chat-id",
            ConversationNotificationMetadata.buildShortcutId(lineNotification)
        )
    }

    @Test
    fun buildShortcutId_usesStableFallbackForDefaultChatId() {
        val first = buildMessage(DEFAULT_CHAT_ID, "Fallback Group", "Sender")
        val second = buildMessage(DEFAULT_CHAT_ID, "Fallback Group", "Sender")

        val firstShortcutId = ConversationNotificationMetadata.buildShortcutId(first)
        val secondShortcutId = ConversationNotificationMetadata.buildShortcutId(second)

        assertEquals(firstShortcutId, secondShortcutId)
        assertTrue(firstShortcutId!!.startsWith("line-conversation-fallback-"))
    }

    @Test
    fun buildShortcutId_returnsNullForCallNotifications() {
        val lineNotification = LineNotification.builder()
            .chatId("call-chat-id")
            .title("Caller")
            .sender(Person.Builder().setName("Caller").build())
            .callState(LineNotification.CallState.INCOMING)
            .build()

        assertNull(ConversationNotificationMetadata.buildShortcutId(lineNotification))
    }

    @Test
    fun buildShortcutLabel_prefersTitleThenSenderThenDefault() {
        val titled = buildMessage("chat-id", "Group Name", "Sender")
        val senderOnly = LineNotification.builder()
            .chatId(DEFAULT_CHAT_ID)
            .sender(Person.Builder().setName("Sender Only").build())
            .build()
        val noIdentity = LineNotification.builder().build()

        assertEquals("Group Name", ConversationNotificationMetadata.buildShortcutLabel(titled))
        assertEquals("Sender Only", ConversationNotificationMetadata.buildShortcutLabel(senderOnly))
        assertEquals("LINE", ConversationNotificationMetadata.buildShortcutLabel(noIdentity))
    }

    private fun buildMessage(chatId: String, title: String, senderName: String): LineNotification {
        return LineNotification.builder()
            .chatId(chatId)
            .title(title)
            .sender(Person.Builder().setName(senderName).build())
            .build()
    }
}
