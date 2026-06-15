package com.mysticwind.linenotificationsupport.utils

import android.app.Notification
import com.mysticwind.linenotificationsupport.line.Constants
import com.mysticwind.linenotificationsupport.model.NotificationExtraConstants
import java.util.Optional

object NotificationExtractor {

    @JvmStatic
    fun getTitle(notification: Notification): String? {
        return notification.extras.getString(Notification.EXTRA_TITLE)
    }

    @JvmStatic
    fun getMessage(notification: Notification): String? {
        return notification.extras.getString(Notification.EXTRA_TEXT)
    }

    @JvmStatic
    fun getSubText(notification: Notification): String? {
        // around LINE version 12.2.2, the conversation title has been replaced by subtext
        return notification.extras.getString(Notification.EXTRA_SUB_TEXT)
    }

    @JvmStatic
    fun getConversationTitle(notification: Notification): String? {
        return notification.extras.getString(Notification.EXTRA_CONVERSATION_TITLE)
    }

    @JvmStatic
    fun getLineMessageId(notification: Notification): String? {
        return notification.extras.getString(Constants.LINE_MESSAGE_ID_EXTRA_KEY)
    }

    @JvmStatic
    fun getLineChatId(notification: Notification): String? {
        return notification.extras.getString(Constants.LINE_CHAT_ID_EXTRA_KEY)
    }

    @JvmStatic
    fun getLineNotificationSupportMessageId(notification: Notification): Optional<String> {
        return Optional.ofNullable(notification.extras.getString(NotificationExtraConstants.MESSAGE_ID))
    }

    @JvmStatic
    fun getLineNotificationSupportChatId(notification: Notification): Optional<String> {
        return Optional.ofNullable(notification.extras.getString(NotificationExtraConstants.CHAT_ID))
    }

    @JvmStatic
    fun getLineNotificationSupportStickerUrl(notification: Notification): Optional<String> {
        return Optional.ofNullable(notification.extras.getString(NotificationExtraConstants.STICKER_URL))
    }
}
