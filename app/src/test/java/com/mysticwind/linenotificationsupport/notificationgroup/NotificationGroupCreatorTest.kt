package com.mysticwind.linenotificationsupport.notificationgroup

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import com.google.common.collect.ImmutableList
import com.mysticwind.linenotificationsupport.android.AndroidFeatureProvider
import com.mysticwind.linenotificationsupport.model.LineNotificationBuilder
import com.mysticwind.linenotificationsupport.notificationgroup.NotificationGroupCreator.Companion.CALL_CHANNEL_NAME
import com.mysticwind.linenotificationsupport.notificationgroup.NotificationGroupCreator.Companion.CALL_NOTIFICATION_GROUP_ID
import com.mysticwind.linenotificationsupport.notificationgroup.NotificationGroupCreator.Companion.MERGED_MESSAGE_CHANNEL_ID
import com.mysticwind.linenotificationsupport.notificationgroup.NotificationGroupCreator.Companion.MERGED_MESSAGE_CHANNEL_NAME
import com.mysticwind.linenotificationsupport.notificationgroup.NotificationGroupCreator.Companion.MESSAGE_NOTIFICATION_GROUP_ID
import com.mysticwind.linenotificationsupport.notificationgroup.NotificationGroupCreator.Companion.OTHERS_NOTIFICATION_GROUP_ID
import com.mysticwind.linenotificationsupport.notificationgroup.NotificationGroupCreator.Companion.SELF_RESPONSE_CHANNEL_ID
import com.mysticwind.linenotificationsupport.notificationgroup.NotificationGroupCreator.Companion.SELF_RESPONSE_CHANNEL_NAME
import com.mysticwind.linenotificationsupport.preference.PreferenceProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.mockConstruction
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.MockedConstruction
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Collections

@RunWith(MockitoJUnitRunner::class)
class NotificationGroupCreatorTest {

    companion object {
        private const val SOME_RANDOM_CHAT_ID = "chatId"
        private const val SOME_RANDOM_CHAT_TITLE = "Family Group"
        private const val ANOTHER_MESSAGE_CHAT_ID = "anotherMessageChatId"
    }

    @Mock
    private lateinit var mockedNotificationManager: NotificationManager

    @Mock
    private lateinit var mockedAndroidFeatureProvider: AndroidFeatureProvider

    @Mock
    private lateinit var mockedPreferenceProvider: PreferenceProvider

    @Mock
    private lateinit var callNotificationChannelGroup: NotificationChannelGroup

    @Mock
    private lateinit var messageNotificationChannelGroup: NotificationChannelGroup

    @Mock
    private lateinit var otherNotificationChannelGroup: NotificationChannelGroup

    private lateinit var classUnderTest: NotificationGroupCreator

    @Before
    fun setUp() {
        whenever(mockedAndroidFeatureProvider.hasNotificationChannelSupport()).thenReturn(true)
        whenever(callNotificationChannelGroup.id).thenReturn(CALL_NOTIFICATION_GROUP_ID)
        whenever(messageNotificationChannelGroup.id).thenReturn(MESSAGE_NOTIFICATION_GROUP_ID)
        whenever(otherNotificationChannelGroup.id).thenReturn(OTHERS_NOTIFICATION_GROUP_ID)

        classUnderTest = NotificationGroupCreator(mockedNotificationManager, mockedAndroidFeatureProvider, mockedPreferenceProvider)
    }

    @After
    fun tearDown() {
        verifyNoMoreInteractions(mockedNotificationManager, mockedAndroidFeatureProvider, mockedPreferenceProvider)
    }

    @Test
    fun createNotificationGroupsNoGroupsCreated() {
        whenever(mockedNotificationManager.notificationChannelGroups).thenReturn(emptyList())
        val notificationChannel1 = buildNotificationChannelWithoutGroup(LineNotificationBuilder.DEFAULT_CHAT_ID)
        val notificationChannel2 = buildNotificationChannelWithoutGroup(LineNotificationBuilder.CALL_VIRTUAL_CHAT_ID)
        val notificationChannel3 = buildNotificationChannelWithoutGroup(SOME_RANDOM_CHAT_ID)
        val notificationChannels = ImmutableList.of(notificationChannel1, notificationChannel2, notificationChannel3)
        whenever(mockedNotificationManager.notificationChannels).thenReturn(notificationChannels)

        classUnderTest.createNotificationGroups()

        verify(mockedAndroidFeatureProvider).hasNotificationChannelSupport()
        verify(mockedNotificationManager).notificationChannelGroups
        verify(mockedNotificationManager, times(3)).createNotificationChannelGroup(any(NotificationChannelGroup::class.java))
        verify(mockedNotificationManager).notificationChannels
        verify(notificationChannel1).setGroup(OTHERS_NOTIFICATION_GROUP_ID)
        verify(notificationChannel2).setGroup(CALL_NOTIFICATION_GROUP_ID)
        verify(notificationChannel3).setGroup(MESSAGE_NOTIFICATION_GROUP_ID)
        verify(mockedNotificationManager, times(3)).createNotificationChannel(any(NotificationChannel::class.java))
    }

    @Test
    fun createNotificationGroupsNoGroupsCreated_allNotificationChannelWithGroup() {
        whenever(mockedNotificationManager.notificationChannelGroups).thenReturn(emptyList())
        val notificationChannels = ImmutableList.of(
            buildNotificationChannel(CALL_NOTIFICATION_GROUP_ID),
            buildNotificationChannel(MESSAGE_NOTIFICATION_GROUP_ID),
            buildNotificationChannel(OTHERS_NOTIFICATION_GROUP_ID)
        )
        whenever(mockedNotificationManager.notificationChannels).thenReturn(notificationChannels)

        classUnderTest.createNotificationGroups()

        verify(mockedAndroidFeatureProvider).hasNotificationChannelSupport()
        verify(mockedNotificationManager).notificationChannelGroups
        verify(mockedNotificationManager, times(3)).createNotificationChannelGroup(any(NotificationChannelGroup::class.java))
        verify(mockedNotificationManager).notificationChannels
    }

    @Test
    fun createNotificationGroupsAllGroupsCreated() {
        whenever(mockedNotificationManager.notificationChannelGroups).thenReturn(
            ImmutableList.of(callNotificationChannelGroup, messageNotificationChannelGroup, otherNotificationChannelGroup)
        )
        val notificationChannel1 = buildNotificationChannelWithoutGroup(LineNotificationBuilder.DEFAULT_CHAT_ID)
        val notificationChannel2 = buildNotificationChannelWithoutGroup(LineNotificationBuilder.CALL_VIRTUAL_CHAT_ID)
        val notificationChannel3 = buildNotificationChannelWithoutGroup(SOME_RANDOM_CHAT_ID)
        val notificationChannels = ImmutableList.of(notificationChannel1, notificationChannel2, notificationChannel3)
        whenever(mockedNotificationManager.notificationChannels).thenReturn(notificationChannels)

        classUnderTest.createNotificationGroups()

        verify(mockedAndroidFeatureProvider).hasNotificationChannelSupport()
        verify(mockedNotificationManager).notificationChannelGroups
        verify(mockedNotificationManager).notificationChannels
        verify(notificationChannel1).setGroup(OTHERS_NOTIFICATION_GROUP_ID)
        verify(notificationChannel2).setGroup(CALL_NOTIFICATION_GROUP_ID)
        verify(notificationChannel3).setGroup(MESSAGE_NOTIFICATION_GROUP_ID)
        verify(mockedNotificationManager, times(3)).createNotificationChannel(any(NotificationChannel::class.java))
    }

    @Test
    fun createNotificationGroupsAllGroupsCreated_allNotificationChannelWithGroup() {
        whenever(mockedNotificationManager.notificationChannelGroups).thenReturn(
            ImmutableList.of(callNotificationChannelGroup, messageNotificationChannelGroup, otherNotificationChannelGroup)
        )
        val notificationChannels = ImmutableList.of(
            buildNotificationChannel(CALL_NOTIFICATION_GROUP_ID),
            buildNotificationChannel(MESSAGE_NOTIFICATION_GROUP_ID),
            buildNotificationChannel(OTHERS_NOTIFICATION_GROUP_ID)
        )
        whenever(mockedNotificationManager.notificationChannels).thenReturn(notificationChannels)

        classUnderTest.createNotificationGroups()

        verify(mockedAndroidFeatureProvider).hasNotificationChannelSupport()
        verify(mockedNotificationManager).notificationChannelGroups
        verify(mockedNotificationManager).notificationChannels
    }

    private fun buildNotificationChannelWithoutGroup(chatId: String): NotificationChannel {
        val notificationChannel = mock<NotificationChannel>()
        whenever(notificationChannel.id).thenReturn(chatId)
        return notificationChannel
    }

    private fun buildNotificationChannel(notificationGroupId: String): NotificationChannel {
        val notificationChannel = mock<NotificationChannel>()
        whenever(notificationChannel.group).thenReturn(notificationGroupId)
        return notificationChannel
    }

    @Test
    fun createNotificationGroupsWithoutNotificationChannelSupport() {
        whenever(mockedAndroidFeatureProvider.hasNotificationChannelSupport()).thenReturn(false)

        classUnderTest.createNotificationGroups()

        verify(mockedAndroidFeatureProvider).hasNotificationChannelSupport()
    }

    @Test
    fun createNotificationChannelUsesRealChatIdWhenMergeMessageModeIsOff() {
        whenever(mockedPreferenceProvider.shouldUseMergeMessageChatId()).thenReturn(false)
        whenever(mockedNotificationManager.notificationChannelGroups).thenReturn(Collections.emptyList())

        mockNotificationChannelConstruction().use { ignored ->
            val channelId = classUnderTest.createNotificationChannel(SOME_RANDOM_CHAT_ID, SOME_RANDOM_CHAT_TITLE)

            assertTrue(channelId.isPresent)
            assertEquals(SOME_RANDOM_CHAT_ID, channelId.get())

            val createdChannel = getOnlyConstructedNotificationChannel(ignored)
            verify(createdChannel).setDescription("Notification channel for $SOME_RANDOM_CHAT_TITLE")
            verify(createdChannel).enableVibration(false)
            verify(createdChannel).setGroup(MESSAGE_NOTIFICATION_GROUP_ID)
            verify(mockedNotificationManager).createNotificationChannel(createdChannel)
        }

        verify(mockedAndroidFeatureProvider).hasNotificationChannelSupport()
        verify(mockedPreferenceProvider).shouldUseMergeMessageChatId()
        verify(mockedPreferenceProvider).shouldManageLineMessageNotifications()
        verify(mockedNotificationManager).notificationChannelGroups
        verify(mockedNotificationManager).createNotificationChannelGroup(any(NotificationChannelGroup::class.java))
    }

    @Test
    fun createNotificationChannelUsesMergedMessageChannelIdForNormalChatsWhenMergeMessageModeIsOn() {
        whenever(mockedPreferenceProvider.shouldUseMergeMessageChatId()).thenReturn(true)
        whenever(mockedNotificationManager.notificationChannelGroups).thenReturn(Collections.emptyList())

        mockNotificationChannelConstruction().use { ignored ->
            val channelId = classUnderTest.createNotificationChannel(SOME_RANDOM_CHAT_ID, SOME_RANDOM_CHAT_TITLE)

            assertTrue(channelId.isPresent)
            assertEquals(MERGED_MESSAGE_CHANNEL_ID, channelId.get())

            val createdChannel = getOnlyConstructedNotificationChannel(ignored)
            verify(createdChannel).setDescription("Notification channel for $MERGED_MESSAGE_CHANNEL_NAME")
            verify(createdChannel).enableVibration(false)
            verify(createdChannel).setGroup(MESSAGE_NOTIFICATION_GROUP_ID)
            verify(mockedNotificationManager).createNotificationChannel(createdChannel)
        }

        verify(mockedAndroidFeatureProvider).hasNotificationChannelSupport()
        verify(mockedPreferenceProvider).shouldUseMergeMessageChatId()
        verify(mockedPreferenceProvider).shouldManageLineMessageNotifications()
        verify(mockedNotificationManager).notificationChannelGroups
        verify(mockedNotificationManager).createNotificationChannelGroup(any(NotificationChannelGroup::class.java))
    }

    @Test
    fun createNotificationChannelWithManagedMessagesEnabledUsesHighImportanceAndVibration() {
        whenever(mockedPreferenceProvider.shouldUseMergeMessageChatId()).thenReturn(false)
        whenever(mockedPreferenceProvider.shouldManageLineMessageNotifications()).thenReturn(true)
        whenever(mockedNotificationManager.notificationChannelGroups).thenReturn(Collections.emptyList())

        mockNotificationChannelConstruction().use { ignored ->
            val channelId = classUnderTest.createNotificationChannel(SOME_RANDOM_CHAT_ID, SOME_RANDOM_CHAT_TITLE)

            assertTrue(channelId.isPresent)
            assertEquals(SOME_RANDOM_CHAT_ID, channelId.get())

            val createdChannel = getOnlyConstructedNotificationChannel(ignored)
            assertEquals(NotificationManager.IMPORTANCE_HIGH, createdChannel.importance)
            verify(createdChannel).enableVibration(true)
            verify(createdChannel).setGroup(MESSAGE_NOTIFICATION_GROUP_ID)
            verify(mockedNotificationManager).createNotificationChannel(createdChannel)
        }

        verify(mockedAndroidFeatureProvider).hasNotificationChannelSupport()
        verify(mockedPreferenceProvider).shouldUseMergeMessageChatId()
        verify(mockedPreferenceProvider).shouldManageLineMessageNotifications()
        verify(mockedNotificationManager).notificationChannelGroups
        verify(mockedNotificationManager).createNotificationChannelGroup(any(NotificationChannelGroup::class.java))
    }

    @Test
    fun createNotificationChannelUsesNoTitleDefaultWhenMessageTitleBlank() {
        whenever(mockedPreferenceProvider.shouldUseMergeMessageChatId()).thenReturn(false)
        whenever(mockedNotificationManager.notificationChannelGroups).thenReturn(Collections.emptyList())

        mockNotificationChannelConstruction().use { ignored ->
            val channelId = classUnderTest.createNotificationChannel(SOME_RANDOM_CHAT_ID, "   ")

            assertTrue(channelId.isPresent)
            assertEquals(SOME_RANDOM_CHAT_ID, channelId.get())

            val createdChannel = getOnlyConstructedNotificationChannel(ignored)
            assertEquals("No title", createdChannel.name)
            verify(createdChannel).setDescription("Notification channel for No title")
            verify(createdChannel).setGroup(MESSAGE_NOTIFICATION_GROUP_ID)
            verify(mockedNotificationManager).createNotificationChannel(createdChannel)
        }

        verify(mockedAndroidFeatureProvider).hasNotificationChannelSupport()
        verify(mockedPreferenceProvider).shouldUseMergeMessageChatId()
        verify(mockedPreferenceProvider).shouldManageLineMessageNotifications()
        verify(mockedNotificationManager).notificationChannelGroups
        verify(mockedNotificationManager).createNotificationChannelGroup(any(NotificationChannelGroup::class.java))
    }

    @Test
    fun createNotificationChannelDoesNotRewriteCallOrDefaultChatIdsWhenMergeMessageModeIsOn() {
        whenever(mockedNotificationManager.notificationChannelGroups).thenReturn(Collections.emptyList())

        mockNotificationChannelConstruction().use { ignored ->
            val defaultChannelId = classUnderTest.createNotificationChannel(LineNotificationBuilder.DEFAULT_CHAT_ID, "Fallback")
            val callChannelId = classUnderTest.createNotificationChannel(LineNotificationBuilder.CALL_VIRTUAL_CHAT_ID, "Call")

            assertTrue(defaultChannelId.isPresent)
            assertEquals(LineNotificationBuilder.DEFAULT_CHAT_ID, defaultChannelId.get())
            assertTrue(callChannelId.isPresent)
            assertEquals(LineNotificationBuilder.CALL_VIRTUAL_CHAT_ID, callChannelId.get())

            val createdChannels = ignored.constructed()
            assertEquals(2, createdChannels.size)

            val defaultChannel = createdChannels[0]
            val callChannel = createdChannels[1]
            verify(defaultChannel).setDescription("Notification channel for Fallback")
            verify(defaultChannel).setGroup(OTHERS_NOTIFICATION_GROUP_ID)
            verify(callChannel).setDescription("Notification channel for $CALL_CHANNEL_NAME")
            verify(callChannel).setGroup(CALL_NOTIFICATION_GROUP_ID)
            verify(mockedNotificationManager).createNotificationChannel(defaultChannel)
            verify(mockedNotificationManager).createNotificationChannel(callChannel)
        }

        verify(mockedAndroidFeatureProvider, times(2)).hasNotificationChannelSupport()
        verify(mockedPreferenceProvider, never()).shouldUseMergeMessageChatId()
        verify(mockedPreferenceProvider, times(2)).shouldManageLineMessageNotifications()
        verify(mockedNotificationManager, times(2)).notificationChannelGroups
        verify(mockedNotificationManager, times(2)).createNotificationChannelGroup(any(NotificationChannelGroup::class.java))
    }

    @Test
    fun migrateToSingleNotificationChannelForMessagesDeletesOldMessageChannelsAndPreservesNonMessageChannels() {
        val anotherMessageChannel = buildNotificationChannelWithoutGroup(ANOTHER_MESSAGE_CHAT_ID)
        val someRandomMessageChannel = buildNotificationChannelWithoutGroup(SOME_RANDOM_CHAT_ID)
        val defaultChannel = buildNotificationChannelWithoutGroup(LineNotificationBuilder.DEFAULT_CHAT_ID)
        val callChannel = buildNotificationChannelWithoutGroup(LineNotificationBuilder.CALL_VIRTUAL_CHAT_ID)
        whenever(mockedNotificationManager.notificationChannels).thenReturn(ImmutableList.of(
            anotherMessageChannel, someRandomMessageChannel, defaultChannel, callChannel
        ))
        whenever(mockedNotificationManager.notificationChannelGroups).thenReturn(Collections.emptyList())

        mockNotificationChannelConstruction().use { ignored ->
            classUnderTest.migrateToSingleNotificationChannelForMessages()

            val createdChannel = getOnlyConstructedNotificationChannel(ignored)
            verify(createdChannel).setDescription("Notification channel for $MERGED_MESSAGE_CHANNEL_NAME")
            verify(createdChannel).enableVibration(false)
            verify(createdChannel).setGroup(MESSAGE_NOTIFICATION_GROUP_ID)
            verify(mockedNotificationManager).createNotificationChannel(createdChannel)
        }

        verify(mockedAndroidFeatureProvider).hasNotificationChannelSupport()
        verify(mockedPreferenceProvider).shouldManageLineMessageNotifications()
        verify(mockedNotificationManager).notificationChannels
        verify(mockedNotificationManager).notificationChannelGroups
        verify(mockedNotificationManager).createNotificationChannelGroup(any(NotificationChannelGroup::class.java))
        verify(mockedNotificationManager).deleteNotificationChannel(ANOTHER_MESSAGE_CHAT_ID)
        verify(mockedNotificationManager).deleteNotificationChannel(SOME_RANDOM_CHAT_ID)
        verify(mockedNotificationManager, never()).deleteNotificationChannel(MERGED_MESSAGE_CHANNEL_ID)
        verify(mockedNotificationManager, never()).deleteNotificationChannel(LineNotificationBuilder.DEFAULT_CHAT_ID)
        verify(mockedNotificationManager, never()).deleteNotificationChannel(LineNotificationBuilder.CALL_VIRTUAL_CHAT_ID)
    }

    @Test
    fun createSelfResponseNotificationChannelCreatesOthersChannelWithoutMessagePreferenceLookup() {
        whenever(mockedNotificationManager.notificationChannelGroups).thenReturn(Collections.emptyList())

        mockNotificationChannelConstruction().use { ignored ->
            val channelId = classUnderTest.createSelfResponseNotificationChannel()

            assertTrue(channelId.isPresent)
            assertEquals(SELF_RESPONSE_CHANNEL_ID, channelId.get())

            val createdChannel = getOnlyConstructedNotificationChannel(ignored)
            verify(createdChannel).setDescription("Notification channel for $SELF_RESPONSE_CHANNEL_NAME")
            verify(createdChannel).enableVibration(false)
            verify(createdChannel).setGroup(OTHERS_NOTIFICATION_GROUP_ID)
            verify(mockedNotificationManager).createNotificationChannel(createdChannel)
        }

        verify(mockedAndroidFeatureProvider).hasNotificationChannelSupport()
        verify(mockedPreferenceProvider, never()).shouldUseMergeMessageChatId()
        verify(mockedPreferenceProvider, never()).shouldManageLineMessageNotifications()
        verify(mockedNotificationManager).notificationChannelGroups
        verify(mockedNotificationManager).createNotificationChannelGroup(any(NotificationChannelGroup::class.java))
    }

    @Test
    fun migrateToMultipleNotificationChannelsForMessagesDeletesMergedMessageChannelOnly() {
        classUnderTest.migrateToMultipleNotificationChannelsForMessages()

        verify(mockedAndroidFeatureProvider).hasNotificationChannelSupport()
        verify(mockedNotificationManager).deleteNotificationChannel(MERGED_MESSAGE_CHANNEL_ID)
    }

    private fun mockNotificationChannelConstruction(): MockedConstruction<NotificationChannel> {
        return mockConstruction(NotificationChannel::class.java) { mock, context ->
            whenever(mock.id).thenReturn(context.arguments()[0] as String)
            whenever(mock.name).thenReturn(context.arguments()[1] as CharSequence)
            whenever(mock.importance).thenReturn(context.arguments()[2] as Int)
        }
    }

    private fun getOnlyConstructedNotificationChannel(mockedConstruction: MockedConstruction<NotificationChannel>): NotificationChannel {
        assertEquals(1, mockedConstruction.constructed().size)
        return mockedConstruction.constructed()[0]
    }
}
