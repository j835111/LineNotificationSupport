package com.mysticwind.linenotificationsupport.conversationstarter;

import com.mysticwind.linenotificationsupport.conversationstarter.persistence.dao.KeywordDao;
import com.mysticwind.linenotificationsupport.conversationstarter.persistence.dto.KeywordEntry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RoomChatKeywordDaoTest {

    private static final String CHAT_ID = "chatId";
    private static final String KEYWORD = "keyword";

    @Mock
    private KeywordDao mockedKeywordDao;

    private ExecutorService ioExecutor;
    private RoomChatKeywordDao classUnderTest;

    @Before
    public void setUp() throws Exception {
        when(mockedKeywordDao.getAllEntries()).thenReturn(Collections.emptyList());
        ioExecutor = Executors.newSingleThreadExecutor();
        Constructor<RoomChatKeywordDao> constructor = RoomChatKeywordDao.class.getDeclaredConstructor(
                KeywordDao.class,
                ExecutorService.class
        );
        constructor.setAccessible(true);
        classUnderTest = constructor.newInstance(mockedKeywordDao, ioExecutor);
    }

    @After
    public void tearDown() throws Exception {
        ioExecutor.shutdownNow();
    }

    @Test
    public void createOrUpdateKeyword_persistsBeforeReturning() {
        classUnderTest.createOrUpdateKeyword(CHAT_ID, KEYWORD);

        ArgumentCaptor<KeywordEntry> captor = ArgumentCaptor.forClass(KeywordEntry.class);
        verify(mockedKeywordDao).insert(captor.capture());
        assertEquals(CHAT_ID, captor.getValue().getChatId());
        assertEquals(KEYWORD, captor.getValue().getKeyword());

        Optional<String> chatId = classUnderTest.getChatId(KEYWORD);
        assertTrue(chatId.isPresent());
        assertEquals(CHAT_ID, chatId.get());
    }
}
