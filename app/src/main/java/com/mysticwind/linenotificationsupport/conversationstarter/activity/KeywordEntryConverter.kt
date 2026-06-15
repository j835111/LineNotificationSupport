package com.mysticwind.linenotificationsupport.conversationstarter.activity

import com.google.common.base.Converter
import com.mysticwind.linenotificationsupport.conversationstarter.model.KeywordEntry

class KeywordEntryConverter : Converter<KeywordEntry, MutableKeywordEntry>() {

    override fun doForward(keywordEntry: KeywordEntry): MutableKeywordEntry =
        MutableKeywordEntry.builder()
            .chatId(keywordEntry.chatId)
            .chatName(keywordEntry.chatName)
            .keyword(keywordEntry.keyword.orElse(null))
            .hasReplyAction(keywordEntry.hasReplyAction)
            .build()

    override fun doBackward(mutableKeywordEntry: MutableKeywordEntry): KeywordEntry =
        KeywordEntry.builder()
            .chatId(mutableKeywordEntry.chatId)
            .chatName(mutableKeywordEntry.chatName)
            .keyword(mutableKeywordEntry.keyword)
            .hasReplyAction(mutableKeywordEntry.hasReplyAction)
            .build()
}
