package com.mysticwind.linenotificationsupport.provision

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder
import androidx.datastore.rxjava3.RxDataStore
import io.reactivex.rxjava3.annotations.NonNull
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single

class FeatureProvisionStateProvider(context: Context) {

    companion object {
        private const val FILE_NAME = "provision"
        const val SHOW_DISABLE_POWER_OPTIMIZATION_TIP_KEY = "show_disable_power_optimization_tip"
    }

    private val SHOW_DISABLE_POWER_OPTIMIZATION_TIP_PREFERENCE_KEY: Preferences.Key<Boolean> =
        booleanPreferencesKey(SHOW_DISABLE_POWER_OPTIMIZATION_TIP_KEY)

    private val dataStore: RxDataStore<Preferences> =
        RxPreferenceDataStoreBuilder(context, FILE_NAME).build()

    fun isDisablePowerOptimizationTipShown(): @NonNull Flowable<Boolean> {
        return dataStore.data()
            .map { prefs -> prefs.get(SHOW_DISABLE_POWER_OPTIMIZATION_TIP_PREFERENCE_KEY) ?: false }
    }

    fun setDisablePowerOptimizationTipShown() {
        updateDisablePowerOptimizationTipShown(true)
    }

    fun shutdown() {
        dataStore.dispose()
    }

    private fun updateDisablePowerOptimizationTipShown(value: Boolean) {
        dataStore.updateDataAsync { preferences ->
            val mutablePreferences: MutablePreferences = preferences.toMutablePreferences()
            mutablePreferences[SHOW_DISABLE_POWER_OPTIMIZATION_TIP_PREFERENCE_KEY] = value
            Single.just(mutablePreferences)
        }
    }
}
