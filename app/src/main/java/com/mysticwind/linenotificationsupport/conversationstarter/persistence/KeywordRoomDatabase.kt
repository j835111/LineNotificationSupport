package com.mysticwind.linenotificationsupport.conversationstarter.persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mysticwind.linenotificationsupport.conversationstarter.persistence.dao.KeywordDao
import com.mysticwind.linenotificationsupport.conversationstarter.persistence.dto.KeywordEntry
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Database(entities = [KeywordEntry::class], version = 1, exportSchema = false)
abstract class KeywordRoomDatabase : RoomDatabase() {

    abstract fun keywordDao(): KeywordDao

    companion object {
        @Volatile
        private var INSTANCE: KeywordRoomDatabase? = null
        private const val NUMBER_OF_THREADS = 4

        @JvmField
        val databaseWriteExecutor: ExecutorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS)

        @JvmStatic
        fun getDatabase(context: Context): KeywordRoomDatabase {
            return INSTANCE ?: synchronized(KeywordRoomDatabase::class.java) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    KeywordRoomDatabase::class.java,
                    "keyword_database"
                )
                    .allowMainThreadQueries()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
