package com.mysticwind.linenotificationsupport.conversationstarter

import com.google.common.collect.ImmutableList
import com.mysticwind.linenotificationsupport.R
import com.mysticwind.linenotificationsupport.conversationstarter.model.KeywordEntry
import com.mysticwind.linenotificationsupport.model.LineNotification
import com.mysticwind.linenotificationsupport.notification.AndroidNotificationManager
import com.mysticwind.linenotificationsupport.notification.NotificationPublisherFactory
import com.mysticwind.linenotificationsupport.reply.MyPersonLabelProvider
import com.mysticwind.linenotificationsupport.ui.LocalizationDao
import com.mysticwind.linenotificationsupport.utils.NotificationIdGenerator
import java.time.Instant
import java.util.Objects
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversationStarterNotificationManager @Inject constructor(
    private val notificationPublisherFactory: NotificationPublisherFactory,
    notificationIdGenerator: NotificationIdGenerator,
    private val chatKeywordManager: ChatKeywordManager,
    private val startConversationActionBuilder: StartConversationActionBuilder,
    private val keywordSettingActivityLauncher: KeywordSettingActivityLauncher,
    private val myPersonLabelProvider: MyPersonLabelProvider,
    private val androidNotificationManager: AndroidNotificationManager,
    private val localizationDao: LocalizationDao
) {
    companion object {
        const val CONVERSATION_STARTER_CHAT_ID = "CONVERSATION-STARTER-CHAT-ID"
    }

    private val notificationId: Int

    init {
        Objects.requireNonNull(notificationPublisherFactory)
        Objects.requireNonNull(notificationIdGenerator)
        Objects.requireNonNull(chatKeywordManager)
        Objects.requireNonNull(startConversationActionBuilder)
        Objects.requireNonNull(keywordSettingActivityLauncher)
        Objects.requireNonNull(myPersonLabelProvider)
        Objects.requireNonNull(androidNotificationManager)
        Objects.requireNonNull(localizationDao)
        notificationId = notificationIdGenerator.getNextNotificationId()
    }

    fun publishNotification(): Set<String> {
        val keywordEntryList = chatKeywordManager.getAvailableKeywordToChatNameMap()

        val messages = resolveMessages(keywordEntryList)

        // let's see if we get bitten by building a fake LineNotification
        notificationPublisherFactory.get().publishNotification(
            LineNotification.builder()
                .title(localizationDao.getLocalizedString(R.string.conversation_start_notification_title))
                .messages(messages)
                .timestamp(Instant.now().toEpochMilli())
                .isSelfResponse(true)
                .chatId(CONVERSATION_STARTER_CHAT_ID)
                .sender(myPersonLabelProvider.getMyPerson())
                .actions(startConversationActionBuilder.buildActions())
                .clickIntent(keywordSettingActivityLauncher.buildPendingIntent())
                .build(),
            notificationId
        )
        return keywordEntryList
            .filter { it.hasReplyAction }
            .filter { it.keyword.isPresent }
            .map { it.chatId }
            .toSet()
    }

    private fun resolveMessages(keywordEntryList: List<KeywordEntry>): List<String> {
        val availableKeywordEntries = keywordEntryList
            .filter { it.hasReplyAction }
            .filter { it.keyword.isPresent }

        if (availableKeywordEntries.isNotEmpty()) {
            val availableKeywordMessage = availableKeywordEntries
                .map { entry -> "${entry.keyword.get()} -> ${entry.chatName}" }
                .reduce { s1, s2 -> "$s1\n$s2" }
            val firstKeywordEntry = availableKeywordEntries[0]
            val sampleMessage = localizationDao.getLocalizedString(R.string.conversation_start_notification_content_sample_message)
            val sampleMessageWithKeyword = firstKeywordEntry.keyword.get() + " " + sampleMessage
            return ImmutableList.of(
                localizationDao.getLocalizedString(
                    R.string.conversation_start_notification_content_guidance,
                    sampleMessageWithKeyword,
                    firstKeywordEntry.chatName,
                    sampleMessage
                ),
                localizationDao.getLocalizedString(R.string.conversation_start_notification_content_list_of_chat_prefix) + availableKeywordMessage
            )
        }

        if (keywordEntryList.isNotEmpty()) {
            return ImmutableList.of(
                localizationDao.getLocalizedString(R.string.conversation_start_notification_content_no_reply_action)
            )
        }

        return ImmutableList.of(
            localizationDao.getLocalizedString(R.string.conversation_start_notification_content_no_keywords)
        )
    }

    fun cancelNotification() {
        androidNotificationManager.cancelNotification(CONVERSATION_STARTER_CHAT_ID)
    }
}
