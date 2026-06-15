package com.mysticwind.linenotificationsupport.conversationstarter.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class KeywordEntryTest {

    @Test
    public void blankKeywordBecomesEmptyOptional() {
        KeywordEntry entry = KeywordEntry.builder()
                .chatId("chat-1")
                .chatName("Chat 1")
                .keyword(" ")
                .hasReplyAction(true)
                .build();

        assertFalse(entry.getKeyword().isPresent());
        assertTrue(entry.isHasReplyAction());
    }

    @Test
    public void nonBlankKeywordIsPreserved() {
        KeywordEntry entry = KeywordEntry.builder()
                .chatId("chat-1")
                .chatName("Chat 1")
                .keyword("hello")
                .build();

        assertEquals("hello", entry.getKeyword().get());
    }
}
