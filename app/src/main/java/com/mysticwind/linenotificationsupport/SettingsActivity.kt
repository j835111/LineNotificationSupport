package com.mysticwind.linenotificationsupport

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.mysticwind.linenotificationsupport.android.AndroidFeatureProvider
import com.mysticwind.linenotificationsupport.line.Constants
import com.mysticwind.linenotificationsupport.notificationgroup.NotificationGroupCreator
import com.mysticwind.linenotificationsupport.permission.AndroidPermissionRequester
import com.mysticwind.linenotificationsupport.preference.PreferenceProvider
import com.mysticwind.linenotificationsupport.preference.PreferenceProvider.Companion.MERGE_NOTIFICATION_CHANNEL_PREFERENCE_KEY
import dagger.hilt.android.AndroidEntryPoint
import org.apache.commons.lang3.StringUtils
import java.util.Objects
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    @Inject
    lateinit var notificationGroupCreator: NotificationGroupCreator

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var preferenceProvider: PreferenceProvider

    @Inject
    lateinit var androidPermissionRequester: AndroidPermissionRequester

    @Inject
    lateinit var androidFeatureProvider: AndroidFeatureProvider

    private lateinit var silentLineMessageNotificationSettingsDialog: Dialog
    private lateinit var alertLineMessageNotificationSettingsDialog: Dialog
    private var onPreferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    class SettingsFragment(private val androidFeatureProvider: AndroidFeatureProvider) : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            if (!androidFeatureProvider.canControlBluetooth()) {
                val preference: Preference? = findPreference(BLUETOOTH_CONTROL_PREFERENCE_KEY)
                preference?.isVisible = false
            }
        }

        override fun onResume() {
            super.onResume()
        }

        override fun onPause() {
            super.onPause()
        }

        companion object {
            private const val BLUETOOTH_CONTROL_PREFERENCE_KEY = "bluetooth_control_in_calls"
        }
    }

    private fun setupOnPreferenceChangeListener() {
        onPreferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, preferenceKey ->
            if (StringUtils.equals(MERGE_NOTIFICATION_CHANNEL_PREFERENCE_KEY, preferenceKey)) {
                val shouldMergeNotification = prefs.getBoolean(preferenceKey, false)
                if (shouldMergeNotification) {
                    notificationGroupCreator.migrateToSingleNotificationChannelForMessages()
                } else {
                    notificationGroupCreator.migrateToMultipleNotificationChannelsForMessages()
                }
            }
            if (PreferenceProvider.MANAGE_LINE_MESSAGE_NOTIFICATIONS_PREFERENCE_KEY == preferenceKey) {
                if (preferenceProvider.shouldManageLineMessageNotifications()) {
                    silentLineMessageNotificationSettingsDialog.show()
                } else {
                    alertLineMessageNotificationSettingsDialog.show()
                }
            }
            if (PreferenceProvider.BLUETOOTH_CONTROL_ONGOING_CALL_KEY == preferenceKey) {
                androidPermissionRequester.requestBluetoothPermissionIfNecessary(this@SettingsActivity)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment(androidFeatureProvider))
                .commit()
        }
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        silentLineMessageNotificationSettingsDialog = createSilentLineMessageNotificationSettingsDialog()
        alertLineMessageNotificationSettingsDialog = createAlertLineMessageNotificationSettingsDialog()

        setupOnPreferenceChangeListener()
    }

    private fun createSilentLineMessageNotificationSettingsDialog(): Dialog {
        return AlertDialog.Builder(this)
            .setMessage(R.string.make_line_message_notification_silent)
            .setPositiveButton(R.string.go_to_settings) { _, _ ->
                val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, Constants.LINE_PACKAGE_NAME)
                    .putExtra(Settings.EXTRA_CHANNEL_ID, Constants.NEW_MESSAGE_NOTIFICATION_CHANNEL_NAME)
                startActivity(intent)
            }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setCancelable(true)
            .create()
    }

    private fun createAlertLineMessageNotificationSettingsDialog(): Dialog {
        return AlertDialog.Builder(this)
            .setMessage(R.string.reminder_for_enabling_line_message_notification)
            .setPositiveButton(R.string.go_to_settings) { _, _ ->
                val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, Constants.LINE_PACKAGE_NAME)
                    .putExtra(Settings.EXTRA_CHANNEL_ID, Constants.NEW_MESSAGE_NOTIFICATION_CHANNEL_NAME)
                startActivity(intent)
            }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setCancelable(true)
            .create()
    }

    override fun onResume() {
        super.onResume()

        sharedPreferences.registerOnSharedPreferenceChangeListener(onPreferenceChangeListener)
    }

    override fun onPause() {
        super.onPause()

        sharedPreferences.unregisterOnSharedPreferenceChangeListener(onPreferenceChangeListener)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        // handles the back button on the action bar
        if (id == android.R.id.home) {
            finish()
        }

        return super.onOptionsItemSelected(item)
    }
}
