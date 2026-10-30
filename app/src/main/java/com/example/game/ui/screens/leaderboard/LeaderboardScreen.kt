package com.example.game.ui.screens.leaderboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.viewmodel.AppScreen
import com.example.game.viewmodel.MainViewModel

data class LeaderboardEntry(
    val rank: Int,
    val name: String,
    val avatar: String,
    val score: Int,
    val stars: Int,
    val isCurrentUser: Boolean = false
)

@Composable
fun LeaderboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.playerProfile.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    val userScore = profile?.highestScore ?: 14500
    val userStars = profile?.starsTotal ?: 18
    val userName = profile?.username ?: "You"
    val userAvatar = profile?.avatarEmoji ?: "🧙‍♂️"

    val sampleLeaderboard = remember(userScore, userStars) {
        listOf(
            LeaderboardEntry(1, "Astraea_Mage", "🧙‍♀️", 48500, 142),
            LeaderboardEntry(2, "ZephyrGamer", "🦅", 44200, 138),
            LeaderboardEntry(3, "DragonLord", "🐲", 39800, 120),
            LeaderboardEntry(4, userName, userAvatar, maxOf(userScore, 24000), userStars, isCurrentUser = true),
            LeaderboardEntry(5, "CrystalKnight", "🛡️", 22500, 85),
            LeaderboardEntry(6, "NovaStriker", "✨", 19400, 72),
            LeaderboardEntry(7, "ShadowWeaver", "🔮", 16800, 60),
            LeaderboardEntry(8, "GlacierWarden", "🧊", 14300, 54),
            LeaderboardEntry(9, "PyreMancer", "🔥", 12100, 45),
            LeaderboardEntry(10, "MoonLight", "🌙", 9800, 30)
        )
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
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.HOME) },
                        modifier = Modifier.testTag("leaderboard_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF1E3A4B))
                    }
                    Text(
                        text = "HALL OF FAME",
                        color = Color(0xFF1E3A4B),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                }
            }
        },
        containerColor = Color(0xFFE8F7FB),
        modifier = modifier.fillMaxSize().testTag("leaderboard_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Category Tabs: Global, Weekly, Friends
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Color(0xFF00ACC1)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("GLOBAL", fontWeight = FontWeight.Bold, color = if (selectedTab == 0) Color(0xFF00ACC1) else Color(0xFF5C798A)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("WEEKLY", fontWeight = FontWeight.Bold, color = if (selectedTab == 1) Color(0xFF00ACC1) else Color(0xFF5C798A)) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("FRIENDS", fontWeight = FontWeight.Bold, color = if (selectedTab == 2) Color(0xFF00ACC1) else Color(0xFF5C798A)) }
                )
            }

            // Top 3 Podium Card (Light sunny podium)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFBCE7F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Rank 2
                    PodiumPillar(entry = sampleLeaderboard[1], height = 75.dp, crown = "🥈")
                    // Rank 1
                    PodiumPillar(entry = sampleLeaderboard[0], height = 100.dp, crown = "👑")
                    // Rank 3
                    PodiumPillar(entry = sampleLeaderboard[2], height = 60.dp, crown = "🥉")
                }
            }

            // List of Players
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(sampleLeaderboard) { index, entry ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (entry.isCurrentUser) Color(0xFFFFF9C4) else Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (entry.isCurrentUser) Color(0xFFFFD54F) else Color(0xFFBCE7F0)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "#${entry.rank}",
                                    color = if (entry.rank <= 3) Color(0xFFE65100) else Color(0xFF5C798A),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.width(32.dp)
                                )
                                Text(text = entry.avatar, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = entry.name,
                                        color = Color(0xFF1E3A4B),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "⭐ ${entry.stars} Stars",
                                        color = Color(0xFFE65100),
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Text(
                                text = "${entry.score} pts",
                                color = Color(0xFF00ACC1),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PodiumPillar(
    entry: LeaderboardEntry,
    height: androidx.compose.ui.unit.Dp,
    crown: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = crown, fontSize = 20.sp)
        Text(text = entry.avatar, fontSize = 24.sp)
        Text(
            text = entry.name.split("_").first(),
            color = Color(0xFF1E3A4B),
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            color = if (crown == "👑") Color(0xFFFDC72B) else Color(0xFFE0F7FA),
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBCE7F0)),
            modifier = Modifier
                .width(68.dp)
                .height(height)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "${entry.score}",
                    color = Color(0xFF1E3A4B),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
        }
    }
}
