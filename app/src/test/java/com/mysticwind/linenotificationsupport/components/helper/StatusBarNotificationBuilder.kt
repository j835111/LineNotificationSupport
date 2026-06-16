package com.mysticwind.linenotificationsupport.components.helper

import android.app.Notification
import android.os.Bundle
import android.service.notification.StatusBarNotification
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class StatusBarNotificationBuilder {

    private val mockedStatusBarNotification: StatusBarNotification = mock()
    private val mockedNotification: Notification = mock()
    private val mockedExtras: Bundle = mock()

    init {
        whenever(mockedStatusBarNotification.notification).thenReturn(mockedNotification)
        mockedNotification.extras = mockedExtras
    }

    fun withAndroidText(androidText: String?): StatusBarNotificationBuilder {
        whenever(mockedExtras.getString("android.text")).thenReturn(androidText)
        return this
    }

    fun withAndroidTitle(androidTitle: String?): StatusBarNotificationBuilder {
        whenever(mockedExtras.getString("android.title")).thenReturn(androidTitle)
        return this
    }

    fun withAndroidConversationTitle(androidConversationTitle: String?): StatusBarNotificationBuilder {
        whenever(mockedExtras.getString("android.conversationTitle")).thenReturn(androidConversationTitle)
        return this
    }

    fun withHiddenConversationTitle(hiddenConversationTitle: String?): StatusBarNotificationBuilder {
        whenever(mockedExtras.getString("android.hiddenConversationTitle")).thenReturn(hiddenConversationTitle)
        return this
    }

    fun withLineChatId(lineChatId: String?): StatusBarNotificationBuilder {
        whenever(mockedExtras.getString("line.chat.id")).thenReturn(lineChatId)
        return this
    }

    fun withLineMessageId(lineMessageId: String?): StatusBarNotificationBuilder {
        whenever(mockedExtras.getString("line.message.id")).thenReturn(lineMessageId)
        return this
    }

    fun withCategory(category: String?): StatusBarNotificationBuilder {
        mockedNotification.category = category
        return this
    }

    fun withTag(tag: String?): StatusBarNotificationBuilder {
        whenever(mockedStatusBarNotification.tag).thenReturn(tag)
        return this
    }

    fun withActions(vararg actions: Notification.Action): StatusBarNotificationBuilder {
        mockedNotification.actions = actions
        return this
    }

    fun withChannelId(channelId: String?): StatusBarNotificationBuilder {
        whenever(mockedNotification.channelId).thenReturn(channelId)
        return this
    }

    fun withTickerText(tickerText: String?): StatusBarNotificationBuilder {
        mockedNotification.tickerText = tickerText
        return this
    }

    fun withWhen(timestamp: Long): StatusBarNotificationBuilder {
        mockedNotification.`when` = timestamp
        whenever(mockedStatusBarNotification.postTime).thenReturn(timestamp)
        return this
    }

    fun build(): StatusBarNotification = mockedStatusBarNotification
}
