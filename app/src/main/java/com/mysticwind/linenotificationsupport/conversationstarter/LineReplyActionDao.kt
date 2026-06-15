package com.mysticwind.linenotificationsupport.conversationstarter

import android.app.Notification
import java.util.Optional

interface LineReplyActionDao {

    fun saveLineReplyAction(chatId: String, lineReplyAction: Notification.Action)

    fun getLineReplyAction(chatId: String): Optional<Notification.Action>
}
