package com.mysticwind.linenotificationsupport.notification

import com.mysticwind.linenotificationsupport.model.LineNotification
import java.nio.charset.StandardCharsets
import java.util.UUID

internal fun buildFallbackIdSeed(lineNotification: LineNotification): String? {
    val title = lineNotification.title?.trim().orEmpty()
    val sender = lineNotification.sender?.name?.toString()?.trim().orEmpty()
    val seed = listOf(title, sender).filter { it.isNotBlank() }.joinToString("|")
    return seed.takeIf { it.isNotBlank() }
}

internal fun seedToUUID(seed: String): UUID =
    UUID.nameUUIDFromBytes(seed.toByteArray(StandardCharsets.UTF_8))
