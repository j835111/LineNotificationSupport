package com.mysticwind.linenotificationsupport

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.Person
import androidx.core.graphics.drawable.IconCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.mysticwind.linenotificationsupport.model.LineNotification
import com.mysticwind.linenotificationsupport.notification.NotificationPublisherFactory
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    companion object {
        private const val DEFAULT_NOTIFICATION_GROUP_KEY = "message-group"
        private const val DEFAULT_NOTIFICATION_TITLE = "Title"
        private const val DEFAULT_NOTIFICATION_MESSAGE_PREFIX = "Message: "

        const val EXTRA_TEST_NOTIFICATION_GROUP_KEY = "test_notification_group_key"
        const val EXTRA_TEST_NOTIFICATION_TITLE = "test_notification_title"
        const val EXTRA_TEST_NOTIFICATION_MESSAGE_PREFIX = "test_notification_message_prefix"
    }

    @Inject
    lateinit var notificationPublisherFactory: NotificationPublisherFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // This debug screen should be able to publish test notifications even when
        // NotificationListenerService has not rebuilt the publisher in this process yet.
        notificationPublisherFactory.initializeIfNeeded(
            getSystemService(NotificationManager::class.java)?.activeNotifications?.toList() ?: emptyList()
        )

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
            sendNotification(buildMessage(), null)
        }

        fab.setOnLongClickListener {
            sendNotification(
                "Message with big picture: " + Instant.now().toString(),
                "https://stickershop.line-scdn.net/products/0/0/9/1917/android/stickers/37789.png"
            )
            true
        }
    }

    private fun randomNumber(): Int {
        return (Math.random() * 100 % 10).toInt()
    }

    private fun buildMessage(): String {
        val prefix = intent.getStringExtra(EXTRA_TEST_NOTIFICATION_MESSAGE_PREFIX)
            ?: DEFAULT_NOTIFICATION_MESSAGE_PREFIX
        return prefix + Instant.now().toString() + " https://www.google.com/search?q=" + randomNumber()
    }

    private fun sendNotification(message: String, url: String?) {
        val groupKey = intent.getStringExtra(EXTRA_TEST_NOTIFICATION_GROUP_KEY)
            ?: DEFAULT_NOTIFICATION_GROUP_KEY
        val title = intent.getStringExtra(EXTRA_TEST_NOTIFICATION_TITLE)
            ?: DEFAULT_NOTIFICATION_TITLE
        val notificationId = (System.currentTimeMillis() / 1000).toInt()

        val sender = Person.Builder()
            .setName("sender")
            .setIcon(IconCompat.createWithResource(this, R.mipmap.ic_launcher_round))
            .build()

        val timestamp = Instant.now().toEpochMilli()
        val lineNotification = LineNotification.builder()
            .title(title)
            .message(message)
            .lineStickerUrl(url)
            .chatId(groupKey)
            .sender(sender)
            .timestamp(timestamp)
            .icon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher_round))
            .action(buildRemoteInputAction())
            .build()
        notificationPublisherFactory.get().publishNotification(lineNotification, notificationId)
    }

    private fun buildRemoteInputAction(): Notification.Action {
        val requestCode = 1
        val messageReplyIntent = Intent(this, MainActivity::class.java)
        val actionIntent = PendingIntent.getBroadcast(
            applicationContext,
            requestCode,
            messageReplyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        return Notification.Action.Builder(
            android.R.drawable.btn_default, "Reply", actionIntent
        )
            .addRemoteInput(
                RemoteInput.Builder("quick_reply")
                    .setLabel("Quick reply")
                    .build()
            )
            .build()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
