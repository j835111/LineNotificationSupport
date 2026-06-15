package com.mysticwind.linenotificationsupport.ui.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mysticwind.linenotificationsupport.ui.LocaleDao
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class LocaleChangeBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var localeDao: LocaleDao

    override fun onReceive(context: Context, intent: Intent) {
        localeDao.notifyLocaleChange()
        val locale = localeDao.getLocale()
        Timber.i("Locale has been changed to %s", locale)
    }
}
