package com.mysticwind.linenotificationsupport.chatname.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mysticwind.linenotificationsupport.chatname.ChatNameManager
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class DeleteFriendNameCacheBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var chatNameManager: ChatNameManager

    override fun onReceive(context: Context, intent: Intent) {
        Timber.i("Received intent [%s] to delete friend name cache", intent.action)

        chatNameManager.deleteFriendNameCache()
    }

}
