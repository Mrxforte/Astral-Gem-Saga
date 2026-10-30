package com.example.game.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
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
import com.example.game.characters.AnimatedCharacterView
import com.example.game.model.CharacterMood
import com.example.game.model.CompanionCatalog
import com.example.game.model.LevelConfig
import kotlinx.coroutines.delay

@Composable
fun LevelStartCinematic(
    levelConfig: LevelConfig,
    onReady: () -> Unit
) {
    val companion = remember(levelConfig.worldId) {
        CompanionCatalog.getCompanionForWorld(levelConfig.worldId)
    }

    var countdownStep by remember { mutableStateOf(3) }
    var scaleTarget by remember { mutableStateOf(1.4f) }

    val animScale by animateFloatAsState(
        targetValue = scaleTarget,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "count_scale"
    )

    LaunchedEffect(Unit) {
        delay(600)
        while (countdownStep > 0) {
            scaleTarget = 1.35f
            delay(120)
            scaleTarget = 1.0f
            delay(480)
            countdownStep--
        }
        scaleTarget = 1.5f
        delay(400)
        onReady()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x77000000))
            .clickable(onClick = onReady) // Tap to skip
            .testTag("level_start_cinematic"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Companion Greeting
            AnimatedCharacterView(
                character = companion,
                mood = CharacterMood.EXCITED,
                speechText = companion.greetingQuote,
                sizeDp = 90,
                showSpeechBubble = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = Color.White,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFBCE7F0)),
                shadowElevation = 6.dp,
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "WORLD ${levelConfig.worldId}",
                        color = Color(0xFF00ACC1),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = levelConfig.title,
                        color = Color(0xFF1E3A4B),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "OBJECTIVE: Score ${levelConfig.targetScore} points & clear seals",
                        color = Color(0xFF5C798A),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Punchy Animated Countdown Bubble (Sunny Gold Bubble)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(90.dp)
                    .scale(animScale)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFFFFF176), Color(0xFFFDC72B))
                        ),
                        CircleShape
                    )
                    .border(3.dp, Color.White, CircleShape)
            ) {
                Text(
                    text = if (countdownStep > 0) "$countdownStep" else "GO!",
                    color = Color(0xFF1E3A4B),
                    fontSize = if (countdownStep > 0) 42.sp else 32.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun LevelCompleteCinematic(
    score: Int,
    stars: Int,
    coinsEarned: Int,
    worldId: Int,
    onNextLevel: () -> Unit,
    onMap: () -> Unit
) {
    val companion = remember(worldId) {
        CompanionCatalog.getCompanionForWorld(worldId)
    }

    // Star reveal animations
    var visibleStars by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        delay(400)
        if (stars >= 1) { visibleStars = 1; delay(350) }
        if (stars >= 2) { visibleStars = 2; delay(350) }
        if (stars >= 3) { visibleStars = 3 }
    }

    // Animated score odometer
    var displayScore by remember { mutableStateOf(0) }
    LaunchedEffect(score) {
        val step = maxOf(1, score / 25)
        while (displayScore < score) {
            delay(20)
            displayScore = minOf(score, displayScore + step)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x99000000))
            .testTag("level_complete_cinematic"),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFBCE7F0)),
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                // Victorious Companion Dancing
                AnimatedCharacterView(
                    character = companion,
                    mood = CharacterMood.VICTORY,
                    speechText = companion.victoryQuote,
                    sizeDp = 90,
                    showSpeechBubble = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "VICTORY!",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E3A4B),
                    letterSpacing = 2.sp
                )

                Text(
                    text = "LEVEL COMPLETED",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF70C635),
                    letterSpacing = 3.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 3 Animated Stars
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (s in 1..3) {
                        val earned = s <= visibleStars
                        val starScale by animateFloatAsState(
                            targetValue = if (earned) 1.25f else 0.85f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "star_$s"
                        )
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (earned) Color(0xFFFDC72B) else Color(0xFFE0E0E0),
                            modifier = Modifier
                                .size(48.dp)
                                .scale(starScale)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Score and Coins Panel
                Surface(
                    color = Color(0xFFF7FCFE),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBCE7F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "FINAL SCORE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5C798A)
                        )
                        Text(
                            text = "$displayScore",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF00ACC1)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "+$coinsEarned GOLD COINS 🪙",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Button(
                    onClick = onNextLevel,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF70C635)),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF55A822)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("cinematic_next_level_button")
                ) {
                    Text(
                        text = "CONTINUE",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onMap) {
                    Text(
                        text = "RETURN TO MAP",
                        color = Color(0xFF5C798A),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
