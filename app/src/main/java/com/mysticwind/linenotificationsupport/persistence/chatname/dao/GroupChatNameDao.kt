package com.mysticwind.linenotificationsupport.persistence.chatname.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mysticwind.linenotificationsupport.persistence.chatname.dto.GroupChatNameEntry

@Dao
interface GroupChatNameDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entry: GroupChatNameEntry)

    @Query("SELECT * FROM group_chat_names WHERE chat_id = :chatId")
    fun getEntry(chatId: String): GroupChatNameEntry

    @Query("SELECT * FROM group_chat_names")
    fun getAllEntries(): List<GroupChatNameEntry>
}
