package com.mysticwind.linenotificationsupport.conversationstarter.persistence.dto

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "chat_id_keywords", indices = [Index("chat_id")])
class KeywordEntry {

    @PrimaryKey
    @ColumnInfo(name = "chat_id")
    @NonNull
    var chatId: String = ""

    @ColumnInfo(name = "keyword")
    var keyword: String? = null

    @ColumnInfo(name = "created_at")
    var createdAtTimestamp: Long = 0

    @ColumnInfo(name = "updated_at")
    var updatedAtTimestamp: Long = 0

    constructor()

    constructor(
        @NonNull chatId: String,
        keyword: String?,
        createdAtTimestamp: Long,
        updatedAtTimestamp: Long
    ) {
        this.chatId = chatId
        this.keyword = keyword
        this.createdAtTimestamp = createdAtTimestamp
        this.updatedAtTimestamp = updatedAtTimestamp
    }

    class Builder {
        private var chatId: String? = null
        private var keyword: String? = null
        private var createdAtTimestamp: Long = 0
        private var updatedAtTimestamp: Long = 0

        fun chatId(chatId: String?) = apply { this.chatId = chatId }
        fun keyword(keyword: String?) = apply { this.keyword = keyword }
        fun createdAtTimestamp(createdAtTimestamp: Long) = apply { this.createdAtTimestamp = createdAtTimestamp }
        fun updatedAtTimestamp(updatedAtTimestamp: Long) = apply { this.updatedAtTimestamp = updatedAtTimestamp }

        fun build(): KeywordEntry = KeywordEntry(chatId!!, keyword, createdAtTimestamp, updatedAtTimestamp)
    }

    companion object {
        @JvmStatic
        fun builder(): Builder = Builder()
    }
}
