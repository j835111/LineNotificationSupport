package com.mysticwind.linenotificationsupport.notification.reactor

import android.service.notification.StatusBarNotification
import com.google.common.collect.ImmutableSet
import com.mysticwind.linenotificationsupport.chatname.dataaccessor.GroupChatNameDataAccessor
import com.mysticwind.linenotificationsupport.line.Constants
import com.mysticwind.linenotificationsupport.utils.NotificationExtractor
import com.mysticwind.linenotificationsupport.utils.StatusBarNotificationExtractor
import org.apache.commons.lang3.StringUtils
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRoomNamePersistenceIncomingNotificationReactor @Inject constructor(
    private val groupChatNameDataAccessor: GroupChatNameDataAccessor
) : IncomingNotificationReactor {

    companion object {
        private val INTERESTED_PACKAGES: Set<String> = ImmutableSet.of(Constants.LINE_PACKAGE_NAME)
    }

    override fun interestedPackages(): Collection<String> = INTERESTED_PACKAGES

    override fun isInterestInNotificationGroup(): Boolean = true

    override fun reactToIncomingNotification(statusBarNotification: StatusBarNotification): Reaction {
        val chatId = NotificationExtractor.getLineChatId(statusBarNotification.notification)
        val groupChatTitle = getGroupChatTitle(statusBarNotification)
        if (StringUtils.isNotBlank(chatId) && StringUtils.isNotBlank(groupChatTitle)) {
            Timber.d(
                "Identified map of chat ID [%s] to chat name [%s] from a notification key [%s] isSummary [%s] package [%s]",
                chatId,
                groupChatTitle,
                statusBarNotification.key,
                StatusBarNotificationExtractor.isSummary(statusBarNotification),
                statusBarNotification.packageName
            )
            groupChatNameDataAccessor.persistRelationship(chatId!!, groupChatTitle!!)
        }
        return Reaction.NONE
    }

    // TODO this is copied from ChatTitleAndSenderResolver
    private fun getGroupChatTitle(statusBarNotification: StatusBarNotification): String? {
        val subText = NotificationExtractor.getSubText(statusBarNotification.notification)
        if (StringUtils.isNotBlank(subText)) {
            return subText
        }
        // chat groups will have a conversationTitle (but not groups of people)
        return NotificationExtractor.getConversationTitle(statusBarNotification.notification)
    }
}
