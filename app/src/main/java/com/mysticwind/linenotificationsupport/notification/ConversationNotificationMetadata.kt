package com.mysticwind.linenotificationsupport.notification

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.content.LocusIdCompat
import androidx.core.graphics.drawable.IconCompat
import com.mysticwind.linenotificationsupport.line.LineLauncher
import com.mysticwind.linenotificationsupport.model.LineNotification
import com.mysticwind.linenotificationsupport.model.LineNotificationBuilder
import timber.log.Timber
import java.nio.charset.StandardCharsets
import java.util.UUID

object ConversationNotificationMetadata {

    private const val LINE_SHORTCUT_PREFIX = "line-conversation-chat-"
    private const val LINE_FALLBACK_SHORTCUT_PREFIX = "line-conversation-fallback-"
    private const val DEFAULT_LABEL = "LINE"

    @JvmStatic
    fun applyToBuilder(
        context: Context,
        builder: NotificationCompat.Builder,
        lineNotification: LineNotification
    ) {
        val shortcutId = buildShortcutId(lineNotification) ?: return
        val locusId = LocusIdCompat(shortcutId)

        val shortcutBuilder = ShortcutInfoCompat.Builder(context, shortcutId)
            .setShortLabel(buildShortcutLabel(lineNotification))
            .setLongLabel(buildShortcutLongLabel(lineNotification))
            .setIntent(LineLauncher.buildIntent(lineNotification.chatId))
            .setLongLived(true)
            .setLocusId(locusId)

        lineNotification.sender?.let { shortcutBuilder.setPersons(arrayOf(it)) }
        lineNotification.icon?.let { shortcutBuilder.setIcon(IconCompat.createWithBitmap(it)) }

        val shortcut = shortcutBuilder.build()
        try {
            if (!ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)) {
                Timber.w("Failed to publish conversation shortcut for [%s]", shortcutId)
            }
        } catch (e: Exception) {
            Timber.w(e, "Failed to publish conversation shortcut for [%s]: %s", shortcutId, e.message)
        }

        builder.setShortcutInfo(shortcut)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
    }

    @JvmStatic
    fun buildShortcutId(lineNotification: LineNotification): String? {
        if (lineNotification.callState != null) {
            return null
        }

        val chatId = lineNotification.chatId
        if (!chatId.isNullOrBlank() && chatId != LineNotificationBuilder.DEFAULT_CHAT_ID) {
            return LINE_SHORTCUT_PREFIX + chatId
        }

        val title = lineNotification.title?.trim().orEmpty()
        val sender = lineNotification.sender?.name?.toString()?.trim().orEmpty()
        val fallbackSeed = listOf(title, sender)
            .filter { it.isNotBlank() }
            .joinToString("|")
        if (fallbackSeed.isBlank()) {
            return null
        }

        val fallbackId = UUID.nameUUIDFromBytes(fallbackSeed.toByteArray(StandardCharsets.UTF_8))
        return LINE_FALLBACK_SHORTCUT_PREFIX + fallbackId
    }

    @JvmStatic
    fun buildShortcutLabel(lineNotification: LineNotification): String {
        return lineNotification.title
            ?.takeIf { it.isNotBlank() }
            ?: lineNotification.sender?.name?.toString()?.takeIf { it.isNotBlank() }
            ?: DEFAULT_LABEL
    }

    private fun buildShortcutLongLabel(lineNotification: LineNotification): String {
        val title = lineNotification.title?.trim().orEmpty()
        val sender = lineNotification.sender?.name?.toString()?.trim().orEmpty()
        return when {
            title.isNotBlank() && sender.isNotBlank() && title != sender -> "$title • $sender"
            title.isNotBlank() -> title
            sender.isNotBlank() -> sender
            else -> DEFAULT_LABEL
        }
    }
}
