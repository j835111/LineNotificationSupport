package com.mysticwind.linenotificationsupport.utils

import android.app.Notification
import android.service.notification.StatusBarNotification
import org.apache.commons.lang3.StringUtils
import timber.log.Timber

object StatusBarNotificationExtractor {

    @JvmStatic
    fun isSummary(statusBarNotification: StatusBarNotification): Boolean {
        if ((statusBarNotification.notification.flags and Notification.FLAG_GROUP_SUMMARY) > 0) {
            return true
        }

        val summaryText = statusBarNotification.notification.extras
            .getString(Notification.EXTRA_SUMMARY_TEXT)
        if (StringUtils.isNotBlank(summaryText)) {
            Timber.d("Summary notification with message [%s]: it contains summary text [%s]",
                NotificationExtractor.getMessage(statusBarNotification.notification), summaryText)
            return true
        }

        return false
    }

    @JvmStatic
    fun isMessage(statusBarNotification: StatusBarNotification): Boolean {
        return Notification.CATEGORY_MESSAGE == statusBarNotification.notification.category
    }

    @JvmStatic
    fun isCall(statusBarNotification: StatusBarNotification): Boolean {
        return Notification.CATEGORY_CALL == statusBarNotification.notification.category
    }
}
