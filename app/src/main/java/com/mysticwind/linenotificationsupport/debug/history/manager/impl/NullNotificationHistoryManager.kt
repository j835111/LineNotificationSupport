package com.mysticwind.linenotificationsupport.debug.history.manager.impl

import android.service.notification.StatusBarNotification
import androidx.lifecycle.LiveData
import com.mysticwind.linenotificationsupport.debug.history.dto.NotificationHistoryEntry
import com.mysticwind.linenotificationsupport.debug.history.manager.NotificationHistoryManager
import timber.log.Timber

enum class NullNotificationHistoryManager : NotificationHistoryManager {
    INSTANCE;

    override fun record(statusBarNotification: StatusBarNotification, lineVersion: String) {
        Timber.d("Dummy Record: $statusBarNotification")
    }

    override fun getHistory(): LiveData<List<NotificationHistoryEntry>> {
        return androidx.lifecycle.MutableLiveData(emptyList())
    }
}
