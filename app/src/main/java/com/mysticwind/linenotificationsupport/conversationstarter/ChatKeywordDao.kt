package com.mysticwind.linenotificationsupport.conversationstarter

import java.util.Optional

interface ChatKeywordDao {

    fun createOrUpdateKeyword(chatId: String, keyword: String)

    fun getKeywords(): Set<String>

    /**
     * @return chatId to keyword map
     */
    fun getAllKeywords(): Map<String, String>

    fun getChatId(keyword: String): Optional<String>
}
