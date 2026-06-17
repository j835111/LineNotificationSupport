package com.mysticwind.linenotificationsupport.reply

import android.app.Notification
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WearableReplyActionSemantics @Inject constructor() {
    fun applyTo(builder: Notification.Action.Builder): Notification.Action.Builder {
        return builder
            .setAllowGeneratedReplies(true)
            .setSemanticAction(Notification.Action.SEMANTIC_ACTION_REPLY)
    }
}
