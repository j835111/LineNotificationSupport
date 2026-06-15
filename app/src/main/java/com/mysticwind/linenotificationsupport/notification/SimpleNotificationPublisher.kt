package com.mysticwind.linenotificationsupport.notification

import android.content.Context
import android.service.notification.StatusBarNotification
import com.mysticwind.linenotificationsupport.model.LineNotification
import com.mysticwind.linenotificationsupport.module.HiltQualifiers
import com.mysticwind.linenotificationsupport.notificationgroup.NotificationGroupCreator
import com.mysticwind.linenotificationsupport.preference.PreferenceProvider
import com.mysticwind.linenotificationsupport.utils.BigPictureStyleImageSupportedNotificationPublisherAsyncTask
import com.mysticwind.linenotificationsupport.utils.GroupIdResolver
import com.mysticwind.linenotificationsupport.utils.MessageStyleImageSupportedNotificationPublisherAsyncTask
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Collections
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SimpleNotificationPublisher @Inject constructor(
    @ApplicationContext private val context: Context,
    @HiltQualifiers.PackageName private val packageName: String,
    private val groupIdResolver: GroupIdResolver,
    private val preferenceProvider: PreferenceProvider,
    private val notificationGroupCreator: NotificationGroupCreator
) : NotificationPublisher {

    private var notificationSentListeners: Collection<NotificationSentListener> = Collections.emptyList()

    fun setNotificationSentListeners(notificationSentListeners: Collection<NotificationSentListener>) {
        this.notificationSentListeners = notificationSentListeners
    }

    override fun publishNotification(lineNotification: LineNotification, notificationId: Int) {
        if (preferenceProvider.shouldUseLegacyStickerLoader()) {
            BigPictureStyleImageSupportedNotificationPublisherAsyncTask(
                context, notificationGroupCreator, lineNotification, notificationId
            ).execute()
        } else {
            val useSingleNotificationConversations = preferenceProvider.shouldUseSingleNotificationForConversations()
            MessageStyleImageSupportedNotificationPublisherAsyncTask(
                context, notificationGroupCreator, lineNotification, notificationId, useSingleNotificationConversations
            ).execute()
        }
        notificationSentListeners.forEach { listener ->
            listener.notificationSent(lineNotification, notificationId)
        }
    }

    override fun republishNotification(lineNotification: LineNotification, notificationId: Int) {
        publishNotification(lineNotification, notificationId)
    }

    override fun updateNotificationDismissed(statusBarNotification: StatusBarNotification) {
        // do nothing
    }
}
