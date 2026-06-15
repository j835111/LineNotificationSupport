package com.mysticwind.linenotificationsupport.notification.reactor

import android.app.Notification
import android.service.notification.StatusBarNotification
import com.google.common.collect.ImmutableSet
import com.mysticwind.linenotificationsupport.conversationstarter.LineReplyActionDao
import com.mysticwind.linenotificationsupport.line.Constants
import com.mysticwind.linenotificationsupport.utils.NotificationExtractor
import com.mysticwind.linenotificationsupport.utils.StatusBarNotificationExtractor
import org.apache.commons.lang3.StringUtils
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LineReplyActionPersistenceIncomingNotificationReactor @Inject constructor(
    private val lineReplyActionDao: LineReplyActionDao
) : IncomingNotificationReactor {

    companion object {
        private val INTERESTED_PACKAGES: Set<String> = ImmutableSet.of(Constants.LINE_PACKAGE_NAME)
    }

    override fun interestedPackages(): Collection<String> = INTERESTED_PACKAGES

    override fun isInterestInNotificationGroup(): Boolean = false

    override fun reactToIncomingNotification(statusBarNotification: StatusBarNotification): Reaction {
        if (!isMessage(statusBarNotification)) {
            return Reaction.NONE
        }

        val chatId = NotificationExtractor.getLineChatId(statusBarNotification.notification)
        if (StringUtils.isBlank(chatId)) {
            return Reaction.NONE
        }

        val replyAction = resolveReplyAction(statusBarNotification.notification.actions)
        replyAction?.let { action -> persistReplyAction(chatId!!, action) }

        return Reaction.NONE
    }

    private fun isMessage(statusBarNotification: StatusBarNotification): Boolean =
        StatusBarNotificationExtractor.isMessage(statusBarNotification)

    private fun resolveReplyAction(actions: Array<Notification.Action>?): Notification.Action? {
        if (actions == null) {
            return null
        }
        // mute and reply buttons
        if (actions.size < 2) {
            return null
        }
        return actions[1]
    }

    private fun persistReplyAction(chatId: String, action: Notification.Action) {
        Timber.i("Persisted reply action chat ID [%s] title [%s] action [%s]", chatId, action.title, action)
        lineReplyActionDao.saveLineReplyAction(chatId, action)
    }
}
