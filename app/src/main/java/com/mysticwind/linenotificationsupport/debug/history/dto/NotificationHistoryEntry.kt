package com.mysticwind.linenotificationsupport.debug.history.dto

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notification_history",
    indices = [Index("record_date_time")]
)
data class NotificationHistoryEntry(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    @ColumnInfo(name = "record_date_time") var recordDateTime: String? = null,
    @ColumnInfo(name = "line_version") var lineVersion: String? = null,
    @ColumnInfo(name = "notification") var notification: String? = null
) {
    companion object {
        @JvmStatic
        fun builder(): Builder = Builder()
    }

    class Builder {
        private var id: Long = 0
        private var recordDateTime: String? = null
        private var lineVersion: String? = null
        private var notification: String? = null

        fun id(id: Long) = apply { this.id = id }
        fun recordDateTime(recordDateTime: String?) = apply { this.recordDateTime = recordDateTime }
        fun lineVersion(lineVersion: String?) = apply { this.lineVersion = lineVersion }
        fun notification(notification: String?) = apply { this.notification = notification }

        fun build(): NotificationHistoryEntry = NotificationHistoryEntry(id, recordDateTime, lineVersion, notification)
    }
}
