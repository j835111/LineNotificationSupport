package com.mysticwind.linenotificationsupport.identicalmessage

import com.mysticwind.linenotificationsupport.model.LineNotification
import org.apache.commons.lang3.tuple.Pair
import timber.log.Timber
import java.util.Optional

class IgnoreIdenticalMessageHandler(
    private val identicalMessageEvaluator: IdenticalMessageEvaluator
) : IdenticalMessageHandler {

    override fun handle(lineNotification: LineNotification, notificationId: Int): Optional<Pair<LineNotification, Int>> {
        // call messages should always be treated as new
        if (lineNotification.callState == LineNotification.CallState.INCOMING ||
            lineNotification.callState == LineNotification.CallState.MISSED_CALL ||
            // this is de-duped, auto notifications may not stop at all
            lineNotification.callState == LineNotification.CallState.IN_A_CALL
        ) {
            return Optional.of(Pair.of(lineNotification, notificationId))
        }
        val result = identicalMessageEvaluator.evaluate(lineNotification, notificationId)
        if (result.isDuplicate()) {
            Timber.i("Detected duplicate LINE message: " + lineNotification.message)
            return Optional.empty()
        }
        return Optional.of(Pair.of(lineNotification, notificationId))
    }
}
