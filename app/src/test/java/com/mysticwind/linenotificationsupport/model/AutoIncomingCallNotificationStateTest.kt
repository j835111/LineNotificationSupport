package com.mysticwind.linenotificationsupport.model

import com.google.common.collect.ImmutableSet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.util.Collections

@RunWith(MockitoJUnitRunner::class)
class AutoIncomingCallNotificationStateTest {

    companion object {
        private const val NOTIFICATION_ID = 100
    }

    private val lineNotification = LineNotification.builder().build()

    @Test
    fun testHappyCase() {
        val classUnderTest = AutoIncomingCallNotificationState.builder()
            .lineNotification(lineNotification)
            .timeoutInSeconds(1)
            .build()
        assertEquals(Collections.EMPTY_SET, classUnderTest.getIncomingCallNotificationIds())

        assertTrue(classUnderTest.shouldNotify())

        classUnderTest.notified(NOTIFICATION_ID)
        Thread.sleep(1000)

        assertFalse(classUnderTest.shouldNotify())
        assertEquals(ImmutableSet.of(NOTIFICATION_ID), classUnderTest.getIncomingCallNotificationIds())
        assertEquals(lineNotification, classUnderTest.lineNotification)
    }

    @Test
    fun testMissedCall() {
        val classUnderTest = AutoIncomingCallNotificationState.builder()
            .lineNotification(lineNotification)
            .timeoutInSeconds(1)
            .build()

        assertTrue(classUnderTest.shouldNotify())

        classUnderTest.setMissedCall()

        assertFalse(classUnderTest.shouldNotify())
    }

    @Test
    fun testCancel() {
        val classUnderTest = AutoIncomingCallNotificationState.builder()
            .lineNotification(lineNotification)
            .timeoutInSeconds(1)
            .build()

        assertTrue(classUnderTest.shouldNotify())

        classUnderTest.cancel()

        assertFalse(classUnderTest.shouldNotify())
    }

    @Test
    fun testAccepted() {
        val classUnderTest = AutoIncomingCallNotificationState.builder()
            .lineNotification(lineNotification)
            .timeoutInSeconds(1)
            .build()

        assertTrue(classUnderTest.shouldNotify())

        classUnderTest.setAccepted()

        assertFalse(classUnderTest.shouldNotify())
    }
}
