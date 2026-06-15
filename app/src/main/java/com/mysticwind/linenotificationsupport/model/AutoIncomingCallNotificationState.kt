package com.mysticwind.linenotificationsupport.model

import com.google.common.collect.ImmutableSet
import java.time.Instant

class AutoIncomingCallNotificationState internal constructor(
    val lineNotification: LineNotification,
    val waitDurationInSeconds: Double,
    timeoutInSeconds: Long
) {

    private var autoNotifyCallUntilTimestampSecond: Long =
        Instant.now().epochSecond + timeoutInSeconds
    private val incomingCallNotificationIds: MutableSet<Int> = HashSet()

    fun shouldNotify(): Boolean =
        autoNotifyCallUntilTimestampSecond > Instant.now().epochSecond

    fun notified(incomingCallNotificationId: Int) {
        incomingCallNotificationIds.add(incomingCallNotificationId)
    }

    fun getIncomingCallNotificationIds(): Set<Int> =
        ImmutableSet.copyOf(incomingCallNotificationIds)

    fun setMissedCall() {
        autoNotifyCallUntilTimestampSecond = 0L
    }

    fun setAccepted() {
        autoNotifyCallUntilTimestampSecond = 0L
    }

    fun cancel() {
        autoNotifyCallUntilTimestampSecond = 0L
    }

    companion object {
        @JvmStatic
        fun builder(): Builder = Builder()
    }

    class Builder {
        private var lineNotification: LineNotification? = null
        private var waitDurationInSeconds: Double = 0.0
        private var timeoutInSeconds: Long = 0L

        fun lineNotification(lineNotification: LineNotification): Builder {
            this.lineNotification = lineNotification
            return this
        }

        fun waitDurationInSeconds(waitDurationInSeconds: Double): Builder {
            this.waitDurationInSeconds = waitDurationInSeconds
            return this
        }

        fun timeoutInSeconds(timeoutInSeconds: Long): Builder {
            this.timeoutInSeconds = timeoutInSeconds
            return this
        }

        fun build(): AutoIncomingCallNotificationState =
            AutoIncomingCallNotificationState(
                lineNotification!!,
                waitDurationInSeconds,
                timeoutInSeconds
            )
    }
}
