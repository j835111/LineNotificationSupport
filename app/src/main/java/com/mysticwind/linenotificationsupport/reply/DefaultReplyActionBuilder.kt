package com.mysticwind.linenotificationsupport.reply

import android.app.Notification
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.Context
import android.content.Intent
import com.mysticwind.linenotificationsupport.R
import com.mysticwind.linenotificationsupport.reply.broadcastreceiver.ReplyActionBroadcastReceiver
import com.mysticwind.linenotificationsupport.ui.LocalizationDao
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Objects
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultReplyActionBuilder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val localizationDao: LocalizationDao
) : ReplyActionBuilder {

    companion object {
        const val REPLY_MESSAGE_ACTION = "reply_message"
        const val RESPONSE_REMOTE_INPUT_KEY = "response"
        const val LINE_REPLY_ACTION_KEY = "line.reply.action"
        const val CHAT_ID_KEY = "chat.id"

        private const val DEFAULT_REPLY_LABEL = "Reply"
    }

    init {
        Objects.requireNonNull(context)
        Objects.requireNonNull(localizationDao)
    }

    override fun buildReplyAction(chatId: String, originalLineReplyAction: Notification.Action): Notification.Action {
        val remoteInput = RemoteInput.Builder(RESPONSE_REMOTE_INPUT_KEY)
            .setLabel(localizationDao.getLocalizedString(R.string.conversation_notification_action_button_message))
            .build()

        val replyPendingIntent = PendingIntent.getBroadcast(
            context,
            chatId.hashCode(),
            getMessageReplyIntent(chatId, originalLineReplyAction),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val buttonLabel = localizationDao.getLocalizedString(R.string.conversation_notification_action_button)

        return Notification.Action.Builder(null, buttonLabel, replyPendingIntent)
            .addRemoteInput(remoteInput)
            .build()
    }

    private fun getMessageReplyIntent(chatId: String, originalLineReplyAction: Notification.Action): Intent {
        val intent = Intent(context, ReplyActionBroadcastReceiver::class.java)
        intent.action = REPLY_MESSAGE_ACTION
        intent.putExtra(CHAT_ID_KEY, chatId)
        intent.putExtra(LINE_REPLY_ACTION_KEY, originalLineReplyAction)
        return intent
    }
}
