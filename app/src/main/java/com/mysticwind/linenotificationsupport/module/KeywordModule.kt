package com.mysticwind.linenotificationsupport.module

import com.mysticwind.linenotificationsupport.conversationstarter.ChatKeywordDao
import com.mysticwind.linenotificationsupport.conversationstarter.InMemoryLineReplyActionDao
import com.mysticwind.linenotificationsupport.conversationstarter.LineReplyActionDao
import com.mysticwind.linenotificationsupport.conversationstarter.RoomChatKeywordDao
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
// TODO how to make this depend on ChatNameModule?
abstract class KeywordModule {

    /* Related classes using @Inject
      ConversationStarterNotificationManager
      ChatKeywordManager
      KeywordSettingActivityLauncher
      StartConversationActionBuilder
     */

    @Singleton
    @Binds
    abstract fun bindChatKeywordDao(roomChatKeywordDao: RoomChatKeywordDao): ChatKeywordDao

    @Singleton
    @Binds
    abstract fun bindLineReplyActionDao(inMemoryLineReplyActionDao: InMemoryLineReplyActionDao): LineReplyActionDao
}
