package com.mysticwind.linenotificationsupport.notification

import com.mysticwind.linenotificationsupport.model.LineNotification
import com.mysticwind.linenotificationsupport.preference.PreferenceProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.eq
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.capture
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
class BigNotificationSplittingNotificationPublisherDecoratorTest {

    @Mock
    private lateinit var notificationPublisher: NotificationPublisher

    @Mock
    private lateinit var preferenceProvider: PreferenceProvider

    @Captor
    private lateinit var lineNotificationCaptor: ArgumentCaptor<LineNotification>

    private lateinit var classUnderTest: BigNotificationSplittingNotificationPublisherDecorator

    @Before
    fun setUp() {
        whenever(preferenceProvider.getMessageSizeLimit()).thenReturn(15)
        whenever(preferenceProvider.getMaxPageCount()).thenReturn(3)

        classUnderTest = BigNotificationSplittingNotificationPublisherDecorator(notificationPublisher, preferenceProvider)
    }

    @After
    fun tearDown() {
        verifyNoMoreInteractions(notificationPublisher, preferenceProvider)
    }

    @Test
    fun testSingleNotification() {
        classUnderTest.publishNotification(buildNotification("123456789012345"), 1)

        verify(notificationPublisher).publishNotification(capture(lineNotificationCaptor), eq(1))
        assertEquals("123456789012345", lineNotificationCaptor.value.message)
        verify(preferenceProvider).getMessageSizeLimit()
    }

    @Test
    fun testTwoNotifications() {
        classUnderTest.publishNotification(buildNotification("1234567890123456"), 1)

        verify(notificationPublisher).publishNotification(capture(lineNotificationCaptor), eq(1))
        val lineNotification = lineNotificationCaptor.value
        assertEquals("1234567890(...)", lineNotification.messages[0])
        assertEquals("(...)123456", lineNotification.messages[1])
        verify(preferenceProvider).getMessageSizeLimit()
        verify(preferenceProvider).getMaxPageCount()
    }

    @Test
    fun testTwoNotificationsOnEdge() {
        classUnderTest.publishNotification(buildNotification("12345678901234567890"), 1)

        verify(notificationPublisher).publishNotification(capture(lineNotificationCaptor), eq(1))
        val lineNotification = lineNotificationCaptor.value
        assertEquals("1234567890(...)", lineNotification.messages[0])
        assertEquals("(...)1234567890", lineNotification.messages[1])
        verify(preferenceProvider).getMessageSizeLimit()
        verify(preferenceProvider).getMaxPageCount()
    }

    @Test
    fun testThreeNotifications() {
        classUnderTest.publishNotification(buildNotification("123456789012345678901"), 1)

        verify(notificationPublisher).publishNotification(capture(lineNotificationCaptor), eq(1))
        val lineNotification = lineNotificationCaptor.value
        assertEquals("1234567890(...)", lineNotification.messages[0])
        assertEquals("(...)12345(...)", lineNotification.messages[1])
        assertEquals("(...)678901", lineNotification.messages[2])
        verify(preferenceProvider).getMessageSizeLimit()
        verify(preferenceProvider).getMaxPageCount()
    }

    @Test
    fun testThreeNotificationsOnEdge() {
        classUnderTest.publishNotification(buildNotification("1234567890123456789012345"), 1)

        verify(notificationPublisher).publishNotification(capture(lineNotificationCaptor), eq(1))
        val lineNotification = lineNotificationCaptor.value
        assertEquals("1234567890(...)", lineNotification.messages[0])
        assertEquals("(...)12345(...)", lineNotification.messages[1])
        assertEquals("(...)6789012345", lineNotification.messages[2])
        verify(preferenceProvider).getMessageSizeLimit()
        verify(preferenceProvider).getMaxPageCount()
    }

    @Test
    fun testThreeNotificationsExceedingMaxPages() {
        classUnderTest.publishNotification(buildNotification("12345678901234567890123456"), 1)

        verify(notificationPublisher).publishNotification(capture(lineNotificationCaptor), eq(1))
        val lineNotification = lineNotificationCaptor.value
        assertEquals("1234567890(...)", lineNotification.messages[0])
        assertEquals("(...)12345(...)", lineNotification.messages[1])
        assertEquals("(...)67890(...)", lineNotification.messages[2])
        verify(preferenceProvider).getMessageSizeLimit()
        verify(preferenceProvider).getMaxPageCount()
    }

    @Test
    fun testTwoNotificationsWithEnglishWords() {
        classUnderTest.publishNotification(buildNotification("i am testing a sentence"), 1)

        verify(notificationPublisher).publishNotification(capture(lineNotificationCaptor), eq(1))
        val lineNotification = lineNotificationCaptor.value
        assertEquals("i am(...)", lineNotification.messages[0])
        assertEquals("(...)testi(...)", lineNotification.messages[1])
        assertEquals("(...)ng a(...)", lineNotification.messages[2])
        verify(preferenceProvider).getMessageSizeLimit()
        verify(preferenceProvider).getMaxPageCount()
    }

    @Test
    fun testMultipleNotificationsWithUrl() {
        classUnderTest.publishNotification(buildNotification("123456789012345 http://www.google.com"), 1)

        verify(notificationPublisher).publishNotification(capture(lineNotificationCaptor), eq(1))
        val lineNotification = lineNotificationCaptor.value
        assertEquals("1234567890(...)", lineNotification.messages[0])
        assertEquals("(...)12345 http://www.google.com", lineNotification.messages[1])
        verify(preferenceProvider).getMessageSizeLimit()
        verify(preferenceProvider).getMaxPageCount()
    }

    @Test
    fun testMultipleNotificationsStartingWithUrl() {
        classUnderTest.publishNotification(buildNotification("http://google.com 1234567890"), 1)

        verify(notificationPublisher).publishNotification(capture(lineNotificationCaptor), eq(1))
        val lineNotification = lineNotificationCaptor.value
        assertEquals("http://google.com (...)", lineNotification.messages[0])
        assertEquals("(...)1234567890", lineNotification.messages[1])
        verify(preferenceProvider).getMessageSizeLimit()
        verify(preferenceProvider).getMaxPageCount()
    }

    private fun buildNotification(message: String): LineNotification {
        return LineNotification.builder().message(message).build()
    }
}
