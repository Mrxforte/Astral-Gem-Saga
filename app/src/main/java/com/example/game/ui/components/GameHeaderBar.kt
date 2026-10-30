package com.example.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.data.entity.PlayerProfileEntity

@Composable
fun GameHeaderBar(
    profile: PlayerProfileEntity?,
    isMuted: Boolean,
    onProfileClick: () -> Unit,
    onLivesClick: () -> Unit,
    onCoinsClick: () -> Unit,
    onToggleAudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.White.copy(alpha = 0.95f),
        tonalElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBCE7F0)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("game_header_bar")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            // Profile & Avatar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onProfileClick() }
                    .padding(4.dp)
                    .testTag("header_profile_button")
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color(0xFFE0F7FA), CircleShape)
                ) {
                    Text(text = profile?.avatarEmoji ?: "🧙‍♂️", fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = profile?.username ?: "Hero",
                        color = Color(0xFF1E3A4B),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1
                    )
                    Text(
                        text = "⭐ ${profile?.starsTotal ?: 0}",
                        color = Color(0xFFE65100),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                }
            }

            // Stats Badges (Lives & Coins)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Lives Badge
                Surface(
                    color = Color(0xFFFFF0F2),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFCDD2)),
                    modifier = Modifier
                        .clickable { onLivesClick() }
                        .testTag("header_lives_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "❤️", fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${profile?.lives ?: 5}/5",
                            color = Color(0xFFC62828),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Refill Lives",
                            tint = Color(0xFFC62828),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                // Coins Badge
                Surface(
                    color = Color(0xFFFFFDE7),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFF59D)),
                    modifier = Modifier
                        .clickable { onCoinsClick() }
                        .testTag("header_coins_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "🪙", fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${profile?.coins ?: 500}",
                            color = Color(0xFFF57F17),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Buy Coins",
                            tint = Color(0xFFF57F17),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                // Audio Mute Quick Toggle
                IconButton(
                    onClick = onToggleAudio,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("header_audio_toggle")
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = "Toggle Audio",
                        tint = if (isMuted) Color.Gray else Color(0xFF00ACC1),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
