package com.mysticwind.linenotificationsupport.notification

import com.mysticwind.linenotificationsupport.model.LineNotification
import com.mysticwind.linenotificationsupport.model.LineNotificationBuilder
import java.nio.charset.StandardCharsets
import java.util.UUID

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

        val title = lineNotification.title?.trim().orEmpty()
        val sender = lineNotification.sender?.name?.toString()?.trim().orEmpty()
        val fallbackSeed = listOf(title, sender)
            .filter { it.isNotBlank() }
            .joinToString("|")
        if (fallbackSeed.isBlank()) {
            return null
        }

        val fallbackId = UUID.nameUUIDFromBytes(fallbackSeed.toByteArray(StandardCharsets.UTF_8))
        return LINE_FALLBACK_DISMISSAL_PREFIX + fallbackId
    }
}
