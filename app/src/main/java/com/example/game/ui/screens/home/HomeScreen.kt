package com.example.game.ui.screens.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.characters.AnimatedCharacterView
import com.example.game.data.entity.LevelProgressEntity
import com.example.game.data.entity.PlayerProfileEntity
import com.example.game.model.CharacterMood
import com.example.game.model.CompanionCatalog
import com.example.game.model.WorldsCatalog
import com.example.game.ui.components.AnimatedWorldBackground
import com.example.game.ui.components.GameHeaderBar
import com.example.game.viewmodel.AppScreen
import com.example.game.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.playerProfile.collectAsState()
    val progressList by viewModel.levelProgressList.collectAsState()
    val dailyBanner by viewModel.dailyRewardBanner.collectAsState()

    // Determine highest unlocked level
    val maxCompleted = progressList.maxOfOrNull { it.levelId } ?: 0
    val nextLevelId = maxCompleted + 1
    val currentWorld = WorldsCatalog.getWorldForLevel(nextLevelId)
    val companion = remember(currentWorld.id) { CompanionCatalog.getCompanionForWorld(currentWorld.id) }

    var companionSpeech by remember { mutableStateOf(companion.greetingQuote) }
    var companionMood by remember { mutableStateOf(CharacterMood.IDLE) }
    var showLivesModal by remember { mutableStateOf(false) }
    var showSettingsModal by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "play_pulse")
    val playScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "play_scale"
    )

    Scaffold(
        topBar = {
            GameHeaderBar(
                profile = profile,
                isMuted = viewModel.audioManager.isMuted,
                onProfileClick = { viewModel.navigateTo(AppScreen.PROFILE) },
                onLivesClick = { showLivesModal = true },
                onCoinsClick = { viewModel.navigateTo(AppScreen.SHOP) },
                onToggleAudio = { viewModel.toggleAudio() },
                modifier = Modifier.statusBarsPadding()
            )
        },
        containerColor = Color(0xFF4EC0CA),
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen")
    ) { innerPadding ->
        AnimatedWorldBackground(worldId = currentWorld.id) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Daily Reward Alert Banner
                if (dailyBanner != null) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFFFD54F)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = dailyBanner ?: "",
                                    color = Color(0xFFE65100),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { viewModel.dismissDailyBanner() }) {
                                    Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color(0xFF5C798A))
                                }
                            }
                        }
                    }
                }

                // Interactive Companion Showcase
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AnimatedCharacterView(
                            character = companion,
                            mood = companionMood,
                            speechText = companionSpeech,
                            sizeDp = 100,
                            showSpeechBubble = true,
                            onTap = {
                                viewModel.audioManager.playSound(com.example.game.audio.SoundType.PIECE_SELECT)
                                viewModel.hapticsManager.lightTap()
                                companionMood = CharacterMood.EXCITED
                                val funQuotes = listOf(
                                    "✨ The crystal runes are pulsing with energy!",
                                    "🌟 Connect 5 gems in a line to form a Color Bomb!",
                                    "💥 Combine two Color Bombs to unleash the SUPERNOVA!",
                                    "🔮 You are the realm's greatest enchanter!",
                                    "🏆 Ready to conquer World ${currentWorld.id}?"
                                )
                                companionSpeech = funQuotes.random()
                            }
                        )
                    }
                }

                // Hero Banner: Current World & Level Card (Flappy Sky Gradient)
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFBCE7F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFF4EC0CA),
                                            Color(0xFF81D4FA)
                                        )
                                    )
                                )
                                .padding(20.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "${currentWorld.worldIcon} WORLD ${currentWorld.id}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFF9C4),
                                    letterSpacing = 2.sp
                                )
                                Text(
                                    text = currentWorld.name,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = currentWorld.subtitle,
                                    fontSize = 13.sp,
                                    color = Color(0xFFE1F5FE),
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Play Button with golden sunny style
                                Button(
                                    onClick = {
                                        if ((profile?.lives ?: 5) > 0) {
                                            viewModel.openLevel(nextLevelId)
                                        } else {
                                            showLivesModal = true
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFFDC72B),
                                        contentColor = Color(0xFF1E3A4B)
                                    ),
                                    shape = RoundedCornerShape(28.dp),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFF57F17)),
                                    modifier = Modifier
                                        .scale(playScale)
                                        .height(56.dp)
                                        .fillMaxWidth(0.85f)
                                        .testTag("home_play_next_button")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            modifier = Modifier.size(28.dp),
                                            tint = Color(0xFF1E3A4B)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "PLAY LEVEL $nextLevelId",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF1E3A4B)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Quick Nav Hub: Grid of 6 Main Modes
                item {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "EXPEDITIONS & REALMS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF1E3A4B),
                            letterSpacing = 1.sp
                        )
                        IconButton(onClick = { showSettingsModal = true }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color(0xFF1E3A4B))
                        }
                    }
                }

                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        HubActionCard(
                            title = "World Map",
                            subtitle = "10 Worlds Saga",
                            iconEmoji = "🗺️",
                            badge = "$maxCompleted Conquered",
                            badgeColor = Color(0xFF00E5FF),
                            onClick = { viewModel.navigateTo(AppScreen.WORLD_MAP) },
                            modifier = Modifier.weight(1f).testTag("hub_world_map_card")
                        )
                        HubActionCard(
                            title = "Boss Raids",
                            subtitle = "Guardian Battles",
                            iconEmoji = "🐉",
                            badge = "Titan Boss",
                            badgeColor = Color(0xFFFF1744),
                            onClick = { viewModel.openLevel(20) },
                            modifier = Modifier.weight(1f).testTag("hub_boss_card")
                        )
                    }
                }

                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        HubActionCard(
                            title = "Daily Gift",
                            subtitle = "7-Day Streak",
                            iconEmoji = "🎁",
                            badge = "Ready!",
                            badgeColor = Color(0xFFFFD54F),
                            onClick = { viewModel.navigateTo(AppScreen.DAILY_REWARDS) },
                            modifier = Modifier.weight(1f).testTag("hub_daily_card")
                        )
                        HubActionCard(
                            title = "Level Editor",
                            subtitle = "Create & Export",
                            iconEmoji = "🛠️",
                            badge = "Architect",
                            badgeColor = Color(0xFFB388FF),
                            onClick = { viewModel.navigateTo(AppScreen.LEVEL_EDITOR) },
                            modifier = Modifier.weight(1f).testTag("hub_editor_card")
                        )
                    }
                }

                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        HubActionCard(
                            title = "Arcane Shop",
                            subtitle = "Spells & Coins",
                            iconEmoji = "🔮",
                            badge = "Hot Deals",
                            badgeColor = Color(0xFF69F0AE),
                            onClick = { viewModel.navigateTo(AppScreen.SHOP) },
                            modifier = Modifier.weight(1f).testTag("hub_shop_card")
                        )
                        HubActionCard(
                            title = "Hall of Fame",
                            subtitle = "Leaderboards",
                            iconEmoji = "🏆",
                            badge = "Global",
                            badgeColor = Color(0xFFFFAB00),
                            onClick = { viewModel.navigateTo(AppScreen.LEADERBOARD) },
                            modifier = Modifier.weight(1f).testTag("hub_leaderboard_card")
                        )
                    }
                }

                item {
                    // Achievements banner card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFBCE7F0)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.navigateTo(AppScreen.ACHIEVEMENTS) }
                            .testTag("hub_achievements_banner")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🎖️", fontSize = 28.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Quests & Achievements",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF1E3A4B)
                                    )
                                    Text(
                                        text = "30 Challenges available to claim gold",
                                        fontSize = 12.sp,
                                        color = Color(0xFF5C798A)
                                    )
                                }
                            }
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color(0xFFF57F17)
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Settings Modal Dialog (Light Style)
        if (showSettingsModal) {
            AlertDialog(
                onDismissRequest = { showSettingsModal = false },
                containerColor = Color.White,
                shape = RoundedCornerShape(20.dp),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "⚙️", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Game Settings", color = Color(0xFF1E3A4B), fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Music & Ambient Audio", color = Color(0xFF1E3A4B), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Switch(
                                checked = !viewModel.audioManager.isMuted,
                                onCheckedChange = { viewModel.toggleAudio() }
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Haptic Vibrations", color = Color(0xFF1E3A4B), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Switch(
                                checked = viewModel.hapticsManager.isHapticsEnabled,
                                onCheckedChange = { viewModel.toggleHaptics() }
                            )
                        }
                        Text(
                            text = "Audio: Procedural Real-Time Synthesizer (Zero-Latency)",
                            color = Color(0xFF00ACC1),
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Graphics: High Performance Canvas (60-120 FPS)",
                            color = Color(0xFF70C635),
                            fontSize = 11.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showSettingsModal = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFDC72B))
                    ) {
                        Text("DONE", color = Color(0xFF1E3A4B), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // Lives Refill Modal Dialog (Light Style)
        if (showLivesModal) {
            AlertDialog(
                onDismissRequest = { showLivesModal = false },
                containerColor = Color.White,
                shape = RoundedCornerShape(20.dp),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "❤️", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Need More Lives?", color = Color(0xFF1E3A4B), fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Lives recharge automatically over time (1 life every 15 minutes). Or refill instantly now!",
                            color = Color(0xFF5C798A),
                            fontSize = 13.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF7FCFE), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFFBCE7F0), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(text = "Instant Full Refill (5 Lives)", color = Color(0xFF1E3A4B), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Button(
                                onClick = {
                                    viewModel.refillLivesWithCoins()
                                    showLivesModal = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFDC72B)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("150 🪙", color = Color(0xFF1E3A4B), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF7FCFE), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFFBCE7F0), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(text = "Watch Ad for +1 Life", color = Color(0xFF1E3A4B), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Button(
                                onClick = {
                                    viewModel.watchRewardedAdForLife()
                                    showLivesModal = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00ACC1)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("FREE AD", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showLivesModal = false }) {
                        Text("CLOSE", color = Color(0xFF5C798A))
                    }
                }
            )
        }
    }
}

@Composable
private fun HubActionCard(
    title: String,
    subtitle: String,
    iconEmoji: String,
    badge: String,
    badgeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .height(115.dp)
            .border(1.2.dp, Color(0xFFBCE7F0), RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = iconEmoji, fontSize = 24.sp)
                Surface(
                    color = badgeColor.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = badge,
                        color = badgeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF1E3A4B)
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color(0xFF5C798A)
                )
            }
        }
    }
}
