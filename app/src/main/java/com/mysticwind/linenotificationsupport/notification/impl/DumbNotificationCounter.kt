package com.mysticwind.linenotificationsupport.notification.impl

import android.service.notification.StatusBarNotification
import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import com.google.common.collect.Multimaps
import com.mysticwind.linenotificationsupport.module.HiltQualifiers
import com.mysticwind.linenotificationsupport.notification.SlotAvailabilityChecker
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DumbNotificationCounter @Inject constructor(
    @HiltQualifiers.MaxNotificationsPerApp private val maxNotifications: Int
) : SlotAvailabilityChecker {

    private val groupToNotificationKeyMultimap: Multimap<String, String> =
        Multimaps.synchronizedSetMultimap(HashMultimap.create())

    // TODO refactor NotificationCounter interface to use notification keys
    fun notified(group: String, notificationKey: String): Int {
        Timber.d("Published notification key (%s) group (%s)", notificationKey, group)
        groupToNotificationKeyMultimap.put(group, notificationKey)
        return getSlotsUsed()
    }

    fun dismissed(group: String, notificationKey: String): Int {
        Timber.d("Dismissed notification key (%s) group (%s)", notificationKey, group)
        groupToNotificationKeyMultimap.get(group).remove(notificationKey)
        return getSlotsUsed()
    }

    override fun hasSlot(group: String): Boolean {
        val remainingSlots = maxNotifications - getSlotsUsed()
        val notificationKeys = groupToNotificationKeyMultimap.get(group)
        return if (notificationKeys.isEmpty()) {
            remainingSlots >= 1
        } else {
            remainingSlots >= 2
        }
    }

    private fun getSlotsUsed(): Int {
        val slotsUsed = groupToNotificationKeyMultimap.values().size
        Timber.d("Slots used [%d] remaining [%d]", slotsUsed, maxNotifications - slotsUsed)
        return slotsUsed
    }

    // This probably risks concurrency
    // returns whether or not it is valid (not changed)
    fun validateNotifications(currentGroupToNotificationKeyMultimap: Multimap<String, String>): Boolean {
        return if (groupToNotificationKeyMultimap != currentGroupToNotificationKeyMultimap) {
            Timber.w(
                "Notifications being tracked are different! Tracked [%s] Current [%s]",
                groupToNotificationKeyMultimap.values(), currentGroupToNotificationKeyMultimap.values()
            )
            groupToNotificationKeyMultimap.clear()
            groupToNotificationKeyMultimap.putAll(currentGroupToNotificationKeyMultimap)
            false
        } else {
            Timber.d("Verified notifications are the same")
            true
        }
    }

    fun updateStateFromExistingNotifications(existingNotifications: List<StatusBarNotification>) {
        for (notification in existingNotifications) {
            groupToNotificationKeyMultimap.clear()
            Timber.d(
                "Restoring notification group [%s] key [%s]",
                notification.notification.group, notification.key
            )
            groupToNotificationKeyMultimap.put(notification.notification.group, notification.key)
        }
    }
}
