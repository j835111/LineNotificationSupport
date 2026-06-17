package com.mysticwind.linenotificationsupport.model

import android.app.Notification
import android.content.Context
import android.service.notification.StatusBarNotification
import com.google.common.collect.ImmutableList
import com.mysticwind.linenotificationsupport.components.helper.StatusBarNotificationBuilder
import com.mysticwind.linenotificationsupport.model.LineNotificationBuilder.Companion.CALL_VIRTUAL_CHAT_ID
import com.mysticwind.linenotificationsupport.model.LineNotificationBuilder.Companion.DEFAULT_CHAT_ID
import com.mysticwind.linenotificationsupport.model.LineNotificationBuilder.Companion.GENERAL_NOTIFICATION_CHANNEL
import com.mysticwind.linenotificationsupport.reply.MockedReplyActionBuilder
import com.mysticwind.linenotificationsupport.utils.ChatTitleAndSenderResolver
import com.mysticwind.linenotificationsupport.utils.StatusBarNotificationPrinter
import org.apache.commons.lang3.tuple.Pair
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner.Silent::class)
class LineNotificationBuilderTest {

    companion object {
        private const val CALL_CATEGORY = "call"
        private const val MESSAGE_CATEGORY = "msg"
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
        private const val TICKER_TEXT_SENDER_AND_NEW_MESSAGE = "Sender : You have a new message."
        private const val TICKER_TEXT_SENDER_AND_INCOMING_CALL_MESSAGE = "Sender : LINE語音通話來電中…"
        private const val TICKER_TEXT_SENDER_AND_ONGOING_CALL_MESSAGE = "Sender : LINE通話中"
        private const val TICKER_TEXT_MISSED_CALL_AND_SENDER = "LINE未接來電 : Sender"
        private const val TICKER_TEXT_SENDER_AND_INCOMING_VIDEO_CALL_MESSAGE = "Sender : LINE視訊通話來電中…"
        private const val TICKER_TEXT_INDIVIDUAL_JOINED_GROUP_CHAT = "Sender已加入「GroupName」。"
        private const val TICKER_TEXT_SENDER_WITH_STICKER = "Sender 傳送了貼圖"
        private const val TITLE_GROUPNAME_AND_SENDER = "GroupName：Sender"
        private const val TITLE_SENDER = "Sender"
        private const val TITLE_MISSED_CALL = "LINE未接來電"
        private const val TITLE_LINE = "LINE"
        private const val TAG_MESSAGE = "NOTIFICATION_TAG_MESSAGE"
        private val TAG_NULL: String? = null
        private const val TAG_MISSED_CALL = "NOTIFICATION_TAG_MISSED_CALL"
        private const val TAG_GROUP = "NOTIFICATION_TAG_GROUP"
        private const val NOTIFICATION_CHANNEL_NEW_MESSAGES = "jp.naver.line.android.notification.NewMessages"
        private const val NOTIFICATION_CHANNEL_CALLS = "jp.naver.line.android.notification.Calls"
        private const val NOTIFICATION_CHANNEL_FRIEND_REQUESTS = "jp.naver.line.android.notification.FriendRequests"
        private const val TITLE = "title"
        private const val SENDER = "sender"
        private const val LINE_MESSAGE_ID = "lineMessageId"
    }

    @Mock
    private lateinit var mockedContext: Context

    @Mock
    private lateinit var mockedChatTitleAndSenderResolver: ChatTitleAndSenderResolver

    @Mock
    private lateinit var mockedStatusBarNotificationPrinter: StatusBarNotificationPrinter

    @Mock
    private lateinit var action1: Notification.Action

    @Mock
    private lateinit var action2: Notification.Action

    private lateinit var classUnderTest: LineNotificationBuilder

    @Before
    fun setUp() {
        whenever(mockedChatTitleAndSenderResolver.resolveTitleAndSender(any<StatusBarNotification>())).thenReturn(Pair.of(TITLE, SENDER))

        classUnderTest = LineNotificationBuilder(mockedContext, mockedChatTitleAndSenderResolver, mockedStatusBarNotificationPrinter, MockedReplyActionBuilder())
    }

    @Test
    fun testGroupChatWithGroupName() {
        val lineNotification = classUnderTest.from(buildGroupChatWithGroupNameNotification())

        assertEquals(SENDER, lineNotification.sender!!.name)
        assertEquals(TITLE, lineNotification.title)
        assertEquals(CHAT_ID, lineNotification.chatId)
        assertNull(lineNotification.callState)
        assertEquals(1, lineNotification.actions.size)
        MockedReplyActionBuilder.validateAction(action2, lineNotification.actions[0])
        assertEquals(LINE_MESSAGE_ID, lineNotification.lineMessageId)
        assertFalse(lineNotification.isSelfResponse)
    }

    @Test
    fun testGroupChatWithMissingGroupNameNotification() {
        val lineNotification = classUnderTest.from(buildGroupChatWithMissingGroupNameNotification())

        assertEquals(SENDER, lineNotification.sender!!.name)
        assertEquals(TITLE, lineNotification.title)
        assertEquals(CHAT_ID, lineNotification.chatId)
        assertNull(lineNotification.callState)
        assertEquals(1, lineNotification.actions.size)
        MockedReplyActionBuilder.validateAction(action2, lineNotification.actions[0])
        assertEquals(LINE_MESSAGE_ID, lineNotification.lineMessageId)
        assertFalse(lineNotification.isSelfResponse)
    }

    @Test
    fun testNewMessageNotification() {
        val lineNotification = classUnderTest.from(buildNewMessageNotification())

        assertEquals(SENDER, lineNotification.sender!!.name)
        assertEquals(TITLE, lineNotification.title)
        assertEquals(CHAT_ID, lineNotification.chatId)
        assertNull(lineNotification.callState)
        assertTrue(lineNotification.actions.isEmpty())
        assertEquals(LINE_MESSAGE_ID, lineNotification.lineMessageId)
        assertFalse(lineNotification.isSelfResponse)
    }

    @Test
    fun testReplacingNewMessageNotification() {
        val lineNotification = classUnderTest.from(buildOneOnOneMessageNotification())

        assertEquals(SENDER, lineNotification.sender!!.name)
        assertEquals(TITLE, lineNotification.title)
        assertEquals(CHAT_ID, lineNotification.chatId)
        assertNull(lineNotification.callState)
        assertEquals(1, lineNotification.actions.size)
        MockedReplyActionBuilder.validateAction(action2, lineNotification.actions[0])
        assertEquals(LINE_MESSAGE_ID, lineNotification.lineMessageId)
        assertFalse(lineNotification.isSelfResponse)
    }

    @Test
    fun testOneOnOneMessageNotification() {
        val lineNotification = classUnderTest.from(buildOneOnOneMessageNotification())

        assertEquals(SENDER, lineNotification.sender!!.name)
        assertEquals(TITLE, lineNotification.title)
        assertEquals(CHAT_ID, lineNotification.chatId)
        assertNull(lineNotification.callState)
        assertEquals(1, lineNotification.actions.size)
        MockedReplyActionBuilder.validateAction(action2, lineNotification.actions[0])
        assertEquals(LINE_MESSAGE_ID, lineNotification.lineMessageId)
        assertFalse(lineNotification.isSelfResponse)
    }

    @Test
    fun testOneOnOneMessageNotificationWithOnlyOneOriginalAction() {
        val lineNotification = classUnderTest.from(buildOneOnOneMessageNotificationWithOneAction())

        assertEquals(SENDER, lineNotification.sender!!.name)
        assertEquals(TITLE, lineNotification.title)
        assertEquals(CHAT_ID, lineNotification.chatId)
        assertNull(lineNotification.callState)
        assertTrue(lineNotification.actions.isEmpty())
        assertEquals(LINE_MESSAGE_ID, lineNotification.lineMessageId)
        assertFalse(lineNotification.isSelfResponse)
    }

    @Test
    fun testIncomingCallNotification() {
        val lineNotification = classUnderTest.from(buildIncomingCallNotification())

        assertEquals(SENDER, lineNotification.sender!!.name)
        assertEquals(TITLE, lineNotification.title)
        assertEquals(CALL_VIRTUAL_CHAT_ID, lineNotification.chatId)
        assertEquals(LineNotification.CallState.INCOMING, lineNotification.callState)
        assertEquals(ImmutableList.of(action2, action1), lineNotification.actions)
        assertNull(lineNotification.lineMessageId)
        assertFalse(lineNotification.isSelfResponse)
    }

    @Test
    fun testIncomingCallNotificationWithOnlyOneOriginalAction() {
        val lineNotification = classUnderTest.from(buildIncomingCallNotificationWithOneAction())

        assertEquals(SENDER, lineNotification.sender!!.name)
        assertEquals(TITLE, lineNotification.title)
        assertEquals(CALL_VIRTUAL_CHAT_ID, lineNotification.chatId)
        assertEquals(LineNotification.CallState.INCOMING, lineNotification.callState)
        assertEquals(ImmutableList.of(action1), lineNotification.actions)
        assertNull(lineNotification.lineMessageId)
        assertFalse(lineNotification.isSelfResponse)
    }

    @Test
    fun testOngoingCallNotification() {
        val lineNotification = classUnderTest.from(buildOngoingCallNotification())

        assertEquals(SENDER, lineNotification.sender!!.name)
        assertEquals(TITLE, lineNotification.title)
        assertEquals(CALL_VIRTUAL_CHAT_ID, lineNotification.chatId)
        assertEquals(LineNotification.CallState.IN_A_CALL, lineNotification.callState)
        assertEquals(ImmutableList.of(action1), lineNotification.actions)
        assertNull(lineNotification.lineMessageId)
        assertFalse(lineNotification.isSelfResponse)
    }

    @Test
    fun testOngoingCallNotificationWithNoActions() {
        val lineNotification = classUnderTest.from(buildOngoingCallNotificationWithNoActions())

        assertEquals(SENDER, lineNotification.sender!!.name)
        assertEquals(TITLE, lineNotification.title)
        assertEquals(CALL_VIRTUAL_CHAT_ID, lineNotification.chatId)
        assertEquals(LineNotification.CallState.IN_A_CALL, lineNotification.callState)
        assertTrue(lineNotification.actions.isEmpty())
        assertNull(lineNotification.lineMessageId)
        assertFalse(lineNotification.isSelfResponse)
    }

    @Test
    fun testMissedCallNotification() {
        val lineNotification = classUnderTest.from(buildMissedCallNotification())

        assertEquals(SENDER, lineNotification.sender!!.name)
        assertEquals(TITLE, lineNotification.title)
        assertEquals(CALL_VIRTUAL_CHAT_ID, lineNotification.chatId)
        assertEquals(LineNotification.CallState.MISSED_CALL, lineNotification.callState)
        assertEquals(ImmutableList.of(action2), lineNotification.actions)
        assertNull(lineNotification.lineMessageId)
        assertFalse(lineNotification.isSelfResponse)
    }

    @Test
    fun testMissedCallNotificationWithOnlyOneOriginalAction() {
        val lineNotification = classUnderTest.from(buildMissedCallNotificationWithOneAction())

        assertEquals(SENDER, lineNotification.sender!!.name)
        assertEquals(TITLE, lineNotification.title)
        assertEquals(CALL_VIRTUAL_CHAT_ID, lineNotification.chatId)
        assertEquals(LineNotification.CallState.MISSED_CALL, lineNotification.callState)
        assertTrue(lineNotification.actions.isEmpty())
        assertNull(lineNotification.lineMessageId)
        assertFalse(lineNotification.isSelfResponse)
    }

    @Test
    fun testIncomingVideoCallNotification() {
        val lineNotification = classUnderTest.from(buildIncomingVideoCallNotification())

        assertEquals(SENDER, lineNotification.sender!!.name)
        assertEquals(TITLE, lineNotification.title)
        assertEquals(CALL_VIRTUAL_CHAT_ID, lineNotification.chatId)
        assertEquals(LineNotification.CallState.INCOMING, lineNotification.callState)
        assertEquals(ImmutableList.of(action2, action1), lineNotification.actions)
        assertNull(lineNotification.lineMessageId)
        assertFalse(lineNotification.isSelfResponse)
    }

    @Test
    fun testJoinedGroupChatNotification() {
        val lineNotification = classUnderTest.from(buildJoinedGroupChatNotification())

        assertEquals(SENDER, lineNotification.sender!!.name)
        assertEquals(TITLE, lineNotification.title)
        assertEquals(DEFAULT_CHAT_ID, lineNotification.chatId)
        assertTrue(lineNotification.actions.isEmpty())
        assertNull(lineNotification.lineMessageId)
        assertFalse(lineNotification.isSelfResponse)
    }

    @Test
    fun testGroupMessageWithStickerNotification() {
        val lineNotification = classUnderTest.from(buildGroupMessageWithStickerNotification())

        assertEquals(SENDER, lineNotification.sender!!.name)
        assertEquals(TITLE, lineNotification.title)
        assertEquals(CHAT_ID, lineNotification.chatId)
        assertNull(lineNotification.callState)
        assertEquals(1, lineNotification.actions.size)
        MockedReplyActionBuilder.validateAction(action2, lineNotification.actions[0])
        assertEquals(LINE_MESSAGE_ID, lineNotification.lineMessageId)
        assertFalse(lineNotification.isSelfResponse)
    }

    @Test
    fun testNoSenderReturnsDefaultSender() {
        whenever(mockedChatTitleAndSenderResolver.resolveTitleAndSender(any<StatusBarNotification>())).thenReturn(Pair.of(TITLE, null))

        val lineNotification = classUnderTest.from(buildOneOnOneMessageNotification())

        assertEquals("?", lineNotification.sender!!.name)
        assertEquals(TITLE, lineNotification.title)
        assertEquals(CHAT_ID, lineNotification.chatId)
        assertNull(lineNotification.callState)
        assertEquals(1, lineNotification.actions.size)
        MockedReplyActionBuilder.validateAction(action2, lineNotification.actions[0])
        assertEquals(LINE_MESSAGE_ID, lineNotification.lineMessageId)
        assertFalse(lineNotification.isSelfResponse)
    }

    private fun buildGroupChatWithGroupNameNotification() =
        buildNotification(CONVERSATION_TITLE_GROUP_NAME, TEXT_FULL_MESSAGE, TITLE_GROUPNAME_AND_SENDER, CHAT_ID,
            TICKER_TEXT_SENDER_AND_SHORT_MESSAGE, MESSAGE_CATEGORY, TAG_MESSAGE, NOTIFICATION_CHANNEL_NEW_MESSAGES, 2, true)

    private fun buildGroupChatWithMissingGroupNameNotification() =
        buildNotification(CONVERSATION_TITLE_NULL, TEXT_FULL_MESSAGE, TITLE_SENDER, CHAT_ID,
            TICKER_TEXT_SENDER_AND_SHORT_MESSAGE, MESSAGE_CATEGORY, TAG_MESSAGE, NOTIFICATION_CHANNEL_NEW_MESSAGES, 2, true)

    private fun buildNewMessageNotification() =
        buildNotification(CONVERSATION_TITLE_NULL, TEXT_NEW_MESSAGE, TITLE_SENDER, CHAT_ID,
            TICKER_TEXT_SENDER_AND_NEW_MESSAGE, MESSAGE_CATEGORY, TAG_NULL, NOTIFICATION_CHANNEL_NEW_MESSAGES, 0, true)

    private fun buildOneOnOneMessageNotification() =
        buildNotification(CONVERSATION_TITLE_NULL, TEXT_FULL_MESSAGE, TITLE_SENDER, CHAT_ID,
            TICKER_TEXT_SENDER_AND_SHORT_MESSAGE, MESSAGE_CATEGORY, TAG_MESSAGE, NOTIFICATION_CHANNEL_NEW_MESSAGES, 2, true)

    private fun buildOneOnOneMessageNotificationWithOneAction() =
        buildNotification(CONVERSATION_TITLE_NULL, TEXT_FULL_MESSAGE, TITLE_SENDER, CHAT_ID,
            TICKER_TEXT_SENDER_AND_SHORT_MESSAGE, MESSAGE_CATEGORY, TAG_MESSAGE, NOTIFICATION_CHANNEL_NEW_MESSAGES, 1, true)

    private fun buildIncomingCallNotification() =
        buildNotification(CONVERSATION_TITLE_NULL, TEXT_INCOMING_CALL, TITLE_SENDER, CHAT_ID_NULL,
            TICKER_TEXT_SENDER_AND_INCOMING_CALL_MESSAGE, CALL_CATEGORY, TAG_NULL, NOTIFICATION_CHANNEL_CALLS, 2, false)

    private fun buildIncomingCallNotificationWithOneAction() =
        buildNotification(CONVERSATION_TITLE_NULL, TEXT_INCOMING_CALL, TITLE_SENDER, CHAT_ID_NULL,
            TICKER_TEXT_SENDER_AND_INCOMING_CALL_MESSAGE, CALL_CATEGORY, TAG_NULL, NOTIFICATION_CHANNEL_CALLS, 1, false)

    private fun buildOngoingCallNotification() =
        buildNotification(CONVERSATION_TITLE_NULL, TEXT_ONGOING_CALL, TITLE_SENDER, CHAT_ID_NULL,
            TICKER_TEXT_SENDER_AND_ONGOING_CALL_MESSAGE, MESSAGE_CATEGORY, TAG_NULL, GENERAL_NOTIFICATION_CHANNEL, 1, false)

    private fun buildOngoingCallNotificationWithNoActions() =
        buildNotification(CONVERSATION_TITLE_NULL, TEXT_ONGOING_CALL, TITLE_SENDER, CHAT_ID_NULL,
            TICKER_TEXT_SENDER_AND_ONGOING_CALL_MESSAGE, MESSAGE_CATEGORY, TAG_NULL, GENERAL_NOTIFICATION_CHANNEL, 0, false)

    private fun buildMissedCallNotification() =
        buildNotification(CONVERSATION_TITLE_NULL, TEXT_SENDER, TITLE_MISSED_CALL, CHAT_ID_NULL,
            TICKER_TEXT_MISSED_CALL_AND_SENDER, MESSAGE_CATEGORY, TAG_MISSED_CALL, NOTIFICATION_CHANNEL_NEW_MESSAGES, 2, false)

    private fun buildMissedCallNotificationWithOneAction() =
        buildNotification(CONVERSATION_TITLE_NULL, TEXT_SENDER, TITLE_MISSED_CALL, CHAT_ID_NULL,
            TICKER_TEXT_MISSED_CALL_AND_SENDER, MESSAGE_CATEGORY, TAG_MISSED_CALL, NOTIFICATION_CHANNEL_NEW_MESSAGES, 1, false)

    private fun buildIncomingVideoCallNotification() =
        buildNotification(CONVERSATION_TITLE_NULL, TEXT_INCOMING_VIDEO_CALL, TITLE_SENDER, CHAT_ID_NULL,
            TICKER_TEXT_SENDER_AND_INCOMING_VIDEO_CALL_MESSAGE, CALL_CATEGORY, TAG_NULL, NOTIFICATION_CHANNEL_CALLS, 2, false)

    private fun buildJoinedGroupChatNotification() =
        buildNotification(CONVERSATION_TITLE_NULL, TEXT_INDIVIDUAL_JOINED_GROUP_CHAT, TITLE_LINE, CHAT_ID_NULL,
            TICKER_TEXT_INDIVIDUAL_JOINED_GROUP_CHAT, MESSAGE_CATEGORY, TAG_GROUP, NOTIFICATION_CHANNEL_FRIEND_REQUESTS, 0, false)

    private fun buildGroupMessageWithStickerNotification() =
        buildNotification(CONVERSATION_TITLE_GROUP_NAME, TEXT_SENDER_WITH_STICKER, TITLE_GROUPNAME_AND_SENDER, CHAT_ID,
            TICKER_TEXT_SENDER_WITH_STICKER, MESSAGE_CATEGORY, TAG_MESSAGE, NOTIFICATION_CHANNEL_NEW_MESSAGES, 2, true)

    private fun buildNotification(
        conversationTitle: String?,
        androidText: String?,
        androidTitle: String?,
        lineChatId: String?,
        tickerText: String?,
        category: String?,
        tag: String?,
        notificationChannel: String?,
        actionCount: Int,
        hasMessageId: Boolean
    ): StatusBarNotification {
        val builder = StatusBarNotificationBuilder()
            .withAndroidText(androidText)
            .withAndroidConversationTitle(conversationTitle)
            .withAndroidTitle(androidTitle)
            .withLineChatId(lineChatId)
            .withChannelId(notificationChannel)
            .withTickerText(tickerText)
            .withCategory(category)
            .withTag(tag)

        when (actionCount) {
            1 -> builder.withActions(action1)
            2 -> builder.withActions(action1, action2)
        }

        if (hasMessageId) {
            builder.withLineMessageId(LINE_MESSAGE_ID)
        }

        return builder.build()
    }

    @After
    fun verifyMocks() {
        verify(mockedChatTitleAndSenderResolver).resolveTitleAndSender(any())
    }
}
