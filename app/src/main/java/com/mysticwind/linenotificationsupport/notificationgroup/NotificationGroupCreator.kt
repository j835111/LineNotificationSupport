package com.mysticwind.linenotificationsupport.notificationgroup

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import com.mysticwind.linenotificationsupport.android.AndroidFeatureProvider
import com.mysticwind.linenotificationsupport.model.LineNotificationBuilder
import com.mysticwind.linenotificationsupport.preference.PreferenceProvider
import org.apache.commons.lang3.StringUtils
import java.util.Optional
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationGroupCreator @Inject constructor(
    private val notificationManager: NotificationManager,
    private val androidFeatureProvider: AndroidFeatureProvider,
    private val preferenceProvider: PreferenceProvider
) {

    companion object {
        @JvmField val MESSAGE_NOTIFICATION_GROUP_ID = "message_notification_group"
        @JvmField val MESSAGE_NOTIFICATION_GROUP_NAME = "Messages"
        @JvmField val CALL_NOTIFICATION_GROUP_ID = "call_notification_group"
        @JvmField val CALL_NOTIFICATION_GROUP_NAME = "Calls"
        @JvmField val OTHERS_NOTIFICATION_GROUP_ID = "others_notification_group"
        @JvmField val OTHERS_NOTIFICATION_GROUP_NAME = "Others"

        @JvmField val CALL_CHANNEL_NAME = "Calls"
        @JvmField val MERGED_MESSAGE_CHANNEL_ID = "merged_message_channel_id"
        @JvmField val MERGED_MESSAGE_CHANNEL_NAME = "All Messages"
        @JvmField val SELF_RESPONSE_CHANNEL_ID = "self_response_channel_id"
        @JvmField val SELF_RESPONSE_CHANNEL_NAME = "My Responses"
        private const val NO_CHANNEL_NAME_DEFAULT = "No title"

        private val NOTIFICATION_GROUP_ID_TO_NAME_MAP: Map<String, String> = ImmutableMap.of(
            MESSAGE_NOTIFICATION_GROUP_ID, MESSAGE_NOTIFICATION_GROUP_NAME,
            CALL_NOTIFICATION_GROUP_ID, CALL_NOTIFICATION_GROUP_NAME,
            OTHERS_NOTIFICATION_GROUP_ID, OTHERS_NOTIFICATION_GROUP_NAME
        )

        private val CALL_CHAT_IDS: Set<String> = ImmutableSet.of(LineNotificationBuilder.CALL_VIRTUAL_CHAT_ID)
        private val UNGROUPED_CHAT_ID: Set<String> = ImmutableSet.of(LineNotificationBuilder.DEFAULT_CHAT_ID)
    }

    fun createNotificationGroups() {
        if (!androidFeatureProvider.hasNotificationChannelSupport()) {
            return
        }

        val notificationChannelGroupIds = notificationManager.notificationChannelGroups
            .map { it.id }
            .toSet()

        createNotificationChannelGroupIfNotExist(notificationChannelGroupIds, CALL_NOTIFICATION_GROUP_ID)
        createNotificationChannelGroupIfNotExist(notificationChannelGroupIds, MESSAGE_NOTIFICATION_GROUP_ID)
        createNotificationChannelGroupIfNotExist(notificationChannelGroupIds, OTHERS_NOTIFICATION_GROUP_ID)

        for (notificationChannel in notificationManager.notificationChannels) {
            if (!StringUtils.isBlank(notificationChannel.group)) {
                continue
            }
            addGroupToNotificationChannel(notificationChannel)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    @SuppressLint("NewApi")
    private fun createNotificationChannelGroupIfNotExist(
        existingNotificationChannelGroupIds: Set<String>,
        notificationGroupId: String
    ) {
        if (!existingNotificationChannelGroupIds.contains(notificationGroupId)) {
            val notificationGroupName = NOTIFICATION_GROUP_ID_TO_NAME_MAP[notificationGroupId]
            notificationManager.createNotificationChannelGroup(
                NotificationChannelGroup(notificationGroupId, notificationGroupName)
            )
        }
    }

    @SuppressLint("NewApi")
    private fun createNotificationChannelGroupIfNotExist(notificationGroupId: String) {
        val notificationChannelGroup = notificationManager.notificationChannelGroups
            .map { it.id }
            .firstOrNull()
        if (notificationChannelGroup == null) {
            val notificationGroupName = NOTIFICATION_GROUP_ID_TO_NAME_MAP[notificationGroupId]
            notificationManager.createNotificationChannelGroup(
                NotificationChannelGroup(notificationGroupId, notificationGroupName)
            )
        }
    }

    @SuppressLint("NewApi")
    private fun addGroupToNotificationChannel(notificationChannel: NotificationChannel) {
        val notificationGroupId = resolveNotificationChannelGroup(notificationChannel.id)
        notificationChannel.setGroup(notificationGroupId)
    }

    private fun resolveNotificationChannelGroup(notificationChannelId: String): String {
        return when {
            isCallNotificationChannel(notificationChannelId) -> CALL_NOTIFICATION_GROUP_ID
            SELF_RESPONSE_CHANNEL_ID == notificationChannelId -> OTHERS_NOTIFICATION_GROUP_ID
            isMessageNotificationChannel(notificationChannelId) -> MESSAGE_NOTIFICATION_GROUP_ID
            else -> OTHERS_NOTIFICATION_GROUP_ID
        }
    }

    @SuppressLint("NewApi")
    private fun isCallNotificationChannel(channelId: String): Boolean {
        return CALL_CHAT_IDS.contains(channelId)
    }

    @SuppressLint("NewApi")
    private fun isMessageNotificationChannel(channelId: String): Boolean {
        if (isCallNotificationChannel(channelId)) {
            return false
        }
        if (UNGROUPED_CHAT_ID.contains(channelId)) {
            return false
        }
        return true
    }

    // TODO missing unit tests
    /**
     * Creates the notification channel. This assumes the notification groups have been created.
     * @param chatId
     * @param messageTitle
     */
    fun createNotificationChannel(chatId: String, messageTitle: String): Optional<String> {
        if (!androidFeatureProvider.hasNotificationChannelSupport()) {
            return Optional.empty()
        }
        val channelId = getChannelId(chatId)
        val channelName = getChannelName(channelId, messageTitle)

        createNotificationChannelWithChannelIdAndName(channelId, channelName)

        return Optional.of(channelId)
    }

    fun createSelfResponseNotificationChannel(): Optional<String> {
        if (!androidFeatureProvider.hasNotificationChannelSupport()) {
            return Optional.empty()
        }
        createNotificationChannelWithChannelIdAndName(
            SELF_RESPONSE_CHANNEL_ID,
            SELF_RESPONSE_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_MIN,
            false
        )
        return Optional.of(SELF_RESPONSE_CHANNEL_ID)
    }

    // TODO unit tests
    private fun getChannelId(chatId: String): String {
        if (LineNotificationBuilder.CALL_VIRTUAL_CHAT_ID == chatId ||
            LineNotificationBuilder.DEFAULT_CHAT_ID == chatId
        ) {
            return chatId
        }
        return if (preferenceProvider.shouldUseMergeMessageChatId()) {
            MERGED_MESSAGE_CHANNEL_ID
        } else {
            chatId
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun createNotificationChannelWithChannelIdAndName(channelId: String, channelName: String) {
        var vibrate = false
        var importanceLevel = NotificationManager.IMPORTANCE_DEFAULT
        // TODO should this apply to calls and others?
        if (preferenceProvider.shouldManageLineMessageNotifications()) {
            vibrate = true
            importanceLevel = NotificationManager.IMPORTANCE_HIGH
        }
        createNotificationChannelWithChannelIdAndName(channelId, channelName, importanceLevel, vibrate)
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun createNotificationChannelWithChannelIdAndName(
        channelId: String,
        channelName: String,
        importance: Int,
        vibrate: Boolean
    ) {
        val description = "Notification channel for $channelName"
        val channel = NotificationChannel(channelId, channelName, importance)
        channel.description = description
        channel.enableVibration(vibrate)

        val group = resolveNotificationChannelGroup(channelId)
        createNotificationChannelGroupIfNotExist(group)
        channel.group = group

        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        notificationManager.createNotificationChannel(channel)
    }

    private fun getChannelName(channelId: String, defaultChannelName: String): String {
        if (LineNotificationBuilder.CALL_VIRTUAL_CHAT_ID == channelId) {
            return CALL_CHANNEL_NAME
        } else if (MERGED_MESSAGE_CHANNEL_ID == channelId) {
            return MERGED_MESSAGE_CHANNEL_NAME
        }
        // TODO unit tests
        return if (StringUtils.isBlank(defaultChannelName)) {
            NO_CHANNEL_NAME_DEFAULT
        } else {
            defaultChannelName
        }
    }

    fun migrateToSingleNotificationChannelForMessages() {
        if (!androidFeatureProvider.hasNotificationChannelSupport()) {
            return
        }

        val messageNotificationChannelIds = notificationManager.notificationChannels
            .map { it.id }
            .filter { isMessageNotificationChannel(it) }
            .toSet()

        if (!messageNotificationChannelIds.contains(MERGED_MESSAGE_CHANNEL_ID)) {
            createNotificationChannelWithChannelIdAndName(MERGED_MESSAGE_CHANNEL_ID, MERGED_MESSAGE_CHANNEL_NAME)
        }

        // delete all other notification channels
        messageNotificationChannelIds
            .filter { it != MERGED_MESSAGE_CHANNEL_ID }
            .forEach { notificationManager.deleteNotificationChannel(it) }
    }

    fun migrateToMultipleNotificationChannelsForMessages() {
        if (!androidFeatureProvider.hasNotificationChannelSupport()) {
            return
        }
        notificationManager.deleteNotificationChannel(MERGED_MESSAGE_CHANNEL_ID)
    }
}
