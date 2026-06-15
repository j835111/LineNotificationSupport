package com.mysticwind.linenotificationsupport.identicalmessage

import com.mysticwind.linenotificationsupport.model.LineNotification
import org.apache.commons.lang3.tuple.Pair
import java.util.Optional

class MergeIdenticalMessageHandler(
    private val identicalMessageEvaluator: IdenticalMessageEvaluator
) : IdenticalMessageHandler {

    override fun handle(lineNotification: LineNotification, notificationId: Int): Optional<Pair<LineNotification, Int>> {
        val result = identicalMessageEvaluator.evaluate(lineNotification, notificationId)

        if (!result.isDuplicate()) {
            return Optional.of(Pair.of(lineNotification, notificationId))
        }
        return Optional.of(
            Pair.of(
                buildMergedNotification(result),
                result.getNotificationId().get()
            )
        )
    }

    private fun buildMergedNotification(result: IdenticalMessageEvaluator.EvaluationResult): LineNotification {
        return result.getPreviousLineNotification().get().toBuilder()
            .message(buildNewMessage(result))
            .build()
    }

    private fun buildNewMessage(result: IdenticalMessageEvaluator.EvaluationResult): String {
        return String.format(
            "%s (%d)",
            result.getPreviousLineNotification().get().message,
            result.numberOfDuplicates
        )
    }
}
