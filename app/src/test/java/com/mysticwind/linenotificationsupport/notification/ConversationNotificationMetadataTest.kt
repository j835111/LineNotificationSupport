package com.mysticwind.linenotificationsupport.notification

import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import androidx.core.app.Person
import androidx.core.graphics.drawable.IconCompat
import com.mysticwind.linenotificationsupport.model.LineNotification
import com.mysticwind.linenotificationsupport.model.LineNotificationBuilder.Companion.DEFAULT_CHAT_ID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mockStatic
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
class ConversationNotificationMetadataTest {

    companion object {
        private const val APP_ICON_RES_ID = 123
    }

    @Mock
    private lateinit var context: Context

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

    @Test
    fun buildShortcutIcon_usesNotificationBitmapWhenAvailable() {
        val iconBitmap = mock<Bitmap>()
        val expectedIcon = mock<IconCompat>()
        val lineNotification = LineNotification.builder()
            .icon(iconBitmap)
            .build()

        mockStatic(IconCompat::class.java).use { iconCompatMock ->
            iconCompatMock.`when`<IconCompat> {
                IconCompat.createWithBitmap(iconBitmap)
            }.thenReturn(expectedIcon)

            val shortcutIcon = ConversationNotificationMetadata.buildShortcutIcon(context, lineNotification)

            assertSame(expectedIcon, shortcutIcon)
        }
    }

    @Test
    fun buildShortcutIcon_fallsBackToApplicationIconWhenNotificationBitmapMissing() {
        val applicationInfo = ApplicationInfo().apply { icon = APP_ICON_RES_ID }
        val expectedIcon = mock<IconCompat>()
        whenever(context.applicationInfo).thenReturn(applicationInfo)
        val lineNotification = LineNotification.builder().build()

        mockStatic(IconCompat::class.java).use { iconCompatMock ->
            iconCompatMock.`when`<IconCompat> {
                IconCompat.createWithResource(context, APP_ICON_RES_ID)
            }.thenReturn(expectedIcon)

            val shortcutIcon = ConversationNotificationMetadata.buildShortcutIcon(context, lineNotification)

            assertSame(expectedIcon, shortcutIcon)
        }
    }

    @Test
    fun buildShortcutIcon_returnsNullWhenNeitherNotificationBitmapNorApplicationIconExists() {
        whenever(context.applicationInfo).thenReturn(ApplicationInfo())

        val shortcutIcon = ConversationNotificationMetadata.buildShortcutIcon(
            context,
            LineNotification.builder().build()
        )

        assertNull(shortcutIcon)
    }

    private fun buildMessage(chatId: String, title: String, senderName: String): LineNotification {
        return LineNotification.builder()
            .chatId(chatId)
            .title(title)
            .sender(Person.Builder().setName(senderName).build())
            .build()
    }
}
