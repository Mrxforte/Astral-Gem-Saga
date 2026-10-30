package com.example.game.data.dao

import androidx.room.*
import com.example.game.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    // Level Progress
    @Query("SELECT * FROM level_progress ORDER BY levelId ASC")
    fun getAllLevelProgress(): Flow<List<LevelProgressEntity>>

    @Query("SELECT * FROM level_progress WHERE levelId = :levelId")
    suspend fun getLevelProgress(levelId: Int): LevelProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLevelProgress(progress: LevelProgressEntity)

    // Player Profile
    @Query("SELECT * FROM player_profile WHERE id = 1")
    fun getPlayerProfile(): Flow<PlayerProfileEntity?>

    @Query("SELECT * FROM player_profile WHERE id = 1")
    suspend fun getPlayerProfileSync(): PlayerProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePlayerProfile(profile: PlayerProfileEntity)

    // Boosters
    @Query("SELECT * FROM boosters")
    fun getAllBoosters(): Flow<List<BoosterEntity>>

    @Query("SELECT * FROM boosters WHERE boosterType = :type")
    suspend fun getBooster(type: String): BoosterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveBooster(booster: BoosterEntity)

    // Achievements
    @Query("SELECT * FROM achievements")
    fun getAllAchievements(): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<AchievementEntity>)

    @Update
    suspend fun updateAchievement(achievement: AchievementEntity)

    // Custom Levels (Level Editor)
    @Query("SELECT * FROM custom_levels ORDER BY id DESC")
    fun getAllCustomLevels(): Flow<List<CustomLevelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomLevel(customLevel: CustomLevelEntity): Long

    @Delete
    suspend fun deleteCustomLevel(customLevel: CustomLevelEntity)
}
