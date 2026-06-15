package com.mysticwind.linenotificationsupport.notification.reactor

import android.service.notification.StatusBarNotification
import com.google.common.collect.ImmutableSet
import com.mysticwind.linenotificationsupport.module.HiltQualifiers
import com.mysticwind.linenotificationsupport.notification.impl.DumbNotificationCounter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DumbNotificationCounterNotificationReactor @Inject constructor(
    @HiltQualifiers.PackageName thisPackageName: String,
    private val dumbNotificationCounter: DumbNotificationCounter
) : IncomingNotificationReactor, DismissedNotificationReactor {

    private val interestedPackages: Set<String> = ImmutableSet.of(thisPackageName)

    override fun interestedPackages(): Collection<String> = interestedPackages

    override fun isInterestInNotificationGroup(): Boolean = true

    override fun reactToIncomingNotification(statusBarNotification: StatusBarNotification): Reaction {
        dumbNotificationCounter.notified(
            statusBarNotification.notification.group,
            statusBarNotification.key
        )
        return Reaction.NONE
    }

    override fun reactToDismissedNotification(statusBarNotification: StatusBarNotification): Reaction {
        dumbNotificationCounter.dismissed(
            statusBarNotification.notification.group,
            statusBarNotification.key
        )
        return Reaction.NONE
    }
}
