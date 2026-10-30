package com.example.game.data.repository

import com.example.game.data.dao.GameDao
import com.example.game.data.entity.*
import com.example.game.model.BoosterType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar

class GameRepository(private val gameDao: GameDao) {

    val levelProgressFlow: Flow<List<LevelProgressEntity>> = gameDao.getAllLevelProgress()
    val playerProfileFlow: Flow<PlayerProfileEntity?> = gameDao.getPlayerProfile()
    val boostersFlow: Flow<List<BoosterEntity>> = gameDao.getAllBoosters()
    val achievementsFlow: Flow<List<AchievementEntity>> = gameDao.getAllAchievements()
    val customLevelsFlow: Flow<List<CustomLevelEntity>> = gameDao.getAllCustomLevels()

    suspend fun initializeDefaultsIfNeeded() {
        val profile = gameDao.getPlayerProfileSync()
        if (profile == null) {
            gameDao.savePlayerProfile(
                PlayerProfileEntity(
                    id = 1,
                    username = "Crystal Hero",
                    avatarEmoji = "🧙‍♂️",
                    coins = 650,
                    starsTotal = 0,
                    lives = 5,
                    lastLifeRechargeTimestamp = System.currentTimeMillis()
                )
            )

            // Seed initial booster inventory
            BoosterType.values().forEach { booster ->
                val initialCount = when (booster) {
                    BoosterType.HAMMER -> 3
                    BoosterType.SHUFFLE -> 2
                    BoosterType.EXTRA_MOVES -> 2
                    BoosterType.ROW_BLAST -> 1
                    BoosterType.COLUMN_BLAST -> 1
                    BoosterType.COLOR_BOMB -> 1
                    BoosterType.AREA_BOMB -> 2
                }
                gameDao.saveBooster(BoosterEntity(booster.name, initialCount))
            }

            // Seed 30 original achievements
            seedAchievements()
        }
    }

    private suspend fun seedAchievements() {
        val achievements = listOf(
            AchievementEntity("first_match", "Spark of Magic", "Make your very first gem match", 0, 1, false, 50, "✨"),
            AchievementEntity("first_special", "Mystic Forger", "Create a Line Blast or Mana Bomb", 0, 1, false, 75, "⚡"),
            AchievementEntity("color_bomb_creator", "Prismatic Weaver", "Create a magical Color Bomb", 0, 1, false, 100, "🌈"),
            AchievementEntity("color_bomb_combo", "Celestial Supernova", "Combine two Color Bombs together", 0, 1, false, 300, "💥"),
            AchievementEntity("three_star_first", "Perfectionist", "Earn 3 stars on any level", 0, 1, false, 100, "⭐"),
            AchievementEntity("clear_world_1", "Crystal Valley Victor", "Complete all levels in Crystal Valley", 0, 20, false, 250, "💎"),
            AchievementEntity("clear_world_2", "Forest Guardian", "Complete all levels in Mystic Forest", 0, 20, false, 300, "🌲"),
            AchievementEntity("combo_master_3", "Cascade Master", "Trigger a 3x or higher combo chain", 0, 3, false, 120, "🔥"),
            AchievementEntity("combo_master_5", "Unstoppable Force", "Trigger a 5x or higher combo chain", 0, 5, false, 250, "🌪️"),
            AchievementEntity("shatter_ice_50", "Glacier Breaker", "Destroy 50 ice blockers", 0, 50, false, 150, "❄️"),
            AchievementEntity("clear_jelly_100", "Jelly Sweeper", "Clear 100 jelly tiles", 0, 100, false, 180, "🍧"),
            AchievementEntity("boss_slayer_1", "Colossus Banisher", "Defeat your first guardian boss", 0, 1, false, 300, "🐉"),
            AchievementEntity("hammer_time", "Forge Master", "Use the Magic Hammer booster 5 times", 0, 5, false, 100, "🔨"),
            AchievementEntity("matches_100", "Apprentice Enchanter", "Complete 100 gem matches", 0, 100, false, 100, "🔮"),
            AchievementEntity("matches_500", "Archmage Matcher", "Complete 500 gem matches", 0, 500, false, 250, "🧙"),
            AchievementEntity("matches_1000", "Grand Sorcerer", "Complete 1,000 gem matches", 0, 1000, false, 500, "👑"),
            AchievementEntity("reach_level_10", "Rising Star", "Reach Level 10", 0, 10, false, 150, "🔟"),
            AchievementEntity("reach_level_25", "Seasoned Adventurer", "Reach Level 25", 0, 25, false, 250, "🌟"),
            AchievementEntity("reach_level_50", "High Wizard", "Reach Level 50", 0, 50, false, 500, "🏰"),
            AchievementEntity("stars_50", "Constellation", "Earn 50 stars total across all levels", 0, 50, false, 200, "🌠"),
            AchievementEntity("stars_100", "Galaxy Gatherer", "Earn 100 stars total across all levels", 0, 100, false, 400, "🌌"),
            AchievementEntity("coin_hoarder_1000", "Treasury Keeper", "Accumulate 1,000 total gold coins", 0, 1000, false, 200, "💰"),
            AchievementEntity("daily_streak_3", "Dedicated Pilgrim", "Claim daily login rewards 3 days in a row", 0, 3, false, 150, "📅"),
            AchievementEntity("daily_streak_7", "Weekly Devotion", "Claim 7 days of daily login rewards", 0, 7, false, 350, "🗓️"),
            AchievementEntity("custom_level_creator", "Architect of Fate", "Design and save a level in Level Editor", 0, 1, false, 150, "🛠️"),
            AchievementEntity("speedy_win", "Lightning Reflexes", "Win a level with 10+ moves remaining", 0, 1, false, 200, "⚡"),
            AchievementEntity("high_score_25k", "Score Master", "Achieve a score over 25,000 in a single level", 0, 25000, false, 250, "🎯"),
            AchievementEntity("use_all_boosters", "Arsenal Master", "Use every type of booster at least once", 0, 7, false, 300, "🎒"),
            AchievementEntity("boss_slayer_3", "Guardian Bane", "Defeat 3 different guardian bosses", 0, 3, false, 500, "⚔️"),
            AchievementEntity("perfection_10", "Triple Crown", "Earn 3 stars on 10 different levels", 0, 10, false, 300, "👑")
        )
        gameDao.insertAchievements(achievements)
    }

    suspend fun checkAndRechargeLives() {
        val profile = gameDao.getPlayerProfileSync() ?: return
        if (profile.lives >= 5) return

        val now = System.currentTimeMillis()
        val rechargeIntervalMs = 15 * 60 * 1000L // 15 minutes per life
        val elapsed = now - profile.lastLifeRechargeTimestamp
        val livesToAdd = (elapsed / rechargeIntervalMs).toInt()

        if (livesToAdd > 0) {
            val newLives = minOf(5, profile.lives + livesToAdd)
            val newTimestamp = if (newLives >= 5) now else profile.lastLifeRechargeTimestamp + (livesToAdd * rechargeIntervalMs)
            gameDao.savePlayerProfile(profile.copy(lives = newLives, lastLifeRechargeTimestamp = newTimestamp))
        }
    }

    suspend fun consumeLife(): Boolean {
        checkAndRechargeLives()
        val profile = gameDao.getPlayerProfileSync() ?: return false
        if (profile.lives <= 0) return false
        val newLives = profile.lives - 1
        val newTimestamp = if (profile.lives == 5) System.currentTimeMillis() else profile.lastLifeRechargeTimestamp
        gameDao.savePlayerProfile(profile.copy(lives = newLives, lastLifeRechargeTimestamp = newTimestamp))
        return true
    }

    suspend fun addLife(amount: Int = 1) {
        val profile = gameDao.getPlayerProfileSync() ?: return
        val newLives = minOf(5, profile.lives + amount)
        gameDao.savePlayerProfile(profile.copy(lives = newLives))
    }

    suspend fun recordLevelCompletion(
        levelId: Int,
        stars: Int,
        score: Int,
        movesUsed: Int,
        coinsEarned: Int
    ) {
        val existing = gameDao.getLevelProgress(levelId)
        val bestStars = maxOf(stars, existing?.stars ?: 0)
        val bestScore = maxOf(score, existing?.highScore ?: 0)

        gameDao.insertOrUpdateLevelProgress(
            LevelProgressEntity(
                levelId = levelId,
                stars = bestStars,
                highScore = bestScore,
                movesUsed = movesUsed
            )
        )

        // Update player profile
        val profile = gameDao.getPlayerProfileSync()
        if (profile != null) {
            val addedStars = if (existing == null) stars else maxOf(0, stars - existing.stars)
            val updatedProfile = profile.copy(
                coins = profile.coins + coinsEarned,
                starsTotal = profile.starsTotal + addedStars,
                highestScore = maxOf(profile.highestScore, score)
            )
            gameDao.savePlayerProfile(updatedProfile)
        }

        // Update achievements
        updateAchievementProgress("three_star_first", if (stars >= 3) 1 else 0)
        updateAchievementProgress("reach_level_10", levelId)
        updateAchievementProgress("reach_level_25", levelId)
        updateAchievementProgress("reach_level_50", levelId)
        updateAchievementProgress("high_score_25k", score)
    }

    suspend fun updateAchievementProgress(achievementId: String, value: Int) {
        val achievements = gameDao.getAllAchievements().firstOrNull() ?: return
        val target = achievements.firstOrNull { it.id == achievementId } ?: return
        if (target.isClaimed) return
        val newProgress = maxOf(target.progress, value)
        gameDao.updateAchievement(target.copy(progress = newProgress))
    }

    suspend fun claimAchievement(achievementId: String): Int {
        val achievements = gameDao.getAllAchievements().firstOrNull() ?: return 0
        val target = achievements.firstOrNull { it.id == achievementId } ?: return 0
        if (target.isClaimed || target.progress < target.target) return 0

        gameDao.updateAchievement(target.copy(isClaimed = true))
        val profile = gameDao.getPlayerProfileSync() ?: return target.rewardCoins
        gameDao.savePlayerProfile(profile.copy(coins = profile.coins + target.rewardCoins))
        return target.rewardCoins
    }

    suspend fun consumeBooster(booster: BoosterType): Boolean {
        val entity = gameDao.getBooster(booster.name) ?: return false
        if (entity.count <= 0) return false
        gameDao.saveBooster(entity.copy(count = entity.count - 1))
        return true
    }

    suspend fun addBooster(booster: BoosterType, count: Int = 1) {
        val entity = gameDao.getBooster(booster.name)
        val current = entity?.count ?: 0
        gameDao.saveBooster(BoosterEntity(booster.name, current + count))
    }

    suspend fun buyBoosterWithCoins(booster: BoosterType): Boolean {
        val profile = gameDao.getPlayerProfileSync() ?: return false
        if (profile.coins < booster.coinCost) return false
        gameDao.savePlayerProfile(profile.copy(coins = profile.coins - booster.coinCost))
        addBooster(booster, 1)
        return true
    }

    suspend fun updateProfile(username: String, avatarEmoji: String) {
        val profile = gameDao.getPlayerProfileSync() ?: return
        gameDao.savePlayerProfile(profile.copy(username = username, avatarEmoji = avatarEmoji))
    }

    suspend fun addCoins(amount: Int) {
        val profile = gameDao.getPlayerProfileSync() ?: return
        gameDao.savePlayerProfile(profile.copy(coins = profile.coins + amount))
    }

    suspend fun claimDailyReward(): Pair<Int, String>? {
        val profile = gameDao.getPlayerProfileSync() ?: return null
        val calendar = Calendar.getInstance()
        val currentDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)

        if (profile.lastDailyClaimDay == currentDayOfYear) {
            return null // Already claimed today
        }

        val isConsecutive = profile.lastDailyClaimDay == currentDayOfYear - 1
        val newStreak = if (isConsecutive) (profile.dailyStreak % 7) + 1 else 1

        val (coins, itemDesc) = when (newStreak) {
            1 -> 100 to "100 Gold Coins"
            2 -> 150 to "150 Coins + 1 Magic Hammer"
            3 -> 200 to "200 Coins + 1 Mana Bomb"
            4 -> 250 to "250 Coins + 1 Star Shuffle"
            5 -> 300 to "300 Coins + 2 Extra Moves"
            6 -> 400 to "400 Coins + 1 Color Bomb"
            7 -> 1000 to "1,000 Coins + Full Life Refill + Grand Chest!"
            else -> 100 to "100 Gold Coins"
        }

        if (newStreak == 2) addBooster(BoosterType.HAMMER, 1)
        if (newStreak == 3) addBooster(BoosterType.AREA_BOMB, 1)
        if (newStreak == 4) addBooster(BoosterType.SHUFFLE, 1)
        if (newStreak == 5) addBooster(BoosterType.EXTRA_MOVES, 2)
        if (newStreak == 6) addBooster(BoosterType.COLOR_BOMB, 1)
        if (newStreak == 7) {
            addLife(5)
            addBooster(BoosterType.COLOR_BOMB, 2)
            addBooster(BoosterType.HAMMER, 2)
        }

        gameDao.savePlayerProfile(
            profile.copy(
                coins = profile.coins + coins,
                lastDailyClaimDay = currentDayOfYear,
                dailyStreak = newStreak
            )
        )

        updateAchievementProgress("daily_streak_3", newStreak)
        updateAchievementProgress("daily_streak_7", newStreak)

        return coins to itemDesc
    }

    suspend fun saveCustomLevel(title: String, json: String): Long {
        val id = gameDao.insertCustomLevel(CustomLevelEntity(title = title, jsonDefinition = json))
        updateAchievementProgress("custom_level_creator", 1)
        return id
    }

    suspend fun deleteCustomLevel(customLevel: CustomLevelEntity) {
        gameDao.deleteCustomLevel(customLevel)
    }
}
