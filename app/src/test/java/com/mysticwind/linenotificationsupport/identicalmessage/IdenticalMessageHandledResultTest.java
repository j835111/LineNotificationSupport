package com.mysticwind.linenotificationsupport.identicalmessage;

import static org.junit.Assert.assertEquals;

import com.mysticwind.linenotificationsupport.model.LineNotification;

import org.junit.Test;

public class IdenticalMessageHandledResultTest {

    @Test
    public void builderAndEqualityRemainStable() {
        LineNotification notification = LineNotification.builder()
                .message("message")
                .build();

        IdenticalMessageHandledResult left = IdenticalMessageHandledResult.builder()
                .notificationId(1)
                .replacedMessage("replaced")
                .lineNotification(notification)
                .build();

        IdenticalMessageHandledResult right = IdenticalMessageHandledResult.builder()
                .notificationId(1)
                .replacedMessage("replaced")
                .lineNotification(notification)
                .build();

        assertEquals(left, right);
        assertEquals(left.hashCode(), right.hashCode());
        assertEquals("replaced", left.getReplacedMessage());
    }
}
