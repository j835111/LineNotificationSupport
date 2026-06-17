package com.mysticwind.linenotificationsupport.notification

import androidx.core.app.Person
import com.mysticwind.linenotificationsupport.model.LineNotification
import com.mysticwind.linenotificationsupport.model.LineNotificationBuilder.Companion.CALL_VIRTUAL_CHAT_ID
import com.mysticwind.linenotificationsupport.model.LineNotificationBuilder.Companion.DEFAULT_CHAT_ID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WearableDismissalMetadataTest {

    @Test
    fun buildDismissalId_prefersRealChatId() {
        val lineNotification = buildMessage("real-chat-id", "Group Name", "Sender")

        assertEquals(
            "line-dismissal-chat-real-chat-id",
            WearableDismissalMetadata.buildDismissalId(lineNotification)
        )
    }

    @Test
    fun buildDismissalId_usesStableFallbackForDefaultChatId() {
        val first = buildMessage(DEFAULT_CHAT_ID, "Fallback Group", "Sender")
        val second = buildMessage(DEFAULT_CHAT_ID, "Fallback Group", "Sender")

        val firstDismissalId = WearableDismissalMetadata.buildDismissalId(first)
        val secondDismissalId = WearableDismissalMetadata.buildDismissalId(second)

        assertEquals(firstDismissalId, secondDismissalId)
        assertTrue(firstDismissalId!!.startsWith("line-dismissal-fallback-"))
    }

    @Test
    fun buildDismissalId_returnsNullForCallNotifications() {
        val lineNotification = LineNotification.builder()
            .chatId(CALL_VIRTUAL_CHAT_ID)
            .title("Caller")
            .sender(Person.Builder().setName("Caller").build())
            .callState(LineNotification.CallState.INCOMING)
            .build()

        assertNull(WearableDismissalMetadata.buildDismissalId(lineNotification))
    }

    private fun buildMessage(chatId: String, title: String, senderName: String): LineNotification {
        return LineNotification.builder()
            .chatId(chatId)
            .title(title)
            .sender(Person.Builder().setName(senderName).build())
            .build()
    }
}
