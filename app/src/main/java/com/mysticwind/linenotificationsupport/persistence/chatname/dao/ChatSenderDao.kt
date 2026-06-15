package com.mysticwind.linenotificationsupport.persistence.chatname.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mysticwind.linenotificationsupport.persistence.chatname.dto.ChatSenderEntry

@Dao
interface ChatSenderDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(entry: ChatSenderEntry)

    @Query("SELECT * FROM chat_senders")
    fun getAllEntries(): List<ChatSenderEntry>

    @Query("DELETE FROM chat_senders")
    fun deleteAllEntries()
}
