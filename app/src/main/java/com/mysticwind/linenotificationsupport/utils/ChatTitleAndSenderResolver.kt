package com.mysticwind.linenotificationsupport.utils

import android.service.notification.StatusBarNotification
import com.mysticwind.linenotificationsupport.chatname.ChatNameManager
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.tuple.Pair
import timber.log.Timber
import java.util.Objects
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatTitleAndSenderResolver @Inject constructor(
    private val chatNameManager: ChatNameManager
) {

    init {
        Objects.requireNonNull(chatNameManager)
    }

    fun resolveTitleAndSender(statusBarNotification: StatusBarNotification): Pair<String, String> {
        // individual: android.title is the sender
        // group chat: android.title is "group title：sender", android.conversationTitle is group title
        // chat with multi-folks: android.title is also the sender, no way to differentiate between individual and multi-folks :(

        // it is straightforward for chat groups
        var sender = getAndroidTitle(statusBarNotification)

        // just use the sender if not chat (e.g. calls)
        val chatId = NotificationExtractor.getLineChatId(statusBarNotification.notification)
        if (StringUtils.isBlank(chatId)) {
            return Pair.of(sender, sender)
        }

        var highConfidenceChatGroupName: String? = null
        if (isChatGroup(statusBarNotification)) {
            highConfidenceChatGroupName = getGroupChatTitle(statusBarNotification)
            sender = calculateGroupSender(statusBarNotification)
        }

        val chatName = chatNameManager.getChatName(chatId ?: "", sender, highConfidenceChatGroupName)
        return Pair.of(chatName, sender)
    }

    private fun isChatGroup(statusBarNotification: StatusBarNotification): Boolean {
        val title = getGroupChatTitle(statusBarNotification)
        return StringUtils.isNotBlank(title)
    }

    private fun getGroupChatTitle(statusBarNotification: StatusBarNotification): String? {
        val subText = NotificationExtractor.getSubText(statusBarNotification.notification)
        if (StringUtils.isNotBlank(subText)) {
            return subText
        }
        // chat groups will have a conversationTitle (but not groups of people)
        return NotificationExtractor.getConversationTitle(statusBarNotification.notification)
    }

    private fun calculateGroupSender(statusBarNotification: StatusBarNotification): String {
        val groupName = getGroupChatTitle(statusBarNotification)
        // group title will be something like GROUP_NAME: SENDER
        val androidTitle = getAndroidTitle(statusBarNotification)
        // tickerText will be something like SENDER: message
        val tickerText = statusBarNotification.notification.tickerText.toString()

        // step 1: remove GROUP_NAME from androidTitle (remainder: ": SENDER")
        val androidTitleWithoutGroupName = androidTitle.replace(groupName ?: "", "")
        // step 2: find the common substring from the results in step 1 and 2
        for (index in androidTitleWithoutGroupName.indices) {
            val character = androidTitleWithoutGroupName[index]
            if (character == tickerText[0]) {
                // we might have found a match - may not be a match if the sender starts with colon (is it possible?)
                val potentialMatch = androidTitleWithoutGroupName.substring(index)
                if (tickerText.startsWith(potentialMatch)) {
                    return potentialMatch
                }
            }
        }
        // fallback if we can't find a common substring for whatever reason
        Timber.w("Cannot find common substring with group:(%s) title:(%s) ticker(%s)",
            groupName, androidTitle, tickerText)
        return tickerText
    }

    private fun getAndroidTitle(statusBarNotification: StatusBarNotification): String {
        return NotificationExtractor.getTitle(statusBarNotification.notification) ?: ""
    }
}
