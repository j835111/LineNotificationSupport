package com.mysticwind.linenotificationsupport.chatname.dataaccessor

import com.google.common.collect.Multimap

interface MultiPersonChatNameDataAccessor {

    fun addRelationshipAndGetChatGroupName(chatId: String, sender: String?): String

    fun getAllChatIdToSenders(): Multimap<String, String>

    fun deleteAllEntries()

}
