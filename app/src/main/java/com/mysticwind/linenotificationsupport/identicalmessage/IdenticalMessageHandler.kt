package com.mysticwind.linenotificationsupport.identicalmessage

import com.mysticwind.linenotificationsupport.model.LineNotification
import org.apache.commons.lang3.tuple.Pair
import java.util.Optional

interface IdenticalMessageHandler {

    /**
     * Handles notification if it is an identical message.
     * @param lineNotification the new line notification
     * @param notificationId the notificationId to be sent
     * @return the LineNotification and the notification ID should be sent/update.
     */
    fun handle(lineNotification: LineNotification, notificationId: Int): Optional<Pair<LineNotification, Int>>
}
