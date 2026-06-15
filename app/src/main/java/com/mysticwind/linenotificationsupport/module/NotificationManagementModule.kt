package com.mysticwind.linenotificationsupport.module

import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import com.mysticwind.linenotificationsupport.notification.AndroidNotificationManager
import com.mysticwind.linenotificationsupport.notification.impl.DefaultAndroidNotificationManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationManagementModule {

    /* Related classes using @Inject
        NotificationGroupCreator
        AndroidFeatureProvider
     */

    @Singleton
    @Binds
    abstract fun bindAndroidNotificationManager(defaultAndroidNotificationManager: DefaultAndroidNotificationManager): AndroidNotificationManager

    companion object {
        @Singleton
        @Provides
        @JvmStatic
        fun provideNotificationManager(@ApplicationContext context: Context): NotificationManager {
            return context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        }
    }
}
