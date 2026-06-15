package com.mysticwind.linenotificationsupport.reply

import android.app.Notification

interface ReplyActionBuilder {
    fun buildReplyAction(chatId: String, originalLineReplyAction: Notification.Action): Notification.Action
}
