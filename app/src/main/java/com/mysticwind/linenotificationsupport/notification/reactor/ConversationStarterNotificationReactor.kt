package com.mysticwind.linenotificationsupport.notification.reactor

import android.os.Handler
import android.service.notification.StatusBarNotification
import com.google.common.collect.ImmutableSet
import com.mysticwind.linenotificationsupport.conversationstarter.ConversationStarterNotificationManager
import com.mysticwind.linenotificationsupport.line.Constants
import com.mysticwind.linenotificationsupport.module.HiltQualifiers
import com.mysticwind.linenotificationsupport.notification.MaxNotificationHandlingNotificationPublisherDecorator
import com.mysticwind.linenotificationsupport.preference.PreferenceProvider
import com.mysticwind.linenotificationsupport.utils.NotificationExtractor
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.Validate
import timber.log.Timber
import java.util.HashSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversationStarterNotificationReactor @Inject constructor(
    @HiltQualifiers.PackageName private val thisPackageName: String,
    private val conversationStarterNotificationManager: ConversationStarterNotificationManager,
    private val preferenceProvider: PreferenceProvider,
    private val handler: Handler
) : IncomingNotificationReactor, DismissedNotificationReactor {

    private val interestedPackages: Set<String>
    private var knownAvailableChatIds: MutableSet<String> = HashSet()

    init {
        Validate.notBlank(thisPackageName)
        // LINE for incoming and self for dismissing
        interestedPackages = ImmutableSet.of(Constants.LINE_PACKAGE_NAME, thisPackageName)
    }

    override fun interestedPackages(): Collection<String> = interestedPackages

    override fun isInterestInNotificationGroup(): Boolean = false

    override fun reactToIncomingNotification(statusBarNotification: StatusBarNotification): Reaction {
        if (!preferenceProvider.shouldShowConversationStarterNotification()) {
            return Reaction.NONE
        }

        if (Constants.LINE_PACKAGE_NAME != statusBarNotification.packageName) {
            return Reaction.NONE
        }

        val chatId = NotificationExtractor.getLineChatId(statusBarNotification.notification)
        if (StringUtils.isBlank(chatId)) {
            return Reaction.NONE
        }
        if (knownAvailableChatIds.contains(chatId)) {
            return Reaction.NONE
        }
        publishNotification()
        return Reaction.NONE
    }

    private fun publishNotification() {
        val chatIds = conversationStarterNotificationManager.publishNotification()
        knownAvailableChatIds.addAll(chatIds)
    }

    override fun reactToDismissedNotification(statusBarNotification: StatusBarNotification): Reaction {
        if (!preferenceProvider.shouldShowConversationStarterNotification()) {
            return Reaction.NONE
        }

        if (thisPackageName != statusBarNotification.packageName) {
            return Reaction.NONE
        }
        val chatId = NotificationExtractor.getLineNotificationSupportChatId(statusBarNotification.notification)
        if (!chatId.isPresent) {
            return Reaction.NONE
        }
        if (ConversationStarterNotificationManager.CONVERSATION_STARTER_CHAT_ID != chatId.get()) {
            return Reaction.NONE
        }
        Timber.d("Detected dismiss of conversation starter notification")
        handler.postDelayed({
            Timber.d("Republish conversation starter notification")
            publishNotification()
        }, MaxNotificationHandlingNotificationPublisherDecorator.DISMISS_COOL_DOWN_IN_MILLIS)
        return Reaction.NONE
    }
}
