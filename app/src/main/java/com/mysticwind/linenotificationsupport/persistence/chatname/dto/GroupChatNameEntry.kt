package com.mysticwind.linenotificationsupport.persistence.chatname.dto

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "group_chat_names", indices = [Index("chat_id")])
data class GroupChatNameEntry(
    @PrimaryKey
    @ColumnInfo(name = "chat_id")
    @NonNull
    var chatId: String = "",

    @ColumnInfo(name = "chat_group_name")
    var chatGroupName: String? = null,

    @ColumnInfo(name = "created_at")
    var createdAtTimestamp: Long = 0,

    @ColumnInfo(name = "updated_at")
    var updatedAtTimestamp: Long = 0
)
