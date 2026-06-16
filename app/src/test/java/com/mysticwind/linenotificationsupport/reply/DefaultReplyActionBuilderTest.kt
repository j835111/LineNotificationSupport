package com.mysticwind.linenotificationsupport.reply

import android.app.Notification
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.Context
import android.content.Intent
import com.mysticwind.linenotificationsupport.R
import com.mysticwind.linenotificationsupport.ui.LocalizationDao
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mockConstruction
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
class DefaultReplyActionBuilderTest {

    companion object {
        private const val CHAT_ID = "chat-id"
    }

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var localizationDao: LocalizationDao

    @Mock
    private lateinit var wearableReplyActionSemantics: WearableReplyActionSemantics

    @Mock
    private lateinit var replyPendingIntent: PendingIntent

    @Mock
    private lateinit var originalLineReplyAction: Notification.Action

    private lateinit var classUnderTest: DefaultReplyActionBuilder

    @Before
    fun setUp() {
        classUnderTest = DefaultReplyActionBuilder(context, localizationDao, wearableReplyActionSemantics)
        whenever(localizationDao.getLocalizedString(R.string.conversation_notification_action_button_message))
            .thenReturn("Reply message")
        whenever(localizationDao.getLocalizedString(R.string.conversation_notification_action_button))
            .thenReturn("Reply")
    }

    @Test
    fun buildReplyActionRoutesBuilderThroughWearableReplySemantics() {
        val remoteInput = mock<RemoteInput>()
        val builtAction = mock<Notification.Action>()

        mockStatic(PendingIntent::class.java).use { pendingIntentMock ->
            mockConstruction(Intent::class.java).use { _ ->
            mockConstruction(RemoteInput.Builder::class.java) { mock, _ ->
                whenever(mock.setLabel(any())).thenReturn(mock)
                whenever(mock.build()).thenReturn(remoteInput)
            }.use { _ ->
                mockConstruction(Notification.Action.Builder::class.java) { mock, _ ->
                    whenever(mock.addRemoteInput(remoteInput)).thenReturn(mock)
                    whenever(mock.build()).thenReturn(builtAction)
                    whenever(wearableReplyActionSemantics.applyTo(mock)).thenReturn(mock)
                }.use { actionBuilderMock ->
                    pendingIntentMock.`when`<PendingIntent> {
                        PendingIntent.getBroadcast(any<Context>(), any<Int>(), any(), any<Int>())
                    }.thenReturn(replyPendingIntent)

                    val result = classUnderTest.buildReplyAction(CHAT_ID, originalLineReplyAction)

                    val constructedBuilder = actionBuilderMock.constructed()[0]
                    assertSame(builtAction, result)
                    verify(wearableReplyActionSemantics).applyTo(constructedBuilder)
                }
            }
            } // mockConstruction(Intent)
        }
    }
}
