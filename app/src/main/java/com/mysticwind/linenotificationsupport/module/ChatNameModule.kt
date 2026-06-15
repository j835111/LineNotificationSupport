package com.mysticwind.linenotificationsupport.module

import android.content.Context
import androidx.room.Room
import com.mysticwind.linenotificationsupport.chatname.dataaccessor.CachingGroupChatNameDataAccessorDecorator
import com.mysticwind.linenotificationsupport.chatname.dataaccessor.CachingMultiPersonChatNameDataAccessorDecorator
import com.mysticwind.linenotificationsupport.chatname.dataaccessor.GroupChatNameDataAccessor
import com.mysticwind.linenotificationsupport.chatname.dataaccessor.MultiPersonChatNameDataAccessor
import com.mysticwind.linenotificationsupport.chatname.dataaccessor.RoomGroupChatNameDataAccessor
import com.mysticwind.linenotificationsupport.chatname.dataaccessor.RoomMultiPersonChatNameDataAccessor
import com.mysticwind.linenotificationsupport.persistence.ChatGroupDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ChatNameModule {

    /* Related classes using @Inject
      LineNotificationBuilder
      StatusBarNotificationPrinter
      ChatNameManager
      RoomGroupChatNameDataAccessor
      RoomMultiPersonChatNameDataAccessor
      ChatTitleAndSenderResolver
     */

    companion object {
        @Singleton
        @Provides
        @JvmStatic
        fun bindGroupChatNameDataAccessor(roomGroupChatNameDataAccessor: RoomGroupChatNameDataAccessor): GroupChatNameDataAccessor {
            return CachingGroupChatNameDataAccessorDecorator(roomGroupChatNameDataAccessor)
        }

        @Singleton
        @Provides
        @JvmStatic
        fun bindChatGroupDatabase(@ApplicationContext context: Context): ChatGroupDatabase {
            return Room.databaseBuilder(context, ChatGroupDatabase::class.java, "chat_group_database.db")
                .allowMainThreadQueries()
                .build()
        }

        @Singleton
        @Provides
        @JvmStatic
        fun bindMultiPersonChatNameDataAccessor(roomMultiPersonChatNameDataAccessor: RoomMultiPersonChatNameDataAccessor): MultiPersonChatNameDataAccessor {
            return CachingMultiPersonChatNameDataAccessorDecorator(roomMultiPersonChatNameDataAccessor)
        }
    }
}
