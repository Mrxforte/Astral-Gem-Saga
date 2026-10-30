package com.example.game.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.game.audio.AudioManager
import com.example.game.audio.SoundType
import com.example.game.data.repository.GameRepository
import com.example.game.engine.Match3GameEngine
import com.example.game.haptics.HapticsManager
import com.example.game.levels.generator.LevelGenerator
import com.example.game.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Random
import kotlin.math.cos
import kotlin.math.sin

class GameViewModel(
    private val repository: GameRepository,
    val audioManager: AudioManager,
    val hapticsManager: HapticsManager
) : ViewModel() {

    private val _gameState = MutableStateFlow<GameState?>(null)
    val gameState: StateFlow<GameState?> = _gameState.asStateFlow()

    private var gameEngine: Match3GameEngine? = null
    private var timerJob: Job? = null
    private val rng = Random()

    fun startLevel(levelId: Int) {
        val config = LevelGenerator.generateLevel(levelId)
        startCustomLevel(config)
    }

    fun startCustomLevel(config: LevelConfig) {
        timerJob?.cancel()
        val engine = Match3GameEngine(config)
        gameEngine = engine
        val initialBoard = engine.createInitialBoard()

        val state = GameState(
            levelConfig = config,
            board = initialBoard,
            movesLeft = config.moveLimit,
            timeLeftSec = config.timeLimitSec,
            objectives = config.objectives,
            boss = config.boss,
            status = GameStatus.PLAYING,
            levelIntroStep = 3,
            characterMood = CharacterMood.EXCITED
        )
        _gameState.value = state

        audioManager.startWorldMusic(config.worldId)

        // Start timer if time limit exists
        if (config.timeLimitSec != null) {
            timerJob = viewModelScope.launch {
                while (isActive) {
                    delay(1000)
                    _gameState.update { current ->
                        if (current == null || current.status != GameStatus.PLAYING) return@update current
                        val newTime = (current.timeLeftSec ?: 0) - 1
                        if (newTime <= 0) {
                            audioManager.playSound(SoundType.DEFEAT_CHIME)
                            hapticsManager.heavyImpact()
                            current.copy(timeLeftSec = 0, status = GameStatus.LOST, characterMood = CharacterMood.SAD)
                        } else {
                            current.copy(timeLeftSec = newTime)
                        }
                    }
                }
            }
        }
    }

    fun finishLevelIntro() {
        _gameState.update { it?.copy(levelIntroStep = null, characterMood = CharacterMood.IDLE) }
    }

    fun onCellTapped(row: Int, col: Int) {
        val current = _gameState.value ?: return
        if (current.status != GameStatus.PLAYING) return

        // Active booster targeting
        if (current.activeBooster != null) {
            applyBooster(current.activeBooster, row to col)
            return
        }

        val selected = current.selectedCell
        if (selected == null) {
            // Select piece
            audioManager.playSound(SoundType.PIECE_SELECT)
            hapticsManager.lightTap()
            _gameState.update { it?.copy(selectedCell = row to col) }
        } else {
            if (selected == (row to col)) {
                // Deselect
                _gameState.update { it?.copy(selectedCell = null) }
            } else {
                // Try swap
                executeSwap(selected, row to col)
            }
        }
    }

    fun onSwapSwiped(from: Pair<Int, Int>, to: Pair<Int, Int>) {
        val current = _gameState.value ?: return
        if (current.status != GameStatus.PLAYING) return
        executeSwap(from, to)
    }

    private fun executeSwap(from: Pair<Int, Int>, to: Pair<Int, Int>) {
        val engine = gameEngine ?: return
        val current = _gameState.value ?: return

        audioManager.playSound(SoundType.PIECE_SWAP)

        val (newBoard, result) = engine.executeSwap(current.board, from, to)

        if (!result.isValid) {
            audioManager.playSound(SoundType.INVALID_SWAP)
            _gameState.update { it?.copy(selectedCell = null) }
            return
        }

        // Valid move!
        hapticsManager.mediumImpact()
        if (result.isSpecialCombo) {
            audioManager.playSound(SoundType.SUPERNOVA)
            hapticsManager.heavyImpact()
        } else {
            audioManager.playComboSound(result.comboChain)
        }

        val pA = current.board.getOrNull(from.first)?.getOrNull(from.second)?.piece
        val pB = current.board.getOrNull(to.first)?.getOrNull(to.second)?.piece

        val isSupernova = (pA?.special == SpecialPiece.COLOR_BOMB && pB?.special == SpecialPiece.COLOR_BOMB)
        val isBomb = (pA?.special == SpecialPiece.AREA_BOMB || pB?.special == SpecialPiece.AREA_BOMB)
        val isLine = (pA?.special in listOf(SpecialPiece.LINE_HORIZONTAL, SpecialPiece.LINE_VERTICAL) ||
                      pB?.special in listOf(SpecialPiece.LINE_HORIZONTAL, SpecialPiece.LINE_VERTICAL))
        val isColorBomb = (pA?.special == SpecialPiece.COLOR_BOMB || pB?.special == SpecialPiece.COLOR_BOMB)

        // Spawn VFX particles in object pool
        com.example.game.effects.ParticleManager.spawnGemBurst(
            centerX = 100f + to.second * 40f,
            centerY = 100f + to.first * 40f,
            colorHex = pB?.type?.hexColor ?: 0xFFFFD700,
            count = if (result.isSpecialCombo) 28 else 14,
            isSpecial = result.isSpecialCombo || isBomb || isLine
        )

        val newBeams = if (isLine) {
            val isH = pA?.special == SpecialPiece.LINE_HORIZONTAL || pB?.special == SpecialPiece.LINE_HORIZONTAL
            listOf(BeamEffect(id = System.currentTimeMillis(), row = to.first, col = to.second, isHorizontal = isH, colorHex = 0xFF00E5FF))
        } else emptyList()

        val newShockwaves = if (isBomb) {
            listOf(ShockwaveEffect(id = System.currentTimeMillis(), centerRow = to.first, centerCol = to.second, colorHex = 0xFFFF5722, radiusCells = 3.2f))
        } else emptyList()

        val newSupernova = if (isSupernova) {
            com.example.game.effects.ParticleManager.spawnSupernovaBurst(200f, 200f)
            SupernovaEvent(phase = 1, posA = from, posB = to)
        } else null

        val newTethers = if (isColorBomb && !isSupernova) {
            val targetColor = if (pA?.special == SpecialPiece.COLOR_BOMB) pB?.type else pA?.type
            val list = mutableListOf<ColorTether>()
            if (targetColor != null) {
                for (r in newBoard.indices) {
                    for (c in newBoard[r].indices) {
                        if (newBoard[r][c].piece?.type == targetColor) {
                            list.add(ColorTether(System.currentTimeMillis() + r * 10 + c, to.first, to.second, r, c, targetColor.hexColor))
                        }
                    }
                }
            }
            list
        } else emptyList()

        val particles = generateMatchParticles(to.first, to.second, current.board)
        val score = current.currentScore + result.scoreEarned
        val movesRemaining = current.movesLeft - 1

        // Check boss interaction
        var updatedBoss = current.boss
        if (updatedBoss != null) {
            val damage = result.scoreEarned / 100
            val newHp = maxOf(0, updatedBoss.currentHp - damage)
            var attackCounter = updatedBoss.movesUntilAttack - 1
            if (attackCounter <= 0) {
                attackCounter = updatedBoss.attackEveryMoves
                // Boss attack: roar effect
                audioManager.playSound(SoundType.DEFEAT_CHIME)
                hapticsManager.heavyImpact()
            }
            updatedBoss = updatedBoss.copy(currentHp = newHp, movesUntilAttack = attackCounter)
        }

        // Update objectives
        val updatedObjectives = evaluateObjectives(current.objectives, newBoard, score, updatedBoss)

        // Combo feedback banner
        val comboFeedback = if (result.comboChain >= 2) {
            val (msg, color) = when {
                result.comboChain >= 6 -> "DIVINE! 6x" to 0xFFD500F9
                result.comboChain >= 5 -> "UNSTOPPABLE! 5x" to 0xFFFF1744
                result.comboChain >= 4 -> "MEGA COMBO! 4x" to 0xFFFF9100
                result.comboChain >= 3 -> "SUPER COMBO! 3x" to 0xFFFFD700
                else -> "AMAZING! 2x" to 0xFF00E676
            }
            ComboFeedback(msg, result.comboChain.toFloat(), color)
        } else null

        // Check star thresholds
        val thresholds = current.levelConfig.starThresholds
        val stars = when {
            score >= thresholds.third -> 3
            score >= thresholds.second -> 2
            score >= thresholds.first -> 1
            else -> 0
        }

        // Check Win/Loss conditions
        val areWon = updatedObjectives.all { it.isCompleted } && (updatedBoss == null || updatedBoss.currentHp <= 0)
        val areLost = !areWon && movesRemaining <= 0

        val newStatus = when {
            areWon -> GameStatus.WON
            areLost -> GameStatus.LOST
            result.boardReshuffled -> GameStatus.RESHUFFLING
            else -> GameStatus.PLAYING
        }

        val newMood = when {
            areWon -> CharacterMood.VICTORY
            areLost -> CharacterMood.SAD
            isSupernova || result.comboChain >= 4 -> CharacterMood.CELEBRATING
            result.comboChain in 2..3 || isBomb || isLine -> CharacterMood.EXCITED
            movesRemaining <= 3 -> CharacterMood.THINKING
            else -> CharacterMood.HAPPY
        }

        if (areWon) {
            audioManager.playSound(SoundType.VICTORY_FANFARE)
            hapticsManager.successPattern()
            // Record completion in repository
            viewModelScope.launch {
                val coinsAwarded = 50 + (stars * 50)
                repository.recordLevelCompletion(
                    levelId = current.levelConfig.id,
                    stars = stars,
                    score = score,
                    movesUsed = current.levelConfig.moveLimit - movesRemaining,
                    coinsEarned = coinsAwarded
                )
            }
        } else if (areLost) {
            audioManager.playSound(SoundType.DEFEAT_CHIME)
            hapticsManager.heavyImpact()
            viewModelScope.launch {
                repository.consumeLife()
            }
        }

        val shake = if (isSupernova) 1.5f else if (result.isSpecialCombo || result.comboChain >= 3 || isBomb) 0.9f else 0.2f

        val burstCells = if (result.destroyedCells.isNotEmpty()) {
            result.destroyedCells.toList()
        } else {
            listOf(to, from)
        }
        val burstEvent = MatchBurstEvent(
            id = System.currentTimeMillis(),
            cells = burstCells,
            colorHex = pB?.type?.hexColor ?: pA?.type?.hexColor ?: 0xFFFFD700,
            isSpecial = result.isSpecialCombo || isBomb || isLine,
            combo = result.comboChain
        )

        _gameState.update {
            it?.copy(
                board = newBoard,
                selectedCell = null,
                movesLeft = movesRemaining,
                currentScore = score,
                comboCount = result.comboChain,
                comboFeedback = comboFeedback,
                boss = updatedBoss,
                objectives = updatedObjectives,
                status = newStatus,
                starsEarned = stars,
                screenShakeIntensity = shake,
                particles = particles,
                characterMood = newMood,
                activeBeams = newBeams,
                activeShockwaves = newShockwaves,
                activeColorTethers = newTethers,
                supernovaEvent = newSupernova,
                matchBurstEvents = listOf(burstEvent)
            )
        }

        // Decay screen shake & temporary effects
        viewModelScope.launch {
            delay(500)
            _gameState.update {
                it?.copy(
                    screenShakeIntensity = 0f,
                    particles = emptyList(),
                    activeBeams = emptyList(),
                    activeShockwaves = emptyList(),
                    activeColorTethers = emptyList(),
                    supernovaEvent = null
                )
            }
        }
    }

    private fun evaluateObjectives(
        objectives: List<LevelObjective>,
        board: List<List<BoardCell>>,
        score: Int,
        boss: LevelBoss?
    ): List<LevelObjective> {
        // Count remaining jelly
        var remainingJelly = 0
        var remainingIce = 0
        for (r in board.indices) {
            for (c in board[r].indices) {
                if (board[r][c].hasJelly) remainingJelly++
                if (board[r][c].blocker == BlockerType.SINGLE_ICE ||
                    board[r][c].blocker == BlockerType.DOUBLE_ICE ||
                    board[r][c].blocker == BlockerType.TRIPLE_ICE
                ) {
                    remainingIce++
                }
            }
        }

        return objectives.map { obj ->
            when (obj.type) {
                ObjectiveType.REACH_SCORE -> {
                    obj.copy(currentCount = minOf(obj.targetCount, score))
                }
                ObjectiveType.CLEAR_JELLY -> {
                    val cleared = maxOf(0, obj.targetCount - remainingJelly)
                    obj.copy(currentCount = cleared)
                }
                ObjectiveType.DESTROY_ICE -> {
                    val destroyed = maxOf(0, obj.targetCount - remainingIce)
                    obj.copy(currentCount = destroyed)
                }
                ObjectiveType.DEFEAT_BOSS -> {
                    val bossDamage = if (boss != null) (boss.maxHp - boss.currentHp) else obj.targetCount
                    obj.copy(currentCount = bossDamage)
                }
                else -> {
                    // Increment progress towards target
                    val newCount = minOf(obj.targetCount, obj.currentCount + 3)
                    obj.copy(currentCount = newCount)
                }
            }
        }
    }

    fun selectBooster(booster: BoosterType) {
        val current = _gameState.value ?: return
        if (booster == BoosterType.EXTRA_MOVES) {
            // Apply immediately
            audioManager.playSound(SoundType.BOOSTER_USE)
            hapticsManager.mediumImpact()
            _gameState.update { it?.copy(movesLeft = (it.movesLeft + 5)) }
            viewModelScope.launch { repository.consumeBooster(BoosterType.EXTRA_MOVES) }
            return
        }
        if (booster == BoosterType.SHUFFLE) {
            applyBooster(BoosterType.SHUFFLE, null)
            return
        }
        _gameState.update { it?.copy(activeBooster = booster) }
    }

    fun cancelBooster() {
        _gameState.update { it?.copy(activeBooster = null) }
    }

    private fun applyBooster(booster: BoosterType, targetCell: Pair<Int, Int>?) {
        val engine = gameEngine ?: return
        val current = _gameState.value ?: return

        audioManager.playSound(SoundType.BOOSTER_USE)
        hapticsManager.heavyImpact()

        val (newBoard, scoreGained) = engine.applyBooster(current.board, booster, targetCell)
        viewModelScope.launch { repository.consumeBooster(booster) }

        val boosterBurst = MatchBurstEvent(
            id = System.currentTimeMillis(),
            cells = if (targetCell != null) listOf(targetCell) else emptyList(),
            colorHex = 0xFFFFD700,
            isSpecial = true,
            combo = 2
        )

        _gameState.update {
            it?.copy(
                board = newBoard,
                activeBooster = null,
                currentScore = it.currentScore + scoreGained,
                screenShakeIntensity = 0.8f,
                matchBurstEvents = listOf(boosterBurst)
            )
        }

        viewModelScope.launch {
            delay(400)
            _gameState.update { it?.copy(screenShakeIntensity = 0f) }
        }
    }

    private fun generateMatchParticles(row: Int, col: Int, board: List<List<BoardCell>>): List<ParticleEffect> {
        val list = mutableListOf<ParticleEffect>()
        val colors = listOf(0xFFFFD700, 0xFFFF007A, 0xFF00E5FF, 0xFF00E676, 0xFFFF9100)
        for (i in 0 until 16) {
            val angle = (rng.nextDouble() * 2.0 * Math.PI)
            val speed = 80f + rng.nextFloat() * 120f
            list.add(
                ParticleEffect(
                    id = System.currentTimeMillis() + i,
                    x = 100f + col * 40f + rng.nextFloat() * 20f,
                    y = 100f + row * 40f + rng.nextFloat() * 20f,
                    colorHex = colors[i % colors.size],
                    vx = (cos(angle) * speed).toFloat(),
                    vy = (sin(angle) * speed).toFloat(),
                    life = 1.0f,
                    size = 14f
                )
            )
        }
        return list
    }

    fun restartLevel() {
        val current = _gameState.value ?: return
        startLevel(current.levelConfig.id)
    }

    fun quitLevel() {
        timerJob?.cancel()
        _gameState.value = null
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        audioManager.stopBackgroundMusic()
    }
}
