package com.mysticwind.linenotificationsupport.android

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidFeatureProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun hasNotificationChannelSupport(): Boolean {
        // NotificationChannels are only supported API 26+
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }

    fun canControlBluetooth(): Boolean {
        // Bluetooth enable/disable are only supported before API 33
        // https://developer.android.com/reference/android/bluetooth/BluetoothAdapter?hl=en#enable()
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
    }

    fun hasBluetoothControlPermissions(): Boolean {
        // Android 12 and above requires BLUETOOTH_CONNECT permission that requires additional approval
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return true
        }
        return if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Timber.w("No permissions to control Bluetooth!!!")
            false
        } else {
            true
        }
    }

    fun hasPublishNotificationPermission(): Boolean {
        return if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Timber.w("No permissions to publish Notifications!!!")
            false
        } else {
            true
        }
    }

}
