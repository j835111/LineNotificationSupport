package com.mysticwind.linenotificationsupport.module

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import com.mysticwind.linenotificationsupport.reply.MyPersonLabelProvider
import com.mysticwind.linenotificationsupport.reply.impl.LocalizedMyPersonLabelProvider
import com.mysticwind.linenotificationsupport.ui.LocaleDao
import com.mysticwind.linenotificationsupport.ui.LocalizationDao
import com.mysticwind.linenotificationsupport.ui.UserAlertDao
import com.mysticwind.linenotificationsupport.ui.impl.AndroidLocaleDao
import com.mysticwind.linenotificationsupport.ui.impl.AndroidLocalizationDao
import com.mysticwind.linenotificationsupport.ui.impl.ToastUserAlertDao
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UiModule {

    /* Related classes using @Inject
      LineAppVersionProvider
     */

    @Singleton
    @Binds
    abstract fun bindMyPersonLabelProvider(localizedMyPersonLabelProvider: LocalizedMyPersonLabelProvider): MyPersonLabelProvider

    @Singleton
    @Binds
    abstract fun bindLocaleDao(androidLocaleDao: AndroidLocaleDao): LocaleDao

    @Singleton
    @Binds
    abstract fun bindUserAlertDao(toastUserAlertDao: ToastUserAlertDao): UserAlertDao

    @Singleton
    @Binds
    abstract fun bindLocalizationDao(androidLocalizationDao: AndroidLocalizationDao): LocalizationDao

    companion object {
        @Singleton
        @Provides
        @JvmStatic
        fun resources(): Resources {
            return Resources.getSystem()
        }

        @Singleton
        @Provides
        @JvmStatic
        fun providePackageManager(@ApplicationContext context: Context): PackageManager {
            return context.packageManager
        }
    }
}
