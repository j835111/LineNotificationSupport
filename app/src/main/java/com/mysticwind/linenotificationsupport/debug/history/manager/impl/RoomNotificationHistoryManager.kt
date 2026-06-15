package com.mysticwind.linenotificationsupport.debug.history.manager.impl

import android.os.AsyncTask
import android.service.notification.StatusBarNotification
import androidx.lifecycle.LiveData
import com.mysticwind.linenotificationsupport.debug.history.dto.NotificationHistoryEntry
import com.mysticwind.linenotificationsupport.debug.history.manager.NotificationHistoryManager
import com.mysticwind.linenotificationsupport.persistence.AppDatabase
import com.mysticwind.linenotificationsupport.utils.StatusBarNotificationPrinter
import timber.log.Timber
import java.time.Instant

class RoomNotificationHistoryManager(
    private val appDatabase: AppDatabase,
    private val statusBarNotificationPrinter: StatusBarNotificationPrinter
) : NotificationHistoryManager {

    override fun record(statusBarNotification: StatusBarNotification, lineVersion: String) {
        val entry = buildEntry(statusBarNotification, lineVersion)

        object : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg voids: Void?): Void? {
                try {
                    val id = appDatabase.lineNotificationHistoryDao().insert(entry)
                    entry.id = id
                    Timber.i("Recorded entry with ID: " + entry.id)
                } catch (e: Exception) {
                    Timber.e(e, "Error recording notification: " + e.message)
                }
                return null
            }
        }.execute()
    }

    private fun buildEntry(notification: StatusBarNotification, lineVersion: String): NotificationHistoryEntry {
        return NotificationHistoryEntry.builder()
            .notification(statusBarNotificationPrinter.toString(notification))
            .lineVersion(lineVersion)
            .recordDateTime(Instant.now().toString())
            .build()
    }

    override fun getHistory(): LiveData<List<NotificationHistoryEntry>> {
        return appDatabase.lineNotificationHistoryDao().getAllEntries()
    }
}
