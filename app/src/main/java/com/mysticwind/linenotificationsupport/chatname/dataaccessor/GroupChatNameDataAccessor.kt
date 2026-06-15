package com.mysticwind.linenotificationsupport.chatname.dataaccessor

import java.util.Optional

interface GroupChatNameDataAccessor {

    fun persistRelationship(chatId: String, chatGroupName: String)

    fun getChatGroupName(chatId: String): Optional<String>

    fun getAllChatGroups(): Map<String, String>

}
