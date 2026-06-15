package com.mysticwind.linenotificationsupport

import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import net.yslibrary.historian.Historian
import net.yslibrary.historian.tree.HistorianTree
import timber.log.Timber
import java.io.File

@HiltAndroidApp
class Application : android.app.Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(object : Timber.DebugTree() {
                override fun createStackElementTag(element: StackTraceElement): String? {
                    val tag = super.createStackElementTag(element) ?: return null
                    return "LNS-$tag"
                }
            })
            val historian = Historian.builder(this)
                .directory(File(cacheDir, "historian"))
                .size(100_000)
                .logLevel(Log.DEBUG)
                .debug(true)
                .build()
            try {
                historian.initialize()
            } catch (e: Exception) {
                Timber.e(e, "Failed to initialize Historian: [%s]", e.message)
                historian.terminate()
                historian.initialize()
            }
            Timber.plant(HistorianTree.with(historian))
        }
    }
}
