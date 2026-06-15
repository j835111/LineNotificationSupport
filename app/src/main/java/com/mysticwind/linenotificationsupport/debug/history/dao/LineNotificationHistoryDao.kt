package com.mysticwind.linenotificationsupport.debug.history.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mysticwind.linenotificationsupport.debug.history.dto.NotificationHistoryEntry

@Dao
interface LineNotificationHistoryDao {

    @Query("SELECT * FROM notification_history ORDER BY record_date_time DESC")
    fun getAllEntries(): LiveData<List<NotificationHistoryEntry>>

    @Query("SELECT * FROM notification_history WHERE record_date_time BETWEEN :recordDateTimeStart AND :recordDateTimeEnd ORDER BY record_date_time DESC")
    fun getEntriesBetweenRecordDateTimes(recordDateTimeStart: String, recordDateTimeEnd: String): List<NotificationHistoryEntry>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(entry: NotificationHistoryEntry): Long

    @Delete
    fun delete(entry: NotificationHistoryEntry)

    @Query("DELETE FROM notification_history")
    fun deleteAllEntries()
}
