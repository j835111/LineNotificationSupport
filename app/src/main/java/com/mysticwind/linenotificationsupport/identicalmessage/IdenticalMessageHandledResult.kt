package com.mysticwind.linenotificationsupport.identicalmessage

import com.mysticwind.linenotificationsupport.model.LineNotification
import java.util.Objects

class IdenticalMessageHandledResult(
    val notificationId: Int,
    val replacedMessage: String?,
    val lineNotification: LineNotification?
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IdenticalMessageHandledResult) return false
        return notificationId == other.notificationId
                && Objects.equals(replacedMessage, other.replacedMessage)
                && Objects.equals(lineNotification, other.lineNotification)
    }

    override fun hashCode(): Int =
        Objects.hash(notificationId, replacedMessage, lineNotification)

    companion object {
        @JvmStatic
        fun builder(): Builder = Builder()
    }

    class Builder {
        private var notificationId: Int = 0
        private var replacedMessage: String? = null
        private var lineNotification: LineNotification? = null

        fun notificationId(notificationId: Int): Builder {
            this.notificationId = notificationId
            return this
        }

        fun replacedMessage(replacedMessage: String?): Builder {
            this.replacedMessage = replacedMessage
            return this
        }

        fun lineNotification(lineNotification: LineNotification?): Builder {
            this.lineNotification = lineNotification
            return this
        }

        fun build(): IdenticalMessageHandledResult =
            IdenticalMessageHandledResult(notificationId, replacedMessage, lineNotification)
    }
}
