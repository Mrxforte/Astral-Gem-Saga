package com.example.game.ui.screens.editor

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
import androidx.compose.material.icons.filled.*
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
import com.example.game.levels.generator.LevelGenerator
import com.example.game.model.*
import com.example.game.viewmodel.AppScreen
import com.example.game.viewmodel.GameViewModel
import com.example.game.viewmodel.MainViewModel
import kotlinx.coroutines.launch

enum class EditorBrushType {
    PIECE,
    BLOCKER,
    SPECIAL,
    JELLY,
    ERASE
}

data class EditorBrush(
    val type: EditorBrushType,
    val pieceType: PieceType? = null,
    val blockerType: BlockerType? = null,
    val specialPiece: SpecialPiece? = null,
    val label: String,
    val iconEmoji: String,
    val colorHex: Long = 0xFF00ACC1
)

@Composable
fun LevelEditorScreen(
    gameViewModel: GameViewModel,
    mainViewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    var boardSize by remember { mutableStateOf(7) }
    var levelTitle by remember { mutableStateOf("Custom Sunny Dungeon") }
    var moveLimit by remember { mutableStateOf(25) }
    var targetScore by remember { mutableStateOf(8000) }

    // Board cell definitions: row to col -> Pair<PieceType?, BlockerType>
    val cellPieces = remember { mutableStateMapOf<Pair<Int, Int>, PieceType>() }
    val cellBlockers = remember { mutableStateMapOf<Pair<Int, Int>, BlockerType>() }
    val cellJelly = remember { mutableStateMapOf<Pair<Int, Int>, Boolean>() }

    // Available Brushes
    val brushes = listOf(
        EditorBrush(EditorBrushType.PIECE, pieceType = PieceType.RUBY, label = "Ruby", iconEmoji = "◆", colorHex = 0xFFE53935),
        EditorBrush(EditorBrushType.PIECE, pieceType = PieceType.SAPPHIRE, label = "Sapphire", iconEmoji = "▲", colorHex = 0xFF1E88E5),
        EditorBrush(EditorBrushType.PIECE, pieceType = PieceType.EMERALD, label = "Emerald", iconEmoji = "⬡", colorHex = 0xFF43A047),
        EditorBrush(EditorBrushType.PIECE, pieceType = PieceType.TOPAZ, label = "Topaz", iconEmoji = "★", colorHex = 0xFFFDD835),
        EditorBrush(EditorBrushType.PIECE, pieceType = PieceType.AMETHYST, label = "Amethyst", iconEmoji = "●", colorHex = 0xFF8E24AA),
        EditorBrush(EditorBrushType.BLOCKER, blockerType = BlockerType.SINGLE_ICE, label = "Ice", iconEmoji = "❄️", colorHex = 0xFF00E5FF),
        EditorBrush(EditorBrushType.BLOCKER, blockerType = BlockerType.STONE, label = "Stone", iconEmoji = "🪨", colorHex = 0xFF78909C),
        EditorBrush(EditorBrushType.BLOCKER, blockerType = BlockerType.LOCKED_TILE, label = "Chain", iconEmoji = "⛓️", colorHex = 0xFFFFD700),
        EditorBrush(EditorBrushType.JELLY, label = "Jelly", iconEmoji = "🍧", colorHex = 0xFFFF4081),
        EditorBrush(EditorBrushType.ERASE, label = "Clear", iconEmoji = "🧹", colorHex = 0xFFE0E0E0)
    )
    var selectedBrush by remember { mutableStateOf(brushes.first()) }

    var validationMsg by remember { mutableStateOf<String?>(null) }
    var jsonExportText by remember { mutableStateOf<String?>(null) }

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
                        onClick = { mainViewModel.navigateTo(AppScreen.HOME) },
                        modifier = Modifier.testTag("editor_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF1E3A4B))
                    }
                    Text(
                        text = "LEVEL ARCHITECT",
                        color = Color(0xFF1E3A4B),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                    Button(
                        onClick = {
                            val config = LevelConfig(
                                id = 9999,
                                worldId = 1,
                                title = levelTitle,
                                rows = boardSize,
                                cols = boardSize,
                                moveLimit = moveLimit,
                                targetScore = targetScore,
                                objectives = listOf(
                                    LevelObjective(ObjectiveType.REACH_SCORE, targetScore)
                                ),
                                initialBlockers = cellBlockers.toMap(),
                                initialJelly = cellJelly.keys.toSet()
                            )
                            gameViewModel.startCustomLevel(config)
                            mainViewModel.navigateTo(AppScreen.GAMEPLAY)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF70C635)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("editor_playtest_button")
                    ) {
                        Text("TEST PLAY", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                    }
                }
            }
        },
        containerColor = Color(0xFFE8F7FB),
        modifier = modifier.fillMaxSize().testTag("level_editor_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Level Title and Board Size Selector
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFBCE7F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = levelTitle,
                        onValueChange = { levelTitle = it },
                        label = { Text("Level Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1E3A4B),
                            unfocusedTextColor = Color(0xFF1E3A4B),
                            focusedBorderColor = Color(0xFF00ACC1)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Grid Dimensions: ${boardSize}x${boardSize}", color = Color(0xFF1E3A4B), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(6, 7, 8, 9).forEach { sz ->
                                FilterChip(
                                    selected = boardSize == sz,
                                    onClick = { boardSize = sz },
                                    label = { Text("${sz}x$sz", fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Moves: $moveLimit", color = Color(0xFFE65100), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FilledTonalButton(onClick = { moveLimit = maxOf(10, moveLimit - 5) }) { Text("-5") }
                            FilledTonalButton(onClick = { moveLimit = minOf(60, moveLimit + 5) }) { Text("+5") }
                        }
                    }
                }
            }

            // Brush Palette
            Text(
                text = "BRUSH PALETTE: ${selectedBrush.label}",
                color = Color(0xFF00ACC1),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(brushes) { brush ->
                    val isSelected = brush == selectedBrush
                    Surface(
                        color = if (isSelected) Color(brush.colorHex) else Color.White,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (isSelected) Color(0xFF1E3A4B) else Color(0xFFBCE7F0)
                        ),
                        modifier = Modifier
                            .clickable { selectedBrush = brush }
                            .padding(2.dp)
                            .testTag("brush_${brush.label.lowercase()}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Text(text = brush.iconEmoji, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = brush.label,
                                color = if (isSelected) Color.White else Color(0xFF1E3A4B),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Interactive Editor Board Grid
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFBCE7F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    for (r in 0 until boardSize) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            for (c in 0 until boardSize) {
                                val piece = cellPieces[r to c]
                                val blocker = cellBlockers[r to c]
                                val isJelly = cellJelly.containsKey(r to c)

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isJelly) Color(0x66FF4081)
                                            else if (blocker == BlockerType.STONE) Color(0xFFECEFF1)
                                            else Color(0xFFF7FCFE)
                                        )
                                        .border(1.dp, Color(0xFFBCE7F0), RoundedCornerShape(8.dp))
                                        .clickable {
                                            when (selectedBrush.type) {
                                                EditorBrushType.PIECE -> {
                                                    selectedBrush.pieceType?.let { cellPieces[r to c] = it }
                                                    cellBlockers.remove(r to c)
                                                }
                                                EditorBrushType.BLOCKER -> {
                                                    selectedBrush.blockerType?.let { cellBlockers[r to c] = it }
                                                    if (selectedBrush.blockerType == BlockerType.STONE) cellPieces.remove(r to c)
                                                }
                                                EditorBrushType.JELLY -> {
                                                    if (cellJelly.containsKey(r to c)) cellJelly.remove(r to c)
                                                    else cellJelly[r to c] = true
                                                }
                                                EditorBrushType.ERASE -> {
                                                    cellPieces.remove(r to c)
                                                    cellBlockers.remove(r to c)
                                                    cellJelly.remove(r to c)
                                                }
                                                else -> {}
                                            }
                                        }
                                ) {
                                    if (blocker == BlockerType.STONE) {
                                        Text("🪨", fontSize = 14.sp)
                                    } else if (blocker == BlockerType.SINGLE_ICE) {
                                        Text("❄️", fontSize = 14.sp)
                                    } else if (blocker == BlockerType.LOCKED_TILE) {
                                        Text("⛓️", fontSize = 14.sp)
                                    } else if (piece != null) {
                                        Text(piece.symbol, color = Color(piece.hexColor), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Actions: Validate, Save to Room, Export JSON
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = {
                        val config = LevelConfig(
                            id = 0,
                            worldId = 1,
                            title = levelTitle,
                            rows = boardSize,
                            cols = boardSize,
                            moveLimit = moveLimit,
                            targetScore = targetScore,
                            objectives = listOf(LevelObjective(ObjectiveType.REACH_SCORE, targetScore))
                        )
                        val isValid = LevelGenerator.validateLevelSolvability(config)
                        validationMsg = if (isValid) "✓ Level layout is valid & solvable!" else "⚠️ Validation Warning: Check move limit."
                    },
                    modifier = Modifier.weight(1f).testTag("editor_validate_button")
                ) {
                    Text("VALIDATE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        val json = """
                            {
                              "title": "$levelTitle",
                              "rows": $boardSize,
                              "cols": $boardSize,
                              "moves": $moveLimit,
                              "score": $targetScore,
                              "blockers": ${cellBlockers.size},
                              "jelly": ${cellJelly.size}
                            }
                        """.trimIndent()
                        coroutineScope.launch {
                            mainViewModel.repository.saveCustomLevel(levelTitle, json)
                            validationMsg = "Saved to My Levels in Room Database!"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00ACC1)),
                    modifier = Modifier.weight(1f).testTag("editor_save_button")
                ) {
                    Text("SAVE ROOM", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Button(
                    onClick = {
                        jsonExportText = """
                            {
                              "title": "$levelTitle",
                              "dimensions": "${boardSize}x${boardSize}",
                              "moveLimit": $moveLimit,
                              "targetScore": $targetScore,
                              "blockersCount": ${cellBlockers.size}
                            }
                        """.trimIndent()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFDC72B)),
                    modifier = Modifier.weight(1f).testTag("editor_export_button")
                ) {
                    Text("EXPORT", color = Color(0xFF1E3A4B), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            validationMsg?.let {
                Text(
                    text = it,
                    color = if (it.startsWith("✓")) Color(0xFF2E7D32) else Color(0xFFE65100),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            // Export Modal
            jsonExportText?.let { json ->
                AlertDialog(
                    onDismissRequest = { jsonExportText = null },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    title = { Text("Level JSON Schema", color = Color(0xFF1E3A4B), fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            Text(text = "Compatible with Web/Desktop Level CMS:", color = Color(0xFF5C798A), fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = Color(0xFFF7FCFE),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBCE7F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = json,
                                    color = Color(0xFF1E3A4B),
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { jsonExportText = null },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF70C635))
                        ) {
                            Text("DONE", color = Color.White)
                        }
                    }
                )
            }
        }
    }
}
