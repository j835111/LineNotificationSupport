package com.mysticwind.linenotificationsupport.notification

import android.service.notification.StatusBarNotification
import com.google.common.base.CharMatcher
import com.mysticwind.linenotificationsupport.model.LineNotification
import com.mysticwind.linenotificationsupport.preference.PreferenceProvider
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.tuple.Pair
import java.net.MalformedURLException
import java.net.URL

class BigNotificationSplittingNotificationPublisherDecorator(
    private val notificationPublisher: NotificationPublisher,
    private val preferenceProvider: PreferenceProvider
) : NotificationPublisher {

    companion object {
        private const val MESSAGE_SEPARATOR = "(...)"
        private const val MESSAGE_SEPARATOR_LENGTH = MESSAGE_SEPARATOR.length
    }

    override fun publishNotification(lineNotification: LineNotification, notificationId: Int) {
        if (lineNotification.message.isNullOrBlank()) {
            notificationPublisher.publishNotification(lineNotification, notificationId)
            return
        }
        val message = lineNotification.message!!
        val messageSizeLimit = preferenceProvider.getMessageSizeLimit()
        if (message.length <= messageSizeLimit) {
            // don't do anything else
            notificationPublisher.publishNotification(lineNotification, notificationId)
            return
        }
        val splitMessages = splitMessage(message, messageSizeLimit)
        val notificationWithSplitMessages = lineNotification.toBuilder()
            .messages(splitMessages)
            .build()
        notificationPublisher.publishNotification(notificationWithSplitMessages, notificationId)
    }

    private fun splitMessage(originalMessage: String, messageSizeLimit: Int): List<String> {
        val maxPageCount = preferenceProvider.getMaxPageCount()
        val splitMessages = mutableListOf<String>()
        // For example, messageSizeLimit = 60
        // Page size: first, N, N+1
        // 60
        // 55, 55
        // 55, 50, 55
        // 55, 50, 50, 55
        var remainingMessage = removeLeadingSpaces(originalMessage)
        var pageCount = 0
        while (pageCount++ < maxPageCount) {
            remainingMessage = removeLeadingSpaces(remainingMessage)
            if (remainingMessage.length <= messageSizeLimit) {
                splitMessages.add(remainingMessage)
                break
            }
            val firstHalfMessage = findNextPage(remainingMessage, messageSizeLimit)
            if (firstHalfMessage.length >= remainingMessage.length) {
                splitMessages.add(firstHalfMessage)
                break
            } else {
                splitMessages.add(firstHalfMessage + MESSAGE_SEPARATOR)
                remainingMessage = MESSAGE_SEPARATOR + removeLeadingSpaces(remainingMessage.substring(firstHalfMessage.length))
            }
        }
        return splitMessages
    }

    private fun removeLeadingSpaces(message: String): String {
        for (characterIndex in message.indices) {
            val character = message[characterIndex]
            if (!CharMatcher.whitespace().matches(character)) {
                return message.substring(characterIndex)
            }
        }
        return ""
    }

    private fun findNextPage(message: String, messageSizeLimit: Int): String {
        val urlIndexAndUrl = findIndexAndUrl(message)
        if (urlIndexAndUrl.left >= 0 && urlIndexAndUrl.left <= messageSizeLimit) {
            // there is url within this page, handle this specially
            val endIndex = urlIndexAndUrl.left + urlIndexAndUrl.right.length
            return if (message.length == endIndex) {
                // end of message
                message.substring(0, endIndex)
            } else {
                // URL will need to end with a space in order to load correctly
                message.substring(0, endIndex) + " "
            }
        }

        // no url within this page
        var lastWhitespaceIndex = 0
        var characterIndex = 0
        while (characterIndex < message.length && characterIndex < (messageSizeLimit - MESSAGE_SEPARATOR_LENGTH)) {
            val character = message[characterIndex]
            if (CharMatcher.whitespace().matches(character)) {
                lastWhitespaceIndex = characterIndex
            }
            characterIndex++
        }
        return if (lastWhitespaceIndex > 0) {
            message.substring(0, lastWhitespaceIndex)
        } else {
            message.substring(0, characterIndex)
        }
    }

    private fun findIndexAndUrl(message: String): Pair<Int, String> {
        // separate input by spaces ( URLs don't have spaces )
        val parts = message.split("\\s+".toRegex())

        // Attempt to convert each item into an URL.
        for (item in parts) {
            if (isUrl(item)) {
                return Pair.of(message.indexOf(item), item)
            }
        }
        return Pair.of(-1, "")
    }

    // https://stackoverflow.com/questions/285619/how-to-detect-the-presence-of-url-in-a-string
    private fun isUrl(string: String): Boolean {
        return try {
            URL(string)
            true
        } catch (e: MalformedURLException) {
            false
        }
    }

    override fun republishNotification(lineNotification: LineNotification, notificationId: Int) {
        // do nothing
        notificationPublisher.republishNotification(lineNotification, notificationId)
    }

    override fun updateNotificationDismissed(statusBarNotification: StatusBarNotification) {
        // do nothing
        notificationPublisher.updateNotificationDismissed(statusBarNotification)
    }
}
