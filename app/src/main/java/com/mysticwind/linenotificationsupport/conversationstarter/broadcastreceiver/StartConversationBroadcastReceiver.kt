package com.mysticwind.linenotificationsupport.conversationstarter.broadcastreceiver

import android.app.Notification
import android.app.RemoteInput
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.common.collect.ImmutableList
import com.mysticwind.linenotificationsupport.R
import com.mysticwind.linenotificationsupport.chatname.ChatNameManager
import com.mysticwind.linenotificationsupport.conversationstarter.ChatKeywordDao
import com.mysticwind.linenotificationsupport.conversationstarter.ConversationStarterNotificationManager
import com.mysticwind.linenotificationsupport.conversationstarter.LineReplyActionDao
import com.mysticwind.linenotificationsupport.conversationstarter.StartConversationActionBuilder
import com.mysticwind.linenotificationsupport.model.LineNotification
import com.mysticwind.linenotificationsupport.notification.AndroidNotificationManager
import com.mysticwind.linenotificationsupport.notification.NotificationPublisherFactory
import com.mysticwind.linenotificationsupport.reply.LineRemoteInputReplier
import com.mysticwind.linenotificationsupport.reply.MyPersonLabelProvider
import com.mysticwind.linenotificationsupport.reply.ReplyActionBuilder
import com.mysticwind.linenotificationsupport.ui.LocalizationDao
import com.mysticwind.linenotificationsupport.ui.UserAlertDao
import com.mysticwind.linenotificationsupport.utils.NotificationIdGenerator
import dagger.hilt.android.AndroidEntryPoint
import org.apache.commons.lang3.StringUtils
import timber.log.Timber
import java.time.Instant
import java.util.Optional
import javax.inject.Inject

@AndroidEntryPoint
class StartConversationBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var lineRemoteInputReplier: LineRemoteInputReplier

    @Inject
    lateinit var chatKeywordDao: ChatKeywordDao

    @Inject
    lateinit var lineReplyActionDao: LineReplyActionDao

    @Inject
    lateinit var notificationManager: AndroidNotificationManager

    @Inject
    lateinit var chatNameManager: ChatNameManager

    @Inject
    lateinit var myPersonLabelProvider: MyPersonLabelProvider

    @Inject
    lateinit var replyActionBuilder: ReplyActionBuilder

    @Inject
    lateinit var notificationPublisherFactory: NotificationPublisherFactory

    @Inject
    lateinit var notificationIdGenerator: NotificationIdGenerator

    @Inject
    lateinit var localizationDao: LocalizationDao

    @Inject
    lateinit var userAlertDao: UserAlertDao

    private inner class ChatIdAndMessage(val chatId: String, val message: String)

    override fun onReceive(context: Context, intent: Intent) {
        processInput(context, intent)
        clearNotificationSpinner()
    }

    fun processInput(context: Context, intent: Intent) {
        val action = intent.action
        if (StartConversationActionBuilder.START_CONVERSATION_ACTION != action) {
            return
        }

        val messageWithKeyword = getInputMessageWithKeyword(intent)
        if (!messageWithKeyword.isPresent) {
            userAlertDao.notify(localizationDao.getLocalizedString(R.string.conversation_start_remote_input_invalid_message, messageWithKeyword))
            return
        }

        val chatIdAndMessage = resolveChatIdAndMessage(messageWithKeyword.get())
        if (!chatIdAndMessage.isPresent) {
            Timber.i("Cannot find matching chat ID from message [%s]", messageWithKeyword.get())
            userAlertDao.notify(localizationDao.getLocalizedString(R.string.conversation_start_remote_input_no_keyword, messageWithKeyword.get()))
            return
        }

        Timber.d("Resolved chat ID [%s] and message [%s]", chatIdAndMessage.get().chatId, chatIdAndMessage.get().message)

        val lineReplyAction = getLineReplyAction(chatIdAndMessage.get().chatId)
        if (!lineReplyAction.isPresent) {
            Timber.i("Cannot find matching Line Reply Action: chat ID [%s] message [%s]", chatIdAndMessage.get().chatId, messageWithKeyword.get())
            userAlertDao.notify(
                localizationDao.getLocalizedString(
                    R.string.conversation_start_remote_input_no_reply_action,
                    chatNameManager.getChatName(chatIdAndMessage.get().chatId ?: "") ?: ""
                )
            )
            return
        }

        Timber.i("Received start conversation action with message [%s]", messageWithKeyword)

        if (messageWithKeyword.isPresent && lineReplyAction.isPresent) {
            lineRemoteInputReplier.sendReply(lineReplyAction.get(), chatIdAndMessage.get().message)
            generateNewConversationNotification(context, chatIdAndMessage.get().chatId, chatIdAndMessage.get().message)
        }
    }

    private fun getInputMessageWithKeyword(intent: Intent): Optional<String> {
        val remoteInput: Bundle? = RemoteInput.getResultsFromIntent(intent)
        if (remoteInput == null) {
            Timber.d("Null RemoteInput")
            return Optional.empty()
        }
        val messageWithKeyword: CharSequence? = remoteInput.getCharSequence(StartConversationActionBuilder.MESSAGE_REMOTE_INPUT_KEY)
        if (messageWithKeyword == null) {
            Timber.d("Null message from %s", StartConversationActionBuilder.MESSAGE_REMOTE_INPUT_KEY)
            return Optional.empty()
        }
        if (StringUtils.isBlank(messageWithKeyword.toString())) {
            Timber.d("Blank message: [%s]", messageWithKeyword.toString())
            return Optional.empty()
        }
        return Optional.of(messageWithKeyword.toString())
    }

    private fun resolveChatIdAndMessage(messageWithKeyword: String): Optional<ChatIdAndMessage> {
        val keywords = chatKeywordDao.getKeywords()
        val matchingKeyword = keywords.stream()
            .filter { keyword -> messageWithKeyword.startsWith(keyword) }
            .findFirst()
        if (!matchingKeyword.isPresent) {
            return Optional.empty()
        }
        val chatId = chatKeywordDao.getChatId(matchingKeyword.get())
        if (!chatId.isPresent) {
            return Optional.empty()
        }
        val messageWithoutKeyword = messageWithKeyword.replaceFirst(matchingKeyword.get(), "").trim()
        if (StringUtils.isBlank(messageWithoutKeyword)) {
            Timber.i("Blank message after removing keyword [%s]", messageWithKeyword)
            return Optional.empty()
        }
        return Optional.of(ChatIdAndMessage(chatId.get(), messageWithoutKeyword))
    }

    private fun getLineReplyAction(chatId: String): Optional<Notification.Action> =
        lineReplyActionDao.getLineReplyAction(chatId)

    private fun generateNewConversationNotification(context: Context, chatId: String, message: String) {
        val chatName = chatNameManager.getChatName(chatId)
        val lineReplyAction = getLineReplyAction(chatId).get()
        val lineNotification = LineNotification.builder()
            .lineMessageId(Instant.now().toEpochMilli().toString()) // just generate a fake one
            .title(chatName)
            .message(message)
            .sender(myPersonLabelProvider.getMyPerson())
            .chatId(chatId)
            .timestamp(Instant.now().toEpochMilli())
            .actions(ImmutableList.of(replyActionBuilder.buildReplyAction(chatId, lineReplyAction)))
            .isSelfResponse(true)
            .build()

        notificationPublisherFactory.get().publishNotification(lineNotification, notificationIdGenerator.getNextNotificationId())
    }

    private fun clearNotificationSpinner() {
        notificationManager.clearRemoteInputNotificationSpinner(ConversationStarterNotificationManager.CONVERSATION_STARTER_CHAT_ID)
    }
}
