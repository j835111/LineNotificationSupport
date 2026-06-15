package com.mysticwind.linenotificationsupport.preference

import android.content.SharedPreferences
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceWriter @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {

    fun setControlBluetoothDuringCalls(value: Boolean) {
        Timber.i("Updating preference [%s] value [%s]", PreferenceProvider.BLUETOOTH_CONTROL_ONGOING_CALL_KEY, value)

        sharedPreferences.edit()
            .putBoolean(PreferenceProvider.BLUETOOTH_CONTROL_ONGOING_CALL_KEY, value)
            .commit()
    }

    fun disableShowConversationStarterNotification() {
        sharedPreferences.edit()
            .putBoolean(PreferenceProvider.CONVERSATION_STARTER_KEY, false)
            .commit()
    }
}
