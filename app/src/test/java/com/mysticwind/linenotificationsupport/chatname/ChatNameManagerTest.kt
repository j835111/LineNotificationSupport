package com.mysticwind.linenotificationsupport.chatname

import com.mysticwind.linenotificationsupport.chatname.dataaccessor.GroupChatNameDataAccessor
import com.mysticwind.linenotificationsupport.chatname.dataaccessor.MultiPersonChatNameDataAccessor
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
import java.util.Optional

@RunWith(MockitoJUnitRunner::class)
class ChatNameManagerTest {

    companion object {
        private const val CHAT_ID = "chatId"
        private const val SENDER = "sender"
        private const val SENDER_2 = "sender2"
        private const val MULTI_PERSON_CHAT_NAME = "multiPersonChatName"
        private const val HIGH_CONFIDENCE_GROUP_NAME = "highConfidenceGroupName"
        private const val DIFFERENT_CHAT_NAME = "differentChatName"
    }

    @Mock
    private lateinit var mockedGroupChatNameDataAccessor: GroupChatNameDataAccessor

    @Mock
    private lateinit var mockedMultiPersonChatNameDataAccessor: MultiPersonChatNameDataAccessor

    private lateinit var classUnderTest: ChatNameManager

    @Before
    fun setUp() {
        whenever(mockedGroupChatNameDataAccessor.getChatGroupName(any<String>())).thenReturn(Optional.empty())
        whenever(mockedMultiPersonChatNameDataAccessor.addRelationshipAndGetChatGroupName(any<String>(), anyOrNull())).thenReturn(MULTI_PERSON_CHAT_NAME)

        classUnderTest = ChatNameManager(mockedGroupChatNameDataAccessor, mockedMultiPersonChatNameDataAccessor)
    }

    @After
    fun tearDown() {
        verifyNoMoreInteractions(
            mockedGroupChatNameDataAccessor,
            mockedMultiPersonChatNameDataAccessor
        )
    }

    @Test
    fun getChatName_singlePersonChat() {
        val chatName = classUnderTest.getChatName(CHAT_ID, SENDER)

        assertEquals(MULTI_PERSON_CHAT_NAME, chatName)
        verify(mockedGroupChatNameDataAccessor).getChatGroupName(CHAT_ID)
        verify(mockedMultiPersonChatNameDataAccessor).addRelationshipAndGetChatGroupName(CHAT_ID, SENDER)
    }

    @Test
    fun getChatName_multiPersonChat() {
        var chatName = classUnderTest.getChatName(CHAT_ID, SENDER)

        assertEquals(MULTI_PERSON_CHAT_NAME, chatName)
        verify(mockedGroupChatNameDataAccessor).getChatGroupName(CHAT_ID)
        verify(mockedMultiPersonChatNameDataAccessor).addRelationshipAndGetChatGroupName(CHAT_ID, SENDER)

        chatName = classUnderTest.getChatName(CHAT_ID, SENDER_2)

        assertEquals(MULTI_PERSON_CHAT_NAME, chatName)
        verify(mockedGroupChatNameDataAccessor, times(2)).getChatGroupName(CHAT_ID)
        verify(mockedMultiPersonChatNameDataAccessor).addRelationshipAndGetChatGroupName(CHAT_ID, SENDER_2)
    }

    @Test
    fun getChatName_groupChatWithHighConfidenceGroupName_firstCall() {
        val chatName = classUnderTest.getChatName(CHAT_ID, SENDER, HIGH_CONFIDENCE_GROUP_NAME)

        assertEquals(HIGH_CONFIDENCE_GROUP_NAME, chatName)
        verify(mockedGroupChatNameDataAccessor).getChatGroupName(CHAT_ID)
        verify(mockedGroupChatNameDataAccessor).persistRelationship(CHAT_ID, HIGH_CONFIDENCE_GROUP_NAME)
    }

    @Test
    fun getChatName_groupChatWithHighConfidenceGroupName_secondCall_sameChatName() {
        whenever(mockedGroupChatNameDataAccessor.getChatGroupName(any<String>())).thenReturn(Optional.of(HIGH_CONFIDENCE_GROUP_NAME))

        val chatName = classUnderTest.getChatName(CHAT_ID, SENDER, HIGH_CONFIDENCE_GROUP_NAME)

        assertEquals(HIGH_CONFIDENCE_GROUP_NAME, chatName)
        verify(mockedGroupChatNameDataAccessor).getChatGroupName(CHAT_ID)
    }

    @Test
    fun getChatName_groupChatWithHighConfidenceGroupName_secondCall_differentChatName() {
        whenever(mockedGroupChatNameDataAccessor.getChatGroupName(any<String>())).thenReturn(Optional.of(DIFFERENT_CHAT_NAME))

        val chatName = classUnderTest.getChatName(CHAT_ID, SENDER, HIGH_CONFIDENCE_GROUP_NAME)

        assertEquals(HIGH_CONFIDENCE_GROUP_NAME, chatName)
        verify(mockedGroupChatNameDataAccessor).getChatGroupName(CHAT_ID)
        verify(mockedGroupChatNameDataAccessor).persistRelationship(CHAT_ID, HIGH_CONFIDENCE_GROUP_NAME)
    }

    @Test
    fun getChatName_groupChatWithoutHighConfidenceGroupName() {
        whenever(mockedGroupChatNameDataAccessor.getChatGroupName(any<String>())).thenReturn(Optional.of(HIGH_CONFIDENCE_GROUP_NAME))

        val chatName = classUnderTest.getChatName(CHAT_ID, SENDER)

        assertEquals(HIGH_CONFIDENCE_GROUP_NAME, chatName)
        verify(mockedGroupChatNameDataAccessor).getChatGroupName(CHAT_ID)
    }

    @Test
    fun getChatName_groupChatWithoutHighConfidenceGroupName_noPersistedGroupChatName() {
        val chatName = classUnderTest.getChatName(CHAT_ID, SENDER)

        assertEquals(MULTI_PERSON_CHAT_NAME, chatName)
        verify(mockedGroupChatNameDataAccessor).getChatGroupName(CHAT_ID)
        verify(mockedMultiPersonChatNameDataAccessor).addRelationshipAndGetChatGroupName(CHAT_ID, SENDER)
    }
}
