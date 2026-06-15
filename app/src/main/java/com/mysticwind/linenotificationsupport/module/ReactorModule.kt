package com.mysticwind.linenotificationsupport.module

import com.google.common.collect.ImmutableList
import com.mysticwind.linenotificationsupport.bluetooth.BluetoothController
import com.mysticwind.linenotificationsupport.bluetooth.impl.AndroidBluetoothController
import com.mysticwind.linenotificationsupport.debug.DebugModeProvider
import com.mysticwind.linenotificationsupport.notification.reactor.CallInProgressTrackingReactor
import com.mysticwind.linenotificationsupport.notification.reactor.ChatRoomNamePersistenceIncomingNotificationReactor
import com.mysticwind.linenotificationsupport.notification.reactor.ConversationStarterNotificationReactor
import com.mysticwind.linenotificationsupport.notification.reactor.DismissedNotificationReactor
import com.mysticwind.linenotificationsupport.notification.reactor.DumbNotificationCounterNotificationReactor
import com.mysticwind.linenotificationsupport.notification.reactor.IncomingNotificationReactor
import com.mysticwind.linenotificationsupport.notification.reactor.LineNotificationLoggingIncomingNotificationReactor
import com.mysticwind.linenotificationsupport.notification.reactor.LineNotificationSupportLoggingIncomingNotificationReactor
import com.mysticwind.linenotificationsupport.notification.reactor.LineReplyActionPersistenceIncomingNotificationReactor
import com.mysticwind.linenotificationsupport.notification.reactor.LoggingDismissedNotificationReactor
import com.mysticwind.linenotificationsupport.notification.reactor.ManageLineNotificationIncomingNotificationReactor
import com.mysticwind.linenotificationsupport.notification.reactor.NotificationPublisherUpdateDismissReactor
import com.mysticwind.linenotificationsupport.notification.reactor.PublishedNotificationTrackerIncomingNotificationReactor
import com.mysticwind.linenotificationsupport.notification.reactor.SameLineMessageIdFilterIncomingNotificationReactor
import com.mysticwind.linenotificationsupport.notification.reactor.SummaryNotificationPublisherNotificationReactor
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ReactorModule {

    /* Related classes using @Inject
        LineNotificationLoggingIncomingNotificationReactor
        LineNotificationSupportLoggingIncomingNotificationReactor
        CallInProgressTrackingReactor
        ChatRoomNamePersistenceIncomingNotificationReactor
        LineReplyActionPersistenceIncomingNotificationReactor
        DumbNotificationCounterNotificationReactor
        SummaryNotificationPublisherNotificationReactor
        SummaryNotificationPublisher
        ManageLineNotificationIncomingNotificationReactor
        SameLineMessageIdFilterIncomingNotificationReactor
        ConversationStarterNotificationReactor
        LoggingDismissedNotificationReactor
        PublishedNotificationTrackerIncomingNotificationReactor
        NotificationPublisherUpdateDismissReactor
     */

    @Singleton
    @Binds
    abstract fun bindBluetoothController(androidBluetoothController: AndroidBluetoothController): BluetoothController

    companion object {
        @Singleton
        @Provides
        @JvmStatic
        fun incomingNotificationReactors(
            debugModeProvider: DebugModeProvider,
            lineNotificationLoggingIncomingNotificationReactor: LineNotificationLoggingIncomingNotificationReactor,
            lineNotificationSupportLoggingIncomingNotificationReactor: LineNotificationSupportLoggingIncomingNotificationReactor,
            callInProgressTrackingReactor: CallInProgressTrackingReactor,
            chatRoomNamePersistenceIncomingNotificationReactor: ChatRoomNamePersistenceIncomingNotificationReactor,
            lineReplyActionPersistenceIncomingNotificationReactor: LineReplyActionPersistenceIncomingNotificationReactor,
            dumbNotificationCounterNotificationReactor: DumbNotificationCounterNotificationReactor,
            summaryNotificationPublisherNotificationReactor: SummaryNotificationPublisherNotificationReactor,
            manageLineNotificationIncomingNotificationReactor: ManageLineNotificationIncomingNotificationReactor,
            sameLineMessageIdFilterIncomingNotificationReactor: SameLineMessageIdFilterIncomingNotificationReactor,
            conversationStarterNotificationReactor: ConversationStarterNotificationReactor,
            publishedNotificationTrackerIncomingNotificationReactor: PublishedNotificationTrackerIncomingNotificationReactor
        ): List<@JvmSuppressWildcards IncomingNotificationReactor> {
            val reactorListBuilder = ImmutableList.builder<IncomingNotificationReactor>()
            if (debugModeProvider.isDebugMode()) {
                reactorListBuilder.add(lineNotificationLoggingIncomingNotificationReactor)
                reactorListBuilder.add(lineNotificationSupportLoggingIncomingNotificationReactor)
            }
            reactorListBuilder.add(callInProgressTrackingReactor)
            reactorListBuilder.add(chatRoomNamePersistenceIncomingNotificationReactor)
            reactorListBuilder.add(lineReplyActionPersistenceIncomingNotificationReactor)
            reactorListBuilder.add(dumbNotificationCounterNotificationReactor)
            reactorListBuilder.add(summaryNotificationPublisherNotificationReactor)
            reactorListBuilder.add(manageLineNotificationIncomingNotificationReactor)
            reactorListBuilder.add(sameLineMessageIdFilterIncomingNotificationReactor)
            reactorListBuilder.add(conversationStarterNotificationReactor)
            reactorListBuilder.add(publishedNotificationTrackerIncomingNotificationReactor)
            return reactorListBuilder.build()
        }

        @Singleton
        @Provides
        @JvmStatic
        fun dismissedNotificationReactors(
            debugModeProvider: DebugModeProvider,
            loggingDismissedNotificationReactor: LoggingDismissedNotificationReactor,
            callInProgressTrackingReactor: CallInProgressTrackingReactor,
            dumbNotificationCounterNotificationReactor: DumbNotificationCounterNotificationReactor,
            summaryNotificationPublisherNotificationReactor: SummaryNotificationPublisherNotificationReactor,
            conversationStarterNotificationReactor: ConversationStarterNotificationReactor,
            notificationPublisherUpdateDismissReactor: NotificationPublisherUpdateDismissReactor
        ): List<@JvmSuppressWildcards DismissedNotificationReactor> {
            val reactorListBuilder = ImmutableList.builder<DismissedNotificationReactor>()
            if (debugModeProvider.isDebugMode()) {
                reactorListBuilder.add(loggingDismissedNotificationReactor)
            }
            reactorListBuilder.add(callInProgressTrackingReactor)
            reactorListBuilder.add(dumbNotificationCounterNotificationReactor)
            reactorListBuilder.add(summaryNotificationPublisherNotificationReactor)
            reactorListBuilder.add(conversationStarterNotificationReactor)
            reactorListBuilder.add(notificationPublisherUpdateDismissReactor)
            return reactorListBuilder.build()
        }
    }
}
