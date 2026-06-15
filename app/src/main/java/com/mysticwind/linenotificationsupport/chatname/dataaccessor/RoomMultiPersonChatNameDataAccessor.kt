package com.mysticwind.linenotificationsupport.chatname.dataaccessor

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import com.mysticwind.linenotificationsupport.persistence.ChatGroupDatabase
import com.mysticwind.linenotificationsupport.persistence.chatname.dto.ChatSenderEntry
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomMultiPersonChatNameDataAccessor @Inject constructor(
    private val chatGroupDatabase: ChatGroupDatabase
) : MultiPersonChatNameDataAccessor {

    override fun addRelationshipAndGetChatGroupName(chatId: String, sender: String?): String {
        require(!chatId.isNullOrBlank()) { "chatId must not be blank" }
        require(!sender.isNullOrBlank()) { "sender must not be blank" }

        val entry = ChatSenderEntry()
        entry.chatId = chatId
        entry.sender = sender
        entry.createdAtTimestamp = Instant.now().toEpochMilli()
        entry.updatedAtTimestamp = Instant.now().toEpochMilli()
        chatGroupDatabase.chatSenderDao().insert(entry)

        return sender
    }

    override fun getAllChatIdToSenders(): Multimap<String, String> {
        val chatIdToSenderMultimap: Multimap<String, String> = HashMultimap.create()
        chatGroupDatabase.chatSenderDao().getAllEntries().forEach { entry ->
            chatIdToSenderMultimap.put(entry.chatId, entry.sender)
        }
        return chatIdToSenderMultimap
    }

    override fun deleteAllEntries() {
        chatGroupDatabase.chatSenderDao().deleteAllEntries()
    }

}
