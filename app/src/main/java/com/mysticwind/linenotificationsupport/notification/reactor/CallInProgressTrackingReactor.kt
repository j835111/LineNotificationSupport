package com.mysticwind.linenotificationsupport.notification.reactor

import android.app.Notification
import android.service.notification.StatusBarNotification
import com.google.common.collect.ImmutableSet
import com.mysticwind.linenotificationsupport.android.AndroidFeatureProvider
import com.mysticwind.linenotificationsupport.bluetooth.BluetoothController
import com.mysticwind.linenotificationsupport.line.Constants
import com.mysticwind.linenotificationsupport.preference.PreferenceProvider
import com.mysticwind.linenotificationsupport.utils.StatusBarNotificationExtractor
import timber.log.Timber
import java.util.HashSet
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallInProgressTrackingReactor @Inject constructor(
    private val preferenceProvider: PreferenceProvider,
    private val bluetoothController: BluetoothController,
    private val androidFeatureProvider: AndroidFeatureProvider
) : IncomingNotificationReactor, DismissedNotificationReactor {

    private val callNotificationKeys: MutableSet<String> = HashSet()

    override fun interestedPackages(): Collection<String> =
        ImmutableSet.of(Constants.LINE_PACKAGE_NAME)

    override fun isInterestInNotificationGroup(): Boolean = false

    override fun reactToIncomingNotification(statusBarNotification: StatusBarNotification): Reaction {
        val notificationKey = statusBarNotification.key
        if (!isLineCallInProgressNotification(statusBarNotification)) {
            Timber.d("Notification [%s] not a call", notificationKey)
            return Reaction.NONE
        }
        Timber.d("Notification [%s] IS a call", statusBarNotification.key)
        if (callNotificationKeys.contains(notificationKey)) {
            Timber.d("Call notification [%s] already present", notificationKey)
            return Reaction.NONE
        }
        if (callNotificationKeys.size > 0) {
            Timber.e("Already exists call notifications [%s] and will be replaced with key [%s]",
                callNotificationKeys, notificationKey)
            callNotificationKeys.clear()
            callNotificationKeys.add(notificationKey)
            return Reaction.NONE
        }
        callNotificationKeys.add(notificationKey)

        if (shouldControlBluetooth()) {
            // TODO we probably should do a Toast here
            Timber.i("Disabling bluetooth")
            bluetoothController.disableBluetooth()
        }

        return Reaction.NONE
    }

    override fun reactToDismissedNotification(statusBarNotification: StatusBarNotification): Reaction {
        val notificationKey = statusBarNotification.key
        if (callNotificationKeys.contains(notificationKey)) {
            Timber.i("Notification [%s] IS for an in progress call and dismissed", notificationKey)
            callNotificationKeys.remove(notificationKey)

            if (shouldControlBluetooth()) {
                // TODO 1. we probably should do a Toast here
                // TODO 2. we probably want to revert to the original bluetooth status
                Timber.i("Re-enabling bluetooth")
                bluetoothController.enableBluetooth()
            }

            return Reaction.NONE
        }
        Timber.d("Notification [%s] is not a in progress call notification", notificationKey)
        return Reaction.NONE
    }

    // TODO unit tests
    private fun isLineCallInProgressNotification(statusBarNotification: StatusBarNotification): Boolean {
        // essentially, if a notification is a message and persistent, it is due to an active call
        return StatusBarNotificationExtractor.isMessage(statusBarNotification) &&
                (statusBarNotification.notification.flags and Notification.FLAG_ONGOING_EVENT) > 0 &&
                (statusBarNotification.notification.flags and Notification.FLAG_NO_CLEAR) > 0
    }

    private fun shouldControlBluetooth(): Boolean =
        preferenceProvider.shouldControlBluetoothDuringCalls() && androidFeatureProvider.canControlBluetooth()
}
