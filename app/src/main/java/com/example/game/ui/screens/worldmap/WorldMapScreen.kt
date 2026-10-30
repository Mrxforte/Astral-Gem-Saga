package com.example.game.ui.screens.worldmap

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.data.entity.LevelProgressEntity
import com.example.game.model.GameWorld
import com.example.game.model.WorldsCatalog
import com.example.game.viewmodel.AppScreen
import com.example.game.viewmodel.MainViewModel

@Composable
fun WorldMapScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val progressList by viewModel.levelProgressList.collectAsState()
    val selectedWorldId by viewModel.selectedWorldId.collectAsState()

    val currentWorld = WorldsCatalog.ALL_WORLDS.firstOrNull { it.id == selectedWorldId }
        ?: WorldsCatalog.ALL_WORLDS.first()

    val progressMap = remember(progressList) {
        progressList.associateBy { it.levelId }
    }

    val maxCompleted = progressList.maxOfOrNull { it.levelId } ?: 0
    val highestUnlocked = maxCompleted + 1

    Scaffold(
        topBar = {
            Surface(
                color = Color.White.copy(alpha = 0.95f),
                tonalElevation = 2.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBCE7F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.HOME) },
                        modifier = Modifier.testTag("world_map_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Home",
                            tint = Color(0xFF1E3A4B)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${currentWorld.worldIcon} World ${currentWorld.id}: ${currentWorld.name}",
                            color = Color(0xFF1E3A4B),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = currentWorld.subtitle,
                            color = Color(0xFFE65100),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFFE8F7FB),
        modifier = modifier
            .fillMaxSize()
            .testTag("world_map_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // World Carousel Selector
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(WorldsCatalog.ALL_WORLDS) { world ->
                    val isSelected = world.id == selectedWorldId
                    val isWorldUnlocked = highestUnlocked >= world.startLevel
                    WorldTabChip(
                        world = world,
                        isSelected = isSelected,
                        isUnlocked = isWorldUnlocked,
                        onClick = { viewModel.openWorld(world.id) }
                    )
                }
            }

            // World Description & Ambient Sound Indicator
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBCE7F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(text = "🎵", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${currentWorld.description} • Ambient: ${currentWorld.ambientTheme}",
                        color = Color(0xFF5C798A),
                        fontSize = 11.sp
                    )
                }
            }

            // Levels Path Nodes (Levels startLevel to endLevel)
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("world_map_levels_list")
            ) {
                val levels = (currentWorld.startLevel..currentWorld.endLevel).toList()
                items(levels) { levelId ->
                    val isUnlocked = levelId <= highestUnlocked
                    val progress = progressMap[levelId]
                    val isCurrentTarget = levelId == highestUnlocked
                    val isBossLevel = levelId % 20 == 0

                    LevelNodeItem(
                        levelId = levelId,
                        isUnlocked = isUnlocked,
                        isCurrentTarget = isCurrentTarget,
                        isBoss = isBossLevel,
                        stars = progress?.stars ?: 0,
                        highScore = progress?.highScore ?: 0,
                        themeColor = Color(0xFF00ACC1),
                        onNodeClick = {
                            if (isUnlocked) {
                                viewModel.openLevel(levelId)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun WorldTabChip(
    world: GameWorld,
    isSelected: Boolean,
    isUnlocked: Boolean,
    onClick: () -> Unit
) {
    val bgColor = when {
        isSelected -> Color(0xFFFDC72B)
        isUnlocked -> Color.White
        else -> Color(0xFFE0E0E0)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (isSelected) Color(0xFFF57F17) else Color(0xFFBCE7F0)
        ),
        shadowElevation = if (isSelected) 3.dp else 1.dp,
        modifier = Modifier
            .clickable(enabled = isUnlocked) { onClick() }
            .testTag("world_tab_${world.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(text = world.worldIcon, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "W${world.id}",
                color = if (isSelected) Color(0xFF1E3A4B) else if (isUnlocked) Color(0xFF1E3A4B) else Color.Gray,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 12.sp
            )
            if (!isUnlocked) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = Color.Gray,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
private fun LevelNodeItem(
    levelId: Int,
    isUnlocked: Boolean,
    isCurrentTarget: Boolean,
    isBoss: Boolean,
    stars: Int,
    highScore: Int,
    themeColor: Color,
    onNodeClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .then(
                if (isCurrentTarget) {
                    Modifier.background(Brush.horizontalGradient(listOf(Color(0xFFFFF9C4), Color(0xFFFFECB3))))
                } else {
                    Modifier.background(if (isUnlocked) Color.White else Color(0xFFF0F4F8))
                }
            )
            .border(
                width = if (isCurrentTarget) 2.dp else 1.dp,
                color = if (isCurrentTarget) Color(0xFFFDC72B) else Color(0xFFBCE7F0),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = isUnlocked) { onNodeClick() }
            .padding(14.dp)
            .testTag("level_node_$levelId")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Node Circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(46.dp)
                    .background(
                        color = if (isBoss) Color(0xFFEA3F33) else if (isCurrentTarget) Color(0xFFFDC72B) else if (isUnlocked) Color(0xFF00ACC1) else Color(0xFFCFD8DC),
                        shape = CircleShape
                    )
            ) {
                if (!isUnlocked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else if (isBoss) {
                    Text(text = "👑", fontSize = 22.sp)
                } else {
                    Text(
                        text = "$levelId",
                        color = if (isCurrentTarget) Color(0xFF1E3A4B) else Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = if (isBoss) "World Boss: Guardian Titan" else "Level $levelId",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (isUnlocked) Color(0xFF1E3A4B) else Color.Gray
                )
                if (isUnlocked && highScore > 0) {
                    Text(
                        text = "High Score: $highScore",
                        fontSize = 11.sp,
                        color = Color(0xFFE65100)
                    )
                } else if (isCurrentTarget) {
                    Text(
                        text = "Current Quest!",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF57F17)
                    )
                }
            }
        }

        // Stars earned badge
        if (isUnlocked && stars > 0) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                for (s in 1..3) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (s <= stars) Color(0xFFFDC72B) else Color(0xFFE0E0E0),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
