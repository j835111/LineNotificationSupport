package com.mysticwind.linenotificationsupport.utils

import android.app.Notification
import android.service.notification.StatusBarNotification
import com.google.common.base.MoreObjects
import org.apache.commons.lang3.ArrayUtils
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.builder.ToStringBuilder
import timber.log.Timber
import java.util.Arrays
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatusBarNotificationPrinter @Inject constructor() {

    fun print(message: String, statusBarNotification: StatusBarNotification) {
        val prefix = buildPrefix(message)

        Timber.i("%sNotification (%s): %s",
            prefix,
            statusBarNotification.packageName,
            stringifyNotification(statusBarNotification)
        )
    }

    private fun buildPrefix(message: String): String {
        if (StringUtils.isBlank(message)) {
            return ""
        }
        return "[$message] "
    }

    fun print(statusBarNotification: StatusBarNotification) {
        print("", statusBarNotification)
    }

    private fun stringifyNotification(statusBarNotification: StatusBarNotification): String {
        return MoreObjects.toStringHelper(statusBarNotification)
            .add("packageName", statusBarNotification.packageName)
            .add("isSummary", StatusBarNotificationExtractor.isSummary(statusBarNotification))
            .add("groupKey", statusBarNotification.groupKey)
            .add("key", statusBarNotification.key)
            .add("id", statusBarNotification.id)
            .add("tag", statusBarNotification.tag)
            .add("user", if (statusBarNotification.user == null) "N/A" else statusBarNotification.user.toString())
            .add("overrideGroupKey", statusBarNotification.overrideGroupKey)
            .add("notification", ToStringBuilder.reflectionToString(statusBarNotification.notification))
            .add("actionLabels", extractActionLabels(statusBarNotification))
            .toString()
    }

    private fun extractActionLabels(statusBarNotification: StatusBarNotification): String {
        val actions: Array<Notification.Action>? = statusBarNotification.notification.actions
        if (ArrayUtils.isEmpty(actions)) {
            return "N/A"
        }
        return Arrays.stream(actions)
            .filter { action -> action.title != null }
            .map { action -> action.title.toString() }
            .reduce { title1, title2 -> "$title1,$title2" }
            .orElse("No title")
    }

    fun printError(message: String, statusBarNotification: StatusBarNotification) {
        val prefix = buildPrefix(message)

        Timber.e("%sNotification (%s): %s",
            prefix,
            statusBarNotification.packageName,
            stringifyNotification(statusBarNotification)
        )
    }

    fun toString(statusBarNotification: StatusBarNotification): String {
        return stringifyNotification(statusBarNotification)
    }
}
