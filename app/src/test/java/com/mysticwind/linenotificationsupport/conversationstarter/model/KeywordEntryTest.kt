package com.mysticwind.linenotificationsupport.conversationstarter.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KeywordEntryTest {

    @Test
    fun blankKeywordBecomesEmptyOptional() {
        val entry = KeywordEntry.builder()
            .chatId("chat-1")
            .chatName("Chat 1")
            .keyword(" ")
            .hasReplyAction(true)
            .build()

        assertFalse(entry.keyword.isPresent)
        assertTrue(entry.hasReplyAction)
    }

    @Test
    fun nonBlankKeywordIsPreserved() {
        val entry = KeywordEntry.builder()
            .chatId("chat-1")
            .chatName("Chat 1")
            .keyword("hello")
            .build()

        assertEquals("hello", entry.keyword.get())
    }
}
