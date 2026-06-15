package com.mysticwind.linenotificationsupport.notification.reactor

import android.service.notification.StatusBarNotification
import com.google.common.collect.ImmutableSet
import com.mysticwind.linenotificationsupport.module.HiltQualifiers
import com.mysticwind.linenotificationsupport.utils.NotificationExtractor
import com.mysticwind.linenotificationsupport.utils.StatusBarNotificationExtractor
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoggingDismissedNotificationReactor @Inject constructor(
    @HiltQualifiers.PackageName packageName: String
) : DismissedNotificationReactor {

    private val interestedPackages: Set<String> = ImmutableSet.of(packageName)

    override fun interestedPackages(): Collection<String> = interestedPackages

    override fun isInterestInNotificationGroup(): Boolean = true

    override fun reactToDismissedNotification(statusBarNotification: StatusBarNotification): Reaction {
        Timber.d(
            "Dismissed LNS notification key [%s] id [%d] group [%s] isSummary [%s] title [%s] message [%s]",
            statusBarNotification.key,
            statusBarNotification.id,
            statusBarNotification.notification.group,
            StatusBarNotificationExtractor.isSummary(statusBarNotification),
            NotificationExtractor.getTitle(statusBarNotification.notification),
            NotificationExtractor.getMessage(statusBarNotification.notification)
        )
        return Reaction.NONE
    }
}
