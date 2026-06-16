package com.mysticwind.linenotificationsupport.chatname.dataaccessor

import com.google.common.collect.HashMultimap
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
class CachingMultiPersonChatNameDataAccessorDecoratorTest {

    companion object {
        private const val CHAT_ID = "chatId"
        private const val SENDER = "sender"
        private const val ANOTHER_SENDER = "anotherSender"
        private const val EXPECTED_MULTI_SENDER = "anotherSender,sender"
    }

    @Mock
    private lateinit var mockedMultiPersonChatNameDataAccessor: MultiPersonChatNameDataAccessor

    private lateinit var classUnderTest: CachingMultiPersonChatNameDataAccessorDecorator

    @Before
    fun setUp() {
        whenever(mockedMultiPersonChatNameDataAccessor.getAllChatIdToSenders()).thenReturn(HashMultimap.create())
    }

    @After
    fun tearDown() {
    }

    @Test
    fun addRelationshipAndGetChatGroupName_noPreviouslyPersistedSenders() {
        classUnderTest = CachingMultiPersonChatNameDataAccessorDecorator(mockedMultiPersonChatNameDataAccessor)

        val groupName = classUnderTest.addRelationshipAndGetChatGroupName(CHAT_ID, SENDER)

        assertEquals(SENDER, groupName)
        verify(mockedMultiPersonChatNameDataAccessor).getAllChatIdToSenders()
        verify(mockedMultiPersonChatNameDataAccessor).addRelationshipAndGetChatGroupName(CHAT_ID, SENDER)
        verifyNoMoreInteractions(mockedMultiPersonChatNameDataAccessor)
    }

    @Test
    fun addRelationshipAndGetChatGroupName_hasPreviouslyPersistedSameSender() {
        val chatIdToSenderMultimap = HashMultimap.create<String, String>()
        chatIdToSenderMultimap.put(CHAT_ID, SENDER)
        whenever(mockedMultiPersonChatNameDataAccessor.getAllChatIdToSenders()).thenReturn(chatIdToSenderMultimap)
        classUnderTest = CachingMultiPersonChatNameDataAccessorDecorator(mockedMultiPersonChatNameDataAccessor)

        val groupName = classUnderTest.addRelationshipAndGetChatGroupName(CHAT_ID, SENDER)

        assertEquals(SENDER, groupName)
        verify(mockedMultiPersonChatNameDataAccessor).getAllChatIdToSenders()
        verifyNoMoreInteractions(mockedMultiPersonChatNameDataAccessor)
    }

    @Test
    fun addRelationshipAndGetChatGroupName_hasPreviouslyPersistedDifferentSender() {
        val chatIdToSenderMultimap = HashMultimap.create<String, String>()
        chatIdToSenderMultimap.put(CHAT_ID, ANOTHER_SENDER)
        whenever(mockedMultiPersonChatNameDataAccessor.getAllChatIdToSenders()).thenReturn(chatIdToSenderMultimap)
        classUnderTest = CachingMultiPersonChatNameDataAccessorDecorator(mockedMultiPersonChatNameDataAccessor)

        val groupName = classUnderTest.addRelationshipAndGetChatGroupName(CHAT_ID, SENDER)

        assertEquals(EXPECTED_MULTI_SENDER, groupName)
        verify(mockedMultiPersonChatNameDataAccessor).getAllChatIdToSenders()
        verify(mockedMultiPersonChatNameDataAccessor).addRelationshipAndGetChatGroupName(CHAT_ID, SENDER)
        verifyNoMoreInteractions(mockedMultiPersonChatNameDataAccessor)
    }

    @Test
    fun getAllChatIdToSenders() {
        val chatIdToSenderMultimap = HashMultimap.create<String, String>()
        chatIdToSenderMultimap.put(CHAT_ID, SENDER)
        whenever(mockedMultiPersonChatNameDataAccessor.getAllChatIdToSenders()).thenReturn(chatIdToSenderMultimap)
        classUnderTest = CachingMultiPersonChatNameDataAccessorDecorator(mockedMultiPersonChatNameDataAccessor)

        classUnderTest.getAllChatIdToSenders()

        assertEquals(ImmutableMap.of(CHAT_ID, ImmutableSet.of(SENDER)), chatIdToSenderMultimap.asMap())
        verify(mockedMultiPersonChatNameDataAccessor).getAllChatIdToSenders()
        verifyNoMoreInteractions(mockedMultiPersonChatNameDataAccessor)
    }
}
