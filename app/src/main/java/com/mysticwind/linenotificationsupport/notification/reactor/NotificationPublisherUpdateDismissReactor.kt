package com.mysticwind.linenotificationsupport.notification.reactor

import android.service.notification.StatusBarNotification
import com.google.common.collect.ImmutableSet
import com.mysticwind.linenotificationsupport.module.HiltQualifiers
import com.mysticwind.linenotificationsupport.notification.NotificationPublisherFactory
import org.apache.commons.lang3.Validate
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationPublisherUpdateDismissReactor @Inject constructor(
    @HiltQualifiers.PackageName packageName: String,
    private val notificationPublisherFactory: NotificationPublisherFactory
) : DismissedNotificationReactor {

    private val packageName: String = Validate.notBlank(packageName)

    override fun interestedPackages(): Collection<String> = ImmutableSet.of(packageName)

    override fun isInterestInNotificationGroup(): Boolean = false

    override fun reactToDismissedNotification(statusBarNotification: StatusBarNotification): Reaction {
        Timber.d("Received notification dismiss [%d]", statusBarNotification.id)
        notificationPublisherFactory.get().updateNotificationDismissed(statusBarNotification)
        return Reaction.NONE
    }
}
