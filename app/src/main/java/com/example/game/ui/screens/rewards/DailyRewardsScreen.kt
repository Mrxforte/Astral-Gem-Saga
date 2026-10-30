package com.example.game.ui.screens.rewards

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.viewmodel.AppScreen
import com.example.game.viewmodel.MainViewModel

@Composable
fun DailyRewardsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.playerProfile.collectAsState()
    val currentStreak = profile?.dailyStreak ?: 0

    val rewardsTable = listOf(
        1 to ("100 Gold Coins" to "🪙"),
        2 to ("150 Coins + 1 Hammer" to "🔨"),
        3 to ("200 Coins + 1 Mana Bomb" to "💣"),
        4 to ("250 Coins + 1 Shuffle" to "🔀"),
        5 to ("300 Coins + 2 Extra Moves" to "➕"),
        6 to ("400 Coins + 1 Color Bomb" to "🌈"),
        7 to ("1,000 Coins + Grand Chest!" to "👑")
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
                        onClick = { viewModel.navigateTo(AppScreen.HOME) },
                        modifier = Modifier.testTag("daily_rewards_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF1E3A4B))
                    }
                    Text(
                        text = "DAILY REWARD STREAK",
                        color = Color(0xFF1E3A4B),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                }
            }
        },
        containerColor = Color(0xFFE8F7FB),
        modifier = modifier.fillMaxSize().testTag("daily_rewards_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "🎁", fontSize = 48.sp)
                Text(
                    text = "Claim Your Daily Fortune",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E3A4B)
                )
                Text(
                    text = "Login each consecutive day to unlock increasingly powerful boosters & coins!",
                    fontSize = 13.sp,
                    color = Color(0xFF5C798A),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            // 7 Days Grid
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                rewardsTable.forEach { (day, info) ->
                    val isPast = day < currentStreak
                    val isCurrent = day == currentStreak

                    Surface(
                        color = if (isCurrent) Color(0xFFFFF9C4) else Color.White,
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            if (isCurrent) 2.dp else 1.dp,
                            if (isCurrent) Color(0xFFFDC72B) else Color(0xFFBCE7F0)
                        ),
                        shadowElevation = if (isCurrent) 3.dp else 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(if (isCurrent) Color(0xFFFDC72B) else Color(0xFFE0F7FA), CircleShape)
                                ) {
                                    Text(
                                        text = info.second,
                                        fontSize = 18.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Day $day",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isCurrent) Color(0xFFE65100) else Color(0xFF1E3A4B)
                                    )
                                    Text(
                                        text = info.first,
                                        fontSize = 12.sp,
                                        color = Color(0xFF5C798A)
                                    )
                                }
                            }

                            if (isPast) {
                                Text("CLAIMED", color = Color(0xFF70C635), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            } else if (isCurrent) {
                                Text("TODAY!", color = Color(0xFFE65100), fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                            } else {
                                Text("LOCKED", color = Color.Gray, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Claim Action Button
            Button(
                onClick = { viewModel.claimDaily() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFDC72B)),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFF57F17)),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(52.dp)
                    .testTag("claim_daily_button")
            ) {
                Text(
                    text = "CLAIM TODAY'S REWARD",
                    color = Color(0xFF1E3A4B),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
            }
        }
    }
}
