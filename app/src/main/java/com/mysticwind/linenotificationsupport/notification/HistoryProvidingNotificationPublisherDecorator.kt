package com.mysticwind.linenotificationsupport.notification

import android.annotation.SuppressLint
import android.app.Notification
import android.os.Build
import android.service.notification.StatusBarNotification
import androidx.core.app.Person
import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import com.google.common.collect.Multimaps
import com.mysticwind.linenotificationsupport.model.LineNotification
import com.mysticwind.linenotificationsupport.model.LineNotificationBuilder
import com.mysticwind.linenotificationsupport.model.NotificationHistoryEntry
import com.mysticwind.linenotificationsupport.preference.PreferenceProvider
import com.mysticwind.linenotificationsupport.utils.LineNotificationSupportMessageExtractor
import com.mysticwind.linenotificationsupport.utils.NotificationExtractor
import org.apache.commons.lang3.StringUtils
import timber.log.Timber
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

class HistoryProvidingNotificationPublisherDecorator : NotificationPublisher {

    private val chatIdToHistoryMap: Multimap<String, NotificationHistoryEntry> =
        Multimaps.synchronizedSetMultimap(HashMultimap.create())
    private val notificationToChatIdMap: MutableMap<Int, String> = ConcurrentHashMap()
    private val fallbackChatIdToNotificationIdMap: MutableMap<String, Int> = ConcurrentHashMap()

    private val notificationPublisher: NotificationPublisher
    private val preferenceProvider: PreferenceProvider

    constructor(
        notificationPublisher: NotificationPublisher,
        preferenceProvider: PreferenceProvider
    ) {
        this.notificationPublisher = notificationPublisher
        this.preferenceProvider = preferenceProvider
    }

    constructor(
        notificationPublisher: NotificationPublisher,
        preferenceProvider: PreferenceProvider,
        existingNotifications: List<StatusBarNotification>
    ) {
        this.notificationPublisher = notificationPublisher
        this.preferenceProvider = preferenceProvider
        for (notification in existingNotifications) {
            Timber.d(
                "Existing notification to restore: group [%s] key [%s] chat ID [%s] message ID [%s]",
                notification.notification.group,
                notification.key,
                NotificationExtractor.getLineNotificationSupportChatId(notification.notification),
                NotificationExtractor.getLineMessageId(notification.notification)
            )
        }
        existingNotifications.stream()
            .filter { notification ->
                NotificationExtractor.getLineNotificationSupportChatId(notification.notification).isPresent
            }
            .forEach { notification ->
                val messageHistory = getMessageHistory(notification.notification)
                Timber.i("Restoring history with message history [%s]", messageHistory)
                if (messageHistory.isEmpty()) {
                    return@forEach
                }
                for (message in messageHistory) {
                    if (!LineNotificationSupportMessageExtractor.getMessageId(message).isPresent) {
                        continue
                    }
                    val chatId = LineNotificationSupportMessageExtractor.getChatId(message)
                    if (!chatId.isPresent) {
                        continue
                    }
                    val historyEntry = NotificationHistoryEntry(
                        LineNotificationSupportMessageExtractor.getMessageId(message).get(),
                        message.text.toString(),
                        getPerson(message),
                        message.timestamp,
                        LineNotificationSupportMessageExtractor.getStickerUrl(message).orElse(null)
                    )
                    Timber.i(
                        "Restore history chat ID [%s], history message ID [%s] message [%s] sticker [%s]",
                        chatId.get(), historyEntry.lineMessageId, historyEntry.message, historyEntry.getLineStickerUrl().orElse(null)
                    )
                    chatIdToHistoryMap.put(chatId.get(), historyEntry)
                }
            }
    }

    @SuppressLint("RestrictedApi")
    private fun getPerson(message: Notification.MessagingStyle.Message): Person {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            message.senderPerson?.let { Person.fromAndroidPerson(it) } ?: Person.Builder().setName("?").build()
        } else {
            val senderName = LineNotificationSupportMessageExtractor.getSender(message).orElse("?")
            Timber.w("Using sender name [%s] for message text [%s]", senderName, message.text)
            Person.Builder()
                .setName(senderName)
                .build()
        }
    }

    private fun getMessageHistory(notification: Notification): List<Notification.MessagingStyle.Message> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Notification.MessagingStyle.Message.getMessagesFromBundleArray(
                notification.extras.getParcelableArray("android.messages")
            )
        } else {
            @Suppress("UNCHECKED_CAST")
            Collections.EMPTY_LIST as List<Notification.MessagingStyle.Message>
        }
    }

    override fun publishNotification(lineNotification: LineNotification, notificationId: Int) {
        insertOrUpdateHistory(lineNotification)

        val history = chatIdToHistoryMap.get(lineNotification.chatId).stream()
            .sorted { entry1, entry2 -> (entry1.timestamp - entry2.timestamp).toInt() }
            .collect(java.util.stream.Collectors.toList())

        val lastNotificationEntry = history.removeAt(history.size - 1)

        val selectedNotificationId = resolveNotificationId(lineNotification.chatId!!, notificationId)

        Timber.d(
            "Publishing notification with history: id [%s] chat ID [%s] history size [%d] latest message [%s]",
            selectedNotificationId,
            lineNotification.chatId,
            history.size,
            lastNotificationEntry.message
        )

        this.notificationPublisher.publishNotification(
            lineNotification.toBuilder()
                .lineMessageId(lastNotificationEntry.lineMessageId)
                .message(lastNotificationEntry.message)
                .sender(lastNotificationEntry.sender)
                .timestamp(lastNotificationEntry.timestamp)
                .lineStickerUrl(lastNotificationEntry.getLineStickerUrl().orElse(null))
                .history(history)
                .build(),
            selectedNotificationId
        )
    }

    private fun resolveNotificationId(chatId: String, notificationId: Int): Int {
        if (LineNotificationBuilder.CALL_VIRTUAL_CHAT_ID == chatId &&
            preferenceProvider.shouldCreateNewContinuousCallNotifications()
        ) {
            return notificationId
        }
        val hashCode = chatId.hashCode()
        val storedChatId = notificationToChatIdMap[hashCode]
        return when {
            storedChatId == null -> {
                notificationToChatIdMap[hashCode] = chatId
                hashCode
            }
            chatId == storedChatId -> hashCode
            else -> {
                // fallback that should almost never happen
                val selectedNotificationId = fallbackChatIdToNotificationIdMap.computeIfAbsent(chatId) { notificationId }
                // TODO what if there is a clash with the notification IDs and the hash codes?
                Timber.w(
                    "Chat ID [%s] hash [%d] has been used, using notification ID [%d] instead",
                    chatId, hashCode, selectedNotificationId
                )
                selectedNotificationId
            }
        }
    }

    private fun insertOrUpdateHistory(lineNotification: LineNotification) {
        val history = chatIdToHistoryMap.get(lineNotification.chatId)
        // remove existing entry for the "New Message" use case
        history.stream()
            .filter { entry -> StringUtils.equals(entry.lineMessageId, lineNotification.lineMessageId) }
            .findAny()
            .ifPresent { entry ->
                chatIdToHistoryMap.remove(lineNotification.chatId, entry)
            }

        // add new entry into history
        chatIdToHistoryMap.put(
            lineNotification.chatId,
            NotificationHistoryEntry(
                lineNotification.lineMessageId,
                lineNotification.message,
                lineNotification.sender,
                lineNotification.timestamp,
                lineNotification.lineStickerUrl
            )
        )
    }

    override fun republishNotification(lineNotification: LineNotification, notificationId: Int) {
        // do nothing
        notificationPublisher.republishNotification(lineNotification, notificationId)
    }

    override fun updateNotificationDismissed(statusBarNotification: StatusBarNotification) {
        val chatId = NotificationExtractor.getLineNotificationSupportChatId(statusBarNotification.notification)

        if (chatId.isPresent) {
            // clean cache
            Timber.d(
                "Cleaning notification history with chatId [%s], number of items [%d]",
                chatId, chatIdToHistoryMap.get(chatId.get()).size
            )
            chatIdToHistoryMap.removeAll(chatId.get())
        }

        notificationPublisher.updateNotificationDismissed(statusBarNotification)
    }
}
