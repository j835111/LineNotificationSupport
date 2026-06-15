package com.mysticwind.linenotificationsupport.module

import android.content.Context
import androidx.room.Room
import com.mysticwind.linenotificationsupport.debug.DebugModeProvider
import com.mysticwind.linenotificationsupport.debug.history.manager.NotificationHistoryManager
import com.mysticwind.linenotificationsupport.debug.history.manager.impl.NullNotificationHistoryManager
import com.mysticwind.linenotificationsupport.debug.history.manager.impl.RoomNotificationHistoryManager
import com.mysticwind.linenotificationsupport.persistence.AppDatabase
import com.mysticwind.linenotificationsupport.utils.StatusBarNotificationPrinter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DebugModule {

    /* Related classes using @Inject
      DebugModeProvider
     */

    companion object {
        @Singleton
        @Provides
        @JvmStatic
        fun provideNotificationHistoryManager(
            debugModeProvider: DebugModeProvider,
            @ApplicationContext context: Context,
            statusBarNotificationPrinter: StatusBarNotificationPrinter
        ): NotificationHistoryManager {
            return if (debugModeProvider.isDebugMode()) {
                val appDatabase = Room.databaseBuilder(context, AppDatabase::class.java, "database").build()
                RoomNotificationHistoryManager(appDatabase, statusBarNotificationPrinter)
            } else {
                NullNotificationHistoryManager.INSTANCE
            }
        }
    }
}
