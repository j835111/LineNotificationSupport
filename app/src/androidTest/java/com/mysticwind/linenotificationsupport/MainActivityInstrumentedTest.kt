package com.mysticwind.linenotificationsupport

import android.app.UiAutomation
import android.content.Intent
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class MainActivityInstrumentedTest {

    @Test
    fun tappingFabShowsSnackbarAndPostsTestNotification() {
        grantNotificationsPermission()

        val uniqueSuffix = Instant.now().toEpochMilli().toString()
        val notificationGroupKey = "message-group-$uniqueSuffix"
        val notificationTitle = "Title-$uniqueSuffix"
        val notificationMessagePrefix = "Smoke-$uniqueSuffix "
        val intent = Intent(
            InstrumentationRegistry.getInstrumentation().targetContext,
            MainActivity::class.java
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(MainActivity.EXTRA_TEST_NOTIFICATION_GROUP_KEY, notificationGroupKey)
            putExtra(MainActivity.EXTRA_TEST_NOTIFICATION_TITLE, notificationTitle)
            putExtra(MainActivity.EXTRA_TEST_NOTIFICATION_MESSAGE_PREFIX, notificationMessagePrefix)
        }
        InstrumentationRegistry.getInstrumentation().startActivitySync(intent)

        onView(withId(R.id.fab)).perform(click())

        onView(withText("Replace with your own action")).check(matches(isDisplayed()))

        assertTrue(waitForNotificationRecord("g:$notificationGroupKey", Duration.ofSeconds(5)))
        assertTrue(waitForNotificationRecord("android.conversationTitle=String ($notificationTitle)", Duration.ofSeconds(5)))
        assertTrue(waitForNotificationRecord("android.text=String ($notificationMessagePrefix", Duration.ofSeconds(5)))
        assertTrue(waitForNotificationRecord("dismissalId=line-dismissal-chat-$notificationGroupKey", Duration.ofSeconds(5)))
        assertTrue(waitForNotificationRecord("shortcut=line-conversation-chat-$notificationGroupKey", Duration.ofSeconds(5)))
    }

    private fun grantNotificationsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            executeShellCommand("pm grant com.mysticwind.linenotificationsupport android.permission.POST_NOTIFICATIONS")
        }
    }

    private fun waitForNotificationRecord(marker: String, timeout: Duration): Boolean {
        val deadline = Instant.now().plus(timeout)
        while (Instant.now().isBefore(deadline)) {
            if (executeShellCommand("dumpsys notification --noredact").contains(marker)) {
                return true
            }
            Thread.sleep(250)
        }
        return false
    }

    private fun executeShellCommand(command: String): String {
        val uiAutomation: UiAutomation = InstrumentationRegistry.getInstrumentation().uiAutomation
        val descriptor: ParcelFileDescriptor = uiAutomation.executeShellCommand(command)
        return descriptor.use {
            BufferedReader(InputStreamReader(FileInputStream(descriptor.fileDescriptor), StandardCharsets.UTF_8))
                .readText()
        }
    }
}
