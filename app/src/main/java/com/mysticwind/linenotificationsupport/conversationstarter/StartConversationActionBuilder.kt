package com.mysticwind.linenotificationsupport.conversationstarter

import android.app.Notification
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.Context
import android.content.Intent
import com.google.common.collect.ImmutableList
import com.mysticwind.linenotificationsupport.R
import com.mysticwind.linenotificationsupport.conversationstarter.broadcastreceiver.DisableStartConversationFeatureBroadcastReceiver
import com.mysticwind.linenotificationsupport.conversationstarter.broadcastreceiver.StartConversationBroadcastReceiver
import com.mysticwind.linenotificationsupport.ui.LocalizationDao
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.util.Objects
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StartConversationActionBuilder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val localizationDao: LocalizationDao
) {
    init {
        Objects.requireNonNull(context)
        Objects.requireNonNull(localizationDao)
    }

    companion object {
        const val START_CONVERSATION_ACTION = "start_conversation_action"
        const val MESSAGE_REMOTE_INPUT_KEY = "message"
        const val DISABLE_START_CONVERSATION_FEATURE_ACTION = "disable_start_conversation_feature_action"
    }

    fun buildActions(): List<Notification.Action> = ImmutableList.of(
        buildRemoteInputAction(),
        buildDisableFeatureAction()
    )

    fun buildRemoteInputAction(): Notification.Action {
        val remoteInput = RemoteInput.Builder(MESSAGE_REMOTE_INPUT_KEY)
            .setLabel(localizationDao.getLocalizedString(R.string.conversation_start_notification_action_button_message))
            .build()

        val replyPendingIntent = PendingIntent.getBroadcast(
            context,
            Instant.now().toEpochMilli().toInt(),
            getMessageReplyIntent(),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        return Notification.Action.Builder(
            null,
            localizationDao.getLocalizedString(R.string.conversation_start_notification_action_button),
            replyPendingIntent
        )
            .addRemoteInput(remoteInput)
            .build()
    }

    private fun getMessageReplyIntent(): Intent {
        val intent = Intent(context, StartConversationBroadcastReceiver::class.java)
        intent.action = START_CONVERSATION_ACTION
        return intent
    }

    private fun buildDisableFeatureAction(): Notification.Action {
        val intent = Intent(context, DisableStartConversationFeatureBroadcastReceiver::class.java)
        intent.action = DISABLE_START_CONVERSATION_FEATURE_ACTION
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        return Notification.Action.Builder(
            null,
            localizationDao.getLocalizedString(R.string.conversation_start_notification_disable_feature_action_button),
            pendingIntent
        ).build()
    }
}
