package com.example.game.model

enum class PieceType(val displayName: String, val hexColor: Long, val symbol: String) {
    RUBY("Ruby", 0xFFE53935, "◆"),           // Bright Red Diamond
    SAPPHIRE("Sapphire", 0xFF1E88E5, "▲"),   // Deep Blue Triangle
    EMERALD("Emerald", 0xFF43A047, "⬡"),     // Vibrant Green Hexagon
    TOPAZ("Topaz", 0xFFFDD835, "★"),         // Radiant Yellow Star
    AMETHYST("Amethyst", 0xFF8E24AA, "●"),   // Rich Purple Circle
    SUN_CRYSTAL("Sun Crystal", 0xFFFB8C00, "☀"), // Fiery Orange Sun
    MOON_CRYSTAL("Moon Crystal", 0xFF00ACC1, "☽") // Mystic Cyan Crescent
}

enum class SpecialPiece {
    NONE,
    LINE_HORIZONTAL, // Clears full row
    LINE_VERTICAL,   // Clears full column
    AREA_BOMB,       // Clears 3x3 blast
    CROSS_BLAST,     // Clears full row AND column
    COLOR_BOMB       // Destroys all pieces of one color
}

enum class BlockerType(val maxHp: Int, val isObstacle: Boolean) {
    NONE(0, false),
    SINGLE_ICE(1, false),     // Frosted tile, damaged by adjacent match
    DOUBLE_ICE(2, false),     // Thick ice, requires 2 hits
    TRIPLE_ICE(3, false),     // Glacier ice, requires 3 hits
    STONE(1, true),           // Solid stone, takes adjacent hit
    LOCKED_TILE(1, false),    // Iron chains around piece
    JELLY(1, false),          // Underneath piece, cleared on match
    LAYERED_JELLY(2, false),  // 2-layer jelly
    SPREADING_SLIME(1, true)  // Spreads if not damaged on turn
}

enum class ObjectiveType(val description: String) {
    REACH_SCORE("Reach Score"),
    CLEAR_JELLY("Clear All Jelly"),
    DESTROY_ICE("Shatter Ice"),
    COLLECT_PIECES("Collect Gems"),
    DESTROY_BLOCKERS("Clear Obstacles"),
    SPECIAL_PIECE_TARGET("Create Specials"),
    DEFEAT_BOSS("Defeat the Guardian")
}

data class LevelObjective(
    val type: ObjectiveType,
    val targetCount: Int,
    val currentCount: Int = 0,
    val targetPieceType: PieceType? = null,
    val targetSpecial: SpecialPiece? = null
) {
    val isCompleted: Boolean get() = currentCount >= targetCount
}

enum class BoosterType(
    val title: String,
    val description: String,
    val coinCost: Int,
    val iconSymbol: String
) {
    HAMMER("Magic Hammer", "Smash any single piece or blocker instantly", 100, "🔨"),
    SHUFFLE("Star Shuffle", "Rearrange all board gems with fresh possibilities", 75, "🔀"),
    EXTRA_MOVES("+5 Moves", "Add 5 crucial extra moves to your counter", 120, "➕"),
    ROW_BLAST("Row Wand", "Instantly wipe out an entire horizontal row", 150, "↔"),
    COLUMN_BLAST("Column Wand", "Instantly vaporize an entire vertical column", 150, "↕"),
    COLOR_BOMB("Color Orb", "Place a ready-to-detonate Color Bomb on the board", 200, "🌈"),
    AREA_BOMB("Mana Bomb", "Place an explosive 3x3 mana bomb", 175, "💣")
}

data class GamePiece(
    val id: Long,
    val type: PieceType,
    val special: SpecialPiece = SpecialPiece.NONE,
    val isMatched: Boolean = false,
    val isSelected: Boolean = false,
    val animOffsetY: Float = 0f, // For smooth falling physics
    val animOffsetX: Float = 0f, // For smooth swap physics
    val scale: Float = 1f,
    val alpha: Float = 1f
)

data class BoardCell(
    val row: Int,
    val col: Int,
    val piece: GamePiece? = null,
    val blocker: BlockerType = BlockerType.NONE,
    val blockerHp: Int = 0,
    val isLocked: Boolean = false,
    val hasJelly: Boolean = false,
    val jellyLayers: Int = 0
)

data class LevelBoss(
    val name: String,
    val title: String,
    val maxHp: Int,
    val currentHp: Int,
    val attackEveryMoves: Int = 4,
    val movesUntilAttack: Int = 4,
    val avatarEmoji: String = "🐉"
)

enum class DifficultyTier(val displayName: String, val colorHex: Long) {
    TUTORIAL("Tutorial", 0xFF4CAF50),
    EASY("Easy", 0xFF8BC34A),
    NORMAL("Normal", 0xFF2196F3),
    HARD("Hard", 0xFFFF9800),
    VERY_HARD("Very Hard", 0xFFF44336),
    EXPERT("Expert", 0xFF9C27B0),
    MASTER("Master", 0xFFE91E63)
}

data class LevelConfig(
    val id: Int,
    val worldId: Int,
    val title: String,
    val rows: Int = 8,
    val cols: Int = 8,
    val moveLimit: Int = 25,
    val timeLimitSec: Int? = null,
    val targetScore: Int = 10000,
    val starThresholds: Triple<Int, Int, Int> = Triple(4000, 8000, 12000),
    val objectives: List<LevelObjective>,
    val availablePieces: List<PieceType> = listOf(
        PieceType.RUBY,
        PieceType.SAPPHIRE,
        PieceType.EMERALD,
        PieceType.TOPAZ,
        PieceType.AMETHYST
    ),
    val initialBlockers: Map<Pair<Int, Int>, BlockerType> = emptyMap(),
    val initialJelly: Set<Pair<Int, Int>> = emptySet(),
    val boss: LevelBoss? = null,
    val difficulty: DifficultyTier = DifficultyTier.NORMAL,
    val seed: Long = 1000L + id
)
