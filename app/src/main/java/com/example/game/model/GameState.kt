package com.example.game.model

enum class GameStatus {
    PLAYING,
    ANIMATING,
    RESHUFFLING,
    WON,
    LOST,
    PAUSED
}

data class ComboFeedback(
    val title: String,
    val multiplier: Float,
    val colorHex: Long,
    val id: Long = System.currentTimeMillis()
)

data class ParticleEffect(
    val id: Long,
    val x: Float,
    val y: Float,
    val colorHex: Long,
    val vx: Float,
    val vy: Float,
    val life: Float = 1.0f,
    val size: Float = 12f
)

data class ScorePopup(
    val id: Long,
    val x: Float,
    val y: Float,
    val text: String,
    val colorHex: Long = 0xFFFFD700,
    val life: Float = 1.0f
)

data class MatchBurstEvent(
    val id: Long = System.currentTimeMillis(),
    val cells: List<Pair<Int, Int>>,
    val colorHex: Long = 0xFFFFD700,
    val isSpecial: Boolean = false,
    val combo: Int = 1
)

data class GameState(
    val levelConfig: LevelConfig,
    val board: List<List<BoardCell>>,
    val movesLeft: Int,
    val timeLeftSec: Int?,
    val currentScore: Int = 0,
    val comboCount: Int = 0,
    val objectives: List<LevelObjective>,
    val status: GameStatus = GameStatus.PLAYING,
    val selectedCell: Pair<Int, Int>? = null,
    val activeBooster: BoosterType? = null,
    val comboFeedback: ComboFeedback? = null,
    val boss: LevelBoss? = null,
    val screenShakeIntensity: Float = 0f,
    val starsEarned: Int = 0,
    val particles: List<ParticleEffect> = emptyList(),
    val scorePopups: List<ScorePopup> = emptyList(),
    val lastMoveGainedScore: Int = 0,
    val isShuffling: Boolean = false,
    val characterMood: CharacterMood = CharacterMood.IDLE,
    val companionSpeechText: String? = null,
    val activeBeams: List<BeamEffect> = emptyList(),
    val activeShockwaves: List<ShockwaveEffect> = emptyList(),
    val activeColorTethers: List<ColorTether> = emptyList(),
    val supernovaEvent: SupernovaEvent? = null,
    val matchBurstEvents: List<MatchBurstEvent> = emptyList(),
    val levelIntroStep: Int? = null,
    val isAnimating: Boolean = false,
    val swappingPair: Pair<Pair<Int, Int>, Pair<Int, Int>>? = null,
    val swapProgress: Float = 0f,
    val poppingCells: Set<Pair<Int, Int>> = emptySet(),
    val popProgress: Float = 0f,
    val fallingOffsets: Map<Long, Float> = emptyMap()
) {
    val areObjectivesCompleted: Boolean
        get() = objectives.isNotEmpty() && objectives.all { it.isCompleted } && (boss == null || boss.currentHp <= 0)

    val isGameOver: Boolean
        get() = status == GameStatus.WON || status == GameStatus.LOST
}
