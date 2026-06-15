package com.mysticwind.linenotificationsupport.notification

import android.service.notification.StatusBarNotification
import com.mysticwind.linenotificationsupport.model.LineNotification

enum class NullNotificationPublisher : NotificationPublisher {

    INSTANCE;

    override fun publishNotification(lineNotification: LineNotification, notificationId: Int) {
        // do nothing
    }

    override fun republishNotification(lineNotification: LineNotification, notificationId: Int) {
        // do nothing
    }

    override fun updateNotificationDismissed(statusBarNotification: StatusBarNotification) {
        // do nothing
    }
}
