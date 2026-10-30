package com.example.game.ui.screens.boss

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.model.GameStatus
import com.example.game.ui.components.GameBoardCanvas
import com.example.game.viewmodel.AppScreen
import com.example.game.viewmodel.GameViewModel
import com.example.game.viewmodel.MainViewModel

@Composable
fun BossBattleScreen(
    gameViewModel: GameViewModel,
    mainViewModel: MainViewModel,
    bossLevelId: Int = 20,
    modifier: Modifier = Modifier
) {
    val gameState by gameViewModel.gameState.collectAsState()

    LaunchedEffect(bossLevelId) {
        gameViewModel.startLevel(bossLevelId)
    }

    val state = gameState
    val boss = state?.boss

    val infiniteTransition = rememberInfiniteTransition(label = "boss_breath")
    val bossPulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "boss_scale"
    )

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
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    IconButton(
                        onClick = {
                            gameViewModel.quitLevel()
                            mainViewModel.navigateTo(AppScreen.HOME)
                        },
                        modifier = Modifier.testTag("boss_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1E3A4B)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "BOSS RAID: ${boss?.name ?: "Guardian Titan"}",
                            color = Color(0xFFE65100),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = boss?.title ?: "Ancient Titan",
                            color = Color(0xFF5C798A),
                            fontSize = 11.sp
                        )
                    }
                    // Moves badge
                    Surface(
                        color = Color(0xFFFDC72B),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF57F17))
                    ) {
                        Text(
                            text = "${state?.movesLeft ?: 0} MOVES",
                            color = Color(0xFF1E3A4B),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFF4EC0CA), // Flappy Sky Cyan
        modifier = modifier.fillMaxSize().testTag("boss_battle_screen")
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 8.dp)
        ) {
            // Boss Arena Stage (Light Card)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFBCE7F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp)
                ) {
                    // Boss Avatar
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(64.dp)
                            .scale(bossPulse)
                            .background(Color(0xFFFFEBEE), CircleShape)
                            .border(2.dp, Color(0xFFFF8A80), CircleShape)
                    ) {
                        Text(text = boss?.avatarEmoji ?: "🐉", fontSize = 34.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Boss Health Bar & Attack countdown
                    Column(modifier = Modifier.weight(1f)) {
                        val currentHp = boss?.currentHp ?: 1
                        val maxHp = boss?.maxHp ?: 100
                        val hpPercent = (currentHp.toFloat() / maxHp.toFloat()).coerceIn(0f, 1f)

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "BOSS HP", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text(text = "$currentHp / $maxHp", color = Color(0xFF1E3A4B), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        LinearProgressIndicator(
                            progress = { hpPercent },
                            color = Color(0xFFFF5252),
                            trackColor = Color(0xFFFFEBEE),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "⚔️ Titan Attacks in: ${boss?.movesUntilAttack ?: 4} moves",
                            color = Color(0xFFE65100),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Board Canvas
            if (state != null) {
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
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Win Dialog
        if (state?.status == GameStatus.WON) {
            AlertDialog(
                onDismissRequest = { },
                containerColor = Color.White,
                shape = RoundedCornerShape(20.dp),
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(text = "🏆", fontSize = 48.sp)
                        Text(text = "TITAN SLAIN!", color = Color(0xFF1E3A4B), fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                    }
                },
                text = {
                    Text(
                        text = "You shattered the guardian's core! 500 Gold Coins awarded and the path to the next realm is unsealed.",
                        color = Color(0xFF5C798A),
                        textAlign = TextAlign.Center
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            gameViewModel.quitLevel()
                            mainViewModel.navigateTo(AppScreen.HOME)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF70C635))
                    ) {
                        Text("CLAIM GLORY", color = Color.White, fontWeight = FontWeight.ExtraBold)
                    }
                }
            )
        }
    }
}
