package com.example.game.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
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
import com.example.game.viewmodel.AppScreen
import com.example.game.viewmodel.MainViewModel

@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.playerProfile.collectAsState()
    val progressList by viewModel.levelProgressList.collectAsState()

    var isEditingName by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf(profile?.username ?: "Crystal Mage") }
    var selectedAvatar by remember { mutableStateOf(profile?.avatarEmoji ?: "🧙‍♂️") }

    val avatars = listOf("🧙‍♂️", "🧙‍♀️", "🧝‍♂️", "🧝‍♀️", "👑", "🐉", "🦅", "⚔️", "✨", "💎")

    val completedLevels = progressList.size
    val totalStars = profile?.starsTotal ?: 0
    val highScore = profile?.highestScore ?: 0

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
                        modifier = Modifier.testTag("profile_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF1E3A4B))
                    }
                    Text(
                        text = "PLAYER ARCHIVES",
                        color = Color(0xFF1E3A4B),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                }
            }
        },
        containerColor = Color(0xFFE8F7FB),
        modifier = modifier.fillMaxSize().testTag("profile_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Player Identity Card (Sunny white card)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFBCE7F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(20.dp)
                ) {
                    // Avatar Circle
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(80.dp)
                            .background(Color(0xFFE0F7FA), CircleShape)
                            .border(3.dp, Color(0xFFFDC72B), CircleShape)
                    ) {
                        Text(text = selectedAvatar, fontSize = 42.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isEditingName) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = newName,
                                onValueChange = { newName = it },
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("profile_name_input")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    viewModel.updateProfile(newName, selectedAvatar)
                                    isEditingName = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF70C635)),
                                modifier = Modifier.testTag("profile_name_save_button")
                            ) {
                                Text("Save", color = Color.White)
                            }
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = profile?.username ?: "Crystal Mage",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1E3A4B)
                            )
                            IconButton(onClick = { isEditingName = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Name", tint = Color(0xFF00ACC1), modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Text(
                        text = "Realm Traveler • Rank #4",
                        fontSize = 12.sp,
                        color = Color(0xFF5C798A)
                    )
                }
            }

            // Choose Avatar Selector
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFBCE7F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "CHOOSE AVATAR EMOJI",
                        color = Color(0xFF00ACC1),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(avatars) { av ->
                            val isSelected = av == selectedAvatar
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        if (isSelected) Color(0xFFFFF9C4) else Color(0xFFF7FCFE),
                                        CircleShape
                                    )
                                    .border(
                                        2.dp,
                                        if (isSelected) Color(0xFFFDC72B) else Color(0xFFBCE7F0),
                                        CircleShape
                                    )
                                    .clickable {
                                        selectedAvatar = av
                                        viewModel.updateProfile(profile?.username ?: "Crystal Mage", av)
                                    }
                            ) {
                                Text(text = av, fontSize = 24.sp)
                            }
                        }
                    }
                }
            }

            // Career Statistics Grid
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFBCE7F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "CAREER STATS",
                        color = Color(0xFF00ACC1),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatTile(label = "Levels Won", value = "$completedLevels", icon = "🏆", modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        StatTile(label = "Total Stars", value = "$totalStars", icon = "⭐", modifier = Modifier.weight(1f))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatTile(label = "High Score", value = "$highScore", icon = "🎯", modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        StatTile(label = "Current Lives", value = "${profile?.lives ?: 5}/5", icon = "❤️", modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFFF7FCFE),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBCE7F0)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = Color(0xFF1E3A4B),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp
            )
            Text(
                text = label,
                color = Color(0xFF5C798A),
                fontSize = 11.sp
            )
        }
    }
}
