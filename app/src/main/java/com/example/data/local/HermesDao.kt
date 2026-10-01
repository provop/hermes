package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HermesDao {
    @Query("SELECT * FROM hermes_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<HermesMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: HermesMessageEntity): Long

    @Query("DELETE FROM hermes_messages")
    suspend fun clearMessages()

    @Query("SELECT * FROM hermes_actions ORDER BY timestamp DESC LIMIT 50")
    fun getRecentActions(): Flow<List<HermesActionLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAction(action: HermesActionLogEntity): Long

    @Query("DELETE FROM hermes_actions")
    suspend fun clearActions()

    @Query("SELECT * FROM hermes_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<HermesSettingsEntity?>

    @Query("SELECT * FROM hermes_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsOnce(): HermesSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: HermesSettingsEntity)
}
