package com.example.game.ui.screens.gameplay

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.characters.AnimatedCharacterView
import com.example.game.model.*
import com.example.game.ui.components.AnimatedWorldBackground
import com.example.game.ui.components.GameBoardCanvas
import com.example.game.ui.components.LevelCompleteCinematic
import com.example.game.ui.components.LevelStartCinematic
import com.example.game.viewmodel.AppScreen
import com.example.game.viewmodel.GameViewModel
import com.example.game.viewmodel.MainViewModel

@Composable
fun GameplayScreen(
    gameViewModel: GameViewModel,
    mainViewModel: MainViewModel,
    levelId: Int,
    modifier: Modifier = Modifier
) {
    val gameState by gameViewModel.gameState.collectAsState()
    val boosters by mainViewModel.boosterInventory.collectAsState()
    var isPaused by remember { mutableStateOf(false) }

    LaunchedEffect(levelId) {
        gameViewModel.startLevel(levelId)
    }

    val state = gameState
    if (state == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color(0xFF4EC0CA)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFFFDC72B))
        }
        return
    }

    val companion = remember(state.levelConfig.worldId) {
        CompanionCatalog.getCompanionForWorld(state.levelConfig.worldId)
    }

    Scaffold(
        topBar = {
            Surface(
                color = Color.White.copy(alpha = 0.95f),
                tonalElevation = 2.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBCE7F0)),
                modifier = Modifier.fillMaxWidth().statusBarsPadding()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    IconButton(
                        onClick = { isPaused = true },
                        modifier = Modifier.testTag("gameplay_pause_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "Pause",
                            tint = Color(0xFF1E3A4B)
                        )
                    }

                    // Level Title & World info + Companion Mood Avatar
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AnimatedCharacterView(
                            character = companion,
                            mood = state.characterMood,
                            sizeDp = 44,
                            showSpeechBubble = false
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.levelConfig.title,
                                color = Color(0xFF1E3A4B),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Score: ${state.currentScore}",
                                color = Color(0xFFE65100),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Moves / Timer Indicator Bubble (Sunny Gold Bubble)
                    Surface(
                        color = Color(0xFFFDC72B),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFF57F17)),
                        modifier = Modifier.testTag("moves_left_bubble")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            if (state.timeLeftSec != null) {
                                Icon(Icons.Default.Timer, contentDescription = null, tint = Color(0xFF1E3A4B), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${state.timeLeftSec}s",
                                    color = Color(0xFF1E3A4B),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp
                                )
                            } else {
                                Text(
                                    text = "${state.movesLeft}",
                                    color = Color(0xFF1E3A4B),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "MOVES",
                                    color = Color(0xFF795548),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFE8F7FB),
        modifier = modifier.fillMaxSize().testTag("gameplay_screen")
    ) { innerPadding ->
        AnimatedWorldBackground(worldId = state.levelConfig.worldId) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Objectives Bar & Combo Banner
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    // Objectives list
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        state.objectives.forEach { obj ->
                            ObjectiveBadge(objective = obj)
                        }
                    }

                    // Combo banner animation
                    AnimatedVisibility(
                        visible = state.comboFeedback != null,
                        enter = scaleIn() + fadeIn(),
                        exit = scaleOut() + fadeOut()
                    ) {
                        state.comboFeedback?.let { feedback ->
                            Surface(
                                color = Color(feedback.colorHex),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(
                                    text = feedback.title,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Active booster indicator notice
                    if (state.activeBooster != null) {
                        Surface(
                            color = Color(0xFF6200EA),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Tap tile to use ${state.activeBooster?.title}",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Cancel",
                                    color = Color(0xFFFF8A80),
                                    fontSize = 11.sp,
                                    modifier = Modifier.clickable { gameViewModel.cancelBooster() }
                                )
                            }
                        }
                    }
                }

                // Game Board Canvas (Center)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    GameBoardCanvas(
                        gameState = state,
                        onCellTapped = { r, c -> gameViewModel.onCellTapped(r, c) },
                        onSwapSwiped = { from, to -> gameViewModel.onSwapSwiped(from, to) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                    )
                }

                // Bottom Boosters Bar (Clean white surface with pastel border)
                Surface(
                    color = Color.White.copy(alpha = 0.96f),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFBCE7F0)),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 10.dp)
                    ) {
                        BoosterActionButton(
                            booster = BoosterType.HAMMER,
                            count = boosters.firstOrNull { it.boosterType == BoosterType.HAMMER.name }?.count ?: 0,
                            isActive = state.activeBooster == BoosterType.HAMMER,
                            onClick = { gameViewModel.selectBooster(BoosterType.HAMMER) }
                        )
                        BoosterActionButton(
                            booster = BoosterType.SHUFFLE,
                            count = boosters.firstOrNull { it.boosterType == BoosterType.SHUFFLE.name }?.count ?: 0,
                            isActive = false,
                            onClick = { gameViewModel.selectBooster(BoosterType.SHUFFLE) }
                        )
                        BoosterActionButton(
                            booster = BoosterType.EXTRA_MOVES,
                            count = boosters.firstOrNull { it.boosterType == BoosterType.EXTRA_MOVES.name }?.count ?: 0,
                            isActive = false,
                            onClick = { gameViewModel.selectBooster(BoosterType.EXTRA_MOVES) }
                        )
                        BoosterActionButton(
                            booster = BoosterType.AREA_BOMB,
                            count = boosters.firstOrNull { it.boosterType == BoosterType.AREA_BOMB.name }?.count ?: 0,
                            isActive = state.activeBooster == BoosterType.AREA_BOMB,
                            onClick = { gameViewModel.selectBooster(BoosterType.AREA_BOMB) }
                        )
                        BoosterActionButton(
                            booster = BoosterType.COLOR_BOMB,
                            count = boosters.firstOrNull { it.boosterType == BoosterType.COLOR_BOMB.name }?.count ?: 0,
                            isActive = state.activeBooster == BoosterType.COLOR_BOMB,
                            onClick = { gameViewModel.selectBooster(BoosterType.COLOR_BOMB) }
                        )
                    }
                }
            }
        }

        // Cinematic Level Start ("READY... 3, 2, 1, GO!")
        if (state.levelIntroStep != null) {
            LevelStartCinematic(
                levelConfig = state.levelConfig,
                onReady = { gameViewModel.finishLevelIntro() }
            )
        }

        // Cinematic Level Complete Celebration
        if (state.status == GameStatus.WON) {
            LevelCompleteCinematic(
                score = state.currentScore,
                stars = state.starsEarned,
                coinsEarned = 50 + (state.starsEarned * 50),
                worldId = state.levelConfig.worldId,
                onNextLevel = {
                    gameViewModel.startLevel(state.levelConfig.id + 1)
                },
                onMap = {
                    gameViewModel.quitLevel()
                    mainViewModel.navigateTo(AppScreen.HOME)
                }
            )
        }

        // Loss Modal (Light Sunny Style)
        if (state.status == GameStatus.LOST) {
            AlertDialog(
                onDismissRequest = { },
                containerColor = Color.White,
                shape = RoundedCornerShape(24.dp),
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "💔", fontSize = 48.sp)
                        Text(
                            text = "OUT OF MOVES!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFEA3F33)
                        )
                    }
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Don't give up! Keep going with +5 extra moves.",
                            fontSize = 13.sp,
                            color = Color(0xFF5C798A),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = {
                                gameViewModel.selectBooster(BoosterType.EXTRA_MOVES)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00ACC1)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("+5 MOVES (Free Ad)", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { gameViewModel.restartLevel() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFDC72B)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("loss_retry_button")
                    ) {
                        Text("RETRY", color = Color(0xFF1E3A4B), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            gameViewModel.quitLevel()
                            mainViewModel.navigateTo(AppScreen.HOME)
                        }
                    ) {
                        Text("QUIT", color = Color(0xFF5C798A))
                    }
                }
            )
        }

        // Pause Modal (Clean Light Card)
        if (isPaused) {
            AlertDialog(
                onDismissRequest = { isPaused = false },
                containerColor = Color.White,
                shape = RoundedCornerShape(24.dp),
                title = {
                    Text("Game Paused", color = Color(0xFF1E3A4B), fontWeight = FontWeight.Bold)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Sound & Music", color = Color(0xFF1E3A4B), fontWeight = FontWeight.Medium)
                            Switch(
                                checked = !mainViewModel.audioManager.isMuted,
                                onCheckedChange = { mainViewModel.toggleAudio() }
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Vibrations", color = Color(0xFF1E3A4B), fontWeight = FontWeight.Medium)
                            Switch(
                                checked = mainViewModel.hapticsManager.isHapticsEnabled,
                                onCheckedChange = { mainViewModel.toggleHaptics() }
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { isPaused = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFDC72B))
                    ) {
                        Text("RESUME", color = Color(0xFF1E3A4B), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            isPaused = false
                            gameViewModel.quitLevel()
                            mainViewModel.navigateTo(AppScreen.HOME)
                        }
                    ) {
                        Text("QUIT LEVEL", color = Color(0xFFE53935))
                    }
                }
            )
        }
    }
}

@Composable
private fun ObjectiveBadge(objective: LevelObjective) {
    Surface(
        color = if (objective.isCompleted) Color(0xFFE8F5E9) else Color.White,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.2.dp,
            if (objective.isCompleted) Color(0xFF70C635) else Color(0xFFBCE7F0)
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            val iconText = when (objective.type) {
                ObjectiveType.REACH_SCORE -> "🎯"
                ObjectiveType.CLEAR_JELLY -> "🍧"
                ObjectiveType.DESTROY_ICE -> "❄️"
                ObjectiveType.COLLECT_PIECES -> objective.targetPieceType?.symbol ?: "💎"
                ObjectiveType.DESTROY_BLOCKERS -> "🪨"
                ObjectiveType.SPECIAL_PIECE_TARGET -> "⚡"
                ObjectiveType.DEFEAT_BOSS -> "🐉"
            }
            Text(text = iconText, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${objective.currentCount}/${objective.targetCount}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (objective.isCompleted) Color(0xFF2E7D32) else Color(0xFF1E3A4B)
            )
            if (objective.isCompleted) {
                Spacer(modifier = Modifier.width(2.dp))
                Icon(Icons.Default.Check, contentDescription = "Done", tint = Color(0xFF2E7D32), modifier = Modifier.size(12.dp))
            }
        }
    }
}

@Composable
private fun BoosterActionButton(
    booster: BoosterType,
    count: Int,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(4.dp)
            .testTag("booster_${booster.name.lowercase()}")
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(46.dp)
                    .background(
                        if (isActive) Color(0xFFFDC72B) else Color(0xFFF2FBFE),
                        CircleShape
                    )
                    .border(
                        1.5.dp,
                        if (isActive) Color(0xFFF57F17) else Color(0xFFBCE7F0),
                        CircleShape
                    )
            ) {
                Text(text = booster.iconSymbol, fontSize = 20.sp)
            }

            // Inventory Count Badge
            Surface(
                color = if (count > 0) Color(0xFF70C635) else Color(0xFFEA3F33),
                shape = CircleShape,
                modifier = Modifier.size(18.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (count > 0) "$count" else "+",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = booster.title.split(" ").first(),
            color = Color(0xFF1E3A4B),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
