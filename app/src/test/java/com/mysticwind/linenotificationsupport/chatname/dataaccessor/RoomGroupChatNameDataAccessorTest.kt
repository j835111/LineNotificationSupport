package com.mysticwind.linenotificationsupport.chatname.dataaccessor

import com.mysticwind.linenotificationsupport.persistence.chatname.dao.GroupChatNameDao
import com.mysticwind.linenotificationsupport.persistence.chatname.dto.GroupChatNameEntry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever
import java.util.Collections
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@RunWith(MockitoJUnitRunner::class)
class RoomGroupChatNameDataAccessorTest {

    companion object {
        private const val CHAT_ID = "chatId"
        private const val GROUP_NAME = "groupName"
    }

    @Mock
    private lateinit var mockedGroupChatNameDao: GroupChatNameDao

    private lateinit var ioExecutor: ExecutorService
    private lateinit var classUnderTest: RoomGroupChatNameDataAccessor

    @Before
    fun setUp() {
        whenever(mockedGroupChatNameDao.getAllEntries()).thenReturn(Collections.emptyList())
        whenever(mockedGroupChatNameDao.getEntry(CHAT_ID)).thenReturn(null)
        ioExecutor = Executors.newSingleThreadExecutor()
        val constructor = RoomGroupChatNameDataAccessor::class.java.getDeclaredConstructor(
            GroupChatNameDao::class.java,
            ExecutorService::class.java
        )
        constructor.isAccessible = true
        classUnderTest = constructor.newInstance(mockedGroupChatNameDao, ioExecutor)
    }

    @After
    fun tearDown() {
        ioExecutor.shutdownNow()
    }

    @Test
    fun persistRelationship_persistsBeforeReturning() {
        classUnderTest.persistRelationship(CHAT_ID, GROUP_NAME)

        val captor = argumentCaptor<GroupChatNameEntry>()
        verify(mockedGroupChatNameDao).insert(captor.capture())
        assertEquals(CHAT_ID, captor.firstValue.chatId)
        assertEquals(GROUP_NAME, captor.firstValue.chatGroupName)

        val groupName = classUnderTest.getChatGroupName(CHAT_ID)
        assertTrue(groupName.isPresent)
        assertEquals(GROUP_NAME, groupName.get())
    }
}
