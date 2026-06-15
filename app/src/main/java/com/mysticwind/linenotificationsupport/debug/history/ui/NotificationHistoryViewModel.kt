package com.mysticwind.linenotificationsupport.debug.history.ui

import android.app.Application
import androidx.annotation.NonNull
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.mysticwind.linenotificationsupport.debug.history.dto.NotificationHistoryEntry
import com.mysticwind.linenotificationsupport.debug.history.manager.impl.RoomNotificationHistoryManager
import com.mysticwind.linenotificationsupport.persistence.AppDatabase
import com.mysticwind.linenotificationsupport.utils.StatusBarNotificationPrinter

class NotificationHistoryViewModel(@NonNull application: Application) : AndroidViewModel(application) {

    companion object {
        private val NOTIFICATION_PRINTER = StatusBarNotificationPrinter()
    }

    private val notificationHistoryManager: RoomNotificationHistoryManager
    val notificationHistory: LiveData<List<NotificationHistoryEntry>>

    init {
        val appDatabase = Room.databaseBuilder(
            application.applicationContext,
            AppDatabase::class.java,
            "database"
        ).build()

        notificationHistoryManager = RoomNotificationHistoryManager(appDatabase, NOTIFICATION_PRINTER)
        notificationHistory = notificationHistoryManager.getHistory()
    }


}
