package com.mysticwind.linenotificationsupport;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertTrue;

import android.content.Intent;
import android.os.ParcelFileDescriptor;
import android.app.UiAutomation;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MainActivityInstrumentedTest {

    @Test
    public void tappingFabShowsSnackbarAndPostsTestNotification() throws Exception {
        grantNotificationsPermission();

        Intent intent = new Intent(
                InstrumentationRegistry.getInstrumentation().getTargetContext(),
                MainActivity.class
        );
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        InstrumentationRegistry.getInstrumentation().startActivitySync(intent);

        onView(withId(R.id.fab)).perform(click());

        onView(withText("Replace with your own action")).check(matches(isDisplayed()));

        assertTrue(waitForNotificationRecord("g:message-group", Duration.ofSeconds(5)));
        assertTrue(waitForNotificationRecord("android.conversationTitle=String (Title)", Duration.ofSeconds(5)));
        assertTrue(waitForNotificationRecord("android.text=String (Message:", Duration.ofSeconds(5)));
        assertTrue(waitForNotificationRecord("dismissalId=line-dismissal-chat-message-group", Duration.ofSeconds(5)));
        assertTrue(waitForNotificationRecord("shortcut=line-conversation-chat-message-group", Duration.ofSeconds(5)));
    }

    private void grantNotificationsPermission() throws IOException {
        executeShellCommand(
                "pm grant com.mysticwind.linenotificationsupport android.permission.POST_NOTIFICATIONS"
        );
    }

    private boolean waitForNotificationRecord(String marker, Duration timeout) throws Exception {
        Instant deadline = Instant.now().plus(timeout);
        while (Instant.now().isBefore(deadline)) {
            String notificationDump = executeShellCommand("dumpsys notification --noredact");
            if (notificationDump.contains(marker)) {
                return true;
            }
            Thread.sleep(250);
        }
        return false;
    }

    private String executeShellCommand(String command) throws IOException {
        UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
        ParcelFileDescriptor descriptor = uiAutomation.executeShellCommand(command);
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(descriptor.getFileDescriptor()),
                        StandardCharsets.UTF_8
                )
        )) {
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append('\n');
            }
            return output.toString();
        } finally {
            descriptor.close();
        }
    }
}
