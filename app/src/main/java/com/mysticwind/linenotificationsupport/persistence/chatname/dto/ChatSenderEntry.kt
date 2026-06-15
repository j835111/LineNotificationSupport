package com.mysticwind.linenotificationsupport.persistence.chatname.dto

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "chat_senders", indices = [Index("chat_id")])
data class ChatSenderEntry(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,

    @ColumnInfo(name = "chat_id")
    var chatId: String? = null,

    @ColumnInfo(name = "sender")
    var sender: String? = null,

    @ColumnInfo(name = "created_at")
    var createdAtTimestamp: Long = 0,

    @ColumnInfo(name = "updated_at")
    var updatedAtTimestamp: Long = 0
)
