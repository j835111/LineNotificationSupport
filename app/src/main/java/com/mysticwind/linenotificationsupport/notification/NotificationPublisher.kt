package com.mysticwind.linenotificationsupport.notification

import android.service.notification.StatusBarNotification
import com.mysticwind.linenotificationsupport.model.LineNotification

interface NotificationPublisher {

    fun publishNotification(lineNotification: LineNotification, notificationId: Int)
    fun republishNotification(lineNotification: LineNotification, notificationId: Int)
    fun updateNotificationDismissed(statusBarNotification: StatusBarNotification)
}
