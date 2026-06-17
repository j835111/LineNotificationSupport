package com.mysticwind.linenotificationsupport.reply

import android.app.Notification
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InOrder
import org.mockito.Mock
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
class WearableReplyActionSemanticsTest {

    private lateinit var classUnderTest: WearableReplyActionSemantics

    @Mock
    private lateinit var builder: Notification.Action.Builder

    @Before
    fun setUp() {
        classUnderTest = WearableReplyActionSemantics()
        whenever(builder.setAllowGeneratedReplies(true)).thenReturn(builder)
        whenever(builder.setSemanticAction(Notification.Action.SEMANTIC_ACTION_REPLY)).thenReturn(builder)
    }

    @Test
    fun applyToMarksActionAsGeneratedReplyFriendlyAndSemanticReply() {
        val result = classUnderTest.applyTo(builder)

        assertSame(builder, result)

        val inOrder: InOrder = inOrder(builder)
        inOrder.verify(builder).setAllowGeneratedReplies(true)
        inOrder.verify(builder).setSemanticAction(Notification.Action.SEMANTIC_ACTION_REPLY)
        verifyNoMoreInteractions(builder)
    }
}
