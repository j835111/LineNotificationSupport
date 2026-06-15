package com.mysticwind.linenotificationsupport

import android.app.Dialog
import android.content.Intent
import android.content.Intent.FLAG_RECEIVER_FOREGROUND
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.mysticwind.linenotificationsupport.chatname.broadcastreceiver.DeleteFriendNameCacheBroadcastReceiver
import com.mysticwind.linenotificationsupport.service.NotificationListenerService
import timber.log.Timber

class DeleteCachedFriendNamesDialogActivity : AppCompatActivity() {

    private lateinit var confirmationDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        confirmationDialog = createConfirmationDialog()
    }

    private fun createConfirmationDialog(): Dialog {
        return AlertDialog.Builder(this)
            .setMessage(R.string.delete_cache_confirmation_dialog_title)
            .setPositiveButton(R.string.delete_cache_confirmation_dialog_yes) { _, _ ->
                Timber.i("Confirmed cleaning database")

                val intent = Intent(applicationContext, DeleteFriendNameCacheBroadcastReceiver::class.java)
                intent.action = NotificationListenerService.DELETE_FRIEND_NAME_CACHE_ACTION
                intent.flags = FLAG_RECEIVER_FOREGROUND
                sendBroadcast(intent)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .setOnDismissListener {
                Timber.i("Dialog dismissed")
                finish()
            }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setCancelable(true)
            .create()
    }

    override fun onResume() {
        super.onResume()

        confirmationDialog.show()
    }
}
