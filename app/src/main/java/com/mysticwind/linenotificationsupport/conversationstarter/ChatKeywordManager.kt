package com.mysticwind.linenotificationsupport.conversationstarter

import com.google.common.collect.ImmutableList
import com.mysticwind.linenotificationsupport.chatname.ChatNameManager
import com.mysticwind.linenotificationsupport.conversationstarter.model.KeywordEntry
import org.apache.commons.lang3.StringUtils
import java.util.Objects
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatKeywordManager @Inject constructor(
    private val chatKeywordDao: ChatKeywordDao,
    private val chatNameManager: ChatNameManager,
    private val lineReplyActionDao: LineReplyActionDao
) {
    init {
        Objects.requireNonNull(chatKeywordDao)
        Objects.requireNonNull(chatNameManager)
        Objects.requireNonNull(lineReplyActionDao)
    }

    fun getAvailableKeywordToChatNameMap(): List<KeywordEntry> {
        val keywordEntryList = mutableListOf<KeywordEntry>()
        val keywords = chatKeywordDao.getKeywords()
        for (keyword in keywords) {
            val chatId = chatKeywordDao.getChatId(keyword)
            if (!chatId.isPresent) {
                continue
            }
            val hasReplyAction = lineReplyActionDao.getLineReplyAction(chatId.get()).isPresent
            val chatName = chatNameManager.getChatName(chatId.get())
            if (StringUtils.isNotBlank(chatName)) {
                keywordEntryList.add(
                    KeywordEntry.builder()
                        .chatId(chatId.get())
                        .chatName(chatName)
                        .keyword(keyword)
                        .hasReplyAction(hasReplyAction)
                        .build()
                )
            }
        }

        sortKeywordEntryList(keywordEntryList)

        return ImmutableList.copyOf(keywordEntryList)
    }

    private fun sortKeywordEntryList(keywordEntryList: MutableList<KeywordEntry>) {
        keywordEntryList.sortWith(Comparator.comparing { entry: KeywordEntry -> entry.chatName })
    }

    fun getAllChatsWithConfiguredKeywords(): List<KeywordEntry> {
        val chats = chatNameManager.getAllChats()
        val chatIdToKeywordMap = chatKeywordDao.getAllKeywords()

        val keywordEntries = mutableListOf<KeywordEntry>()
        for (chat in chats) {
            val keyword = chatIdToKeywordMap[chat.id]
            val hasReplyAction = lineReplyActionDao.getLineReplyAction(chat.id).isPresent
            keywordEntries.add(
                KeywordEntry.builder()
                    .chatId(chat.id)
                    .chatName(chat.name)
                    .keyword(keyword)
                    .hasReplyAction(hasReplyAction)
                    .build()
            )
        }
        sortKeywordEntryList(keywordEntries)
        return keywordEntries
    }
}
