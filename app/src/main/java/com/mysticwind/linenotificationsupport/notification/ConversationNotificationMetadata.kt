package com.mysticwind.linenotificationsupport.notification

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.app.NotificationCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.content.LocusIdCompat
import androidx.core.graphics.drawable.IconCompat
import com.mysticwind.linenotificationsupport.line.Constants.LINE_PACKAGE_NAME
import com.mysticwind.linenotificationsupport.line.LineLauncher
import com.mysticwind.linenotificationsupport.model.LineNotification
import com.mysticwind.linenotificationsupport.model.LineNotificationBuilder
import timber.log.Timber

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
        buildShortcutIcon(context, lineNotification)?.let { shortcutBuilder.setIcon(it) }

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

        val fallbackSeed = buildFallbackIdSeed(lineNotification) ?: return null
        return LINE_FALLBACK_SHORTCUT_PREFIX + seedToUUID(fallbackSeed)
    }

    @JvmStatic
    fun buildShortcutLabel(lineNotification: LineNotification): String {
        return lineNotification.title
            ?.takeIf { it.isNotBlank() }
            ?: lineNotification.sender?.name?.toString()?.takeIf { it.isNotBlank() }
            ?: DEFAULT_LABEL
    }

    internal fun buildShortcutIcon(
        context: Context,
        lineNotification: LineNotification
    ): IconCompat? {
        lineNotification.icon?.let { return IconCompat.createWithBitmap(it) }
        val lineApplicationIcon = try {
            context.packageManager.getApplicationIcon(LINE_PACKAGE_NAME)
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.w(e, "LINE package icon not found for shortcut fallback: %s", LINE_PACKAGE_NAME)
            null
        }
        return convertDrawableToBitmap(lineApplicationIcon)?.let { IconCompat.createWithBitmap(it) }
    }

    private fun convertDrawableToBitmap(drawable: Drawable?): Bitmap? {
        drawable ?: return null
        if (drawable is BitmapDrawable) {
            drawable.bitmap?.let { return it }
        }
        val bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        } else {
            Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        }
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
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
