package com.mysticwind.linenotificationsupport.line

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.common.collect.ImmutableSet
import com.mysticwind.linenotificationsupport.model.LineNotificationBuilder
import org.apache.commons.lang3.StringUtils
import java.time.Instant

object LineLauncher {

    private val NOT_CHAT_IDS: Set<String> = ImmutableSet.of(
        LineNotificationBuilder.CALL_VIRTUAL_CHAT_ID,
        LineNotificationBuilder.DEFAULT_CHAT_ID
    )

    @JvmStatic
    fun buildPendingIntent(context: Context, chatId: String?): PendingIntent {
        return PendingIntent.getActivity(
            context,
            0,
            buildIntentInternal(chatId, true),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    @JvmStatic
    fun buildIntent(chatId: String?): Intent {
        return buildIntentInternal(chatId, false)
    }

    private fun buildIntentInternal(chatId: String?, uniqueAction: Boolean): Intent {
        val intent: Intent
        if (isChatId(chatId)) {
            // Credit for launching LINE specific chat: https://www.dcard.tw/f/3c/p/227637855/b/78
            intent = Intent()
            intent.component = ComponentName(
                "jp.naver.line.android",
                "jp.naver.line.android.activity.shortcut.ShortcutLauncherActivity"
            )
            intent.putExtra("shortcutType", "chatmid")
            intent.putExtra("shortcutTargetId", chatId)
            intent.action = if (uniqueAction) {
                // https://stackoverflow.com/questions/3168484/pendingintent-works-correctly-for-the-first-notification-but-incorrectly-for-the
                chatId + Instant.now()
            } else {
                chatId
            }
        } else {
            intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://line.me/R/nv/chat")
        }
        return intent
    }

    private fun isChatId(chatId: String?): Boolean {
        if (StringUtils.isBlank(chatId)) return false
        if (NOT_CHAT_IDS.contains(chatId)) return false
        return true
    }
}
