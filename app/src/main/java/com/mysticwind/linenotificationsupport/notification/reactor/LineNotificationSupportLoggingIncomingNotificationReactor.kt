package com.mysticwind.linenotificationsupport.notification.reactor

import android.service.notification.StatusBarNotification
import com.google.common.collect.ImmutableSet
import com.mysticwind.linenotificationsupport.module.HiltQualifiers
import com.mysticwind.linenotificationsupport.utils.StatusBarNotificationPrinter
import org.apache.commons.lang3.Validate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LineNotificationSupportLoggingIncomingNotificationReactor @Inject constructor(
    @HiltQualifiers.PackageName packageName: String,
    private val statusBarNotificationPrinter: StatusBarNotificationPrinter
) : IncomingNotificationReactor {

    private val packageNames: Set<String> = ImmutableSet.of(Validate.notBlank(packageName))

    override fun interestedPackages(): Collection<String> = packageNames

    override fun isInterestInNotificationGroup(): Boolean = true

    override fun reactToIncomingNotification(statusBarNotification: StatusBarNotification): Reaction {
        statusBarNotificationPrinter.print("LNS Published", statusBarNotification)
        return Reaction.NONE
    }
}
