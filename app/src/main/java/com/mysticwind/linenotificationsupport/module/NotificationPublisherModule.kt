package com.mysticwind.linenotificationsupport.module

import android.content.Context
import android.os.Build
import android.os.Handler
import com.mysticwind.linenotificationsupport.notification.SlotAvailabilityChecker
import com.mysticwind.linenotificationsupport.notification.impl.DumbNotificationCounter
import com.mysticwind.linenotificationsupport.reply.DefaultReplyActionBuilder
import com.mysticwind.linenotificationsupport.reply.ReplyActionBuilder
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationPublisherModule {

    /* Related classes using @Inject
      NotificationPublisherFactory
      SimpleNotificationPublisher
      PreferenceProvider
      NotificationIdGenerator
      DumbNotificationCounter
      GroupIdResolver
     */

    @Singleton
    @Binds
    abstract fun bindSlotAvailabilityChecker(dumbNotificationCounter: DumbNotificationCounter): SlotAvailabilityChecker

    @Singleton
    @Binds
    abstract fun bindReplyActionBuilder(defaultReplyActionBuilder: DefaultReplyActionBuilder): ReplyActionBuilder

    companion object {
        @Singleton
        @Provides
        @JvmStatic
        fun provideHandler(): Handler {
            return Handler()
        }

        @HiltQualifiers.MaxNotificationsPerApp
        @Singleton
        @Provides
        @JvmStatic
        fun getMaxNotificationsPerApp(): Int {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                25
            } else {
                50
            }
        }

        @HiltQualifiers.PackageName
        @Singleton
        @Provides
        @JvmStatic
        fun providePackageName(@ApplicationContext context: Context): String {
            return context.packageName
        }
    }
}
