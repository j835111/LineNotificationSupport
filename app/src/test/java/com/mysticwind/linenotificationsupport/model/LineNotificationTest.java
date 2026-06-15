package com.mysticwind.linenotificationsupport.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.app.Notification;

import androidx.core.app.Person;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Collections;

import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.Silent.class)
public class LineNotificationTest {

    private static final String TITLE = "title";
    private static final String MESSAGE = "message";
    private static final String CHAT_ID = "chatId";

    @Test
    public void testSelfResponse() {
        LineNotification lineNotification = LineNotification.builder()
                .lineMessageId(String.valueOf(Instant.now().toEpochMilli())) // just generate a fake one
                .title(TITLE)
                .message(MESSAGE)
                .sender(new Person.Builder().setName("You").build()) // TODO localization
                .chatId(CHAT_ID)
                .timestamp(Instant.now().toEpochMilli())
                .isSelfResponse(true)
                .build();

        assertTrue(lineNotification.isSelfResponse());
    }

    @Test
    public void testDefaults() {
        LineNotification lineNotification = LineNotification.builder().build();

        assertEquals(Collections.emptyList(), lineNotification.getMessages());
        assertEquals(Collections.emptyList(), lineNotification.getHistory());
        assertEquals(Collections.emptyList(), lineNotification.getActions());
        assertFalse(lineNotification.getClickIntent().isPresent());
        assertNull(lineNotification.getMessage());
    }

    @Test
    public void testToBuilderCopiesValuesAndSupportsSingularActions() {
        Notification.Action action1 = mock(Notification.Action.class);
        Notification.Action action2 = mock(Notification.Action.class);

        LineNotification original = LineNotification.builder()
                .title(TITLE)
                .message(MESSAGE)
                .chatId(CHAT_ID)
                .action(action1)
                .build();

        LineNotification updated = original.toBuilder()
                .message("updated")
                .action(action2)
                .build();

        assertEquals(MESSAGE, original.getMessage());
        assertEquals("updated", updated.getMessage());
        assertEquals(1, original.getActions().size());
        assertEquals(2, updated.getActions().size());
        assertNotSame(original.getActions(), updated.getActions());
    }

}
