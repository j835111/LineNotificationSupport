package com.mysticwind.linenotificationsupport.notification

import com.mysticwind.linenotificationsupport.model.LineNotification
import com.mysticwind.linenotificationsupport.model.LineNotificationBuilder

object WearableDismissalMetadata {

    private const val LINE_DISMISSAL_PREFIX = "line-dismissal-chat-"
    private const val LINE_FALLBACK_DISMISSAL_PREFIX = "line-dismissal-fallback-"

    @JvmStatic
    fun buildDismissalId(lineNotification: LineNotification): String? {
        if (lineNotification.callState != null) {
            return null
        }

        val chatId = lineNotification.chatId
        if (!chatId.isNullOrBlank()
            && chatId != LineNotificationBuilder.DEFAULT_CHAT_ID
            && chatId != LineNotificationBuilder.CALL_VIRTUAL_CHAT_ID
        ) {
            return LINE_DISMISSAL_PREFIX + chatId
        }

        val fallbackSeed = buildFallbackIdSeed(lineNotification) ?: return null
        return LINE_FALLBACK_DISMISSAL_PREFIX + seedToUUID(fallbackSeed)
    }
}
