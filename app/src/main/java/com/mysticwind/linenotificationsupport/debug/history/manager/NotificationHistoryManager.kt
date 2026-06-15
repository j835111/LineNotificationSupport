package com.mysticwind.linenotificationsupport.debug.history.manager

import android.service.notification.StatusBarNotification
import androidx.lifecycle.LiveData
import com.mysticwind.linenotificationsupport.debug.history.dto.NotificationHistoryEntry

interface NotificationHistoryManager {
    fun record(statusBarNotification: StatusBarNotification, lineVersion: String)
    fun getHistory(): LiveData<List<NotificationHistoryEntry>>
}
