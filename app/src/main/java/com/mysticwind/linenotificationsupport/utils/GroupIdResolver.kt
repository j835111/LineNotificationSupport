package com.mysticwind.linenotificationsupport.utils

import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupIdResolver @Inject constructor() {

    companion object {
        private const val GROUP_ID_START = 0x4000
    }

    private val groupIdToChatIdMap: MutableMap<Int, String> = ConcurrentHashMap()
    private val fallbackChatIdToGroupIdMap: MutableMap<String, Int> = ConcurrentHashMap()
    private var lastGroupId = GROUP_ID_START

    constructor(lastGroupId: Int) : this() {
        this.lastGroupId = lastGroupId
    }

    fun resolveGroupId(chatId: String): Int {
        val calculatedGroupId = chatId.hashCode()
        val storedChatId = groupIdToChatIdMap[calculatedGroupId]
        return when {
            storedChatId == null -> {
                groupIdToChatIdMap[calculatedGroupId] = chatId
                calculatedGroupId
            }
            storedChatId == chatId -> calculatedGroupId
            else -> {
                // fallback that should almost never happen
                val selectedGroupId = fallbackChatIdToGroupIdMap.computeIfAbsent(chatId) { getNextLastGroupId() }
                // TODO what if there is a clash with the group IDs and the hash codes?
                Timber.w("Chat ID [%s] hash [%d] has been used, using group ID [%d] instead",
                    chatId, calculatedGroupId, selectedGroupId)
                selectedGroupId
            }
        }
    }

    @Synchronized
    private fun getNextLastGroupId(): Int {
        return lastGroupId++
    }
}
