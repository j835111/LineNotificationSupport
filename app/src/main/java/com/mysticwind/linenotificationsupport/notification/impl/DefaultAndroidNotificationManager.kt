package com.mysticwind.linenotificationsupport.notification.impl

import android.app.NotificationManager
import android.service.notification.StatusBarNotification
import com.mysticwind.linenotificationsupport.module.HiltQualifiers
import com.mysticwind.linenotificationsupport.notification.AndroidNotificationManager
import com.mysticwind.linenotificationsupport.notification.NotificationFilterStrategy
import com.mysticwind.linenotificationsupport.utils.NotificationExtractor
import com.mysticwind.linenotificationsupport.utils.StatusBarNotificationExtractor
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.Validate
import timber.log.Timber
import java.util.Arrays
import java.util.Collections
import java.util.Optional
import java.util.function.Consumer
import java.util.function.Supplier
import java.util.stream.Collectors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultAndroidNotificationManager @Inject constructor(
    private val notificationManager: NotificationManager,
    @HiltQualifiers.PackageName private val packageName: String
) : AndroidNotificationManager {

    private var otherPackageNotificationSupplier: Supplier<List<StatusBarNotification>>? = null
    private var otherPackageNotificationCanceller: Consumer<String>? = null

    init {
        Validate.notBlank(packageName)
    }

    fun initialize(
        otherPackageNotificationSupplier: Supplier<List<StatusBarNotification>>,
        otherPackageNotificationCanceller: Consumer<String>
    ) {
        this.otherPackageNotificationSupplier = otherPackageNotificationSupplier
        this.otherPackageNotificationCanceller = otherPackageNotificationCanceller
    }

    override fun getNotificationsOfPackage(packageName: String): List<StatusBarNotification> {
        if (otherPackageNotificationSupplier == null) {
            Timber.w("Cannot fetch notifications of package [%s] - otherPackageNotificationSupplier not initialized", packageName)
            return Collections.emptyList()
        }
        return otherPackageNotificationSupplier!!.get().stream()
            .filter { notification -> notification.packageName == packageName }
            .collect(Collectors.toList())
    }

    override fun getOrderedLineNotificationSupportNotificationsOfChatId(
        chatId: String,
        notificationFilterStrategy: Int
    ): List<StatusBarNotification> {
        return Arrays.stream(notificationManager.activeNotifications)
            .filter { statusBarNotification -> StringUtils.equals(packageName, statusBarNotification.packageName) }
            .filter { statusBarNotification ->
                chatId == NotificationExtractor.getLineNotificationSupportChatId(statusBarNotification.notification)
                    .orElse(null)
            }
            .filter { statusBarNotification -> applyFilter(statusBarNotification, notificationFilterStrategy) }
            .sorted { statusBarNotification1, statusBarNotification2 ->
                (statusBarNotification1.notification.`when` - statusBarNotification2.notification.`when`).toInt()
            }
            .collect(Collectors.toList())
    }

    override fun getOrderedLineNotificationSupportNotifications(
        group: String,
        notificationFilterStrategy: Int
    ): List<StatusBarNotification> {
        return Arrays.stream(notificationManager.activeNotifications)
            .filter { statusBarNotification -> StringUtils.equals(packageName, statusBarNotification.packageName) }
            .filter { statusBarNotification -> StringUtils.equals(group, statusBarNotification.notification.group) }
            .filter { statusBarNotification -> applyFilter(statusBarNotification, notificationFilterStrategy) }
            .sorted { statusBarNotification1, statusBarNotification2 ->
                (statusBarNotification1.notification.`when` - statusBarNotification2.notification.`when`).toInt()
            }
            .collect(Collectors.toList())
    }

    private fun applyFilter(
        statusBarNotification: StatusBarNotification,
        notificationFilterStrategy: Int
    ): Boolean {
        if ((notificationFilterStrategy and NotificationFilterStrategy.EXCLUDE_SUMMARY) > 0) {
            return !StatusBarNotificationExtractor.isSummary(statusBarNotification)
        }
        return true
    }

    override fun cancelNotificationById(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }

    override fun cancelNotification(chatId: String) {
        Validate.notBlank(chatId)

        val statusBarNotification = Arrays.stream(notificationManager.activeNotifications)
            .filter { notification -> packageName == notification.packageName }
            .filter { notification ->
                chatId == NotificationExtractor.getLineNotificationSupportChatId(notification.notification)
                    .orElse(null)
            }
            .findFirst()
        statusBarNotification.ifPresent { notification ->
            Timber.d("Cancel notification with ID [%d] for chat [%s]", notification.id, chatId)
            notificationManager.cancel(notification.id)
        }
    }

    override fun cancelNotificationOfPackage(key: String) {
        Validate.notBlank(key)

        if (otherPackageNotificationCanceller == null) {
            Timber.w("Cannot cancel notification of key [%s] - otherPackageNotificationCanceller not initialized", key)
            return
        }

        otherPackageNotificationCanceller!!.accept(key)
    }

    override fun clearRemoteInputNotificationSpinner(chatId: String) {
        Validate.notBlank(chatId)

        val statusBarNotification = Arrays.stream(notificationManager.activeNotifications)
            .filter { notification -> packageName == notification.packageName }
            .filter { notification ->
                chatId == NotificationExtractor.getLineNotificationSupportChatId(notification.notification)
                    .orElse(null)
            }
            .findFirst()
        statusBarNotification.ifPresent { notification ->
            Timber.d("Clear notification spinner with ID [%d]", notification.id)
            notificationManager.notify(notification.id, notification.notification)
        }
    }
}
