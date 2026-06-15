package com.mysticwind.linenotificationsupport.preference

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceProvider @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {

    companion object {
        const val MANAGE_LINE_MESSAGE_NOTIFICATIONS_PREFERENCE_KEY = "manage_line_message_notifications"
        const val AUTO_DISMISS_TRANSFORMED_MESSAGES_PREFERENCE_KEY = "auto_dismiss_line_notification_support_messages"
        const val MERGE_NOTIFICATION_CHANNEL_PREFERENCE_KEY = "merge_message_notification_channels"
        const val MAX_NOTIFICATION_WORKAROUND_PREFERENCE_KEY = "max_notification_workaround"
        const val USE_LEGACY_STICKER_LOADER_PREFERENCE_KEY = "use_legacy_sticker_loader"
        const val USE_MESSAGE_SPLITTER_PREFERENCE_KEY = "use_big_message_splitter"
        const val MESSAGE_SIZE_LIMIT_PREFERENCE_KEY = "message_size_limit"
        const val SPLIT_MESSAGE_MAX_PAGES_KEY = "split_message_max_pages"
        const val SINGLE_NOTIFICATION_CONVERSATIONS_KEY = "single_notification_with_history"
        const val GENERATE_SELF_RESPONSE_MESSAGE_KEY = "generate_self_response_message"
        const val BLUETOOTH_CONTROL_ONGOING_CALL_KEY = "bluetooth_control_in_calls"
        const val CONVERSATION_STARTER_KEY = "conversation_starter"
        const val CREATE_NEW_CONTINUOUS_CALL_NOTIFICATIONS_KEY = "create_new_continuous_call_notifications"
    }

    fun shouldManageLineMessageNotifications(): Boolean {
        return sharedPreferences.getBoolean(MANAGE_LINE_MESSAGE_NOTIFICATIONS_PREFERENCE_KEY, false)
    }

    fun shouldAutoDismissLineNotificationSupportNotifications(): Boolean {
        if (shouldManageLineMessageNotifications()) {
            return false
        }
        return sharedPreferences.getBoolean(AUTO_DISMISS_TRANSFORMED_MESSAGES_PREFERENCE_KEY, true)
    }

    fun shouldUseMergeMessageChatId(): Boolean {
        return sharedPreferences.getBoolean(MERGE_NOTIFICATION_CHANNEL_PREFERENCE_KEY, false)
    }

    fun shouldExecuteMaxNotificationWorkaround(): Boolean {
        if (shouldUseSingleNotificationForConversations()) {
            return false
        }
        return sharedPreferences.getBoolean(MAX_NOTIFICATION_WORKAROUND_PREFERENCE_KEY, true)
    }

    fun shouldUseLegacyStickerLoader(): Boolean {
        if (shouldUseSingleNotificationForConversations()) {
            return false
        }
        return sharedPreferences.getBoolean(USE_LEGACY_STICKER_LOADER_PREFERENCE_KEY, false)
    }

    fun shouldUseMessageSplitter(): Boolean {
        if (shouldUseSingleNotificationForConversations()) {
            return false
        }
        return sharedPreferences.getBoolean(USE_MESSAGE_SPLITTER_PREFERENCE_KEY, true)
    }

    fun getMessageSizeLimit(): Int {
        if (!shouldUseMessageSplitter() || shouldUseSingleNotificationForConversations()) {
            throw IllegalArgumentException(
                String.format("Should not need message size limit. Use message splitter [%s] Use single notification [%]",
                    shouldUseMessageSplitter(), shouldUseSingleNotificationForConversations()))
        }
        return sharedPreferences.getInt(MESSAGE_SIZE_LIMIT_PREFERENCE_KEY, 60)
    }

    fun getMaxPageCount(): Int {
        if (!shouldUseMessageSplitter() || shouldUseSingleNotificationForConversations()) {
            throw IllegalArgumentException(
                String.format("Should not need max page count. Use message splitter [%s] Use single notification [%]",
                    shouldUseMessageSplitter(), shouldUseSingleNotificationForConversations()))
        }
        return sharedPreferences.getInt(SPLIT_MESSAGE_MAX_PAGES_KEY, 5)
    }

    fun shouldUseSingleNotificationForConversations(): Boolean {
        return sharedPreferences.getBoolean(SINGLE_NOTIFICATION_CONVERSATIONS_KEY, false)
    }

    fun shouldGenerateSelfResponseMessage(): Boolean {
        return sharedPreferences.getBoolean(GENERATE_SELF_RESPONSE_MESSAGE_KEY, true)
    }

    fun shouldControlBluetoothDuringCalls(): Boolean {
        return sharedPreferences.getBoolean(BLUETOOTH_CONTROL_ONGOING_CALL_KEY, false)
    }

    fun shouldShowConversationStarterNotification(): Boolean {
        return sharedPreferences.getBoolean(CONVERSATION_STARTER_KEY, true)
    }

    fun shouldCreateNewContinuousCallNotifications(): Boolean {
        return sharedPreferences.getBoolean(CREATE_NEW_CONTINUOUS_CALL_NOTIFICATIONS_KEY, true)
    }
}
