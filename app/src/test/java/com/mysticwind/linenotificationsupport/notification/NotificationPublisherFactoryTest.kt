package com.mysticwind.linenotificationsupport.notification

import android.content.Context
import android.os.Handler
import com.mysticwind.linenotificationsupport.preference.PreferenceProvider
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class NotificationPublisherFactoryTest {

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var simpleNotificationPublisher: SimpleNotificationPublisher

    @Mock
    private lateinit var handler: Handler

    @Mock
    private lateinit var preferenceProvider: PreferenceProvider

    @Mock
    private lateinit var slotAvailabilityChecker: SlotAvailabilityChecker

    private lateinit var classUnderTest: NotificationPublisherFactory

    @Before
    fun setUp() {
        classUnderTest = NotificationPublisherFactory(
            context,
            simpleNotificationPublisher,
            handler,
            preferenceProvider,
            slotAvailabilityChecker
        )
    }

    @Test
    fun initializeIfNeededCreatesPublisherWhenUninitialized() {
        assertSame(NullNotificationPublisher.INSTANCE, classUnderTest.get())

        classUnderTest.initializeIfNeeded()

        assertNotSame(NullNotificationPublisher.INSTANCE, classUnderTest.get())
    }

    @Test
    fun initializeIfNeededKeepsExistingPublisherState() {
        classUnderTest.notifyChange()
        val existingPublisher = classUnderTest.get()

        classUnderTest.initializeIfNeeded()

        assertSame(existingPublisher, classUnderTest.get())
    }
}
