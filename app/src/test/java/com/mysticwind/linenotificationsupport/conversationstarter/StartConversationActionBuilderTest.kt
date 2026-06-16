package com.mysticwind.linenotificationsupport.conversationstarter

import android.app.Notification
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.Context
import android.content.Intent
import com.mysticwind.linenotificationsupport.R
import com.mysticwind.linenotificationsupport.reply.WearableReplyActionSemantics
import com.mysticwind.linenotificationsupport.ui.LocalizationDao
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mockConstruction
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
class StartConversationActionBuilderTest {

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var localizationDao: LocalizationDao

    @Mock
    private lateinit var wearableReplyActionSemantics: WearableReplyActionSemantics

    @Mock
    private lateinit var replyPendingIntent: PendingIntent

    @Mock
    private lateinit var disablePendingIntent: PendingIntent

    private lateinit var classUnderTest: StartConversationActionBuilder

    @Before
    fun setUp() {
        classUnderTest = StartConversationActionBuilder(context, localizationDao, wearableReplyActionSemantics)
        whenever(localizationDao.getLocalizedString(R.string.conversation_start_notification_action_button_message))
            .thenReturn("Message")
        whenever(localizationDao.getLocalizedString(R.string.conversation_start_notification_action_button))
            .thenReturn("Start")
        whenever(localizationDao.getLocalizedString(R.string.conversation_start_notification_disable_feature_action_button))
            .thenReturn("Disable")
    }

    @Test
    fun buildRemoteInputActionRoutesBuilderThroughWearableReplySemantics() {
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

                    val result = classUnderTest.buildRemoteInputAction()

                    val constructedBuilder = actionBuilderMock.constructed()[0]
                    assertSame(builtAction, result)
                    verify(wearableReplyActionSemantics).applyTo(constructedBuilder)
                }
            }
            } // mockConstruction(Intent)
        }
    }

    @Test
    fun buildDisableFeatureActionDoesNotUseWearableReplySemantics() {
        val disableAction = mock<Notification.Action>()
        var actionBuilderIndex = 0

        mockStatic(PendingIntent::class.java).use { pendingIntentMock ->
            mockConstruction(Intent::class.java).use { _ ->
            mockConstruction(RemoteInput.Builder::class.java) { mock, _ ->
                whenever(mock.setLabel(any())).thenReturn(mock)
                whenever(mock.build()).thenReturn(mock<RemoteInput>())
            }.use { _ ->
            mockConstruction(Notification.Action.Builder::class.java) { mock, _ ->
                actionBuilderIndex++
                if (actionBuilderIndex == 1) {
                    // First builder: for remote input action — must complete without NPE
                    whenever(mock.addRemoteInput(any())).thenReturn(mock)
                    whenever(mock.build()).thenReturn(mock<Notification.Action>())
                    whenever(wearableReplyActionSemantics.applyTo(mock)).thenReturn(mock)
                } else {
                    // Second builder: for disable feature action
                    whenever(mock.build()).thenReturn(disableAction)
                }
            }.use { _ ->
                pendingIntentMock.`when`<PendingIntent> {
                    PendingIntent.getBroadcast(any<Context>(), any<Int>(), any(), any<Int>())
                }.thenReturn(disablePendingIntent)

                val result = classUnderTest.buildActions()[1]

                assertSame(disableAction, result)
                // applyTo called once (for the remote input action), not for the disable action
                verify(wearableReplyActionSemantics, times(1)).applyTo(any())
            }
            } // RemoteInput.Builder
            } // mockConstruction(Intent)
        }
    }
}
