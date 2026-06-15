package com.mysticwind.linenotificationsupport.utils

import android.app.Notification
import com.mysticwind.linenotificationsupport.model.NotificationExtraConstants
import java.util.Optional

object LineNotificationSupportMessageExtractor {

    @JvmStatic
    fun getMessageId(message: Notification.MessagingStyle.Message): Optional<String> {
        return Optional.ofNullable(message.extras.getString(NotificationExtraConstants.MESSAGE_ID))
    }

    @JvmStatic
    fun getChatId(message: Notification.MessagingStyle.Message): Optional<String> {
        return Optional.ofNullable(message.extras.getString(NotificationExtraConstants.CHAT_ID))
    }

    @JvmStatic
    fun getStickerUrl(message: Notification.MessagingStyle.Message): Optional<String> {
        return Optional.ofNullable(message.extras.getString(NotificationExtraConstants.STICKER_URL))
    }

    @JvmStatic
    fun getSender(message: Notification.MessagingStyle.Message): Optional<String> {
        return Optional.ofNullable(message.extras.getString(NotificationExtraConstants.SENDER_NAME))
    }
}
