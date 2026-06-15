package com.mysticwind.linenotificationsupport.chatname.dataaccessor

import com.mysticwind.linenotificationsupport.persistence.ChatGroupDatabase
import com.mysticwind.linenotificationsupport.persistence.chatname.dao.GroupChatNameDao
import com.mysticwind.linenotificationsupport.persistence.chatname.dto.GroupChatNameEntry
import timber.log.Timber
import java.time.Instant
import java.util.LinkedHashMap
import java.util.Optional
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomGroupChatNameDataAccessor(
    private val groupChatNameDao: GroupChatNameDao,
    private val ioExecutor: ExecutorService
) : GroupChatNameDataAccessor {

    private val chatIdToGroupNameMap: MutableMap<String, String> = LinkedHashMap()

    @Inject
    constructor(chatGroupDatabase: ChatGroupDatabase) :
            this(chatGroupDatabase.groupChatNameDao(), Executors.newSingleThreadExecutor())

    init {
        refreshCache()
    }

    override fun persistRelationship(chatId: String, chatGroupName: String) {
        blockingGet(Callable {
            persistRelationshipOnIo(chatId, chatGroupName)
            null
        })
        synchronized(chatIdToGroupNameMap) {
            chatIdToGroupNameMap[chatId] = chatGroupName
        }
    }

    private fun persistRelationshipOnIo(chatId: String, chatGroupName: String) {
        var entry: GroupChatNameEntry? = groupChatNameDao.getEntry(chatId)
        if (entry == null) {
            entry = GroupChatNameEntry()
            entry.chatId = chatId
            entry.chatGroupName = chatGroupName
            entry.createdAtTimestamp = Instant.now().toEpochMilli()
            entry.updatedAtTimestamp = Instant.now().toEpochMilli()
        } else if (entry.chatGroupName == chatGroupName) {
            return
        } else {
            entry.chatGroupName = chatGroupName
            entry.updatedAtTimestamp = Instant.now().toEpochMilli()
        }
        groupChatNameDao.insert(entry)
        Timber.i("Persisted entry with chat ID [%s] chat group name [%s] ", chatId, chatGroupName)
    }

    override fun getChatGroupName(chatId: String): Optional<String> {
        synchronized(chatIdToGroupNameMap) {
            return Optional.ofNullable(chatIdToGroupNameMap[chatId])
        }
    }

    override fun getAllChatGroups(): Map<String, String> {
        synchronized(chatIdToGroupNameMap) {
            return LinkedHashMap(chatIdToGroupNameMap)
        }
    }

    private fun refreshCache() {
        val loadedEntries = blockingGet(Callable {
            val entries = LinkedHashMap<String, String>()
            for (entry in groupChatNameDao.getAllEntries()) {
                entries[entry.chatId] = entry.chatGroupName ?: ""
            }
            entries
        })
        synchronized(chatIdToGroupNameMap) {
            chatIdToGroupNameMap.clear()
            chatIdToGroupNameMap.putAll(loadedEntries)
        }
    }

    private fun <T> blockingGet(callable: Callable<T>): T {
        return try {
            val future = ioExecutor.submit(callable)
            future.get()
        } catch (e: Exception) {
            throw IllegalStateException("Failed to access group chat database", e)
        }
    }

}
