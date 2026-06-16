package com.mysticwind.linenotificationsupport.utils

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import com.google.common.collect.ImmutableList
import com.google.common.collect.Lists
import com.mysticwind.linenotificationsupport.R
import com.mysticwind.linenotificationsupport.line.LineLauncher
import com.mysticwind.linenotificationsupport.model.LineNotification
import com.mysticwind.linenotificationsupport.model.NotificationExtraConstants
import com.mysticwind.linenotificationsupport.notification.ConversationNotificationMetadata
import com.mysticwind.linenotificationsupport.notification.WearableDismissalMetadata
import com.mysticwind.linenotificationsupport.notificationgroup.NotificationGroupCreator
import org.apache.commons.collections4.CollectionUtils
import org.apache.commons.lang3.ArrayUtils
import org.apache.commons.lang3.StringUtils
import timber.log.Timber
import java.net.HttpURLConnection
import java.net.URL
import java.util.Objects

class BigPictureStyleImageSupportedNotificationPublisherAsyncTask(
    private val context: Context,
    private val notificationGroupCreator: NotificationGroupCreator,
    private val lineNotification: LineNotification,
    private val notificationId: Int
) : AsyncTask<String, Void, Bitmap>() {

    init {
        Objects.requireNonNull(notificationGroupCreator)
    }

    companion object {
        private val LINE_LAUNCHER = LineLauncher
    }

    override fun doInBackground(vararg params: String?): Bitmap? {
        if (StringUtils.isBlank(lineNotification.lineStickerUrl)) {
            return null
        }

        return try {
            val url = URL(lineNotification.lineStickerUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            Timber.e(e, "Failed to download image %s: %s", lineNotification.lineStickerUrl, e.message)
            null
        }
    }

    override fun onPostExecute(downloadedImage: Bitmap?) {
        super.onPostExecute(downloadedImage)

        val style = buildMessageStyle(downloadedImage)

        val channelId = createNotificationChannel()

        val notificationBuilder = NotificationCompat.Builder(context, lineNotification.chatId ?: "")
            .setStyle(style)
            .setContentTitle(lineNotification.title)
            .setContentText(lineNotification.message)
            .setGroup(lineNotification.chatId)
            .setSmallIcon(R.drawable.ic_new_message)
            .setLargeIcon(lineNotification.icon)
            .setContentIntent(resolveContentIntent(context, lineNotification))
            .setChannelId(channelId.orElse(null))
            .setAutoCancel(true)
            .setWhen(lineNotification.timestamp)

        ConversationNotificationMetadata.applyToBuilder(context, notificationBuilder, lineNotification)
        WearableDismissalMetadata.buildDismissalId(lineNotification)?.let { dismissalId ->
            notificationBuilder.extend(NotificationCompat.WearableExtender().setDismissalId(dismissalId))
        }

        val singleNotification = notificationBuilder.build()

        addActionInNotification(singleNotification)
        if (lineNotification.messages.size > 1) {
            Timber.w("Multi-messages, override the tickerText to be the first page [%s]",
                lineNotification.messages[0])
            singleNotification.extras.putString(Notification.EXTRA_TEXT, lineNotification.messages[0])
        }
        singleNotification.extras.putString(NotificationExtraConstants.CHAT_ID, lineNotification.chatId)
        singleNotification.extras.putString(NotificationExtraConstants.MESSAGE_ID, lineNotification.lineMessageId)
        singleNotification.extras.putString(NotificationExtraConstants.STICKER_URL, lineNotification.lineStickerUrl)
        singleNotification.extras.putString(NotificationExtraConstants.SENDER_NAME, lineNotification.sender?.name?.toString() ?: "")

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(notificationId, singleNotification)
    }

    private fun buildMessageStyle(downloadedImage: Bitmap?): NotificationCompat.Style {
        if (downloadedImage != null) {
            return NotificationCompat.BigPictureStyle()
                .bigPicture(downloadedImage)
                .setSummaryText(lineNotification.message)
        }

        val messagingStyle = NotificationCompat.MessagingStyle(
            lineNotification.sender ?: Person.Builder().setName("").build()
        )

        buildMessages(lineNotification).forEach { message ->
            messagingStyle.addMessage(message)
        }

        if (lineNotification.sender?.name != lineNotification.title) {
            // Don't set the conversation title for single person chat
            messagingStyle.conversationTitle = lineNotification.title
        }
        return messagingStyle
    }

    private fun buildMessages(lineNotification: LineNotification): List<NotificationCompat.MessagingStyle.Message> {
        val messages: List<String> = if (CollectionUtils.isEmpty(lineNotification.messages))
            listOf(lineNotification.message ?: "")
        else
            lineNotification.messages

        val messageListBuilder = ImmutableList.builder<NotificationCompat.MessagingStyle.Message>()
        for (message in messages) {
            val messagingStyleMessage = NotificationCompat.MessagingStyle.Message(
                message, lineNotification.timestamp, lineNotification.sender)
            messagingStyleMessage.extras.putString(NotificationExtraConstants.CHAT_ID, lineNotification.chatId)
            messagingStyleMessage.extras.putString(NotificationExtraConstants.MESSAGE_ID, lineNotification.lineMessageId)
            messagingStyleMessage.extras.putString(NotificationExtraConstants.STICKER_URL, lineNotification.lineStickerUrl)
            messagingStyleMessage.extras.putString(NotificationExtraConstants.SENDER_NAME, lineNotification.sender?.name?.toString() ?: "")
            messageListBuilder.add(messagingStyleMessage)
        }
        return messageListBuilder.build()
    }

    private fun addActionInNotification(notification: Notification) {
        if (lineNotification.actions.isEmpty()) {
            return
        }
        val actionsToAdd = lineNotification.actions
        if (ArrayUtils.isEmpty(notification.actions)) {
            notification.actions = actionsToAdd.toTypedArray()
        } else {
            val actions = Lists.newArrayList(*notification.actions)
            actions.addAll(actionsToAdd)
            notification.actions = actions.toTypedArray()
        }
    }

    private fun createNotificationChannel(): java.util.Optional<String> {
        return if (lineNotification.isSelfResponse) {
            notificationGroupCreator.createSelfResponseNotificationChannel()
        } else {
            notificationGroupCreator.createNotificationChannel(lineNotification.chatId ?: "", lineNotification.title ?: "")
        }
    }

    private fun resolveContentIntent(context: Context, lineNotification: LineNotification): PendingIntent {
        return lineNotification.getClickIntent().orElse(
            LINE_LAUNCHER.buildPendingIntent(context, lineNotification.chatId)
        )
    }
}
