package com.mysticwind.linenotificationsupport.notification

interface NotificationCounter : SlotAvailabilityChecker {

    fun notified(group: String, notificationId: Int): Int
    fun dismissed(group: String, notificationId: Int): Int
}
