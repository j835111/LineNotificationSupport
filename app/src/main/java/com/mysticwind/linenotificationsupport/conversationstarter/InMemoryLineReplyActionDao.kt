package com.mysticwind.linenotificationsupport.conversationstarter

import android.app.Notification
import org.apache.commons.lang3.Validate
import java.util.Objects
import java.util.Optional
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryLineReplyActionDao @Inject constructor() : LineReplyActionDao {

    private val chatIdToNotificationActionMap: MutableMap<String, Notification.Action> = HashMap()

    override fun saveLineReplyAction(chatId: String, lineReplyAction: Notification.Action) {
        Validate.notBlank(chatId)
        Objects.requireNonNull(lineReplyAction)
        chatIdToNotificationActionMap[chatId] = lineReplyAction
    }

    override fun getLineReplyAction(chatId: String): Optional<Notification.Action> =
        Optional.ofNullable(chatIdToNotificationActionMap[chatId])
}
