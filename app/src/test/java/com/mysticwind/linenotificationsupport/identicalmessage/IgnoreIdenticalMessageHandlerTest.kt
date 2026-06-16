package com.mysticwind.linenotificationsupport.identicalmessage

import com.mysticwind.linenotificationsupport.model.LineNotification
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
class IgnoreIdenticalMessageHandlerTest {

    companion object {
        private val NOTIFICATION_1 = LineNotification.builder().build()
        private val NOTIFICATION_2 = LineNotification.builder().build()
    }

    @Mock
    private lateinit var evaluator: IdenticalMessageEvaluator

    private lateinit var classUnderTest: IgnoreIdenticalMessageHandler

    @Before
    fun setUp() {
        classUnderTest = IgnoreIdenticalMessageHandler(evaluator)
    }

    @Test
    fun testHandleNoDuplicate() {
        whenever(evaluator.evaluate(any(), any())).thenReturn(IdenticalMessageEvaluator.EvaluationResult.noDuplicate())

        val result = classUnderTest.handle(NOTIFICATION_2, 1)
        assertEquals(NOTIFICATION_2, result.get().left)
        assertEquals(1, result.get().right.toInt())
    }

    @Test
    fun testHandleIsDuplicate() {
        whenever(evaluator.evaluate(any(), any()))
            .thenReturn(IdenticalMessageEvaluator.EvaluationResult.withDuplicate(NOTIFICATION_1, 1, 2))

        val result = classUnderTest.handle(NOTIFICATION_2, 2)
        assertFalse(result.isPresent)
    }
}
