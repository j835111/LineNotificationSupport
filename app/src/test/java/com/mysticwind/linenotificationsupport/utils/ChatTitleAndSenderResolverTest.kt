package com.mysticwind.linenotificationsupport.utils

import android.os.Bundle
import android.service.notification.StatusBarNotification
import com.mysticwind.linenotificationsupport.chatname.ChatNameManager
import com.mysticwind.linenotificationsupport.components.helper.StatusBarNotificationBuilder
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner.Silent::class)
class ChatTitleAndSenderResolverTest {

    companion object {
        private const val CONVERSATION_TITLE_GROUP_NAME = "GroupName"
        private val CONVERSATION_TITLE_NULL: String? = null
        private const val TEXT_FULL_MESSAGE = "Message - LONG"
        private const val TEXT_NEW_MESSAGE = "You have a new message."
        private const val TEXT_INCOMING_CALL = "LINE語音通話來電中…"
        private const val TEXT_ONGOING_CALL = "LINE通話中"
        private const val TEXT_SENDER = "Sender"
        private const val TEXT_INCOMING_VIDEO_CALL = "LINE視訊通話來電中…"
        private const val TEXT_INDIVIDUAL_JOINED_GROUP_CHAT = "Sender已加入「GroupName」。"
        private const val TEXT_SENDER_WITH_STICKER = "Sender 傳送了貼圖"
        private const val CHAT_ID = "ChatId"
        private val CHAT_ID_NULL: String? = null
        private const val TICKER_TEXT_SENDER_AND_SHORT_MESSAGE = "Sender : Message"
        private const val TICKER_TEXT_SENDER_2_AND_SHORT_MESSAGE = "Sender2 : Message"
        private const val TICKER_TEXT_SENDER_AND_NEW_MESSAGE = "Sender : You have a new message."
        private const val TICKER_TEXT_SENDER_AND_INCOMING_CALL_MESSAGE = "Sender : LINE語音通話來電中…"
        private const val TICKER_TEXT_SENDER_AND_ONGOING_CALL_MESSAGE = "Sender : LINE通話中"
        private const val TICKER_TEXT_MISSED_CALL_AND_SENDER = "LINE未接來電 : Sender"
        private const val TICKER_TEXT_SENDER_AND_INCOMING_VIDEO_CALL_MESSAGE = "Sender : LINE視訊通話來電中…"
        private const val TICKER_TEXT_INDIVIDUAL_JOINED_GROUP_CHAT = "Sender已加入「GroupName」。"
        private const val TICKER_TEXT_SENDER_WITH_STICKER = "Sender 傳送了貼圖"
        private const val TITLE_GROUPNAME_AND_SENDER = "GroupName：Sender"
        private const val TITLE_SENDER = "Sender"
        private const val TITLE_SENDER_2 = "Sender2"
        private const val TITLE_MISSED_CALL = "LINE未接來電"
        private const val TITLE_LINE = "LINE"
        private const val EXPECTED_GROUP_NAME = "GroupName"
        private const val EXPECTED_GROUP_NAME_FROM_TWO_SENDERS = "Sender,Sender2"
        private const val EXPECTED_SENDER = "Sender"
        private const val EXPECTED_SENDER_2 = "Sender2"
    }

    @Mock
    private lateinit var statusBarNotification: StatusBarNotification

    @Mock
    private lateinit var extras: Bundle

    @Mock
    private lateinit var chatNameManager: ChatNameManager

    private lateinit var classUnderTest: ChatTitleAndSenderResolver

    @Before
    fun setUp() {
        whenever(chatNameManager.getChatName(any<String>(), any<String>(), anyOrNull())).thenReturn(EXPECTED_GROUP_NAME)

        classUnderTest = ChatTitleAndSenderResolver(chatNameManager)
    }

    @After
    fun tearDown() {
        verifyNoMoreInteractions(chatNameManager)
    }

    @Test
    fun testHappyCase() {
        val titleAndSender = classUnderTest.resolveTitleAndSender(
            buildNotification(CONVERSATION_TITLE_GROUP_NAME, TEXT_FULL_MESSAGE, TITLE_GROUPNAME_AND_SENDER, CHAT_ID, TICKER_TEXT_SENDER_AND_SHORT_MESSAGE)
        )

        assertEquals(EXPECTED_GROUP_NAME, titleAndSender.left)
        assertEquals(EXPECTED_SENDER, titleAndSender.right)
        verify(chatNameManager).getChatName(CHAT_ID, EXPECTED_SENDER, CONVERSATION_TITLE_GROUP_NAME)
    }

    @Test
    fun testStickerNotification() {
        val titleAndSender = classUnderTest.resolveTitleAndSender(buildGroupMessageWithStickerNotification())

        assertEquals(EXPECTED_GROUP_NAME, titleAndSender.left)
        assertEquals(EXPECTED_SENDER, titleAndSender.right)
        verify(chatNameManager).getChatName(CHAT_ID, EXPECTED_SENDER, EXPECTED_GROUP_NAME)
    }

    @Test
    fun testGroupChatWithGroupName() {
        val titleAndSender = classUnderTest.resolveTitleAndSender(buildGroupChatWithGroupNameNotification())

        assertEquals(EXPECTED_GROUP_NAME, titleAndSender.left)
        assertEquals(EXPECTED_SENDER, titleAndSender.right)
        verify(chatNameManager).getChatName(CHAT_ID, EXPECTED_SENDER, EXPECTED_GROUP_NAME)
    }

    @Test
    fun testGroupChatWithMissingGroupName() {
        whenever(chatNameManager.getChatName(any<String>(), any<String>(), anyOrNull())).thenReturn(TITLE_SENDER)

        val titleAndSender = classUnderTest.resolveTitleAndSender(buildGroupChatWithMissingGroupNameNotification())

        assertEquals(EXPECTED_SENDER, titleAndSender.left)
        assertEquals(EXPECTED_SENDER, titleAndSender.right)
        verify(chatNameManager).getChatName(CHAT_ID, EXPECTED_SENDER, null)
    }

    @Test
    fun testGroupChatWithMissingGroupNameFromTwoSenders() {
        whenever(chatNameManager.getChatName(any<String>(), any<String>(), anyOrNull()))
            .thenReturn(EXPECTED_SENDER)
            .thenReturn(EXPECTED_GROUP_NAME_FROM_TWO_SENDERS)
            .thenReturn(EXPECTED_GROUP_NAME)

        var titleAndSender = classUnderTest.resolveTitleAndSender(buildGroupChatWithMissingGroupNameNotification())
        assertEquals(EXPECTED_SENDER, titleAndSender.left)
        assertEquals(EXPECTED_SENDER, titleAndSender.right)

        titleAndSender = classUnderTest.resolveTitleAndSender(buildGroupChatWithMissingGroupNameNotificationFromSecondSender())
        assertEquals(EXPECTED_GROUP_NAME_FROM_TWO_SENDERS, titleAndSender.left)
        assertEquals(EXPECTED_SENDER_2, titleAndSender.right)

        titleAndSender = classUnderTest.resolveTitleAndSender(buildGroupChatWithGroupNameNotification())
        assertEquals(EXPECTED_GROUP_NAME, titleAndSender.left)
        assertEquals(EXPECTED_SENDER, titleAndSender.right)

        verify(chatNameManager).getChatName(CHAT_ID, EXPECTED_SENDER, null)
        verify(chatNameManager).getChatName(CHAT_ID, EXPECTED_SENDER_2, null)
        verify(chatNameManager).getChatName(CHAT_ID, EXPECTED_SENDER, EXPECTED_GROUP_NAME)
    }

    @Test
    fun testNewMessage() {
        whenever(chatNameManager.getChatName(any<String>(), any<String>(), anyOrNull())).thenReturn(EXPECTED_SENDER)

        var titleAndSender = classUnderTest.resolveTitleAndSender(buildNewMessageNotification())
        assertEquals(EXPECTED_SENDER, titleAndSender.left)
        assertEquals(EXPECTED_SENDER, titleAndSender.right)

        titleAndSender = classUnderTest.resolveTitleAndSender(buildOneOnOneMessageNotification())
        assertEquals(EXPECTED_SENDER, titleAndSender.left)
        assertEquals(EXPECTED_SENDER, titleAndSender.right)
        verify(chatNameManager, times(2)).getChatName(CHAT_ID, EXPECTED_SENDER, null)
    }

    @Test
    fun testIncomingCall() {
        val titleAndSender = classUnderTest.resolveTitleAndSender(buildIncomingCallNotification())

        assertEquals(EXPECTED_SENDER, titleAndSender.left)
        assertEquals(EXPECTED_SENDER, titleAndSender.right)
    }

    @Test
    fun testOngoingCall() {
        val titleAndSender = classUnderTest.resolveTitleAndSender(buildOngoingCallNotification())

        assertEquals(EXPECTED_SENDER, titleAndSender.left)
        assertEquals(EXPECTED_SENDER, titleAndSender.right)
    }

    @Test
    fun testMissedCall() {
        val titleAndSender = classUnderTest.resolveTitleAndSender(buildMissedCallNotification())

        assertEquals(TITLE_MISSED_CALL, titleAndSender.left)
        assertEquals(TITLE_MISSED_CALL, titleAndSender.right)
    }

    @Test
    fun testIncomingVideoCall() {
        val titleAndSender = classUnderTest.resolveTitleAndSender(buildIncomingVideoCallNotification())

        assertEquals(EXPECTED_SENDER, titleAndSender.left)
        assertEquals(EXPECTED_SENDER, titleAndSender.right)
    }

    @Test
    fun testJoinedGroupChat() {
        val titleAndSender = classUnderTest.resolveTitleAndSender(buildJoinedGroupChatNotification())

        assertEquals("LINE", titleAndSender.left)
        assertEquals("LINE", titleAndSender.right)
    }

    private fun buildGroupChatWithGroupNameNotification() =
        buildNotification(CONVERSATION_TITLE_GROUP_NAME, TEXT_FULL_MESSAGE, TITLE_GROUPNAME_AND_SENDER, CHAT_ID, TICKER_TEXT_SENDER_AND_SHORT_MESSAGE)

    private fun buildGroupChatWithMissingGroupNameNotification() =
        buildNotification(CONVERSATION_TITLE_NULL, TEXT_FULL_MESSAGE, TITLE_SENDER, CHAT_ID, TICKER_TEXT_SENDER_AND_SHORT_MESSAGE)

    private fun buildGroupChatWithMissingGroupNameNotificationFromSecondSender() =
        buildNotification(CONVERSATION_TITLE_NULL, TEXT_FULL_MESSAGE, TITLE_SENDER_2, CHAT_ID, TICKER_TEXT_SENDER_2_AND_SHORT_MESSAGE)

    private fun buildNewMessageNotification() =
        buildNotification(CONVERSATION_TITLE_NULL, TEXT_NEW_MESSAGE, TITLE_SENDER, CHAT_ID, TICKER_TEXT_SENDER_AND_NEW_MESSAGE)

    private fun buildOneOnOneMessageNotification() =
        buildNotification(CONVERSATION_TITLE_NULL, TEXT_FULL_MESSAGE, TITLE_SENDER, CHAT_ID, TICKER_TEXT_SENDER_AND_SHORT_MESSAGE)

    private fun buildIncomingCallNotification() =
        buildNotification(CONVERSATION_TITLE_NULL, TEXT_INCOMING_CALL, TITLE_SENDER, CHAT_ID_NULL, TICKER_TEXT_SENDER_AND_INCOMING_CALL_MESSAGE)

    private fun buildOngoingCallNotification() =
        buildNotification(CONVERSATION_TITLE_NULL, TEXT_ONGOING_CALL, TITLE_SENDER, CHAT_ID_NULL, TICKER_TEXT_SENDER_AND_ONGOING_CALL_MESSAGE)

    private fun buildMissedCallNotification() =
        buildNotification(CONVERSATION_TITLE_NULL, TEXT_SENDER, TITLE_MISSED_CALL, CHAT_ID_NULL, TICKER_TEXT_MISSED_CALL_AND_SENDER)

    private fun buildIncomingVideoCallNotification() =
        buildNotification(CONVERSATION_TITLE_NULL, TEXT_INCOMING_VIDEO_CALL, TITLE_SENDER, CHAT_ID_NULL, TICKER_TEXT_SENDER_AND_INCOMING_VIDEO_CALL_MESSAGE)

    private fun buildJoinedGroupChatNotification() =
        buildNotification(CONVERSATION_TITLE_NULL, TEXT_INDIVIDUAL_JOINED_GROUP_CHAT, TITLE_LINE, CHAT_ID_NULL, TICKER_TEXT_INDIVIDUAL_JOINED_GROUP_CHAT)

    private fun buildGroupMessageWithStickerNotification() =
        buildNotification(CONVERSATION_TITLE_GROUP_NAME, TEXT_SENDER_WITH_STICKER, TITLE_GROUPNAME_AND_SENDER, CHAT_ID, TICKER_TEXT_SENDER_WITH_STICKER)

    private fun buildNotification(
        conversationTitle: String?,
        androidText: String?,
        androidTitle: String?,
        lineChatId: String?,
        tickerText: String?
    ): StatusBarNotification {
        return StatusBarNotificationBuilder()
            .withAndroidText(androidText)
            .withAndroidConversationTitle(conversationTitle)
            .withAndroidTitle(androidTitle)
            .withLineChatId(lineChatId)
            .withTickerText(tickerText)
            .build()
    }
}
