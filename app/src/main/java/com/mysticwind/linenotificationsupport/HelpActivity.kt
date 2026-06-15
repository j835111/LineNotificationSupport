package com.mysticwind.linenotificationsupport

import android.app.Dialog
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.common.collect.ImmutableMap
import com.mysticwind.linenotificationsupport.conversationstarter.activity.KeywordSettingActivity
import com.mysticwind.linenotificationsupport.debug.DebugModeProvider
import com.mysticwind.linenotificationsupport.line.LineAppVersionProvider
import com.mysticwind.linenotificationsupport.permission.AndroidPermissionRequester
import com.mysticwind.linenotificationsupport.provision.FeatureProvisionStateProvider
import dagger.hilt.android.AndroidEntryPoint
import org.apache.commons.lang3.StringUtils
import timber.log.Timber
import java.util.Arrays
import javax.inject.Inject

@AndroidEntryPoint
class HelpActivity : AppCompatActivity() {

    @Inject
    lateinit var lineAppVersionProvider: LineAppVersionProvider

    @Inject
    lateinit var androidPermissionRequester: AndroidPermissionRequester

    @Inject
    lateinit var notificationManager: NotificationManager

    private var featureProvisionStateProvider: FeatureProvisionStateProvider? = null
    private lateinit var grantPermissionDialog: Dialog
    private lateinit var disablePowerOptimizationTipDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.help_main)

        val recommendedSettingsTextView = findViewById<TextView>(R.id.recommended_settings_text_view)
        recommendedSettingsTextView.movementMethod = LinkMovementMethod.getInstance()

        showLineVersionWarning()

        grantPermissionDialog = createGrantPermissionDialog()
        disablePowerOptimizationTipDialog = createDisablePowerOptimizationTipDialog()

        androidPermissionRequester.requestBluetoothPermissionIfNecessary(this)
    }

    private fun showLineVersionWarning() {
        val lineAppVersion = lineAppVersionProvider.getLineAppVersion()
        if (!lineAppVersion.isPresent) {
            return
        }

        Timber.d("Detected LINE with version: " + lineAppVersion.get())

        val warningMessageId = LINE_VERSION_TO_WARNING_MESSAGE_ID[lineAppVersion.get()] ?: return

        (findViewById<View>(R.id.warning_message_text) as TextView).setText(warningMessageId)
        findViewById<View>(R.id.warning_message_layout).visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()

        if (!hasPublishNotificationAccess()) {
            androidPermissionRequester.requestPublishNotificationPermissionIfNecessary(this)
        }

        if (hasNotificationAccess()) {
            if (grantPermissionDialog.isShowing) {
                grantPermissionDialog.dismiss()
            }
        } else {
            grantPermissionDialog.show()
        }

        perhapsShowDisablePowerOptimizationTip()
    }

    private fun perhapsShowDisablePowerOptimizationTip() {
        if (grantPermissionDialog.isShowing) {
            return
        }

        getFeatureProvisionStateProvider()
            .isDisablePowerOptimizationTipShown()
            .onErrorReturn { error ->
                Timber.e(error, "Error returning isDisablePowerOptimizationTipShown: [%s]", error.message)
                false
            }
            .subscribe { isShownBefore ->
                if (isShownBefore) {
                    dismissDisablePowerOptimizationTipDialog()
                    return@subscribe
                }
                if (!isPowerOptimizationEnabled()) {
                    dismissDisablePowerOptimizationTipDialog()
                    return@subscribe
                }
                this.runOnUiThread { disablePowerOptimizationTipDialog.show() }
            }
    }

    private fun dismissDisablePowerOptimizationTipDialog() {
        if (disablePowerOptimizationTipDialog.isShowing) {
            disablePowerOptimizationTipDialog.dismiss()
        }
    }

    private fun getFeatureProvisionStateProvider(): FeatureProvisionStateProvider {
        return featureProvisionStateProvider ?: FeatureProvisionStateProvider(this).also {
            featureProvisionStateProvider = it
        }
    }

    // https://stackoverflow.com/questions/39256501/check-if-battery-optimization-is-enabled-or-not-for-an-app
    private fun isPowerOptimizationEnabled(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false
        }
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val isIgnoringBatteryOptimizations = powerManager.isIgnoringBatteryOptimizations(packageName)
        Timber.d("isIgnoringBatteryOptimizations [%s]", isIgnoringBatteryOptimizations)
        return !isIgnoringBatteryOptimizations
    }

    private fun createGrantPermissionDialog(): Dialog {
        return AlertDialog.Builder(this)
            .setMessage(R.string.permission_request_dialog_message)
            .setPositiveButton(android.R.string.yes) { _, _ ->
                redirectToNotificationSettingsPage()
            }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setCancelable(false)
            .create()
    }

    private fun createDisablePowerOptimizationTipDialog(): Dialog {
        return AlertDialog.Builder(this)
            .setTitle(R.string.disable_power_optimization_tip_dialog_title)
            .setMessage(R.string.power_optimization_settings_summary)
            .setPositiveButton(R.string.disable_power_optimization_tip_dialog_yes) { _, _ ->
                val intent = Intent()
                intent.action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
                this@HelpActivity.startActivity(intent)
            }
            .setNeutralButton(android.R.string.cancel) { _, _ ->
                getFeatureProvisionStateProvider().setDisablePowerOptimizationTipShown()
            }
            .setIcon(android.R.drawable.ic_dialog_info)
            .setCancelable(false)
            .create()
    }

    private fun hasNotificationAccess(): Boolean {
        val contentResolver = contentResolver
        // it would look something like this: net.dinglisch.android.taskerm.NotificationListenerService:com.mysticwind.linenotificationsupport.donate/com.mysticwind.linenotificationsupport.service.NotificationListenerService
        val enabledNotificationListeners =
            Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        val packageName = packageName
        Timber.w("enabled_notification_listeners: $enabledNotificationListeners")
        if (StringUtils.isBlank(enabledNotificationListeners)) {
            return false
        }
        return Arrays.stream(enabledNotificationListeners.split(":").toTypedArray())
            .filter { enabledNotificationListener -> enabledNotificationListener.startsWith("$packageName/") }
            .findAny()
            .isPresent
    }

    private fun hasPublishNotificationAccess(): Boolean {
        return notificationManager.areNotificationsEnabled()
    }

    private fun redirectToNotificationSettingsPage() {
        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
    }

    override fun onPause() {
        super.onPause()

        val providerToShutdown = this.featureProvisionStateProvider
        this.featureProvisionStateProvider = null
        providerToShutdown?.shutdown()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)

        if (DEBUG_MODE_PROVIDER.isDebugMode()) {
            menu.getItem(1).isVisible = true
            menu.getItem(2).isVisible = true
            menu.getItem(3).isVisible = true
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        //noinspection SimplifiableIfStatement
        return when (id) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_debug -> {
                startActivity(Intent(this, NotificationHistoryDebugActivity::class.java))
                true
            }
            R.id.action_test_notifications -> {
                startActivity(Intent(this, MainActivity::class.java))
                true
            }
            R.id.action_keyword_settings -> {
                startActivity(Intent(this, KeywordSettingActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private val DEBUG_MODE_PROVIDER = DebugModeProvider()

        private val LINE_VERSION_TO_WARNING_MESSAGE_ID: Map<String, Int> = mapOf(
            "10.19.1" to R.string.line_version_warning_10_19_1
        )
    }
}
