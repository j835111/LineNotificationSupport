package com.mysticwind.linenotificationsupport.conversationstarter.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mysticwind.linenotificationsupport.preference.PreferenceWriter
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class DisableStartConversationFeatureBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var preferenceWriter: PreferenceWriter

    override fun onReceive(context: Context, intent: Intent) {
        Timber.i("Received request to disable start conversation feature intent action [%s]", intent.action)
        preferenceWriter.disableShowConversationStarterNotification()
    }
}
