package com.mysticwind.linenotificationsupport.identicalmessage

import com.mysticwind.linenotificationsupport.model.LineNotification
import org.junit.Assert.assertEquals
import org.junit.Test

class IdenticalMessageHandledResultTest {

    @Test
    fun builderAndEqualityRemainStable() {
        val notification = LineNotification.builder()
            .message("message")
            .build()

        val left = IdenticalMessageHandledResult.builder()
            .notificationId(1)
            .replacedMessage("replaced")
            .lineNotification(notification)
            .build()

        val right = IdenticalMessageHandledResult.builder()
            .notificationId(1)
            .replacedMessage("replaced")
            .lineNotification(notification)
            .build()

        assertEquals(left, right)
        assertEquals(left.hashCode(), right.hashCode())
        assertEquals("replaced", left.replacedMessage)
    }
}
