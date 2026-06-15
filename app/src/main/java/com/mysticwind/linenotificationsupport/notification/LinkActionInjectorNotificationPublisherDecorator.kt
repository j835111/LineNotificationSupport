package com.mysticwind.linenotificationsupport.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.service.notification.StatusBarNotification
import com.mysticwind.linenotificationsupport.R
import com.mysticwind.linenotificationsupport.model.LineNotification
import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import timber.log.Timber
import java.net.MalformedURLException
import java.net.URL
import java.util.Optional

class LinkActionInjectorNotificationPublisherDecorator(
    private val notificationPublisher: NotificationPublisher,
    private val context: Context
) : NotificationPublisher {

    override fun publishNotification(lineNotification: LineNotification, notificationId: Int) {
        if (hasLinkAction(lineNotification)) {
            Timber.i(
                "Notification [%d] [%s] already has link action",
                notificationId, lineNotification.message
            )
            this.notificationPublisher.publishNotification(lineNotification, notificationId)
            return
        }
        val url = findUrl(lineNotification.message)
        if (!url.isPresent) {
            this.notificationPublisher.publishNotification(lineNotification, notificationId)
            return
        }

        asyncFetchTitleAndPublish(lineNotification, notificationId, url.get())
    }

    private fun hasLinkAction(lineNotification: LineNotification): Boolean {
        val buttonText = context.getString(R.string.link_button_text)

        return lineNotification.actions.any { action -> StringUtils.equals(action.title, buttonText) }
    }

    private fun asyncFetchTitleAndPublish(
        lineNotification: LineNotification,
        notificationId: Int,
        url: String
    ) {
        object : AsyncTask<Void, Void, String?>() {
            override fun doInBackground(vararg voids: Void): String? {
                val title = getTitle(url)
                return title.orElse(null)
            }

            override fun onPostExecute(title: String?) {
                val linkActionInjectedLineNotification = lineNotification.toBuilder()
                    .action(buildLinkAction(url))
                    .message(injectUrlTitle(lineNotification.message, url, title))
                    .build()
                notificationPublisher.publishNotification(linkActionInjectedLineNotification, notificationId)
            }
        }.execute()
    }

    private fun findUrl(message: String?): Optional<String> {
        if (StringUtils.isBlank(message)) {
            return Optional.empty()
        }
        val httpIndex = getHttpIndex(message!!)
        if (!httpIndex.isPresent) {
            return Optional.empty()
        }
        val messageStartingWithHttp = message.substring(httpIndex.get())

        // separate input by spaces ( URLs don't have spaces )
        val parts = messageStartingWithHttp.split("\\s+".toRegex())

        // Attempt to convert each item into an URL.
        for (item in parts) {
            if (isUrl(item)) {
                return Optional.of(item)
            }
        }
        return Optional.empty()
    }

    private fun getHttpIndex(message: String): Optional<Int> {
        val httpIndex = message.indexOf("http")
        return if (httpIndex < 0) {
            Optional.empty()
        } else {
            Optional.of(httpIndex)
        }
    }

    private fun isUrl(string: String): Boolean {
        return try {
            URL(string)
            true
        } catch (e: MalformedURLException) {
            false
        }
    }

    private fun buildLinkAction(url: String): Notification.Action {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val buttonText = context.getString(R.string.link_button_text)
        return Notification.Action.Builder(android.R.drawable.btn_default, buttonText, pendingIntent)
            .build()
    }

    // TODO move this to a separate decorator/reactor
    // TODO support injecting when there are multiple URLs
    private fun injectUrlTitle(message: String?, url: String, title: String?): String? {
        if (title == null) {
            return message
        }
        if (message == null) {
            return null
        }
        val endIndex = message.indexOf(url) + url.length
        val firstPart = message.substring(0, endIndex)
        val lastPart = message.substring(firstPart.length)
        return String.format("%s (%s) %s", firstPart, title, lastPart)
    }

    private fun getTitle(url: String): Optional<String> {
        return try {
            val document = Jsoup.connect(url).get()
            Optional.ofNullable(document.title())
        } catch (e: Exception) {
            Timber.e(e, "Failed to extract title from url [%s]: [%s]", url, e.message)
            Optional.empty()
        }
    }

    override fun republishNotification(lineNotification: LineNotification, notificationId: Int) {
        // do nothing as the action should have been injected previously through publishNotification()
        this.notificationPublisher.republishNotification(lineNotification, notificationId)
    }

    override fun updateNotificationDismissed(statusBarNotification: StatusBarNotification) {
        // do nothing
        this.notificationPublisher.updateNotificationDismissed(statusBarNotification)
    }
}
