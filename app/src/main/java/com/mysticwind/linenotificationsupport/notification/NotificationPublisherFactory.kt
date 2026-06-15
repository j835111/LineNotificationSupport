package com.mysticwind.linenotificationsupport.notification

import android.content.Context
import android.os.Handler
import android.service.notification.StatusBarNotification
import com.mysticwind.linenotificationsupport.preference.PreferenceProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Collections
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationPublisherFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val simpleNotificationPublisher: SimpleNotificationPublisher,
    private val handler: Handler,
    private val preferenceProvider: PreferenceProvider,
    private val slotAvailabilityChecker: SlotAvailabilityChecker
) {

    private var notificationPublisher: NotificationPublisher = NullNotificationPublisher.INSTANCE

    private var resendUnsentNotificationsNotificationSentListener: ResendUnsentNotificationsNotificationSentListener? = null

    fun get(): NotificationPublisher {
        return notificationPublisher
    }

    fun notifyChange() {
        this.notificationPublisher = buildNotificationPublisherWithPreviousStateRestored(Collections.emptyList())
    }

    fun notifyChangeWithExistingNotifications(existingNotifications: List<StatusBarNotification>) {
        this.notificationPublisher = buildNotificationPublisherWithPreviousStateRestored(existingNotifications)
    }

    private fun buildNotificationPublisherWithPreviousStateRestored(
        existingNotifications: List<StatusBarNotification>
    ): NotificationPublisher {
        val shouldExecuteMaxNotificationWorkaround = preferenceProvider.shouldExecuteMaxNotificationWorkaround()

        val notificationSentListeners = mutableListOf<NotificationSentListener>()
        // don't enable this for single notification conversations just yet because we may still
        // exceed 25 chats
        if (shouldExecuteMaxNotificationWorkaround) {
            resendUnsentNotificationsNotificationSentListener =
                ResendUnsentNotificationsNotificationSentListener(handler, this)
            notificationSentListeners.add(resendUnsentNotificationsNotificationSentListener!!)
        } else {
            resendUnsentNotificationsNotificationSentListener = null
        }

        var notificationPublisher: NotificationPublisher = simpleNotificationPublisher
        simpleNotificationPublisher.setNotificationSentListeners(notificationSentListeners)

        // this should come after HistoryProvidingNotificationPublisherDecorator as it changes the notification ID
        notificationPublisher = DismissActionInjectorNotificationPublisherDecorator(
            notificationPublisher, context
        )

        if (preferenceProvider.shouldUseSingleNotificationForConversations()) {
            // do this before LinkActionInjectorNotificationPublisherDecorator
            // so that link mutations are also persisted
            notificationPublisher = HistoryProvidingNotificationPublisherDecorator(
                notificationPublisher, preferenceProvider, existingNotifications
            )
        }

        notificationPublisher = LinkActionInjectorNotificationPublisherDecorator(
            notificationPublisher, context
        )

        if (shouldExecuteMaxNotificationWorkaround) {
            notificationPublisher = MaxNotificationHandlingNotificationPublisherDecorator(
                handler, notificationPublisher, slotAvailabilityChecker
            )
        }

        if (preferenceProvider.shouldUseMessageSplitter()) {
            notificationPublisher = BigNotificationSplittingNotificationPublisherDecorator(
                notificationPublisher,
                preferenceProvider
            )
        }

        notificationPublisher = NotificationMergingNotificationPublisherDecorator(notificationPublisher)

        return notificationPublisher
    }

    fun trackNotificationPublished(id: Int) {
        resendUnsentNotificationsNotificationSentListener?.notificationReceived(id)
    }
}
