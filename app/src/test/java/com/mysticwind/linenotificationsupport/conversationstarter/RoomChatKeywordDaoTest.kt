package com.mysticwind.linenotificationsupport.conversationstarter

import com.mysticwind.linenotificationsupport.conversationstarter.persistence.dao.KeywordDao
import com.mysticwind.linenotificationsupport.conversationstarter.persistence.dto.KeywordEntry
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
class RoomChatKeywordDaoTest {

    companion object {
        private const val CHAT_ID = "chatId"
        private const val KEYWORD = "keyword"
    }

    @Mock
    private lateinit var mockedKeywordDao: KeywordDao

    private lateinit var ioExecutor: ExecutorService
    private lateinit var classUnderTest: RoomChatKeywordDao

    @Before
    fun setUp() {
        whenever(mockedKeywordDao.getAllEntries()).thenReturn(Collections.emptyList())
        ioExecutor = Executors.newSingleThreadExecutor()
        val constructor = RoomChatKeywordDao::class.java.getDeclaredConstructor(
            KeywordDao::class.java,
            ExecutorService::class.java
        )
        constructor.isAccessible = true
        classUnderTest = constructor.newInstance(mockedKeywordDao, ioExecutor)
    }

    @After
    fun tearDown() {
        ioExecutor.shutdownNow()
    }

    @Test
    fun createOrUpdateKeyword_persistsBeforeReturning() {
        classUnderTest.createOrUpdateKeyword(CHAT_ID, KEYWORD)

        val captor = argumentCaptor<KeywordEntry>()
        verify(mockedKeywordDao).insert(captor.capture())
        assertEquals(CHAT_ID, captor.firstValue.chatId)
        assertEquals(KEYWORD, captor.firstValue.keyword)

        val chatId = classUnderTest.getChatId(KEYWORD)
        assertTrue(chatId.isPresent)
        assertEquals(CHAT_ID, chatId.get())
    }
}
