package com.xianxia.qunxia.data.db

import androidx.room.*

@Dao
interface GameDao {
    // === 世界存档 ===
    @Query("SELECT * FROM world_save WHERE id = 1")
    suspend fun getWorldSave(): WorldSaveEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveWorld(world: WorldSaveEntity)

    @Query("DELETE FROM world_save")
    suspend fun deleteWorldSave()

    // === Token 日志 ===
    @Query("SELECT * FROM token_log ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentTokenLogs(limit: Int = 100): List<TokenLogEntity>

    @Insert
    suspend fun insertTokenLog(log: TokenLogEntity)

    @Query("SELECT SUM(totalTokens) FROM token_log WHERE timestamp >= :since")
    suspend fun getTokenTotalSince(since: Long): Long?

    @Query("SELECT COUNT(*) FROM token_log")
    suspend fun getTokenLogCount(): Int

    // === 设置 ===
    @Query("SELECT * FROM app_settings WHERE id = 1")
    suspend fun getSettings(): AppSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: AppSettingsEntity)
}
