package com.mysticwind.linenotificationsupport.chatname.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class ChatTest {

    @Test
    public void equalityUsesIdAndName() {
        Chat left = new Chat("id-1", "name");
        Chat right = new Chat("id-1", "name");
        Chat different = new Chat("id-2", "name");

        assertEquals(left, right);
        assertEquals(left.hashCode(), right.hashCode());
        assertNotEquals(left, different);
    }
}
