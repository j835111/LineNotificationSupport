package com.mysticwind.linenotificationsupport

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import timber.log.Timber

class DismissNotificationBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val NOTIFICATION_ID = "notificationId"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra(NOTIFICATION_ID, 0)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(notificationId)

        Timber.d("Cancelling notification ID: %d", notificationId)
    }
}
