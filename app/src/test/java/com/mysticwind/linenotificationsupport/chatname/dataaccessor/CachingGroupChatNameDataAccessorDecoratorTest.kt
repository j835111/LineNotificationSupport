package com.mysticwind.linenotificationsupport.chatname.dataaccessor

import com.google.common.collect.ImmutableMap
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import java.util.Collections
import java.util.Optional

@RunWith(MockitoJUnitRunner::class)
class CachingGroupChatNameDataAccessorDecoratorTest {

    companion object {
        private const val CHAT_ID = "chatId"
        private const val GROUP_NAME = "groupName"
    }

    @Mock
    private lateinit var mockedGroupChatNameDataAccessor: GroupChatNameDataAccessor

    private lateinit var classUnderTest: CachingGroupChatNameDataAccessorDecorator

    @Before
    fun setUp() {
        whenever(mockedGroupChatNameDataAccessor.getAllChatGroups()).thenReturn(Collections.emptyMap())
    }

    @After
    fun tearDown() {
    }

    @Test
    fun persistRelationship() {
        classUnderTest = CachingGroupChatNameDataAccessorDecorator(mockedGroupChatNameDataAccessor)

        classUnderTest.persistRelationship(CHAT_ID, GROUP_NAME)

        verify(mockedGroupChatNameDataAccessor).getAllChatGroups()
        verify(mockedGroupChatNameDataAccessor).persistRelationship(CHAT_ID, GROUP_NAME)
    }

    @Test
    fun getChatGroupName_noPreviousPersistedChatGroup() {
        classUnderTest = CachingGroupChatNameDataAccessorDecorator(mockedGroupChatNameDataAccessor)

        val groupName = classUnderTest.getChatGroupName(CHAT_ID)

        assertFalse(groupName.isPresent)
        verify(mockedGroupChatNameDataAccessor).getAllChatGroups()
        verifyNoMoreInteractions(mockedGroupChatNameDataAccessor)
    }

    @Test
    fun getChatGroupName_hasPreviousPersistedChatGroup() {
        whenever(mockedGroupChatNameDataAccessor.getAllChatGroups()).thenReturn(ImmutableMap.of(CHAT_ID, GROUP_NAME))
        classUnderTest = CachingGroupChatNameDataAccessorDecorator(mockedGroupChatNameDataAccessor)

        val groupName: Optional<String> = classUnderTest.getChatGroupName(CHAT_ID)

        assertTrue(groupName.isPresent)
        assertEquals(GROUP_NAME, groupName.get())
        verify(mockedGroupChatNameDataAccessor).getAllChatGroups()
        verifyNoMoreInteractions(mockedGroupChatNameDataAccessor)
    }

    @Test
    fun getAllChatGroups_noPreviousPersistedChatGroup() {
        classUnderTest = CachingGroupChatNameDataAccessorDecorator(mockedGroupChatNameDataAccessor)

        val chatGroups = classUnderTest.getAllChatGroups()

        assertTrue(chatGroups.isEmpty())
        verify(mockedGroupChatNameDataAccessor).getAllChatGroups()
        verifyNoMoreInteractions(mockedGroupChatNameDataAccessor)
    }

    @Test
    fun getAllChatGroups() {
        whenever(mockedGroupChatNameDataAccessor.getAllChatGroups()).thenReturn(ImmutableMap.of(CHAT_ID, GROUP_NAME))
        classUnderTest = CachingGroupChatNameDataAccessorDecorator(mockedGroupChatNameDataAccessor)

        val chatGroups = classUnderTest.getAllChatGroups()

        assertEquals(ImmutableMap.of(CHAT_ID, GROUP_NAME), chatGroups)
        verify(mockedGroupChatNameDataAccessor).getAllChatGroups()
        verifyNoMoreInteractions(mockedGroupChatNameDataAccessor)
    }
}
