package com.mysticwind.linenotificationsupport.notification.reactor

import android.service.notification.StatusBarNotification

interface DismissedNotificationReactor {
    fun interestedPackages(): Collection<String>
    fun isInterestInNotificationGroup(): Boolean
    fun reactToDismissedNotification(statusBarNotification: StatusBarNotification): Reaction
}
