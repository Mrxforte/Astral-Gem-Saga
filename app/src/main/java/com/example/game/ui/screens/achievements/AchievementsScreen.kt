package com.example.game.ui.screens.achievements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.data.entity.AchievementEntity
import com.example.game.viewmodel.AppScreen
import com.example.game.viewmodel.MainViewModel

@Composable
fun AchievementsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val achievements by viewModel.achievements.collectAsState()

    val totalCompleted = achievements.count { it.progress >= it.target }

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
                        onClick = { viewModel.navigateTo(AppScreen.HOME) },
                        modifier = Modifier.testTag("achievements_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF1E3A4B))
                    }
                    Text(
                        text = "QUESTS & ACHIEVEMENTS",
                        color = Color(0xFF1E3A4B),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                    Surface(
                        color = Color(0xFFFDC72B),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF57F17))
                    ) {
                        Text(
                            text = "$totalCompleted/${achievements.size}",
                            color = Color(0xFF1E3A4B),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFFE8F7FB),
        modifier = modifier.fillMaxSize().testTag("achievements_screen")
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            items(achievements) { ach ->
                val isCompleted = ach.progress >= ach.target
                val progressPercent = (ach.progress.toFloat() / ach.target.toFloat()).coerceIn(0f, 1f)

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (ach.isClaimed) Color(0xFFF5F5F5) else if (isCompleted) Color(0xFFFFF9C4) else Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.2.dp,
                        if (isCompleted && !ach.isClaimed) Color(0xFFFFD54F) else Color(0xFFBCE7F0)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(14.dp).fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        if (isCompleted) Color(0xFFFFF176) else Color(0xFFE0F7FA),
                                        CircleShape
                                    )
                                    .border(1.5.dp, Color(0xFFBCE7F0), CircleShape)
                            ) {
                                Text(text = ach.iconEmoji, fontSize = 22.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ach.title,
                                    color = Color(0xFF1E3A4B),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = ach.description,
                                    color = Color(0xFF5C798A),
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    LinearProgressIndicator(
                                        progress = { progressPercent },
                                        color = if (isCompleted) Color(0xFF70C635) else Color(0xFF00ACC1),
                                        trackColor = Color(0xFFE0F7FA),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${ach.progress}/${ach.target}",
                                        color = Color(0xFF5C798A),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Claim or Status Button
                        when {
                            ach.isClaimed -> {
                                Surface(
                                    color = Color(0xFFE0E0E0),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = "Claimed", tint = Color.Gray, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Done", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            isCompleted -> {
                                Button(
                                    onClick = { viewModel.claimAchievement(ach.id) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF70C635),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("claim_achievement_${ach.id}")
                                ) {
                                    Text("CLAIM 🪙${ach.rewardCoins}", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                            else -> {
                                Text(
                                    text = "🪙 ${ach.rewardCoins}",
                                    color = Color(0xFFF57F17),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
