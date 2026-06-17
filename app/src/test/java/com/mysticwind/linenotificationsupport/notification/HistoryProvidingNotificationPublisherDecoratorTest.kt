package com.mysticwind.linenotificationsupport.notification

import com.google.common.collect.ImmutableList
import com.mysticwind.linenotificationsupport.model.LineNotification
import com.mysticwind.linenotificationsupport.model.NotificationHistoryEntry
import com.mysticwind.linenotificationsupport.preference.PreferenceProvider
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import java.util.Collections

@RunWith(MockitoJUnitRunner::class)
class HistoryProvidingNotificationPublisherDecoratorTest {

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val NOTIFICATION_ID_2 = 2
        private const val NOTIFICATION_ID_3 = 3
        private const val CHAT_ID = "chatId"
        private val NOTIFICATION_ID_FROM_CHAT_ID = CHAT_ID.hashCode()
        private const val LINE_MESSAGE_ID = "messageId"
        private const val LINE_MESSAGE_ID_2 = "messageId2"
        private const val MESSAGE = "message"
        private const val UPDATED_MESSAGE = "updatedMessage"
        private const val UPDATED_MESSAGE_2 = "updatedMessage2"
    }

    @Mock
    private lateinit var notificationPublisher: NotificationPublisher
    @Mock
    private lateinit var preferenceProvider: PreferenceProvider

    private lateinit var classUnderTest: HistoryProvidingNotificationPublisherDecorator

    @Before
    fun setUp() {
        classUnderTest = HistoryProvidingNotificationPublisherDecorator(notificationPublisher, preferenceProvider)
    }

    @Test
    fun publishNotificationReplacingPreviousNotification() {
        classUnderTest.publishNotification(
            LineNotification.builder()
                .lineMessageId(LINE_MESSAGE_ID)
                .message(MESSAGE)
                .chatId(CHAT_ID)
                .timestamp(1L)
                .build(),
            NOTIFICATION_ID
        )

        verify(notificationPublisher).publishNotification(
            LineNotification.builder()
                .lineMessageId(LINE_MESSAGE_ID)
                .message(MESSAGE)
                .chatId(CHAT_ID)
                .timestamp(1L)
                .history(Collections.emptyList())
                .build(),
            NOTIFICATION_ID_FROM_CHAT_ID
        )

        classUnderTest.publishNotification(
            LineNotification.builder()
                .lineMessageId(LINE_MESSAGE_ID)
                .message(UPDATED_MESSAGE)
                .chatId(CHAT_ID)
                .timestamp(2L)
                .build(),
            NOTIFICATION_ID_2
        )

        verify(notificationPublisher).publishNotification(
            LineNotification.builder()
                .lineMessageId(LINE_MESSAGE_ID)
                .message(UPDATED_MESSAGE)
                .chatId(CHAT_ID)
                .timestamp(2L)
                .history(Collections.emptyList())
                .build(),
            NOTIFICATION_ID_FROM_CHAT_ID
        )

        classUnderTest.publishNotification(
            LineNotification.builder()
                .lineMessageId(LINE_MESSAGE_ID_2)
                .message(MESSAGE)
                .chatId(CHAT_ID)
                .timestamp(3L)
                .build(),
            NOTIFICATION_ID_2
        )

        verify(notificationPublisher).publishNotification(
            LineNotification.builder()
                .lineMessageId(LINE_MESSAGE_ID_2)
                .message(MESSAGE)
                .chatId(CHAT_ID)
                .timestamp(3L)
                .history(ImmutableList.of(
                    NotificationHistoryEntry(LINE_MESSAGE_ID, UPDATED_MESSAGE, null, 2L, null)
                ))
                .build(),
            NOTIFICATION_ID_FROM_CHAT_ID
        )

        classUnderTest.publishNotification(
            LineNotification.builder()
                .lineMessageId(LINE_MESSAGE_ID_2)
                .message(UPDATED_MESSAGE)
                .chatId(CHAT_ID)
                .timestamp(4L)
                .build(),
            NOTIFICATION_ID_2
        )

        verify(notificationPublisher).publishNotification(
            LineNotification.builder()
                .lineMessageId(LINE_MESSAGE_ID_2)
                .message(UPDATED_MESSAGE)
                .chatId(CHAT_ID)
                .timestamp(4L)
                .history(ImmutableList.of(
                    NotificationHistoryEntry(LINE_MESSAGE_ID, UPDATED_MESSAGE, null, 2L, null)
                ))
                .build(),
            NOTIFICATION_ID_FROM_CHAT_ID
        )

        classUnderTest.publishNotification(
            LineNotification.builder()
                .lineMessageId(LINE_MESSAGE_ID)
                .message(UPDATED_MESSAGE_2)
                .chatId(CHAT_ID)
                .timestamp(2L)
                .build(),
            NOTIFICATION_ID_3
        )

        verify(notificationPublisher).publishNotification(
            LineNotification.builder()
                .lineMessageId(LINE_MESSAGE_ID_2)
                .message(UPDATED_MESSAGE)
                .chatId(CHAT_ID)
                .timestamp(4L)
                .history(ImmutableList.of(
                    NotificationHistoryEntry(LINE_MESSAGE_ID, UPDATED_MESSAGE_2, null, 2L, null)
                ))
                .build(),
            NOTIFICATION_ID_FROM_CHAT_ID
        )
    }
}
