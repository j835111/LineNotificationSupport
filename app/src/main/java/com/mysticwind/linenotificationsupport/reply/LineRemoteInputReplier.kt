package com.mysticwind.linenotificationsupport.reply

import android.app.Notification
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.Context
import android.content.Intent
import android.os.Bundle
import dagger.hilt.android.qualifiers.ApplicationContext
import org.apache.commons.lang3.Validate
import timber.log.Timber
import java.time.Instant
import java.util.Arrays
import java.util.Objects
import javax.inject.Inject
import javax.inject.Singleton

/**
 * https://medium.com/@polidea/how-to-respond-to-any-messaging-notification-on-android-7befa483e2d7
 * https://stackoverflow.com/questions/59251922/how-to-send-a-reply-from-a-notification
 */
@Singleton
class LineRemoteInputReplier @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val LINE_TEXT_REMOTE_INPUT_KEY = "line.text"
    }

    init {
        Objects.requireNonNull(context)
    }

    private val onFinished = PendingIntent.OnFinished { pendingIntent, intent, resultCode, resultData, resultExtras ->
        Timber.i("Completed sending pending intent action [%s], code [%d], data [%s]",
            intent.action, resultCode, resultData)
    }

    fun sendReply(replyAction: Notification.Action, responseText: String) {
        Objects.requireNonNull(replyAction)
        Objects.requireNonNull(responseText)
        Validate.isTrue(replyAction.remoteInputs != null || replyAction.remoteInputs.isNotEmpty(),
            "Invalid remote input from reply action: ${replyAction.title}")

        val intent = Intent()
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)

        val bundle = Bundle()
        val remoteInputKey = resolveRemoteInputKey(replyAction.remoteInputs)
        bundle.putString(remoteInputKey, responseText)

        RemoteInput.addResultsToIntent(replyAction.remoteInputs, intent, bundle)
        try {
            val code = Instant.now().toEpochMilli().toInt()
            replyAction.actionIntent.send(context, code, intent, onFinished, null)
            Timber.d("Sent intent with bundle [%s]", bundle.toString())
        } catch (e: PendingIntent.CanceledException) {
            Timber.e(e, "Failed to send message to LINE: %s", e.message)
        }
    }

    private fun resolveRemoteInputKey(remoteInputs: Array<RemoteInput>): String {
        if (remoteInputs.size == 1) {
            return remoteInputs[0].resultKey
        }
        val remoteInputKeys = Arrays.stream(remoteInputs)
            .map { remoteInput -> remoteInput.resultKey }
            .reduce { remoteInput1, remoteInput2 -> String.format("%1,%2", remoteInput1, remoteInput2) }
            .orElse("N/A")
        Timber.w("More than one remoteInput: %s", remoteInputKeys)
        return LINE_TEXT_REMOTE_INPUT_KEY
    }
}
