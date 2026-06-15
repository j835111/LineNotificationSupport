package com.mysticwind.linenotificationsupport.utils

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationIdGenerator @Inject constructor() {

    companion object {
        private const val MESSAGE_ID_START = 0x1000
        private var lastMessageId = MESSAGE_ID_START
    }

    @Synchronized
    fun getNextNotificationId(): Int {
        return ++lastMessageId
    }
}
