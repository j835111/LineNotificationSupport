package com.mysticwind.linenotificationsupport.notification

interface SlotAvailabilityChecker {

    fun hasSlot(group: String): Boolean
}
