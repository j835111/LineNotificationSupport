package com.mysticwind.linenotificationsupport.notification

import android.service.notification.StatusBarNotification

interface AndroidNotificationManager {

    fun getNotificationsOfPackage(packageName: String): List<StatusBarNotification>
    fun getOrderedLineNotificationSupportNotificationsOfChatId(chatId: String, filterStrategy: Int): List<StatusBarNotification>
    fun getOrderedLineNotificationSupportNotifications(group: String, filterStrategy: Int): List<StatusBarNotification>
    fun cancelNotificationById(notificationId: Int)
    fun cancelNotification(chatId: String)
    fun cancelNotificationOfPackage(key: String)
    fun clearRemoteInputNotificationSpinner(chatId: String)
}
