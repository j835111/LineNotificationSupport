package com.mysticwind.linenotificationsupport.chatname.dataaccessor

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap

class CachingMultiPersonChatNameDataAccessorDecorator(
    private val multiPersonChatNameDataAccessor: MultiPersonChatNameDataAccessor
) : MultiPersonChatNameDataAccessor {

    private val chatIdToSenderMultimap: Multimap<String, String> =
        HashMultimap.create(multiPersonChatNameDataAccessor.getAllChatIdToSenders())

    override fun addRelationshipAndGetChatGroupName(chatId: String, sender: String?): String {
        val senders = chatIdToSenderMultimap.get(chatId)
        if (sender.isNullOrBlank() || senders.contains(sender)) {
            return sortAndMerge(senders)
        }
        chatIdToSenderMultimap.put(chatId, sender)
        // TODO make this async
        multiPersonChatNameDataAccessor.addRelationshipAndGetChatGroupName(chatId, sender)
        // TODO this logic would essentially break the interface contract and should be in upper level class
        return sortAndMerge(chatIdToSenderMultimap.get(chatId))
    }

    private fun sortAndMerge(senders: Collection<String>): String {
        return HashSet(senders)
            .sorted()
            .reduceOrNull { sender1, sender2 -> "$sender1,$sender2" }
            // there should always be at least one sender
            // edge case: client called without sender and returning an empty list due to cleaning of cache
            ?: ""
    }

    override fun getAllChatIdToSenders(): Multimap<String, String> {
        return HashMultimap.create(chatIdToSenderMultimap)
    }

    override fun deleteAllEntries() {
        multiPersonChatNameDataAccessor.deleteAllEntries()
        // clear the map so that new entries will get persisted
        chatIdToSenderMultimap.clear()
    }

}
