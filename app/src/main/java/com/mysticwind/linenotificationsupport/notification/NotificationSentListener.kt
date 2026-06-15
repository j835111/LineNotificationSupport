package com.mysticwind.linenotificationsupport.notification

import com.mysticwind.linenotificationsupport.model.LineNotification

interface NotificationSentListener {

    fun notificationSent(lineNotification: LineNotification, notificationId: Int)
}
