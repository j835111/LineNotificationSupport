package com.mysticwind.linenotificationsupport.reply

import android.app.Notification
import android.os.Bundle
import android.os.Parcelable
import org.junit.Assert.assertEquals
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class MockedReplyActionBuilder : ReplyActionBuilder {

    override fun buildReplyAction(chatId: String, originalLineReplyAction: Notification.Action): Notification.Action {
        val action = mock<Notification.Action>()
        val bundle = mock<Bundle>()
        @Suppress("DEPRECATION")
        whenever(bundle.getParcelable<Parcelable>(KEY)).thenReturn(originalLineReplyAction)
        whenever(action.extras).thenReturn(bundle)
        return action
    }

    companion object {
        private const val KEY = "key"

        @JvmStatic
        fun validateAction(expectedAction: Notification.Action, actualAction: Notification.Action) {
            @Suppress("DEPRECATION")
            val parcelable = actualAction.extras.getParcelable<Parcelable>(KEY)
            assertEquals(expectedAction, parcelable)
        }
    }
}
