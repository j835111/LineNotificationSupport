package com.mysticwind.linenotificationsupport.identicalmessage

import com.mysticwind.linenotificationsupport.model.LineNotification
import org.apache.commons.lang3.tuple.Pair
import java.util.Optional

class AsIsIdenticalMessageHandler : IdenticalMessageHandler {

    override fun handle(lineNotification: LineNotification, notificationId: Int): Optional<Pair<LineNotification, Int>> {
        return Optional.of(Pair.of(lineNotification, notificationId))
    }
}
