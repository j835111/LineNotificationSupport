package com.mysticwind.linenotificationsupport.identicalmessage

import androidx.core.app.Person
import com.mysticwind.linenotificationsupport.identicalmessage.IdenticalMessageEvaluator.Companion.LINE_NOTIFICATION_COMPARATOR
import com.mysticwind.linenotificationsupport.model.LineNotification
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Random

class IdenticalMessageEvaluatorTest {

    companion object {
        private const val MESSAGE = "message"
        private const val DIFFERENT_MESSAGE = "differentMessage"
        private const val CHAT_ID_1 = "chatId1"
        private const val CHAT_ID_2 = "chatId2"
        private const val SENDER_NAME = "senderName"
        private const val TITLE = "title"
        private const val LINE_STICKER_URL = "lineStickerUrl"
        private val FILLED_LINE_NOTIFICATION = LineNotification.builder()
            .message(MESSAGE)
            .sender(Person.Builder().setName(SENDER_NAME).build())
            .title(TITLE)
            .lineStickerUrl(LINE_STICKER_URL)
            .chatId(CHAT_ID_1)
            .callState(LineNotification.CallState.IN_A_CALL)
            .build()
    }

    @Test
    fun testEvaluate() {
        val classUnderTest = IdenticalMessageEvaluator()
        assertEquals(IdenticalMessageEvaluator.EvaluationResult.noDuplicate(), classUnderTest.evaluate(buildNotification(MESSAGE, CHAT_ID_1), 1))
        assertEquals(IdenticalMessageEvaluator.EvaluationResult.noDuplicate(), classUnderTest.evaluate(buildNotification(MESSAGE, CHAT_ID_2), 2))
        val result1 = classUnderTest.evaluate(buildNotification(MESSAGE, CHAT_ID_1), 3)
        assertEquals(1, result1.getNotificationId().get().toInt())
        assertEquals(2, result1.numberOfDuplicates)
        assertEquals(MESSAGE, result1.getPreviousLineNotification().get().message)
        val result2 = classUnderTest.evaluate(buildNotification(MESSAGE, CHAT_ID_1), 4)
        assertEquals(1, result2.getNotificationId().get().toInt())
        assertEquals(3, result2.numberOfDuplicates)
        assertEquals(MESSAGE, result2.getPreviousLineNotification().get().message)
        val result3 = classUnderTest.evaluate(buildNotification(MESSAGE, CHAT_ID_1), 5)
        assertEquals(1, result3.getNotificationId().get().toInt())
        assertEquals(4, result3.numberOfDuplicates)
        assertEquals(MESSAGE, result3.getPreviousLineNotification().get().message)
        assertEquals(IdenticalMessageEvaluator.EvaluationResult.noDuplicate(), classUnderTest.evaluate(buildNotification(DIFFERENT_MESSAGE, CHAT_ID_1), 6))
    }

    @Test
    fun testComparisonToPreventNullPointerException() {
        LINE_NOTIFICATION_COMPARATOR.compare(FILLED_LINE_NOTIFICATION, FILLED_LINE_NOTIFICATION)
        LINE_NOTIFICATION_COMPARATOR.compare(FILLED_LINE_NOTIFICATION, FILLED_LINE_NOTIFICATION.toBuilder().message(null).build())
        LINE_NOTIFICATION_COMPARATOR.compare(FILLED_LINE_NOTIFICATION, FILLED_LINE_NOTIFICATION.toBuilder().sender(null).build())
        LINE_NOTIFICATION_COMPARATOR.compare(FILLED_LINE_NOTIFICATION, FILLED_LINE_NOTIFICATION.toBuilder().sender(Person.Builder().build()).build())
        LINE_NOTIFICATION_COMPARATOR.compare(FILLED_LINE_NOTIFICATION, FILLED_LINE_NOTIFICATION.toBuilder().title(null).build())
        LINE_NOTIFICATION_COMPARATOR.compare(FILLED_LINE_NOTIFICATION, FILLED_LINE_NOTIFICATION.toBuilder().lineStickerUrl(null).build())
        LINE_NOTIFICATION_COMPARATOR.compare(FILLED_LINE_NOTIFICATION, FILLED_LINE_NOTIFICATION.toBuilder().chatId(null).build())
        LINE_NOTIFICATION_COMPARATOR.compare(FILLED_LINE_NOTIFICATION, FILLED_LINE_NOTIFICATION.toBuilder().callState(null).build())
    }

    private fun buildNotification(message: String, chatId: String): LineNotification {
        return FILLED_LINE_NOTIFICATION.toBuilder()
            .message(message)
            .chatId(chatId)
            .timestamp(Random().nextInt().toLong())
            .build()
    }
}
