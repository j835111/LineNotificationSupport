package com.mysticwind.linenotificationsupport.notification.impl

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DumbNotificationCounterTest {

    companion object {
        private const val MAX_NOTIFICATIONS = 5
        private const val GROUP_1 = "group1"
        private const val GROUP_2 = "group2"
    }

    private lateinit var classUnderTest: DumbNotificationCounter

    @Before
    fun setUp() {
        classUnderTest = DumbNotificationCounter(MAX_NOTIFICATIONS)
    }

    @Test
    fun testTracking() {
        assertEquals(1, classUnderTest.notified(GROUP_1, "1"))
        assertEquals(2, classUnderTest.notified(GROUP_1, "2"))
        assertEquals(3, classUnderTest.notified(GROUP_1, "1-group"))
        assertEquals(4, classUnderTest.notified(GROUP_2, "3"))
        assertEquals(5, classUnderTest.notified(GROUP_2, "4"))
        assertEquals(6, classUnderTest.notified(GROUP_2, "2-group"))
        assertEquals(5, classUnderTest.dismissed(GROUP_1, "2"))
        assertEquals(4, classUnderTest.dismissed(GROUP_1, "1"))
        assertEquals(3, classUnderTest.dismissed(GROUP_1, "1-group"))
        assertEquals(2, classUnderTest.dismissed(GROUP_2, "4"))
        assertEquals(1, classUnderTest.dismissed(GROUP_2, "3"))
        assertEquals(0, classUnderTest.dismissed(GROUP_2, "2-group"))
    }

    @Test
    fun testHasSlot() {
        assertTrue(classUnderTest.hasSlot(GROUP_1))
        assertTrue(classUnderTest.hasSlot(GROUP_2))

        classUnderTest.notified(GROUP_1, "1")
        assertTrue(classUnderTest.hasSlot(GROUP_1))
        assertTrue(classUnderTest.hasSlot(GROUP_2))

        classUnderTest.notified(GROUP_1, "2")
        assertTrue(classUnderTest.hasSlot(GROUP_1))
        assertTrue(classUnderTest.hasSlot(GROUP_2))

        classUnderTest.notified(GROUP_1, "1-group")
        assertTrue(classUnderTest.hasSlot(GROUP_1))
        assertTrue(classUnderTest.hasSlot(GROUP_2))

        classUnderTest.notified(GROUP_1, "3")
        assertFalse(classUnderTest.hasSlot(GROUP_1))
        assertTrue(classUnderTest.hasSlot(GROUP_2))

        classUnderTest.notified(GROUP_1, "1-group")
        assertFalse(classUnderTest.hasSlot(GROUP_1))
        assertTrue(classUnderTest.hasSlot(GROUP_2))

        classUnderTest.notified(GROUP_2, "4")
        assertFalse(classUnderTest.hasSlot(GROUP_1))
        assertFalse(classUnderTest.hasSlot(GROUP_2))
    }
}
