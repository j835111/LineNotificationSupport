package com.mysticwind.linenotificationsupport.model

import android.app.Notification
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.service.notification.StatusBarNotification
import androidx.core.app.Person
import androidx.core.graphics.drawable.IconCompat
import com.google.common.collect.ImmutableList
import com.mysticwind.linenotificationsupport.reply.ReplyActionBuilder
import com.mysticwind.linenotificationsupport.utils.ChatTitleAndSenderResolver
import com.mysticwind.linenotificationsupport.utils.NotificationExtractor
import com.mysticwind.linenotificationsupport.utils.StatusBarNotificationExtractor
import com.mysticwind.linenotificationsupport.utils.StatusBarNotificationPrinter
import dagger.hilt.android.qualifiers.ApplicationContext
import org.apache.commons.lang3.StringUtils
import java.util.Collections
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LineNotificationBuilder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val chatTitleAndSenderResolver: ChatTitleAndSenderResolver,
    private val statusBarNotificationPrinter: StatusBarNotificationPrinter,
    private val replyActionBuilder: ReplyActionBuilder
) {

    companion object {
        const val CALL_VIRTUAL_CHAT_ID = "call_virtual_chat_id"
        const val DEFAULT_CHAT_ID = "default_chat_id"

        @JvmField
        protected val MISSED_CALL_TAG = "NOTIFICATION_TAG_MISSED_CALL"

        const val GENERAL_NOTIFICATION_CHANNEL = "jp.naver.line.android.notification.GeneralNotifications"

        @JvmField
        protected val DEFAULT_SENDER_NAME = "?"
    }

    fun from(statusBarNotification: StatusBarNotification): LineNotification {
        val titleAndSender = chatTitleAndSenderResolver.resolveTitleAndSender(statusBarNotification)
        val title = titleAndSender.left
        val sender = titleAndSender.right
        val lineMessageId = NotificationExtractor.getLineMessageId(statusBarNotification.notification)
        val largeIconBitmap = getLargeIconBitmap(statusBarNotification)
        val senderPerson = buildPerson(sender, largeIconBitmap, statusBarNotification)
        val callState = resolveCallState(statusBarNotification)
        val actions = extractActions(statusBarNotification, callState)

        val message = NotificationExtractor.getMessage(statusBarNotification.notification)
        val lineStickerUrl = getLineStickerUrl(statusBarNotification)
        return LineNotification.builder()
            .lineMessageId(lineMessageId)
            .title(title)
            .message(message)
            .sender(senderPerson)
            .lineStickerUrl(lineStickerUrl)
            .chatId(resolveChatId(statusBarNotification, callState))
            .timestamp(statusBarNotification.notification.`when`)
            .clickIntent(statusBarNotification.notification.contentIntent)
            .actions(actions)
            .icon(largeIconBitmap)
            .callState(callState)
            .build()
    }

    private fun resolveCallState(statusBarNotification: StatusBarNotification): LineNotification.CallState? {
        return when {
            StatusBarNotificationExtractor.isCall(statusBarNotification) ->
                LineNotification.CallState.INCOMING
            MISSED_CALL_TAG == statusBarNotification.tag ->
                LineNotification.CallState.MISSED_CALL
            GENERAL_NOTIFICATION_CHANNEL == statusBarNotification.notification.channelId &&
                    StringUtils.isBlank(statusBarNotification.notification.group) ->
                LineNotification.CallState.IN_A_CALL
            else -> null
        }
    }

    private fun resolveChatId(
        statusBarNotification: StatusBarNotification,
        callState: LineNotification.CallState?
    ): String {
        if (callState != null) {
            return CALL_VIRTUAL_CHAT_ID
        }
        val lineChatId = NotificationExtractor.getLineChatId(statusBarNotification.notification)
        return if (StringUtils.isBlank(lineChatId)) DEFAULT_CHAT_ID else lineChatId!!
    }

    private fun getLineStickerUrl(statusBarNotification: StatusBarNotification): String? =
        statusBarNotification.notification.extras.getString("line.sticker.url")

    private fun getLargeIconBitmap(statusBarNotification: StatusBarNotification): Bitmap? {
        val largeIcon: Icon? = statusBarNotification.notification.getLargeIcon()
        return largeIcon?.loadDrawable(context)?.let { convertDrawableToBitmap(it) }
    }

    private fun convertDrawableToBitmap(drawable: Drawable?): Bitmap? {
        drawable ?: return null
        if (drawable is BitmapDrawable) {
            drawable.bitmap?.let { return it }
        }
        val bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        } else {
            Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        }
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun buildPerson(
        sender: String?,
        iconBitmap: Bitmap?,
        statusBarNotification: StatusBarNotification
    ): Person {
        val icon = iconBitmap?.let { IconCompat.createWithBitmap(it) }
        val senderName = if (StringUtils.isBlank(sender)) {
            statusBarNotificationPrinter.printError("No sender identified, using default!", statusBarNotification)
            DEFAULT_SENDER_NAME
        } else {
            sender!!
        }
        return Person.Builder()
            .setName(senderName)
            .setIcon(icon)
            .build()
    }

    private fun extractActions(
        statusBarNotification: StatusBarNotification,
        callState: LineNotification.CallState?
    ): List<Notification.Action> {
        if (isMessage(statusBarNotification, callState)) {
            val lineAction = extractActionsOfIndices(statusBarNotification, 1)
            if (lineAction.isEmpty()) {
                return emptyList()
            }
            val lineChatId = NotificationExtractor.getLineChatId(statusBarNotification.notification)
            return ImmutableList.of(replyActionBuilder.buildReplyAction(lineChatId ?: DEFAULT_CHAT_ID, lineAction[0]))
        }

        callState ?: return emptyList()

        return when (callState) {
            LineNotification.CallState.INCOMING ->
                extractActionsOfIndices(statusBarNotification, 1, 0)
            LineNotification.CallState.MISSED_CALL ->
                extractActionsOfIndices(statusBarNotification, 1)
            LineNotification.CallState.IN_A_CALL ->
                extractActionsOfIndices(statusBarNotification, 0)
        }
    }

    private fun isMessage(
        statusBarNotification: StatusBarNotification,
        callState: LineNotification.CallState?
    ): Boolean {
        if (callState != null) return false
        return StatusBarNotificationExtractor.isMessage(statusBarNotification)
    }

    private fun extractActionsOfIndices(
        notificationFromLine: StatusBarNotification,
        vararg indices: Int
    ): List<Notification.Action> {
        val extractedActions = mutableListOf<Notification.Action>()
        val notificationActions = notificationFromLine.notification.actions ?: return extractedActions
        for (index in indices) {
            if (index < notificationActions.size) {
                extractedActions.add(notificationActions[index])
            }
        }
        return extractedActions
    }
}
