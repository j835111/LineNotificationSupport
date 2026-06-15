package com.mysticwind.linenotificationsupport.notification.reactor

import android.service.notification.StatusBarNotification
import com.google.common.collect.ImmutableList
import com.mysticwind.linenotificationsupport.module.HiltQualifiers
import com.mysticwind.linenotificationsupport.notification.NotificationPublisherFactory
import org.apache.commons.lang3.Validate
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PublishedNotificationTrackerIncomingNotificationReactor @Inject constructor(
    @HiltQualifiers.PackageName packageName: String,
    private val notificationPublisherFactory: NotificationPublisherFactory
) : IncomingNotificationReactor {

    private val packageName: String = Validate.notBlank(packageName)

    override fun interestedPackages(): Collection<String> = ImmutableList.of(packageName)

    override fun isInterestInNotificationGroup(): Boolean = false

    override fun reactToIncomingNotification(statusBarNotification: StatusBarNotification): Reaction {
        Timber.d("Tracking published notification: [%d]", statusBarNotification.id)
        notificationPublisherFactory.trackNotificationPublished(statusBarNotification.id)
        return Reaction.NONE
    }
}
