package com.mysticwind.linenotificationsupport.conversationstarter

import android.content.Context
import com.mysticwind.linenotificationsupport.conversationstarter.persistence.KeywordRoomDatabase
import com.mysticwind.linenotificationsupport.conversationstarter.persistence.dao.KeywordDao
import com.mysticwind.linenotificationsupport.conversationstarter.persistence.dto.KeywordEntry
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.util.LinkedHashMap
import java.util.LinkedHashSet
import java.util.Optional
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomChatKeywordDao private constructor(
    private val keywordDao: KeywordDao,
    private val ioExecutor: ExecutorService
) : ChatKeywordDao {

    private val chatIdToKeywordMap: MutableMap<String, String> = ConcurrentHashMap()

    @Inject
    constructor(@ApplicationContext context: Context) : this(
        KeywordRoomDatabase.getDatabase(context).keywordDao(),
        Executors.newSingleThreadExecutor()
    )

    init {
        refreshCache()
    }

    override fun createOrUpdateKeyword(chatId: String, keyword: String) {
        val normalizedKeyword = keyword ?: ""
        chatIdToKeywordMap[chatId] = normalizedKeyword
        ioExecutor.execute {
            keywordDao.insert(
                KeywordEntry.builder()
                    .chatId(chatId)
                    .keyword(normalizedKeyword)
                    .createdAtTimestamp(Instant.now().toEpochMilli())
                    // TODO fix the updated timestamp
                    .updatedAtTimestamp(Instant.now().toEpochMilli())
                    .build()
            )
        }
    }

    override fun getKeywords(): Set<String> = LinkedHashSet(chatIdToKeywordMap.values)

    override fun getAllKeywords(): Map<String, String> = LinkedHashMap(chatIdToKeywordMap)

    override fun getChatId(keyword: String): Optional<String> =
        chatIdToKeywordMap.entries
            .firstOrNull { it.value == keyword }
            ?.let { Optional.of(it.key) }
            ?: Optional.empty()

    private fun refreshCache() {
        val loadedEntries = blockingGet(Callable {
            val entries = LinkedHashMap<String, String>()
            for (entry in keywordDao.getAllEntries()) {
                entries[entry.chatId] = entry.keyword ?: ""
            }
            entries
        })
        chatIdToKeywordMap.clear()
        chatIdToKeywordMap.putAll(loadedEntries)
    }

    private fun <T> blockingGet(callable: Callable<T>): T {
        try {
            val future = ioExecutor.submit(callable)
            return future.get()
        } catch (e: Exception) {
            throw IllegalStateException("Failed to access keyword database", e)
        }
    }
}
