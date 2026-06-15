package com.mysticwind.linenotificationsupport.chatname

import com.mysticwind.linenotificationsupport.chatname.dataaccessor.GroupChatNameDataAccessor
import com.mysticwind.linenotificationsupport.chatname.dataaccessor.MultiPersonChatNameDataAccessor
import com.mysticwind.linenotificationsupport.chatname.model.Chat
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatNameManager @Inject constructor(
    private val groupChatNameDataAccessor: GroupChatNameDataAccessor,
    private val multiPersonChatNameDataAccessor: MultiPersonChatNameDataAccessor
) {

    fun getChatName(chatId: String): String? {
        return getChatName(chatId, null, null)
    }

    fun getChatName(chatId: String, sender: String?): String? {
        return getChatName(chatId, sender, null)
    }

    fun getChatName(chatId: String, sender: String?, highConfidenceChatGroupName: String?): String? {
        val chatGroupName = groupChatNameDataAccessor.getChatGroupName(chatId)
        if (chatGroupName.isPresent) {
            if (!highConfidenceChatGroupName.isNullOrBlank() &&
                chatGroupName.get() != highConfidenceChatGroupName) {
                groupChatNameDataAccessor.persistRelationship(chatId, highConfidenceChatGroupName)
                return highConfidenceChatGroupName
            }
            if (highConfidenceChatGroupName.isNullOrBlank()) {
                Timber.w("Override with chat room name: " + chatGroupName.get())
            }
            return chatGroupName.get()
        }
        if (!highConfidenceChatGroupName.isNullOrBlank()) {
            groupChatNameDataAccessor.persistRelationship(chatId, highConfidenceChatGroupName)
            return highConfidenceChatGroupName
        }
        val chatName = multiPersonChatNameDataAccessor.addRelationshipAndGetChatGroupName(chatId, sender)
        if (chatName.isBlank()) {
            Timber.w("No chat name returned for chat ID [%s]", chatId)
        }
        return chatName
    }

    fun getAllChats(): Set<Chat> {
        val chatGroups = groupChatNameDataAccessor.getAllChatGroups()
        val chatIdToSenders = multiPersonChatNameDataAccessor.getAllChatIdToSenders()

        val chats = mutableSetOf<Chat>()
        chatGroups.entries.forEach { entry ->
            chats.add(Chat(entry.key, entry.value))
        }

        chatIdToSenders.asMap().entries.forEach { entry ->
            val chatId = entry.key
            if (chatGroups.keys.contains(chatId)) {
                // remove confirmed chat groups
                return@forEach
            }
            // hack to get the merged name
            val chatName = multiPersonChatNameDataAccessor.addRelationshipAndGetChatGroupName(chatId, null)
            // chat name may be null. TODO fix API
            if (!chatName.isNullOrBlank()) {
                chats.add(Chat(chatId, chatName))
            }
        }

        return chats
    }

    fun deleteFriendNameCache() {
        multiPersonChatNameDataAccessor.deleteAllEntries()
    }

}
