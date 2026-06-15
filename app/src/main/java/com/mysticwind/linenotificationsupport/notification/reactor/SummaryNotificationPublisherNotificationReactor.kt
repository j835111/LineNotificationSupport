package com.mysticwind.linenotificationsupport.notification.reactor

import android.service.notification.StatusBarNotification
import com.google.common.collect.ImmutableSet
import com.mysticwind.linenotificationsupport.module.HiltQualifiers
import com.mysticwind.linenotificationsupport.notification.SummaryNotificationPublisher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SummaryNotificationPublisherNotificationReactor @Inject constructor(
    @HiltQualifiers.PackageName thisPackageName: String,
    private val summaryNotificationPublisher: SummaryNotificationPublisher
) : IncomingNotificationReactor, DismissedNotificationReactor {

    private val interestedPackages: Set<String> = ImmutableSet.of(thisPackageName)

    override fun interestedPackages(): Collection<String> = interestedPackages

    override fun isInterestInNotificationGroup(): Boolean = false

    override fun reactToIncomingNotification(statusBarNotification: StatusBarNotification): Reaction {
        summaryNotificationPublisher.updateSummaryWhenNotificationsPublished(
            statusBarNotification.notification.group
        )
        return Reaction.NONE
    }

    override fun reactToDismissedNotification(statusBarNotification: StatusBarNotification): Reaction {
        summaryNotificationPublisher.updateSummaryWhenNotificationsDismissed(
            statusBarNotification.notification.group
        )
        return Reaction.NONE
    }
}
