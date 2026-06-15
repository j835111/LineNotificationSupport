package com.mysticwind.linenotificationsupport

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.common.collect.ImmutableMap
import com.mysticwind.linenotificationsupport.preference.PreferenceProvider
import com.mysticwind.linenotificationsupport.preference.PreferenceWriter
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.function.BiConsumer
import javax.inject.Inject

@AndroidEntryPoint
class SettingsUpdateRequestBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var preferenceWriter: PreferenceWriter

    override fun onReceive(context: Context, intent: Intent) {
        Timber.i(
            "Received request to update settings intent action [%s] key [%s] value [%s]",
            intent.action, intent.getStringExtra(SETTING_KEY_KEY), intent.getStringExtra(SETTING_VALUE_KEY)
        )

        val settingKey = intent.getStringExtra(SETTING_KEY_KEY)
        val settingValue = intent.getStringExtra(SETTING_VALUE_KEY)

        if (BOOLEAN_SETTING_TO_WRITER_FUNCTION_MAP.containsKey(settingKey)) {
            val value = settingValue.toBoolean()

            val writerFunction = BOOLEAN_SETTING_TO_WRITER_FUNCTION_MAP[settingKey]!!
            writerFunction.accept(preferenceWriter, value)

            Timber.i("Successfully updated preference setting key [%s] value [%s]", settingKey, settingValue)
            resultCode = RESULT_OK
            return
        }

        Timber.e(
            "Unsupported intent action [%s] key [%s] value [%s]",
            intent.action, settingKey, settingValue
        )
        resultCode = RESULT_CANCELED
    }

    companion object {
        private val BOOLEAN_SETTING_TO_WRITER_FUNCTION_MAP: Map<String, BiConsumer<PreferenceWriter, Boolean>> =
            ImmutableMap.of(
                PreferenceProvider.BLUETOOTH_CONTROL_ONGOING_CALL_KEY,
                BiConsumer { preferenceWriter, value -> preferenceWriter.setControlBluetoothDuringCalls(value) }
            )

        private const val SETTING_KEY_KEY = "setting-key"
        private const val SETTING_VALUE_KEY = "setting-value"
    }
}
