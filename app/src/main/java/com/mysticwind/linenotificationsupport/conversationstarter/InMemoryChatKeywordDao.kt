package com.mysticwind.linenotificationsupport.conversationstarter

import com.google.common.collect.BiMap
import com.google.common.collect.ImmutableBiMap
import java.util.Optional

class InMemoryChatKeywordDao : ChatKeywordDao {

    companion object {
        private val KEYWORD_TO_CHAT_ID_MAP: BiMap<String, String> = ImmutableBiMap.of(
            "寶貝", "caf2eecbb7109578bf0472dfcba4eca9e"
        )
    }

    override fun createOrUpdateKeyword(chatId: String, keyword: String) {
        KEYWORD_TO_CHAT_ID_MAP.put(keyword, chatId)
    }

    override fun getKeywords(): Set<String> = KEYWORD_TO_CHAT_ID_MAP.keys

    override fun getAllKeywords(): Map<String, String> = KEYWORD_TO_CHAT_ID_MAP.inverse()

    override fun getChatId(keyword: String): Optional<String> =
        Optional.ofNullable(KEYWORD_TO_CHAT_ID_MAP[keyword])
}
