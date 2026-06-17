package com.mysticwind.linenotificationsupport.chatname.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ChatTest {

    @Test
    fun equalityUsesIdAndName() {
        val left = Chat("id-1", "name")
        val right = Chat("id-1", "name")
        val different = Chat("id-2", "name")

        assertEquals(left, right)
        assertEquals(left.hashCode(), right.hashCode())
        assertNotEquals(left, different)
    }
}
