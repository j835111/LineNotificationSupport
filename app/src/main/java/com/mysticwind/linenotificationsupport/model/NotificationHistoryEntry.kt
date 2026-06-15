package com.mysticwind.linenotificationsupport.model

import androidx.core.app.Person
import java.util.Objects
import java.util.Optional

class NotificationHistoryEntry(
    val lineMessageId: String?,
    val message: String?,
    val sender: Person?,
    val timestamp: Long,
    private val lineStickerUrl: String?
) {

    fun getLineStickerUrl(): Optional<String> = Optional.ofNullable(lineStickerUrl)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NotificationHistoryEntry) return false
        return timestamp == other.timestamp
                && Objects.equals(lineMessageId, other.lineMessageId)
                && Objects.equals(message, other.message)
                && Objects.equals(sender, other.sender)
                && Objects.equals(lineStickerUrl, other.lineStickerUrl)
    }

    override fun hashCode(): Int =
        Objects.hash(lineMessageId, message, sender, timestamp, lineStickerUrl)
}
