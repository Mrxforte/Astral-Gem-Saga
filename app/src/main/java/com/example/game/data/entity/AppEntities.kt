package com.example.game.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "level_progress")
data class LevelProgressEntity(
    @PrimaryKey val levelId: Int,
    val stars: Int,
    val highScore: Int,
    val movesUsed: Int,
    val completedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
    @PrimaryKey val id: Int = 1,
    val username: String = "Crystal Mage",
    val avatarEmoji: String = "🧙‍♂️",
    val coins: Int = 500,
    val starsTotal: Int = 0,
    val lives: Int = 5,
    val lastLifeRechargeTimestamp: Long = System.currentTimeMillis(),
    val highestCombo: Int = 0,
    val totalMatches: Int = 0,
    val bossesDefeated: Int = 0,
    val highestScore: Int = 0,
    val lastDailyClaimDay: Int = -1,
    val dailyStreak: Int = 0
)

@Entity(tableName = "boosters")
data class BoosterEntity(
    @PrimaryKey val boosterType: String,
    val count: Int
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val progress: Int,
    val target: Int,
    val isClaimed: Boolean = false,
    val rewardCoins: Int = 100,
    val iconEmoji: String = "🏆"
)

@Entity(tableName = "custom_levels")
data class CustomLevelEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val jsonDefinition: String,
    val createdAt: Long = System.currentTimeMillis()
)
