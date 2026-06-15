package com.mysticwind.linenotificationsupport.bluetooth.impl

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import com.mysticwind.linenotificationsupport.android.AndroidFeatureProvider
import com.mysticwind.linenotificationsupport.bluetooth.BluetoothController
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implemented by referencing https://stackoverflow.com/questions/3806536/how-to-enable-disable-bluetooth-programmatically-in-android
 */
@Singleton
class AndroidBluetoothController @Inject constructor(
    private val androidFeatureProvider: AndroidFeatureProvider
) : BluetoothController {

    override fun enableBluetooth() {
        setBluetoothState(true)
    }

    override fun disableBluetooth() {
        setBluetoothState(false)
    }

    @SuppressLint("MissingPermission") // this is verified in androidFeatureProvider.hasBluetoothControlPermissions()
    private fun setBluetoothState(enable: Boolean) {
        if (!androidFeatureProvider.hasBluetoothControlPermissions()) {
            return
        }
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val isEnabled = bluetoothAdapter.isEnabled
        if (enable && !isEnabled) {
            Timber.i("Enabling Bluetooth")
            bluetoothAdapter.enable()
        } else if (!enable && isEnabled) {
            Timber.i("Disabling Bluetooth")
            bluetoothAdapter.disable()
        }
        // No need to change bluetooth state
    }

}
