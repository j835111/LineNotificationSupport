package com.mysticwind.linenotificationsupport.chatname.dataaccessor;

import com.mysticwind.linenotificationsupport.persistence.chatname.dao.GroupChatNameDao;
import com.mysticwind.linenotificationsupport.persistence.chatname.dto.GroupChatNameEntry;

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
public class RoomGroupChatNameDataAccessorTest {

    private static final String CHAT_ID = "chatId";
    private static final String GROUP_NAME = "groupName";

    @Mock
    private GroupChatNameDao mockedGroupChatNameDao;

    private ExecutorService ioExecutor;
    private RoomGroupChatNameDataAccessor classUnderTest;

    @Before
    public void setUp() throws Exception {
        when(mockedGroupChatNameDao.getAllEntries()).thenReturn(Collections.emptyList());
        when(mockedGroupChatNameDao.getEntry(CHAT_ID)).thenReturn(null);
        ioExecutor = Executors.newSingleThreadExecutor();
        Constructor<RoomGroupChatNameDataAccessor> constructor =
                RoomGroupChatNameDataAccessor.class.getDeclaredConstructor(
                        GroupChatNameDao.class,
                        ExecutorService.class
                );
        constructor.setAccessible(true);
        classUnderTest = constructor.newInstance(mockedGroupChatNameDao, ioExecutor);
    }

    @After
    public void tearDown() throws Exception {
        ioExecutor.shutdownNow();
    }

    @Test
    public void persistRelationship_persistsBeforeReturning() {
        classUnderTest.persistRelationship(CHAT_ID, GROUP_NAME);

        ArgumentCaptor<GroupChatNameEntry> captor = ArgumentCaptor.forClass(GroupChatNameEntry.class);
        verify(mockedGroupChatNameDao).insert(captor.capture());
        assertEquals(CHAT_ID, captor.getValue().getChatId());
        assertEquals(GROUP_NAME, captor.getValue().getChatGroupName());

        Optional<String> groupName = classUnderTest.getChatGroupName(CHAT_ID);
        assertTrue(groupName.isPresent());
        assertEquals(GROUP_NAME, groupName.get());
    }
}
