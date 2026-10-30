package com.example.game.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.game.model.*
import kotlin.math.*

@Composable
fun GameBoardCanvas(
    gameState: GameState,
    onCellTapped: (row: Int, col: Int) -> Unit,
    onSwapSwiped: (from: Pair<Int, Int>, to: Pair<Int, Int>) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gem_glow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rainbow_rotation"
    )

    // Drag tracking state
    var dragStartCoord by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var dragAccumulatedOffset by remember { mutableStateOf(Offset.Zero) }

    BoxWithConstraints(
        modifier = modifier
            .testTag("game_board_canvas")
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        val board = gameState.board
        val numRows = gameState.levelConfig.rows
        val numCols = gameState.levelConfig.cols
        val shake = gameState.screenShakeIntensity

        // Trigger burst of stars & flash shockwave rings when matches occur!
        LaunchedEffect(gameState.matchBurstEvents) {
            if (gameState.matchBurstEvents.isNotEmpty()) {
                val boxWidthPx = constraints.maxWidth.toFloat()
                val boxHeightPx = constraints.maxHeight.toFloat()
                val cellW = boxWidthPx / numCols
                val cellH = boxHeightPx / numRows

                for (event in gameState.matchBurstEvents) {
                    for ((r, c) in event.cells) {
                        val cx = c * cellW + cellW / 2f
                        val cy = r * cellH + cellH / 2f
                        com.example.game.effects.ParticleManager.spawnMatchBurst(
                            centerX = cx,
                            centerY = cy,
                            colorHex = event.colorHex,
                            count = if (event.isSpecial) 28 else (18 + event.combo * 4),
                            isSpecial = event.isSpecial,
                            combo = event.combo
                        )
                    }
                }
            }
        }

        // Procedural shake offsets
        val shakeOffsetX = if (shake > 0f) (sin(System.currentTimeMillis() * 0.05).toFloat() * shake * 12f) else 0f
        val shakeOffsetY = if (shake > 0f) (cos(System.currentTimeMillis() * 0.05).toFloat() * shake * 12f) else 0f

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = shakeOffsetX.dp, y = shakeOffsetY.dp)
                .pointerInput(gameState.status) {
                    if (gameState.status != GameStatus.PLAYING) return@pointerInput

                    detectTapGestures { offset ->
                        val cellW = size.width / numCols
                        val cellH = size.height / numRows
                        val col = (offset.x / cellW).toInt().coerceIn(0, numCols - 1)
                        val row = (offset.y / cellH).toInt().coerceIn(0, numRows - 1)
                        // Immediate spark on tap
                        com.example.game.effects.ParticleManager.spawnMatchBurst(
                            centerX = offset.x,
                            centerY = offset.y,
                            colorHex = 0xFFFFD700,
                            count = 8,
                            isSpecial = false
                        )
                        onCellTapped(row, col)
                    }
                }
                .pointerInput(gameState.status) {
                    if (gameState.status != GameStatus.PLAYING) return@pointerInput

                    detectDragGestures(
                        onDragStart = { offset ->
                            val cellW = size.width / numCols
                            val cellH = size.height / numRows
                            val col = (offset.x / cellW).toInt().coerceIn(0, numCols - 1)
                            val row = (offset.y / cellH).toInt().coerceIn(0, numRows - 1)
                            dragStartCoord = row to col
                            dragAccumulatedOffset = Offset.Zero
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            dragAccumulatedOffset += dragAmount
                            val threshold = 32f

                            val start = dragStartCoord
                            if (start != null) {
                                val (sr, sc) = start
                                if (abs(dragAccumulatedOffset.x) > threshold || abs(dragAccumulatedOffset.y) > threshold) {
                                    val (tr, tc) = if (abs(dragAccumulatedOffset.x) > abs(dragAccumulatedOffset.y)) {
                                        if (dragAccumulatedOffset.x > 0) sr to (sc + 1) else sr to (sc - 1)
                                    } else {
                                        if (dragAccumulatedOffset.y > 0) (sr + 1) to sc else (sr - 1) to sc
                                    }
                                    if (tr in 0 until numRows && tc in 0 until numCols) {
                                        onSwapSwiped(start, tr to tc)
                                    }
                                    dragStartCoord = null
                                }
                            }
                        },
                        onDragEnd = {
                            dragStartCoord = null
                            dragAccumulatedOffset = Offset.Zero
                        },
                        onDragCancel = {
                            dragStartCoord = null
                            dragAccumulatedOffset = Offset.Zero
                        }
                    )
                }
        ) {
            val cellWidth = size.width / numCols
            val cellHeight = size.height / numRows
            val padding = 3.5f

            // 0. Board Outer Backing Plate (Clean, bright white plate with border)
            drawRoundRect(
                color = Color(0xEEFFFFFF),
                topLeft = Offset.Zero,
                size = size,
                cornerRadius = CornerRadius(22f, 22f)
            )
            drawRoundRect(
                color = Color(0xFFBCE7F0),
                topLeft = Offset.Zero,
                size = size,
                cornerRadius = CornerRadius(22f, 22f),
                style = Stroke(width = 3.5f)
            )

            // 1. Draw Grid Background cells (Flappy Bird style bright sunny pastel tiles)
            for (r in 0 until numRows) {
                for (c in 0 until numCols) {
                    val left = c * cellWidth
                    val top = r * cellHeight
                    val isAlt = (r + c) % 2 == 0

                    val cellColor = if (isAlt) Color(0xFFE8F7FB) else Color(0xFFF7FCFE)
                    drawRoundRect(
                        color = cellColor,
                        topLeft = Offset(left + padding, top + padding),
                        size = Size(cellWidth - padding * 2, cellHeight - padding * 2),
                        cornerRadius = CornerRadius(14f, 14f)
                    )

                    // Cell grid border
                    drawRoundRect(
                        color = Color(0xFFC8ECF5),
                        topLeft = Offset(left + padding, top + padding),
                        size = Size(cellWidth - padding * 2, cellHeight - padding * 2),
                        cornerRadius = CornerRadius(14f, 14f),
                        style = Stroke(width = 1.5f)
                    )
                }
            }

            // 2. Draw Jelly Layers
            for (r in 0 until numRows) {
                for (c in 0 until numCols) {
                    val cell = board.getOrNull(r)?.getOrNull(c) ?: continue
                    if (cell.hasJelly) {
                        val left = c * cellWidth + padding + 2
                        val top = r * cellHeight + padding + 2
                        val jellyColor = if (cell.jellyLayers > 1) Color(0xBBFF1493) else Color(0x77FF69B4)
                        drawRoundRect(
                            color = jellyColor,
                            topLeft = Offset(left, top),
                            size = Size(cellWidth - padding * 2 - 4, cellHeight - padding * 2 - 4),
                            cornerRadius = CornerRadius(10f, 10f)
                        )
                    }
                }
            }

            // 3. Draw Pieces
            for (r in 0 until numRows) {
                for (c in 0 until numCols) {
                    val cell = board.getOrNull(r)?.getOrNull(c) ?: continue
                    val piece = cell.piece ?: continue

                    val centerX = c * cellWidth + cellWidth / 2f
                    val centerY = r * cellHeight + cellHeight / 2f
                    val radius = (min(cellWidth, cellHeight) / 2f) - padding * 2.2f

                    val isSelected = gameState.selectedCell == (r to c)
                    val scale = if (isSelected) pulseScale * 1.1f else 1.0f

                    // Draw selection halo
                    if (isSelected) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFFFD700), Color(0x00FFD700)),
                                center = Offset(centerX, centerY),
                                radius = radius * 1.6f
                            ),
                            radius = radius * 1.6f,
                            center = Offset(centerX, centerY)
                        )
                        drawCircle(
                            color = Color(0xFFFFD700),
                            radius = radius * 1.25f,
                            center = Offset(centerX, centerY),
                            style = Stroke(width = 3f)
                        )
                    }

                    // Subtle 3D candy drop shadow under gem
                    drawOval(
                        color = Color(0x221E3A4B),
                        topLeft = Offset(centerX - radius * 0.7f, centerY + radius * 0.5f),
                        size = Size(radius * 1.4f, radius * 0.4f)
                    )

                    // Render Gem according to Type
                    drawGem(
                        type = piece.type,
                        special = piece.special,
                        center = Offset(centerX, centerY),
                        radius = radius * scale,
                        rotation = rotationAngle
                    )
                }
            }

            // 4. Draw Blockers (Ice, Chains, Stone)
            for (r in 0 until numRows) {
                for (c in 0 until numCols) {
                    val cell = board.getOrNull(r)?.getOrNull(c) ?: continue
                    val left = c * cellWidth + padding
                    val top = r * cellHeight + padding
                    val width = cellWidth - padding * 2
                    val height = cellHeight - padding * 2

                    when (cell.blocker) {
                        BlockerType.SINGLE_ICE, BlockerType.DOUBLE_ICE, BlockerType.TRIPLE_ICE -> {
                            drawIceBlocker(left, top, width, height, cell.blockerHp)
                        }
                        BlockerType.STONE -> {
                            drawStoneBlocker(left, top, width, height)
                        }
                        BlockerType.LOCKED_TILE -> {
                            drawLockChains(left, top, width, height)
                        }
                        else -> {}
                    }
                }
            }

            // 5. Draw Active Laser Beams (Line Blast)
            for (beam in gameState.activeBeams) {
                if (beam.isHorizontal && beam.row != null) {
                    val beamY = beam.row * cellHeight + cellHeight / 2f
                    // Outer Glow
                    drawLine(
                        color = Color(beam.colorHex).copy(alpha = 0.5f),
                        start = Offset(0f, beamY),
                        end = Offset(size.width, beamY),
                        strokeWidth = cellHeight * 0.7f,
                        cap = StrokeCap.Round
                    )
                    // Bright Core
                    drawLine(
                        color = Color.White,
                        start = Offset(0f, beamY),
                        end = Offset(size.width, beamY),
                        strokeWidth = cellHeight * 0.22f,
                        cap = StrokeCap.Round
                    )
                } else if (!beam.isHorizontal && beam.col != null) {
                    val beamX = beam.col * cellWidth + cellWidth / 2f
                    // Outer Glow
                    drawLine(
                        color = Color(beam.colorHex).copy(alpha = 0.5f),
                        start = Offset(beamX, 0f),
                        end = Offset(beamX, size.height),
                        strokeWidth = cellWidth * 0.7f,
                        cap = StrokeCap.Round
                    )
                    // Bright Core
                    drawLine(
                        color = Color.White,
                        start = Offset(beamX, 0f),
                        end = Offset(beamX, size.height),
                        strokeWidth = cellWidth * 0.22f,
                        cap = StrokeCap.Round
                    )
                }
            }

            // 6. Draw Color Bomb Lightning Tethers
            for (tether in gameState.activeColorTethers) {
                val startX = tether.fromCol * cellWidth + cellWidth / 2f
                val startY = tether.fromRow * cellHeight + cellHeight / 2f
                val endX = tether.toCol * cellWidth + cellWidth / 2f
                val endY = tether.toRow * cellHeight + cellHeight / 2f

                // Jagged lightning path
                val midX = (startX + endX) / 2f + (sin(rotationAngle * 0.1).toFloat() * 14f)
                val midY = (startY + endY) / 2f + (cos(rotationAngle * 0.1).toFloat() * 14f)

                val lightningPath = Path().apply {
                    moveTo(startX, startY)
                    lineTo(midX, midY)
                    lineTo(endX, endY)
                }
                drawPath(
                    path = lightningPath,
                    color = Color(tether.colorHex).copy(alpha = tether.life.coerceIn(0f, 1f)),
                    style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                )
                drawPath(
                    path = lightningPath,
                    color = Color.White.copy(alpha = tether.life.coerceIn(0f, 1f)),
                    style = Stroke(width = 1.5f, cap = StrokeCap.Round)
                )
            }

            // 7. Draw Active Area Bomb Shockwaves
            for (sw in gameState.activeShockwaves) {
                val cx = sw.centerCol * cellWidth + cellWidth / 2f
                val cy = sw.centerRow * cellHeight + cellHeight / 2f
                val maxRadius = min(cellWidth, cellHeight) * sw.radiusCells
                val currentRadius = maxRadius * (1.0f - (System.currentTimeMillis() - sw.startTimestamp).toFloat() / sw.maxLifeMs).coerceIn(0f, 1f)

                drawCircle(
                    color = Color(sw.colorHex).copy(alpha = 0.7f),
                    radius = (maxRadius - currentRadius).coerceAtLeast(4f),
                    center = Offset(cx, cy),
                    style = Stroke(width = 5f)
                )
            }

            // 8. Draw SUPERNOVA Event (Color Bomb + Color Bomb)
            gameState.supernovaEvent?.let { sn ->
                val centerX = size.width / 2f
                val centerY = size.height / 2f

                // Screen darkener
                drawRect(
                    color = Color(0xCC000000),
                    topLeft = Offset.Zero,
                    size = size
                )

                // Expanding Singularity Core
                rotate(rotationAngle * 2f, Offset(centerX, centerY)) {
                    val rainbowColors = listOf(
                        Color(0xFFFF0055), Color(0xFFFF9900), Color(0xFFFFFF00),
                        Color(0xFF00FF66), Color(0xFF00CCFF), Color(0xFF9900FF), Color(0xFFFF0055)
                    )
                    drawCircle(
                        brush = Brush.sweepGradient(rainbowColors, Offset(centerX, centerY)),
                        radius = size.minDimension * 0.42f,
                        center = Offset(centerX, centerY)
                    )
                }

                // Celestial Shockwave Ring
                drawCircle(
                    color = Color.White,
                    radius = size.minDimension * 0.48f,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 8f)
                )
                drawCircle(
                    color = Color(0xFFFFD700),
                    radius = size.minDimension * 0.52f,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 4f)
                )
            }

            // 9. Draw Particle Pool (Zero Garbage Collection)
            com.example.game.effects.ParticleManager.updateAndDraw(this, deltaSec = 0.016f)

            // 10. Draw Legacy Particles
            for (p in gameState.particles) {
                drawCircle(
                    color = Color(p.colorHex).copy(alpha = p.life),
                    radius = p.size * p.life,
                    center = Offset(p.x, p.y)
                )
            }
        }
    }
}

private fun DrawScope.drawGem(
    type: PieceType,
    special: SpecialPiece,
    center: Offset,
    radius: Float,
    rotation: Float
) {
    if (special == SpecialPiece.COLOR_BOMB) {
        // Swirling Rainbow Celestial Orb
        rotate(rotation, center) {
            val rainbowColors = listOf(
                Color(0xFFFF0055),
                Color(0xFFFF9900),
                Color(0xFFFFFF00),
                Color(0xFF00FF66),
                Color(0xFF00CCFF),
                Color(0xFF9900FF),
                Color(0xFFFF0055)
            )
            drawCircle(
                brush = Brush.sweepGradient(rainbowColors, center),
                radius = radius,
                center = center
            )
            // Inner Core
            drawCircle(
                color = Color.White,
                radius = radius * 0.45f,
                center = center
            )
            drawCircle(
                color = Color(0xFFFFD700),
                radius = radius * 0.25f,
                center = center
            )
        }
        return
    }

    val baseColor = Color(type.hexColor)
    val lightColor = baseColor.copy(alpha = 0.9f)
    val darkColor = Color(
        red = (baseColor.red * 0.45f),
        green = (baseColor.green * 0.45f),
        blue = (baseColor.blue * 0.45f)
    )

    when (type) {
        PieceType.RUBY -> {
            // Diamond polygon with inner facets
            val path = Path().apply {
                moveTo(center.x, center.y - radius) // Top
                lineTo(center.x + radius, center.y) // Right
                lineTo(center.x, center.y + radius) // Bottom
                lineTo(center.x - radius, center.y) // Left
                close()
            }
            drawPath(
                path = path,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFFF8A80), baseColor, darkColor),
                    start = Offset(center.x - radius, center.y - radius),
                    end = Offset(center.x + radius, center.y + radius)
                )
            )
            // Specular Glint
            drawPath(
                path = Path().apply {
                    moveTo(center.x, center.y - radius)
                    lineTo(center.x + radius * 0.4f, center.y)
                    lineTo(center.x, center.y - radius * 0.1f)
                    lineTo(center.x - radius * 0.4f, center.y)
                    close()
                },
                color = Color(0x80FFFFFF)
            )
        }

        PieceType.SAPPHIRE -> {
            // Hexagon with jewel cut
            val path = Path()
            for (i in 0 until 6) {
                val angle = (i * 60.0 - 30.0) * PI / 180.0
                val px = center.x + radius * cos(angle).toFloat()
                val py = center.y + radius * sin(angle).toFloat()
                if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            path.close()
            drawPath(
                path = path,
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF82B1FF), baseColor, darkColor),
                    center = Offset(center.x - radius * 0.3f, center.y - radius * 0.3f),
                    radius = radius * 1.2f
                )
            )
            // Center inner table facet
            drawCircle(
                color = Color(0x55FFFFFF),
                radius = radius * 0.35f,
                center = center
            )
        }

        PieceType.EMERALD -> {
            // Emerald Rectangular Octagon cut
            val path = Path().apply {
                val bevel = radius * 0.35f
                moveTo(center.x - radius + bevel, center.y - radius)
                lineTo(center.x + radius - bevel, center.y - radius)
                lineTo(center.x + radius, center.y - radius + bevel)
                lineTo(center.x + radius, center.y + radius - bevel)
                lineTo(center.x + radius - bevel, center.y + radius)
                lineTo(center.x - radius + bevel, center.y + radius)
                lineTo(center.x - radius, center.y + radius - bevel)
                lineTo(center.x - radius, center.y - radius + bevel)
                close()
            }
            drawPath(
                path = path,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFB9F6CA), baseColor, darkColor),
                    start = Offset(center.x, center.y - radius),
                    end = Offset(center.x, center.y + radius)
                )
            )
            drawPath(path = path, color = Color(0x66FFFFFF), style = Stroke(width = 2f))
        }

        PieceType.TOPAZ -> {
            // Brilliant 5-point Star
            val path = Path()
            val points = 5
            val innerRadius = radius * 0.45f
            for (i in 0 until points * 2) {
                val r = if (i % 2 == 0) radius else innerRadius
                val angle = (i * 36.0 - 90.0) * PI / 180.0
                val px = center.x + r * cos(angle).toFloat()
                val py = center.y + r * sin(angle).toFloat()
                if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            path.close()
            drawPath(
                path = path,
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFF9C4), baseColor, Color(0xFFF57F17)),
                    center = center,
                    radius = radius
                )
            )
        }

        PieceType.AMETHYST -> {
            // Heart or Cabochon drop
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFEA80FC), baseColor, darkColor),
                    center = Offset(center.x - radius * 0.25f, center.y - radius * 0.3f),
                    radius = radius * 1.1f
                ),
                radius = radius,
                center = center
            )
            // Highlight specular dot
            drawCircle(
                color = Color.White.copy(alpha = 0.75f),
                radius = radius * 0.22f,
                center = Offset(center.x - radius * 0.32f, center.y - radius * 0.32f)
            )
        }

        PieceType.SUN_CRYSTAL -> {
            // Fiery radiant sun crystal
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFE082), Color(0xFFFF6D00), Color(0xFFBF360C)),
                    center = center,
                    radius = radius
                ),
                radius = radius * 0.9f,
                center = center
            )
            // Rays
            for (i in 0 until 8) {
                val angle = (i * 45.0) * PI / 180.0
                val rayStart = Offset(
                    center.x + (radius * 0.9f) * cos(angle).toFloat(),
                    center.y + (radius * 0.9f) * sin(angle).toFloat()
                )
                val rayEnd = Offset(
                    center.x + (radius * 1.15f) * cos(angle).toFloat(),
                    center.y + (radius * 1.15f) * sin(angle).toFloat()
                )
                drawLine(
                    color = Color(0xFFFFD54F),
                    start = rayStart,
                    end = rayEnd,
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
            }
        }

        PieceType.MOON_CRYSTAL -> {
            // Glowing cyan celestial crescent
            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFE0F7FA), Color(0xFF00E5FF), Color(0xFF006064)),
                    start = Offset(center.x - radius, center.y - radius),
                    end = Offset(center.x + radius, center.y + radius)
                ),
                radius = radius,
                center = center
            )
            // Cutout circle for crescent moon feel
            drawCircle(
                color = Color(0x33000000),
                radius = radius * 0.8f,
                center = Offset(center.x + radius * 0.35f, center.y - radius * 0.25f)
            )
        }
    }

    // Special Piece Overlays
    when (special) {
        SpecialPiece.LINE_HORIZONTAL -> {
            // Horizontal laser arrows
            drawLine(
                color = Color.White,
                start = Offset(center.x - radius * 0.9f, center.y),
                end = Offset(center.x + radius * 0.9f, center.y),
                strokeWidth = 4f,
                cap = StrokeCap.Round
            )
            drawCircle(color = Color.White, radius = 5f, center = center)
        }
        SpecialPiece.LINE_VERTICAL -> {
            // Vertical laser arrows
            drawLine(
                color = Color.White,
                start = Offset(center.x, center.y - radius * 0.9f),
                end = Offset(center.x, center.y + radius * 0.9f),
                strokeWidth = 4f,
                cap = StrokeCap.Round
            )
            drawCircle(color = Color.White, radius = 5f, center = center)
        }
        SpecialPiece.AREA_BOMB -> {
            // Explosive mana core with pulsing ring
            drawCircle(
                color = Color.White,
                radius = radius * 0.35f,
                center = center
            )
            drawCircle(
                color = Color(0xFFFF3D00),
                radius = radius * 0.75f,
                center = center,
                style = Stroke(width = 3.5f)
            )
        }
        SpecialPiece.CROSS_BLAST -> {
            drawLine(
                color = Color.White,
                start = Offset(center.x - radius * 0.9f, center.y),
                end = Offset(center.x + radius * 0.9f, center.y),
                strokeWidth = 3.5f
            )
            drawLine(
                color = Color.White,
                start = Offset(center.x, center.y - radius * 0.9f),
                end = Offset(center.x, center.y + radius * 0.9f),
                strokeWidth = 3.5f
            )
        }
        else -> {}
    }
}

private fun DrawScope.drawIceBlocker(left: Float, top: Float, width: Float, height: Float, hp: Int) {
    val alpha = when (hp) {
        3 -> 0.85f
        2 -> 0.65f
        else -> 0.45f
    }
    // Frosted glacier rectangle
    drawRoundRect(
        color = Color(0xFFB3E5FC).copy(alpha = alpha),
        topLeft = Offset(left, top),
        size = Size(width, height),
        cornerRadius = CornerRadius(10f, 10f)
    )
    drawRoundRect(
        color = Color.White.copy(alpha = 0.8f),
        topLeft = Offset(left, top),
        size = Size(width, height),
        cornerRadius = CornerRadius(10f, 10f),
        style = Stroke(width = 2.5f)
    )
    // Frost crack lines
    if (hp <= 2) {
        drawLine(
            color = Color.White,
            start = Offset(left + width * 0.2f, top + height * 0.3f),
            end = Offset(left + width * 0.8f, top + height * 0.7f),
            strokeWidth = 2f
        )
    }
    if (hp == 1) {
        drawLine(
            color = Color.White,
            start = Offset(left + width * 0.7f, top + height * 0.2f),
            end = Offset(left + width * 0.3f, top + height * 0.8f),
            strokeWidth = 2f
        )
    }
}

private fun DrawScope.drawStoneBlocker(left: Float, top: Float, width: Float, height: Float) {
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFF78909C), Color(0xFF455A64), Color(0xFF263238)),
            start = Offset(left, top),
            end = Offset(left + width, top + height)
        ),
        topLeft = Offset(left, top),
        size = Size(width, height),
        cornerRadius = CornerRadius(10f, 10f)
    )
    drawRoundRect(
        color = Color(0xFF90A4AE),
        topLeft = Offset(left, top),
        size = Size(width, height),
        cornerRadius = CornerRadius(10f, 10f),
        style = Stroke(width = 2f)
    )
}

private fun DrawScope.drawLockChains(left: Float, top: Float, width: Float, height: Float) {
    val cx = left + width / 2f
    val cy = top + height / 2f

    // Diagonal chains
    drawLine(
        color = Color(0xFFFFD700),
        start = Offset(left + 6f, top + 6f),
        end = Offset(left + width - 6f, top + height - 6f),
        strokeWidth = 3.5f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color(0xFFFFD700),
        start = Offset(left + width - 6f, top + 6f),
        end = Offset(left + 6f, top + height - 6f),
        strokeWidth = 3.5f,
        cap = StrokeCap.Round
    )
    // Center padlock
    drawCircle(
        color = Color(0xFFFFA000),
        radius = min(width, height) * 0.2f,
        center = Offset(cx, cy)
    )
    drawCircle(
        color = Color(0xFF3E2723),
        radius = min(width, height) * 0.08f,
        center = Offset(cx, cy)
    )
}
