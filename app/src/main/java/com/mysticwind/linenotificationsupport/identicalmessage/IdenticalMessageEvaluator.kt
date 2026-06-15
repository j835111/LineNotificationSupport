package com.mysticwind.linenotificationsupport.identicalmessage

import androidx.annotation.VisibleForTesting
import com.mysticwind.linenotificationsupport.model.LineNotification
import org.apache.commons.lang3.StringUtils
import java.util.Comparator
import java.util.Objects
import java.util.Optional

class IdenticalMessageEvaluator {

    class State(
        val lineNotification: LineNotification,
        val notificationid: Int
    ) {
        var numberOfDuplicates: Int = 0
            private set

        fun increase() {
            numberOfDuplicates += 1
        }

        companion object {
            @JvmStatic
            fun builder(): Builder = Builder()
        }

        class Builder {
            private var lineNotification: LineNotification? = null
            private var notificationid: Int = 0

            fun lineNotification(lineNotification: LineNotification): Builder {
                this.lineNotification = lineNotification
                return this
            }

            fun notificationid(notificationid: Int): Builder {
                this.notificationid = notificationid
                return this
            }

            fun build(): State = State(lineNotification!!, notificationid)
        }
    }

    class EvaluationResult(
        private val lineNotification: LineNotification?,
        private val notificationId: Int?,
        val numberOfDuplicates: Int
    ) {

        fun getPreviousLineNotification(): Optional<LineNotification> =
            Optional.ofNullable(lineNotification)

        fun getNotificationId(): Optional<Int> = Optional.ofNullable(notificationId)

        fun isDuplicate(): Boolean = numberOfDuplicates > 0

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is EvaluationResult) return false
            return numberOfDuplicates == other.numberOfDuplicates
                    && Objects.equals(lineNotification, other.lineNotification)
                    && Objects.equals(notificationId, other.notificationId)
        }

        override fun hashCode(): Int =
            Objects.hash(lineNotification, notificationId, numberOfDuplicates)

        companion object {
            @JvmStatic
            fun noDuplicate(): EvaluationResult = EvaluationResult(null, null, 0)

            @JvmStatic
            fun withDuplicate(
                previousLineNotification: LineNotification,
                previousNotificationId: Int,
                numberOfDuplicates: Int
            ): EvaluationResult =
                EvaluationResult(previousLineNotification, previousNotificationId, numberOfDuplicates)
        }
    }

    fun evaluate(lineNotification: LineNotification, newNotificationId: Int): EvaluationResult {
        val chatId = lineNotification.chatId
        val state = CHAT_ID_TO_STATE_MAP[chatId]
        if (state == null || !hasSameMessage(state.lineNotification, lineNotification)) {
            CHAT_ID_TO_STATE_MAP[chatId] = State.builder()
                .lineNotification(lineNotification)
                .notificationid(newNotificationId)
                .build()
            return EvaluationResult.noDuplicate()
        }
        state.increase()
        return EvaluationResult.withDuplicate(
            state.lineNotification,
            state.notificationid,
            state.numberOfDuplicates + 1
        )
    }

    private fun hasSameMessage(
        previousLineNotification: LineNotification,
        newLineNotification: LineNotification
    ): Boolean =
        LINE_NOTIFICATION_COMPARATOR.compare(previousLineNotification, newLineNotification) == 0

    companion object {
        @VisibleForTesting
        @JvmField
        val LINE_NOTIFICATION_COMPARATOR: Comparator<LineNotification> =
            Comparator.comparing<LineNotification, String?>(
                { n: LineNotification -> n.message },
                Comparator.nullsLast(Comparator.naturalOrder<String>())
            )
                .thenComparing { n1, n2 -> compareSender(n1, n2) }
                .thenComparing<String?>(
                    { n: LineNotification -> n.title },
                    Comparator.nullsLast(Comparator.naturalOrder<String>())
                )
                .thenComparing<String?>(
                    { n: LineNotification -> n.lineStickerUrl },
                    Comparator.nullsLast(Comparator.naturalOrder<String>())
                )
                .thenComparing<String?>(
                    { n: LineNotification -> n.chatId },
                    Comparator.nullsLast(Comparator.naturalOrder<String>())
                )
                .thenComparing<LineNotification.CallState?>(
                    { n: LineNotification -> n.callState },
                    Comparator.nullsLast(Comparator.naturalOrder<LineNotification.CallState>())
                )

        private val CHAT_ID_TO_STATE_MAP: MutableMap<String?, State> = HashMap()

        private fun compareSender(n1: LineNotification, n2: LineNotification): Int {
            val senderName1 = senderName(n1)
            val senderName2 = senderName(n2)
            return StringUtils.compare(senderName1, senderName2)
        }

        private fun senderName(lineNotification: LineNotification): String? {
            val sender = lineNotification.sender ?: return null
            return if (StringUtils.isBlank(sender.name)) null else sender.name?.toString()
        }
    }
}
