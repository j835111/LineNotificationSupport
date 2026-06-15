package com.mysticwind.linenotificationsupport.permission

import android.Manifest
import android.app.Activity
import androidx.core.app.ActivityCompat
import com.mysticwind.linenotificationsupport.android.AndroidFeatureProvider
import com.mysticwind.linenotificationsupport.preference.PreferenceProvider
import java.util.Objects
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidPermissionRequester @Inject constructor(
    private val preferenceProvider: PreferenceProvider,
    private val androidFeatureProvider: AndroidFeatureProvider
) {

    fun requestBluetoothPermissionIfNecessary(activity: Activity) {
        if (noBluetoothPermissionWhileRequired()) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                1 /* any request code */
            )
        }
    }

    private fun noBluetoothPermissionWhileRequired(): Boolean {
        // no point getting the permission if BT cannot be controlled
        if (!androidFeatureProvider.canControlBluetooth()) {
            return false
        }
        return preferenceProvider.shouldControlBluetoothDuringCalls() && !androidFeatureProvider.hasBluetoothControlPermissions()
    }

    fun requestPublishNotificationPermissionIfNecessary(activity: Activity) {
        if (androidFeatureProvider.hasPublishNotificationPermission()) {
            return
        }
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            2 /* any request code */
        )
    }
}
