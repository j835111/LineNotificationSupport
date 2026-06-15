package com.mysticwind.linenotificationsupport.conversationstarter.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mysticwind.linenotificationsupport.conversationstarter.persistence.dto.KeywordEntry

@Dao
interface KeywordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entry: KeywordEntry)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(entry: KeywordEntry)

    @Query("SELECT * FROM chat_id_keywords where keyword is not ''")
    fun getAllEntries(): List<KeywordEntry>
}
