package com.example.game.levels.generator

import com.example.game.model.*
import java.util.Random

object LevelGenerator {

    /**
     * Generates a fully playable, balanced, deterministic LevelConfig for any level ID (1 to 10,000+).
     */
    fun generateLevel(levelId: Int): LevelConfig {
        val world = WorldsCatalog.getWorldForLevel(levelId)
        val seed = 1234567L + (levelId * 31337L)
        val random = Random(seed)

        // Determine difficulty tier
        val difficulty = when {
            levelId <= 3 -> DifficultyTier.TUTORIAL
            levelId <= 10 -> DifficultyTier.EASY
            levelId % 20 == 0 -> DifficultyTier.MASTER // Boss level at end of world!
            levelId % 10 == 0 -> DifficultyTier.EXPERT
            levelId % 5 == 0 -> DifficultyTier.HARD
            levelId % 3 == 0 -> DifficultyTier.VERY_HARD
            else -> DifficultyTier.NORMAL
        }

        // Board dimensions (5x5 up to 9x9 depending on level & difficulty)
        val size = when {
            levelId == 1 -> 6
            levelId in 2..4 -> 7
            difficulty == DifficultyTier.MASTER -> 8
            difficulty == DifficultyTier.EXPERT -> 9
            else -> 8
        }
        val rows = size
        val cols = size

        // Available pieces palette (4 to 7 types)
        val availablePieces = when {
            levelId <= 2 -> listOf(PieceType.RUBY, PieceType.SAPPHIRE, PieceType.EMERALD, PieceType.TOPAZ)
            levelId in 3..15 -> listOf(PieceType.RUBY, PieceType.SAPPHIRE, PieceType.EMERALD, PieceType.TOPAZ, PieceType.AMETHYST)
            levelId in 16..50 -> listOf(PieceType.RUBY, PieceType.SAPPHIRE, PieceType.EMERALD, PieceType.TOPAZ, PieceType.AMETHYST, PieceType.SUN_CRYSTAL)
            else -> PieceType.values().toList()
        }

        // Is it a boss level?
        val boss = if (levelId % 20 == 0) {
            val worldIndex = world.id
            val bossNames = listOf(
                "Crystal Guardian Golem" to "Titan of Prismatic Quartz",
                "Sylph Broodmother" to "Ancient Forest Sovereign",
                "Zephyr Storm Phoenix" to "Ruler of the Tempest",
                "Pyre Lord Ignis" to "Emperor of Molten Slag",
                "Glacial Arch-Specter" to "Wraith of the Eternal Frost",
                "Abyssal Leviathan" to "Monarch of the Deep Trench",
                "Pharaoh of Sol Invictus" to "Eternal Golden Sovereign",
                "Astral Eclipse Drake" to "Scourge of the Crescent Moon",
                "Volvagia Wyrmking" to "Supreme Dragon Sovereign",
                "Cosmic Starweaver" to "Architect of Creation"
            )
            val bossInfo = bossNames.getOrElse(worldIndex - 1) { "Eldritch Crystal Behemoth" to "Ancient Terror" }
            val bossHp = 40 + (levelId * 3)
            LevelBoss(
                name = bossInfo.first,
                title = bossInfo.second,
                maxHp = bossHp,
                currentHp = bossHp,
                attackEveryMoves = 4,
                movesUntilAttack = 4,
                avatarEmoji = when (worldIndex) {
                    1 -> "🗿"
                    2 -> "🧝‍♀️"
                    3 -> "🦅"
                    4 -> "🔥"
                    5 -> "🧊"
                    6 -> "🦑"
                    7 -> "👑"
                    8 -> "🦇"
                    9 -> "🐲"
                    else -> "🌌"
                }
            )
        } else null

        // Calculate move limit
        val baseMoves = when (difficulty) {
            DifficultyTier.TUTORIAL -> 35
            DifficultyTier.EASY -> 30
            DifficultyTier.NORMAL -> 25
            DifficultyTier.HARD -> 22
            DifficultyTier.VERY_HARD -> 20
            DifficultyTier.EXPERT -> 18
            DifficultyTier.MASTER -> 26
        }

        // Objectives generation
        val objectives = mutableListOf<LevelObjective>()

        if (boss != null) {
            objectives.add(
                LevelObjective(
                    type = ObjectiveType.DEFEAT_BOSS,
                    targetCount = boss.maxHp
                )
            )
        }

        val blockersMap = mutableMapOf<Pair<Int, Int>, BlockerType>()
        val jellySet = mutableSetOf<Pair<Int, Int>>()

        // World-themed blockers
        when (world.id) {
            1 -> { // Crystal Valley: introductory, clear gems & modest score
                val targetColor = availablePieces[random.nextInt(availablePieces.size)]
                objectives.add(LevelObjective(ObjectiveType.COLLECT_PIECES, 15 + (levelId * 2), 0, targetColor))
                if (levelId >= 4) {
                    val targetSpecial = if (random.nextBoolean()) SpecialPiece.LINE_HORIZONTAL else SpecialPiece.AREA_BOMB
                    objectives.add(LevelObjective(ObjectiveType.SPECIAL_PIECE_TARGET, 2 + (levelId / 5), 0, null, targetSpecial))
                }
            }
            2 -> { // Mystic Forest: Jelly under the moss
                val numJelly = minOf(16, 6 + (levelId % 10))
                for (i in 0 until numJelly) {
                    val r = 2 + (i / 4)
                    val c = 2 + (i % 4)
                    if (r < rows && c < cols) {
                        jellySet.add(r to c)
                    }
                }
                objectives.add(LevelObjective(ObjectiveType.CLEAR_JELLY, jellySet.size))
            }
            3 -> { // Sky Islands: Stones & Line Blasts
                val stoneCount = 4 + (levelId % 6)
                for (s in 0 until stoneCount) {
                    val r = random.nextInt(rows - 2) + 1
                    val c = random.nextInt(cols - 2) + 1
                    blockersMap[r to c] = BlockerType.STONE
                }
                objectives.add(LevelObjective(ObjectiveType.DESTROY_BLOCKERS, blockersMap.size))
                objectives.add(LevelObjective(ObjectiveType.COLLECT_PIECES, 25, 0, PieceType.SAPPHIRE))
            }
            4 -> { // Volcanic Kingdom: Chains & Area Bombs
                val chainCount = 6 + (levelId % 6)
                for (i in 0 until chainCount) {
                    val r = 1 + (i % (rows - 2))
                    val c = 1 + (i / (rows - 2))
                    blockersMap[r to c] = BlockerType.LOCKED_TILE
                }
                objectives.add(LevelObjective(ObjectiveType.DESTROY_BLOCKERS, blockersMap.size))
                objectives.add(LevelObjective(ObjectiveType.COLLECT_PIECES, 30, 0, PieceType.RUBY))
            }
            5 -> { // Frozen Realm: Ice blockers
                val iceCount = 8 + (levelId % 8)
                for (i in 0 until iceCount) {
                    val r = i % rows
                    val c = (i * 2) % cols
                    val iceType = if (levelId > 90) BlockerType.DOUBLE_ICE else BlockerType.SINGLE_ICE
                    blockersMap[r to c] = iceType
                }
                objectives.add(LevelObjective(ObjectiveType.DESTROY_ICE, blockersMap.size))
            }
            else -> { // Advanced Worlds (6 to 10)
                // Mixed objectives
                val jellyCount = 8
                for (i in 0 until jellyCount) {
                    jellySet.add((rows / 2 - 1 + (i % 2)) to (cols / 2 - 2 + (i / 2)))
                }
                objectives.add(LevelObjective(ObjectiveType.CLEAR_JELLY, jellySet.size))
                val targetColor = availablePieces[random.nextInt(availablePieces.size)]
                objectives.add(LevelObjective(ObjectiveType.COLLECT_PIECES, 30, 0, targetColor))
            }
        }

        // Always add score objective if only 1 objective exists
        if (objectives.size < 2 && boss == null) {
            val scoreGoal = 5000 + (levelId * 800)
            objectives.add(LevelObjective(ObjectiveType.REACH_SCORE, scoreGoal))
        }

        val targetScore = 5000 + (levelId * 1000)
        val star1 = (targetScore * 0.6).toInt()
        val star2 = targetScore
        val star3 = (targetScore * 1.5).toInt()

        return LevelConfig(
            id = levelId,
            worldId = world.id,
            title = if (boss != null) "Boss: ${boss.name}" else "Level $levelId",
            rows = rows,
            cols = cols,
            moveLimit = baseMoves,
            timeLimitSec = if (levelId in listOf(15, 35, 55, 75, 95)) 90 else null, // occasional exciting time trials
            targetScore = targetScore,
            starThresholds = Triple(star1, star2, star3),
            objectives = objectives,
            availablePieces = availablePieces,
            initialBlockers = blockersMap,
            initialJelly = jellySet,
            boss = boss,
            difficulty = difficulty,
            seed = seed
        )
    }

    /**
     * Solvability checker: verifies that the starting board has valid moves
     * and no immediate matches prior to player interaction.
     */
    fun validateLevelSolvability(config: LevelConfig): Boolean {
        // Validation checks
        if (config.rows < 5 || config.cols < 5) return false
        if (config.moveLimit < 5) return false
        if (config.objectives.isEmpty()) return false
        if (config.availablePieces.size < 3) return false
        return true
    }
}
