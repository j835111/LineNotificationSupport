package com.mysticwind.linenotificationsupport.utils

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import com.google.common.collect.Lists
import com.mysticwind.linenotificationsupport.R
import com.mysticwind.linenotificationsupport.conversationstarter.ConversationStarterNotificationManager
import com.mysticwind.linenotificationsupport.line.LineLauncher
import com.mysticwind.linenotificationsupport.model.LineNotification
import com.mysticwind.linenotificationsupport.model.LineNotificationBuilder
import com.mysticwind.linenotificationsupport.model.NotificationExtraConstants
import com.mysticwind.linenotificationsupport.model.NotificationHistoryEntry
import com.mysticwind.linenotificationsupport.notificationgroup.NotificationGroupCreator
import org.apache.commons.collections4.CollectionUtils
import org.apache.commons.lang3.ArrayUtils
import org.apache.commons.lang3.StringUtils
import timber.log.Timber
import java.util.Objects
import java.util.Optional

class MessageStyleImageSupportedNotificationPublisherAsyncTask(
    private val context: Context,
    private val notificationGroupCreator: NotificationGroupCreator,
    private val lineNotification: LineNotification,
    private val notificationId: Int,
    private val useSingleNotificationConversations: Boolean
) : AsyncTask<String, Void, NotificationCompat.Style>() {

    init {
        Objects.requireNonNull(notificationGroupCreator)
    }

    companion object {
        private const val SINGLE_NOTIFICATION_GROUP = "single-notification-group"
        private const val AUTHORITY = "com.mysticwind.linenotificationsupport.fileprovider"

        private val LINE_LAUNCHER = LineLauncher

        private val NOT_CHAT_IDS: Set<String> = ImmutableSet.of(
            LineNotificationBuilder.CALL_VIRTUAL_CHAT_ID,
            LineNotificationBuilder.DEFAULT_CHAT_ID,
            ConversationStarterNotificationManager.CONVERSATION_STARTER_CHAT_ID
        )
    }

    override fun doInBackground(vararg params: String?): NotificationCompat.Style {
        val conversationTitle: String? = if (lineNotification.sender?.name == lineNotification.title) {
            // this is usually the case if you're talking to a single person.
            // Don't set the conversation title in this case.
            null
        } else {
            lineNotification.title
        }

        val messagingStyle = NotificationCompat.MessagingStyle(
            lineNotification.sender ?: Person.Builder().setName("").build()
        ).setConversationTitle(conversationTitle)

        val messages = buildMessages()
        for (message in messages) {
            messagingStyle.addMessage(message)
        }
        return messagingStyle
    }

    private fun buildMessages(): List<NotificationCompat.MessagingStyle.Message> {
        val messageListBuilder = ImmutableList.builder<NotificationCompat.MessagingStyle.Message>()

        for (entry in lineNotification.history) {
            val message = NotificationCompat.MessagingStyle.Message(
                entry.message, entry.timestamp, entry.sender)

            entry.getLineStickerUrl().ifPresent { url ->
                getLineStickerUri(url).ifPresent { uri ->
                    message.setData("image/", uri)
                }
            }

            message.extras.putString(NotificationExtraConstants.CHAT_ID, lineNotification.chatId)
            message.extras.putString(NotificationExtraConstants.MESSAGE_ID, entry.lineMessageId)
            message.extras.putString(NotificationExtraConstants.STICKER_URL, entry.getLineStickerUrl().orElse(null))
            message.extras.putString(NotificationExtraConstants.SENDER_NAME, entry.sender?.name?.toString() ?: "")

            messageListBuilder.add(message)
        }

        val splitMessages: List<String> = if (CollectionUtils.isEmpty(lineNotification.messages))
            emptyList()
        else
            lineNotification.messages

        // TODO we should be able to drastically reduce duplicated code by compiling the messages first
        if (splitMessages.isEmpty()) {
            val message = NotificationCompat.MessagingStyle.Message(
                lineNotification.message, lineNotification.timestamp, lineNotification.sender)
            if (StringUtils.isNotBlank(lineNotification.lineStickerUrl)) {
                getLineStickerUri(lineNotification.lineStickerUrl!!).ifPresent { uri ->
                    message.setData("image/", uri)
                }
            }
            messageListBuilder.add(message)
        } else {
            // this also means we don't support attaching the sticker to split messages. We probably
            // don't need to support that.
            for (message in splitMessages) {
                messageListBuilder.add(
                    NotificationCompat.MessagingStyle.Message(
                        message, lineNotification.timestamp, lineNotification.sender)
                )
            }
        }

        return messageListBuilder.build()
    }

    private fun getLineStickerUri(lineStickerUrl: String): Optional<Uri> {
        // Glide auto-caches
        val target = Glide.with(context)
            .downloadOnly()
            .load(lineStickerUrl)
            .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)

        // https://stackoverflow.com/questions/63988424/show-an-image-in-messagingstyle-notification-in-android
        return try {
            val file = target.get() // needs to be called on background thread
            val uri = FileProvider.getUriForFile(context, AUTHORITY, file)
            Timber.i("URL %s downloaded at: %s", lineStickerUrl, uri)
            Optional.of(uri)
        } catch (e: Exception) {
            Timber.e(e, "Failed to download image %s: %s", lineStickerUrl, e.message)
            Optional.empty()
        }
    }

    override fun onPostExecute(notificationStyle: NotificationCompat.Style?) {
        super.onPostExecute(notificationStyle)

        val channelId = createNotificationChannel()

        val singleNotification = NotificationCompat.Builder(context, lineNotification.chatId ?: "")
            .setStyle(notificationStyle)
            .setContentTitle(lineNotification.title)
            .setContentText(lineNotification.message)
            .setGroup(resolveGroup())
            .setSmallIcon(R.drawable.ic_new_message)
            .setLargeIcon(lineNotification.icon)
            .setContentIntent(resolveContentIntent(context, lineNotification))
            .setChannelId(channelId.orElse(null))
            .setAutoCancel(true)
            .setWhen(lineNotification.timestamp)
            .build()

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
        Timber.d("Publishing notification id [%d] channel [%s] group [%s] text [%s] timestamp [%d]",
            notificationId,
            singleNotification.channelId,
            singleNotification.group,
            NotificationExtractor.getMessage(singleNotification),
            singleNotification.`when`)
        notificationManager.notify(notificationId, singleNotification)
    }

    private fun resolveContentIntent(context: Context, lineNotification: LineNotification): PendingIntent {
        return lineNotification.getClickIntent().orElse(
            LINE_LAUNCHER.buildPendingIntent(context, lineNotification.chatId)
        )
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

    private fun createNotificationChannel(): Optional<String> {
        return if (lineNotification.isSelfResponse) {
            notificationGroupCreator.createSelfResponseNotificationChannel()
        } else {
            notificationGroupCreator.createNotificationChannel(lineNotification.chatId ?: "", lineNotification.title ?: "")
        }
    }

    private fun resolveGroup(): String {
        if (!useSingleNotificationConversations) {
            return lineNotification.chatId ?: ""
        }
        if (NOT_CHAT_IDS.contains(lineNotification.chatId)) {
            return lineNotification.chatId ?: ""
        }
        return SINGLE_NOTIFICATION_GROUP
    }
}
