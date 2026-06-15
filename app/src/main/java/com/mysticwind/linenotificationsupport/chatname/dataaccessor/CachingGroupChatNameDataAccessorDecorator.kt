package com.mysticwind.linenotificationsupport.chatname.dataaccessor

import com.google.common.collect.ImmutableMap
import java.util.Optional

class CachingGroupChatNameDataAccessorDecorator(
    private val groupChatNameDataAccessor: GroupChatNameDataAccessor
) : GroupChatNameDataAccessor {

    private val chatIdToGroupChatNameMap: MutableMap<String, String> =
        HashMap(groupChatNameDataAccessor.getAllChatGroups())

    override fun persistRelationship(chatId: String, chatGroupName: String) {
        require(chatId.isNotBlank()) { "chatId must not be blank" }
        require(chatGroupName.isNotBlank()) { "chatGroupName must not be blank" }

        chatIdToGroupChatNameMap[chatId] = chatGroupName
        // TODO optimization to make this async
        groupChatNameDataAccessor.persistRelationship(chatId, chatGroupName)
    }

    override fun getChatGroupName(chatId: String): Optional<String> {
        require(chatId.isNotBlank()) { "chatId must not be blank" }

        return Optional.ofNullable(chatIdToGroupChatNameMap[chatId])
    }

    override fun getAllChatGroups(): Map<String, String> {
        return ImmutableMap.copyOf(chatIdToGroupChatNameMap)
    }

}
