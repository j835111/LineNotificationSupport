package com.mysticwind.linenotificationsupport.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mysticwind.linenotificationsupport.persistence.chatname.dao.ChatSenderDao
import com.mysticwind.linenotificationsupport.persistence.chatname.dao.GroupChatNameDao
import com.mysticwind.linenotificationsupport.persistence.chatname.dto.ChatSenderEntry
import com.mysticwind.linenotificationsupport.persistence.chatname.dto.GroupChatNameEntry

@Database(entities = [GroupChatNameEntry::class, ChatSenderEntry::class], version = 1)
abstract class ChatGroupDatabase : RoomDatabase() {

    abstract fun groupChatNameDao(): GroupChatNameDao

    abstract fun chatSenderDao(): ChatSenderDao
}
