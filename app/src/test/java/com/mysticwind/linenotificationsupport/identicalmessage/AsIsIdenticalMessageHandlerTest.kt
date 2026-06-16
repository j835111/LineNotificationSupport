package com.mysticwind.linenotificationsupport.identicalmessage

import com.mysticwind.linenotificationsupport.model.LineNotification
import org.junit.Assert.assertEquals
import org.junit.Test

class AsIsIdenticalMessageHandlerTest {

    companion object {
        private val NOTIFICATION_1 = LineNotification.builder().build()
        private val NOTIFICATION_2 = LineNotification.builder().build()
        private val CLASS_UNDER_TEST = AsIsIdenticalMessageHandler()
    }

    @Test
    fun testHandle() {
        val result1 = CLASS_UNDER_TEST.handle(NOTIFICATION_1, 1)
        assertEquals(NOTIFICATION_1, result1.get().left)
        assertEquals(1, result1.get().right.toInt())

        val result2 = CLASS_UNDER_TEST.handle(NOTIFICATION_1, 2)
        assertEquals(NOTIFICATION_1, result2.get().left)
        assertEquals(2, result2.get().right.toInt())

        val result3 = CLASS_UNDER_TEST.handle(NOTIFICATION_2, 3)
        assertEquals(NOTIFICATION_2, result3.get().left)
        assertEquals(3, result3.get().right.toInt())
    }
}
