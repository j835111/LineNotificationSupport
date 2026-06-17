package com.mysticwind.linenotificationsupport.components

import android.content.Context
import android.service.notification.StatusBarNotification
import com.google.common.collect.HashMultimap
import com.mysticwind.linenotificationsupport.chatname.ChatNameManager
import com.mysticwind.linenotificationsupport.chatname.dataaccessor.CachingGroupChatNameDataAccessorDecorator
import com.mysticwind.linenotificationsupport.chatname.dataaccessor.CachingMultiPersonChatNameDataAccessorDecorator
import com.mysticwind.linenotificationsupport.chatname.dataaccessor.GroupChatNameDataAccessor
import com.mysticwind.linenotificationsupport.chatname.dataaccessor.MultiPersonChatNameDataAccessor
import com.mysticwind.linenotificationsupport.components.helper.StatusBarNotificationBuilder
import com.mysticwind.linenotificationsupport.model.LineNotification
import com.mysticwind.linenotificationsupport.model.LineNotificationBuilder
import com.mysticwind.linenotificationsupport.reply.MockedReplyActionBuilder
import com.mysticwind.linenotificationsupport.utils.ChatTitleAndSenderResolver
import com.mysticwind.linenotificationsupport.utils.StatusBarNotificationPrinter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import java.util.Optional

@RunWith(MockitoJUnitRunner.Silent::class)
class LineNotificationBuilderTest {

    private val groupChatNameDataAccessor: GroupChatNameDataAccessor =
        CachingGroupChatNameDataAccessorDecorator(object : GroupChatNameDataAccessor {
            private val chatIdToChatGroupNameMap = HashMap<String, String>()

            override fun persistRelationship(chatId: String, chatGroupName: String) {
                chatIdToChatGroupNameMap[chatId] = chatGroupName
            }

            override fun getChatGroupName(chatId: String): Optional<String> {
                return Optional.ofNullable(chatIdToChatGroupNameMap[chatId])
            }

            override fun getAllChatGroups(): Map<String, String> {
                return chatIdToChatGroupNameMap
            }
        })

    private val multiPersonChatNameDataAccessor: MultiPersonChatNameDataAccessor =
        CachingMultiPersonChatNameDataAccessorDecorator(object : MultiPersonChatNameDataAccessor {
            private val chatIdToSenderMultimap = HashMultimap.create<String, String>()

            override fun addRelationshipAndGetChatGroupName(chatId: String, sender: String?): String {
                chatIdToSenderMultimap.put(chatId, sender)
                return sender ?: ""
            }

            override fun getAllChatIdToSenders() = chatIdToSenderMultimap

            override fun deleteAllEntries() {
                chatIdToSenderMultimap.clear()
            }
        })

    private val chatNameManager = ChatNameManager(groupChatNameDataAccessor, multiPersonChatNameDataAccessor)
    private val chatTitleAndSenderResolver = ChatTitleAndSenderResolver(chatNameManager)

    @Mock
    private lateinit var mockedContext: Context

    @Mock
    private lateinit var mockedStatusBarNotificationPrinter: StatusBarNotificationPrinter

    private lateinit var classUnderTest: LineNotificationBuilder

    @Before
    fun setUp() {
        classUnderTest = LineNotificationBuilder(mockedContext, chatTitleAndSenderResolver, mockedStatusBarNotificationPrinter, MockedReplyActionBuilder())
    }

    @Test
    fun testVideoMessage() {
        val statusBarNotification = StatusBarNotificationBuilder()
            .withTag("NOTIFICATION_TAG_MESSAGE")
            .withCategory("msg")
            .withAndroidTitle("GroupName：🌞SenderName")
            .withAndroidConversationTitle("GroupName")
            .withHiddenConversationTitle("GroupName")
            .withLineChatId("cffc56f3a90a8fef933ade4213fe2286f")
            .withLineMessageId("13044851534123")
            .withAndroidText("🌞SenderName傳送了影片")
            .withChannelId("jp.naver.line.android.notification.NewMessages")
            .withTickerText("🌞SenderName傳送了影片")
            .withWhen(1605586932474L)
            .build()

        val lineNotification = classUnderTest.from(statusBarNotification)

        assertEquals("🌞SenderName傳送了影片", lineNotification.message)
        assertEquals("🌞SenderName", lineNotification.sender!!.name)
        assertEquals("GroupName", lineNotification.title)
        assertNull(lineNotification.callState)
        assertEquals("cffc56f3a90a8fef933ade4213fe2286f", lineNotification.chatId)
        assertEquals(1605586932474L, lineNotification.timestamp)
    }
}
