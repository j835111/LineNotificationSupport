package com.mysticwind.linenotificationsupport.model

import android.app.Notification
import android.app.PendingIntent
import android.graphics.Bitmap
import androidx.core.app.Person
import java.util.Objects

class LineNotification private constructor(builder: LineNotificationDefaultValueBuilder) {

    enum class CallState {
        INCOMING,
        IN_A_CALL,
        MISSED_CALL,
    }

    val sender: Person? = builder.sender
    val message: String? = builder.message
    val messages: List<String> = builder.messages?.toList() ?: emptyList()
    val title: String? = builder.title
    val lineMessageId: String? = builder.lineMessageId
    val lineStickerUrl: String? = builder.lineStickerUrl
    val chatId: String? = builder.chatId
    val callState: CallState? = builder.callState
    @get:JvmName("getClickIntentNullable") val clickIntent: PendingIntent? = builder.clickIntent
    val timestamp: Long = builder.timestamp
    val actions: List<Notification.Action> = builder.actions?.toList() ?: emptyList()
    val icon: Bitmap? = builder.icon
    val history: List<NotificationHistoryEntry> = builder.history?.toList() ?: emptyList()
    val isSelfResponse: Boolean = builder.isSelfResponse

    fun getClickIntent(): java.util.Optional<PendingIntent> = java.util.Optional.ofNullable(clickIntent)

    fun toBuilder(): LineNotificationDefaultValueBuilder = LineNotificationDefaultValueBuilder(this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LineNotification) return false
        return timestamp == other.timestamp
                && isSelfResponse == other.isSelfResponse
                && Objects.equals(sender, other.sender)
                && Objects.equals(message, other.message)
                && Objects.equals(messages, other.messages)
                && Objects.equals(title, other.title)
                && Objects.equals(lineMessageId, other.lineMessageId)
                && Objects.equals(lineStickerUrl, other.lineStickerUrl)
                && Objects.equals(chatId, other.chatId)
                && callState == other.callState
                && Objects.equals(clickIntent, other.clickIntent)
                && Objects.equals(actions, other.actions)
                && Objects.equals(icon, other.icon)
                && Objects.equals(history, other.history)
    }

    override fun hashCode(): Int = Objects.hash(
        sender, message, messages, title, lineMessageId, lineStickerUrl, chatId,
        callState, clickIntent, timestamp, actions, icon, history, isSelfResponse
    )

    companion object {
        @JvmStatic
        fun builder(): LineNotificationDefaultValueBuilder = LineNotificationDefaultValueBuilder()
    }

    class LineNotificationDefaultValueBuilder {
        var sender: Person? = null
        var message: String? = null
        var messages: MutableList<String>? = null
        var title: String? = null
        var lineMessageId: String? = null
        var lineStickerUrl: String? = null
        var chatId: String? = null
        var callState: CallState? = null
        var clickIntent: PendingIntent? = null
        var timestamp: Long = 0L
        var actions: MutableList<Notification.Action>? = null
        var icon: Bitmap? = null
        var history: MutableList<NotificationHistoryEntry>? = null
        var isSelfResponse: Boolean = false

        constructor()

        internal constructor(source: LineNotification) {
            this.sender = source.sender
            this.message = source.message
            this.messages = source.messages.toMutableList()
            this.title = source.title
            this.lineMessageId = source.lineMessageId
            this.lineStickerUrl = source.lineStickerUrl
            this.chatId = source.chatId
            this.callState = source.callState
            this.clickIntent = source.clickIntent
            this.timestamp = source.timestamp
            this.actions = source.actions.toMutableList()
            this.icon = source.icon
            this.history = source.history.toMutableList()
            this.isSelfResponse = source.isSelfResponse
        }

        fun clearActions(): LineNotificationDefaultValueBuilder {
            this.actions = null
            return this
        }

        fun sender(sender: Person?): LineNotificationDefaultValueBuilder {
            this.sender = sender
            return this
        }

        fun message(message: String?): LineNotificationDefaultValueBuilder {
            this.message = message
            return this
        }

        fun messages(messages: List<String>?): LineNotificationDefaultValueBuilder {
            this.messages = messages?.toMutableList()
            return this
        }

        fun title(title: String?): LineNotificationDefaultValueBuilder {
            this.title = title
            return this
        }

        fun lineMessageId(lineMessageId: String?): LineNotificationDefaultValueBuilder {
            this.lineMessageId = lineMessageId
            return this
        }

        fun lineStickerUrl(lineStickerUrl: String?): LineNotificationDefaultValueBuilder {
            this.lineStickerUrl = lineStickerUrl
            return this
        }

        fun chatId(chatId: String?): LineNotificationDefaultValueBuilder {
            this.chatId = chatId
            return this
        }

        fun callState(callState: CallState?): LineNotificationDefaultValueBuilder {
            this.callState = callState
            return this
        }

        fun clickIntent(clickIntent: PendingIntent?): LineNotificationDefaultValueBuilder {
            this.clickIntent = clickIntent
            return this
        }

        fun timestamp(timestamp: Long): LineNotificationDefaultValueBuilder {
            this.timestamp = timestamp
            return this
        }

        fun action(action: Notification.Action): LineNotificationDefaultValueBuilder {
            if (this.actions == null) {
                this.actions = mutableListOf()
            }
            this.actions!!.add(action)
            return this
        }

        fun actions(actions: List<Notification.Action>?): LineNotificationDefaultValueBuilder {
            this.actions = actions?.toMutableList()
            return this
        }

        fun icon(icon: Bitmap?): LineNotificationDefaultValueBuilder {
            this.icon = icon
            return this
        }

        fun history(history: List<NotificationHistoryEntry>?): LineNotificationDefaultValueBuilder {
            this.history = history?.toMutableList()
            return this
        }

        fun isSelfResponse(isSelfResponse: Boolean): LineNotificationDefaultValueBuilder {
            this.isSelfResponse = isSelfResponse
            return this
        }

        fun build(): LineNotification = LineNotification(this)
    }
}
