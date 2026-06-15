package com.mysticwind.linenotificationsupport.notification.reactor

import android.service.notification.StatusBarNotification

interface IncomingNotificationReactor {
    fun interestedPackages(): Collection<String>
    fun isInterestInNotificationGroup(): Boolean
    fun reactToIncomingNotification(statusBarNotification: StatusBarNotification): Reaction
}
