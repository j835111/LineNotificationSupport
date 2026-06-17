package com.mysticwind.linenotificationsupport.identicalmessage

import com.mysticwind.linenotificationsupport.model.LineNotification
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
class MergeIdenticalMessageHandlerTest {

    companion object {
        private const val MESSAGE = "message"
        private const val MERGED_MESSAGE = "message (2)"
        private val ORIGINAL_NOTIFICATION = LineNotification.builder().message(MESSAGE).build()
        private val MERGED_NOTIFICATION = LineNotification.builder().message(MERGED_MESSAGE).build()
    }

    @Mock
    private lateinit var evaluator: IdenticalMessageEvaluator

    private lateinit var classUnderTest: MergeIdenticalMessageHandler

    @Before
    fun setUp() {
        classUnderTest = MergeIdenticalMessageHandler(evaluator)
    }

    @Test
    fun testHandleNoDuplicate() {
        whenever(evaluator.evaluate(any(), any())).thenReturn(IdenticalMessageEvaluator.EvaluationResult.noDuplicate())

        val result = classUnderTest.handle(ORIGINAL_NOTIFICATION, 1)
        assertEquals(ORIGINAL_NOTIFICATION, result.get().left)
        assertEquals(1, result.get().right.toInt())
    }

    @Test
    fun testHandleIsDuplicate() {
        whenever(evaluator.evaluate(any(), any()))
            .thenReturn(IdenticalMessageEvaluator.EvaluationResult.withDuplicate(ORIGINAL_NOTIFICATION, 1, 2))

        val result = classUnderTest.handle(ORIGINAL_NOTIFICATION, 2)
        assertTrue(result.isPresent)
        assertEquals(MERGED_NOTIFICATION, result.get().key)
        assertEquals(1, result.get().right.toInt())
    }
}
