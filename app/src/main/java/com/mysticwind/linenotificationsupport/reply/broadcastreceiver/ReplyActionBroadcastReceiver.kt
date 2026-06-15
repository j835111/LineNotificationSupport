package com.mysticwind.linenotificationsupport.reply.broadcastreceiver

import android.app.Notification
import android.app.RemoteInput
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.service.notification.StatusBarNotification
import com.google.common.collect.ImmutableList
import com.mysticwind.linenotificationsupport.chatname.ChatNameManager
import com.mysticwind.linenotificationsupport.model.LineNotification
import com.mysticwind.linenotificationsupport.notification.AndroidNotificationManager
import com.mysticwind.linenotificationsupport.notification.NotificationFilterStrategy
import com.mysticwind.linenotificationsupport.notification.NotificationPublisherFactory
import com.mysticwind.linenotificationsupport.reply.DefaultReplyActionBuilder
import com.mysticwind.linenotificationsupport.reply.LineRemoteInputReplier
import com.mysticwind.linenotificationsupport.reply.MyPersonLabelProvider
import com.mysticwind.linenotificationsupport.utils.NotificationIdGenerator
import dagger.hilt.android.AndroidEntryPoint
import org.apache.commons.lang3.StringUtils
import timber.log.Timber
import java.time.Instant
import java.util.Comparator
import java.util.Optional
import javax.inject.Inject

// TODO merge duplicated code with StartConversationBroadcastReceiver
@AndroidEntryPoint
class ReplyActionBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var lineRemoteInputReplier: LineRemoteInputReplier

    @Inject
    lateinit var chatNameManager: ChatNameManager

    @Inject
    lateinit var myPersonLabelProvider: MyPersonLabelProvider

    @Inject
    lateinit var notificationPublisherFactory: NotificationPublisherFactory

    @Inject
    lateinit var notificationIdGenerator: NotificationIdGenerator

    @Inject
    lateinit var androidNotificationManager: AndroidNotificationManager

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (DefaultReplyActionBuilder.REPLY_MESSAGE_ACTION == action) {

            val responseMessage = getResponseMessage(intent)
            val lineReplyAction = getLineReplyAction(intent)
            Timber.i("Received reply action with response [%s] and line reply action [%s]",
                responseMessage, lineReplyAction)

            if (responseMessage.isPresent && lineReplyAction.isPresent) {
                lineRemoteInputReplier.sendReply(lineReplyAction.get(), responseMessage.get())
                updateNotification(intent, responseMessage.get())
            }
        }
    }

    private fun getResponseMessage(intent: Intent): Optional<String> {
        val remoteInput: Bundle? = RemoteInput.getResultsFromIntent(intent)
        if (remoteInput == null) {
            Timber.d("ReplyActionBroadcastReceiver: Null RemoteInput")
            return Optional.empty()
        }
        val response: CharSequence? = remoteInput.getCharSequence(DefaultReplyActionBuilder.RESPONSE_REMOTE_INPUT_KEY)
        if (response == null) {
            Timber.d("ReplyActionBroadcastReceiver: Null response from %s", DefaultReplyActionBuilder.RESPONSE_REMOTE_INPUT_KEY)
            return Optional.empty()
        }
        if (StringUtils.isBlank(response.toString())) {
            Timber.d("ReplyActionBroadcastReceiver: Blank response: [%s]", response.toString())
            return Optional.empty()
        }
        return Optional.of(response.toString())
    }

    private fun getLineReplyAction(intent: Intent): Optional<Notification.Action> {
        val lineReplyAction: Notification.Action? = intent.getParcelableExtra(DefaultReplyActionBuilder.LINE_REPLY_ACTION_KEY)
        return Optional.ofNullable(lineReplyAction)
    }

    private fun updateNotification(intent: Intent, response: String) {
        val chatId = intent.getStringExtra(DefaultReplyActionBuilder.CHAT_ID_KEY)

        val statusBarNotification = findNotificationOfChatId(chatId)

        if (!statusBarNotification.isPresent) {
            Timber.e("Cannot find corresponding notification for chat ID [%s]", chatId)
            return
        }

        val chatName = chatNameManager.getChatName(chatId ?: "")

        val responseLineNotification = LineNotification.builder()
            .lineMessageId(Instant.now().toEpochMilli().toString()) // just generate a fake one
            .title(chatName)
            .message(response)
            .sender(myPersonLabelProvider.getMyPerson())
            .chatId(chatId)
            .timestamp(Instant.now().toEpochMilli())
            .actions(ImmutableList.copyOf(statusBarNotification.get().notification.actions))
            .isSelfResponse(true)
            .build()

        notificationPublisherFactory.get().publishNotification(responseLineNotification, notificationIdGenerator.getNextNotificationId())
    }

    private fun findNotificationOfChatId(chatId: String?): Optional<StatusBarNotification> {
        return androidNotificationManager.getOrderedLineNotificationSupportNotificationsOfChatId(chatId ?: "", NotificationFilterStrategy.EXCLUDE_SUMMARY).stream()
            .max(Comparator.comparing { notification: StatusBarNotification -> notification.notification.`when` })
    }
}
