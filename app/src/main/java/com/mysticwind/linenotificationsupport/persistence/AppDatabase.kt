package com.mysticwind.linenotificationsupport.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mysticwind.linenotificationsupport.debug.history.dao.LineNotificationHistoryDao
import com.mysticwind.linenotificationsupport.debug.history.dto.NotificationHistoryEntry

@Database(entities = [NotificationHistoryEntry::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun lineNotificationHistoryDao(): LineNotificationHistoryDao
}
