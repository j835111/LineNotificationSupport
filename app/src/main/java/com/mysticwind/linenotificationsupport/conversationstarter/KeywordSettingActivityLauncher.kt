package com.mysticwind.linenotificationsupport.conversationstarter

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.mysticwind.linenotificationsupport.conversationstarter.activity.KeywordSettingActivity
import java.util.Objects
import javax.inject.Inject
import javax.inject.Singleton

import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class KeywordSettingActivityLauncher @Inject constructor(
    @ApplicationContext private val context: Context
) {
    init {
        Objects.requireNonNull(context)
    }

    fun buildPendingIntent(): PendingIntent {
        val intent = Intent(context, KeywordSettingActivity::class.java)
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }
}
