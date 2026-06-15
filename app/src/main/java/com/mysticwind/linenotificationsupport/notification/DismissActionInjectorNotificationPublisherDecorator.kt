package com.mysticwind.linenotificationsupport.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.service.notification.StatusBarNotification
import com.google.common.collect.ImmutableList
import com.mysticwind.linenotificationsupport.DismissNotificationBroadcastReceiver
import com.mysticwind.linenotificationsupport.R
import com.mysticwind.linenotificationsupport.conversationstarter.ConversationStarterNotificationManager
import com.mysticwind.linenotificationsupport.model.LineNotification
import org.apache.commons.lang3.StringUtils
import timber.log.Timber

class DismissActionInjectorNotificationPublisherDecorator(
    private val notificationPublisher: NotificationPublisher,
    private val context: Context
) : NotificationPublisher {

    override fun publishNotification(lineNotification: LineNotification, notificationId: Int) {
        if (hasDismissAction(lineNotification)) {
            Timber.i(
                "Notification [%d] [%s] already has dismiss action",
                notificationId, lineNotification.message
            )
            this.notificationPublisher.publishNotification(lineNotification, notificationId)
            return
        }

        // don't add dismiss button for incoming call state
        if (lineNotification.callState == LineNotification.CallState.INCOMING) {
            this.notificationPublisher.publishNotification(lineNotification, notificationId)
            return
        }

        // conversation start chats restarts itself and dismiss action don't make sense
        if (ConversationStarterNotificationManager.CONVERSATION_STARTER_CHAT_ID == lineNotification.chatId) {
            this.notificationPublisher.publishNotification(lineNotification, notificationId)
            return
        }

        val actions = buildActions(lineNotification.actions, notificationId)

        val dismissActionInjectedLineNotification = lineNotification.toBuilder()
            .actions(actions)
            .build()
        this.notificationPublisher.publishNotification(dismissActionInjectedLineNotification, notificationId)
    }

    // ideally ordered by: reply -> dismiss -> others (e.g. website)
    private fun buildActions(actions: List<Notification.Action>, notificationId: Int): List<Notification.Action> {
        val dismissAction = buildDismissAction(notificationId)

        val actionsToReturn = mutableListOf<Notification.Action>().apply { addAll(actions) }
        if (actionsToReturn.isEmpty()) {
            actionsToReturn.add(dismissAction)
        } else {
            actionsToReturn.add(1, dismissAction)
        }
        return ImmutableList.copyOf(actionsToReturn)
    }

    private fun hasDismissAction(lineNotification: LineNotification): Boolean {
        val dismissButtonText = context.getString(R.string.dismiss_button_text)

        return lineNotification.actions.any { action -> StringUtils.equals(action.title, dismissButtonText) }
    }

    private fun buildDismissAction(notificationId: Int): Notification.Action {
        val buttonIntent = Intent(context, DismissNotificationBroadcastReceiver::class.java)
        buttonIntent.action = "dismiss-$notificationId"
        buttonIntent.putExtra(DismissNotificationBroadcastReceiver.NOTIFICATION_ID, notificationId)

        val actionIntent = PendingIntent.getBroadcast(
            context,
            0,
            buttonIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        val dismissButtonText = context.getString(R.string.dismiss_button_text)
        return Notification.Action.Builder(android.R.drawable.btn_default, dismissButtonText, actionIntent)
            .build()
    }

    override fun republishNotification(lineNotification: LineNotification, notificationId: Int) {
        // do nothing as the action should have been injected previously through publishNotification()
        this.notificationPublisher.republishNotification(lineNotification, notificationId)
    }

    override fun updateNotificationDismissed(statusBarNotification: StatusBarNotification) {
        // do nothing
        this.notificationPublisher.updateNotificationDismissed(statusBarNotification)
    }
}
