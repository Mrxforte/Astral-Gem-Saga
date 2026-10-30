package com.example.game.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.game.model.GameWorld
import com.example.game.model.WorldsCatalog
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class BackgroundParticle(
    val id: Int,
    var xPercent: Float,
    var yPercent: Float,
    val speedY: Float,
    val speedX: Float,
    val size: Float,
    val colorHex: Long,
    val opacity: Float,
    val rotationSpeed: Float = 0f,
    var rotation: Float = 0f
)

@Composable
fun AnimatedWorldBackground(
    worldId: Int = 1,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    val world = remember(worldId) { WorldsCatalog.getWorldForLevel((worldId - 1) * 20 + 1) }

    val infiniteTransition = rememberInfiniteTransition(label = "bg_anim")

    // Slow ambient cycle (10 seconds)
    val timeProg by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bg_time"
    )

    // Secondary undulating wave (4 seconds)
    val waveProg by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bg_wave"
    )

    // Pre-allocated particle pool for this world (35 particles max to guarantee 60-120 FPS!)
    val particles = remember(worldId) {
        val list = mutableListOf<BackgroundParticle>()
        val rnd = java.util.Random(worldId.toLong() * 101L)
        val particleColors = when (worldId) {
            1 -> listOf(0xFFBA68C8, 0xFFE1BEE7, 0xFF00E5FF, 0xFFFFFFFF) // Crystals
            2 -> listOf(0xFF81C784, 0xFFFFF176, 0xFF66BB6A, 0xFFE8F5E9) // Leaves & Fireflies
            3 -> listOf(0xFF81D4FA, 0xFFE1F5FE, 0xFFFFFFFF, 0xFFB3E5FC) // Clouds & Feathers
            4 -> listOf(0xFFFF5722, 0xFFFFAB91, 0xFFFFD54F, 0xFFFF1744) // Embers & Smoke
            5 -> listOf(0xFFFFFFFF, 0xFFB2EBF2, 0xFF80DEEA, 0xFFE0F7FA) // Snowflakes
            6 -> listOf(0xFF80CBC4, 0xFF4DD0E1, 0xFFE0F2F1, 0xFF26A69A) // Sea bubbles
            7 -> listOf(0xFFFFB74D, 0xFFFFE082, 0xFFFFCC80, 0xFFFFF8E1) // Runes & Dust
            8 -> listOf(0xFFB388FF, 0xFFD1C4E9, 0xFFFFFFFF, 0xFF7C4DFF) // Moonlight
            9 -> listOf(0xFFFF1744, 0xFFFF9100, 0xFFFF5252, 0xFFFFD700) // Dragon motes
            else -> listOf(0xFF8C9EFF, 0xFFFF80AB, 0xFF84FFFF, 0xFFFFFFFF) // Celestial stardust
        }

        for (i in 0 until 35) {
            val speedY = when (worldId) {
                4 -> -0.06f - (rnd.nextFloat() * 0.08f) // embers rise
                6 -> -0.05f - (rnd.nextFloat() * 0.06f) // bubbles rise
                5 -> 0.05f + (rnd.nextFloat() * 0.07f) // snowflakes fall
                2 -> if (rnd.nextBoolean()) -0.04f else 0.04f // fireflies/leaves
                else -> -0.03f + (rnd.nextFloat() * 0.06f)
            }
            val speedX = -0.03f + (rnd.nextFloat() * 0.06f)

            list.add(
                BackgroundParticle(
                    id = i,
                    xPercent = rnd.nextFloat(),
                    yPercent = rnd.nextFloat(),
                    speedY = speedY,
                    speedX = speedX,
                    size = 4f + rnd.nextFloat() * 10f,
                    colorHex = particleColors[i % particleColors.size],
                    opacity = 0.25f + rnd.nextFloat() * 0.55f,
                    rotationSpeed = -40f + rnd.nextFloat() * 80f
                )
            )
        }
        list
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Far Background Sky Gradient
            drawFarBackground(world, w, h)

            // 2. World Specific Landscapes (Mountains, Trees, Volcano, Temple Pillars)
            drawWorldScenery(worldId, w, h, waveProg)

            // 3. Environmental Atmosphere Effects (Aurora, Sunlight, Heat, Underwater caustics)
            drawWorldAtmosphere(worldId, w, h, waveProg, timeProg)

            // 4. Floating Animated Particles
            for (p in particles) {
                // Update position smoothly over time
                val currentY = (p.yPercent + p.speedY * timeProg) % 1.0f
                val py = if (currentY < 0f) currentY + 1.0f else currentY
                val currentX = (p.xPercent + p.speedX * timeProg + 0.03f * sin((timeProg * 2.0 * PI + p.id).toDouble()).toFloat()) % 1.0f
                val px = if (currentX < 0f) currentX + 1.0f else currentX

                val cx = px * w
                val cy = py * h
                val rot = (p.rotationSpeed * timeProg * 360f) % 360f

                drawParticleShape(worldId, cx, cy, p.size, Color(p.colorHex).copy(alpha = p.opacity), rot)
            }
        }

        // Overlay screen content on top of living background
        content()
    }
}

private fun DrawScope.drawFarBackground(world: GameWorld, w: Float, h: Float) {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(world.themeColorStart),
                Color(world.themeColorEnd).copy(alpha = 0.9f),
                Color(0xFFE8F7FB)
            ),
            startY = 0f,
            endY = h
        )
    )

    // Flappy Bird style cartoon fluffy clouds drifting in sky
    val cloudColor = Color.White.copy(alpha = 0.8f)
    drawOval(cloudColor, topLeft = Offset(w * 0.08f, h * 0.10f), size = Size(130f, 44f))
    drawCircle(cloudColor, radius = 28f, center = Offset(w * 0.08f + 48f, h * 0.10f + 14f))
    drawCircle(cloudColor, radius = 22f, center = Offset(w * 0.08f + 88f, h * 0.10f + 18f))

    drawOval(cloudColor, topLeft = Offset(w * 0.60f, h * 0.06f), size = Size(160f, 50f))
    drawCircle(cloudColor, radius = 34f, center = Offset(w * 0.60f + 56f, h * 0.06f + 16f))
    drawCircle(cloudColor, radius = 26f, center = Offset(w * 0.60f + 108f, h * 0.06f + 20f))

    // Distant cheerful green rolling hills (iconic Flappy Bird aesthetic)
    val hillPath = Path().apply {
        moveTo(0f, h)
        lineTo(0f, h * 0.90f)
        cubicTo(w * 0.25f, h * 0.85f, w * 0.5f, h * 0.92f, w * 0.75f, h * 0.87f)
        cubicTo(w * 0.9f, h * 0.84f, w * 0.98f, h * 0.88f, w, h * 0.87f)
        lineTo(w, h)
        close()
    }
    drawPath(hillPath, color = Color(0xFF70C635).copy(alpha = 0.35f))
}

private fun DrawScope.drawWorldScenery(worldId: Int, w: Float, h: Float, wave: Float) {
    when (worldId) {
        1 -> {
            // Crystal Valley: Gentle pastel crystal peaks in distance
            val mountainPath = Path().apply {
                moveTo(0f, h)
                lineTo(0f, h * 0.78f)
                lineTo(w * 0.25f, h * 0.68f) // spire 1
                lineTo(w * 0.45f, h * 0.80f)
                lineTo(w * 0.7f, h * 0.64f)  // spire 2
                lineTo(w * 0.88f, h * 0.75f)
                lineTo(w, h * 0.70f)
                lineTo(w, h)
                close()
            }
            drawPath(mountainPath, color = Color(0xFF81D4FA).copy(alpha = 0.45f))

            // Glowing sunbeam crystal
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFF9C4).copy(alpha = 0.4f), Color.Transparent),
                    center = Offset(w * 0.7f, h * 0.64f),
                    radius = 90f
                ),
                radius = 90f,
                center = Offset(w * 0.7f, h * 0.64f)
            )
        }

        2 -> {
            // Mystic Forest: Cheerful green tree canopy silhouette
            val canopyPath = Path().apply {
                moveTo(0f, h)
                lineTo(0f, h * 0.75f)
                cubicTo(w * 0.2f, h * 0.65f, w * 0.4f, h * 0.82f, w * 0.6f, h * 0.70f)
                cubicTo(w * 0.8f, h * 0.62f, w * 0.95f, h * 0.76f, w, h * 0.72f)
                lineTo(w, h)
                close()
            }
            drawPath(canopyPath, color = Color(0xFF55A822).copy(alpha = 0.45f))
        }

        3 -> {
            // Sky Islands: Levitating sunny islands bobbing in the clouds
            val bobY = wave * 12f
            drawOval(
                color = Color(0xFF4FC3F7).copy(alpha = 0.45f),
                topLeft = Offset(w * 0.1f, h * 0.35f + bobY),
                size = Size(w * 0.4f, 36f)
            )
            drawOval(
                color = Color(0xFF4FC3F7).copy(alpha = 0.45f),
                topLeft = Offset(w * 0.55f, h * 0.5f - bobY),
                size = Size(w * 0.38f, 32f)
            )
        }

        4 -> {
            // Volcanic Kingdom: Glowing jagged basalt crags and molten lava river
            val cragPath = Path().apply {
                moveTo(0f, h)
                lineTo(0f, h * 0.78f)
                lineTo(w * 0.3f, h * 0.62f)
                lineTo(w * 0.55f, h * 0.8f)
                lineTo(w * 0.8f, h * 0.65f)
                lineTo(w, h * 0.75f)
                lineTo(w, h)
                close()
            }
            drawPath(cragPath, color = Color(0xFF2E0909).copy(alpha = 0.75f))

            // Lava glow line
            drawLine(
                brush = Brush.horizontalGradient(listOf(Color(0xFFFF3D00), Color(0xFFFFD600), Color(0xFFFF3D00))),
                start = Offset(0f, h * 0.85f),
                end = Offset(w, h * 0.85f),
                strokeWidth = 6f
            )
        }

        5 -> {
            // Frozen Realm: Glacial jagged icebergs
            val icebergPath = Path().apply {
                moveTo(0f, h)
                lineTo(0f, h * 0.7f)
                lineTo(w * 0.35f, h * 0.5f)
                lineTo(w * 0.6f, h * 0.72f)
                lineTo(w * 0.85f, h * 0.48f)
                lineTo(w, h * 0.66f)
                lineTo(w, h)
                close()
            }
            drawPath(icebergPath, color = Color(0xFF052B38).copy(alpha = 0.7f))
        }

        6 -> {
            // Ocean Temple: Submerged temple pediments & coral fans
            drawRect(
                color = Color(0xFF00363A).copy(alpha = 0.4f),
                topLeft = Offset(w * 0.15f, h * 0.65f),
                size = Size(40f, h * 0.35f)
            )
            drawRect(
                color = Color(0xFF00363A).copy(alpha = 0.4f),
                topLeft = Offset(w * 0.75f, h * 0.6f),
                size = Size(40f, h * 0.4f)
            )
        }

        7 -> {
            // Ancient Ruins: Sun temple pillars and obelisks
            drawRect(color = Color(0xFF3E1E04).copy(alpha = 0.6f), topLeft = Offset(w * 0.12f, h * 0.55f), size = Size(35f, h * 0.45f))
            drawRect(color = Color(0xFF3E1E04).copy(alpha = 0.6f), topLeft = Offset(w * 0.82f, h * 0.55f), size = Size(35f, h * 0.45f))
        }

        8 -> {
            // Moon Kingdom: Giant glowing crescent moon
            val moonCenter = Offset(w * 0.75f, h * 0.22f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFEDE7F6), Color(0xFFB39DDB), Color.Transparent),
                    center = moonCenter,
                    radius = 90f
                ),
                radius = 90f,
                center = moonCenter
            )
        }

        9 -> {
            // Dragon Lands: Silhouette of a distant soaring dragon
            val dragonX = ((wave * w * 1.2f) % w)
            val dragonY = h * 0.25f + sin((wave * 2.0 * PI).toDouble()).toFloat() * 18f
            drawCircle(color = Color(0xFF1B0515), radius = 6f, center = Offset(dragonX, dragonY))
            // Wings
            drawLine(
                color = Color(0xFF1B0515),
                start = Offset(dragonX - 18f, dragonY - 10f),
                end = Offset(dragonX + 18f, dragonY - 10f),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
        }

        10 -> {
            // Celestial Realm: Swirling cosmic vortex / galaxy spiral
            val center = Offset(w * 0.5f, h * 0.3f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFE040FB).copy(alpha = 0.35f), Color(0xFF00E5FF).copy(alpha = 0.15f), Color.Transparent),
                    center = center,
                    radius = 180f
                ),
                radius = 180f,
                center = center
            )
        }
    }
}

private fun DrawScope.drawWorldAtmosphere(worldId: Int, w: Float, h: Float, wave: Float, time: Float) {
    when (worldId) {
        5 -> {
            // Aurora Borealis waving band
            val auroraPath = Path().apply {
                moveTo(0f, h * 0.18f + wave * 25f)
                cubicTo(w * 0.3f, h * 0.12f - wave * 20f, w * 0.7f, h * 0.24f + wave * 20f, w, h * 0.15f - wave * 25f)
                lineTo(w, h * 0.32f)
                cubicTo(w * 0.7f, h * 0.38f, w * 0.3f, h * 0.28f, 0f, h * 0.35f)
                close()
            }
            drawPath(
                path = auroraPath,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0x3300E676), Color(0x4400E5FF), Color(0x33D500F9)),
                    start = Offset(0f, 0f),
                    end = Offset(w, h * 0.3f)
                )
            )
        }
        6 -> {
            // Submerged water sunbeams
            val beamPath = Path().apply {
                moveTo(w * 0.2f, 0f)
                lineTo(w * 0.45f, 0f)
                lineTo(w * 0.7f, h)
                lineTo(w * 0.3f, h)
                close()
            }
            drawPath(beamPath, color = Color(0x1580DEEA))
        }
        else -> {}
    }
}

private fun DrawScope.drawParticleShape(
    worldId: Int,
    x: Float,
    y: Float,
    size: Float,
    color: Color,
    rotation: Float
) {
    when (worldId) {
        1 -> {
            // Diamond crystal shard
            rotate(rotation, Offset(x, y)) {
                val path = Path().apply {
                    moveTo(x, y - size)
                    lineTo(x + size * 0.6f, y)
                    lineTo(x, y + size)
                    lineTo(x - size * 0.6f, y)
                    close()
                }
                drawPath(path, color)
            }
        }
        5 -> {
            // Snowflake star
            drawLine(color, Offset(x - size, y), Offset(x + size, y), strokeWidth = 1.5f)
            drawLine(color, Offset(x, y - size), Offset(x, y + size), strokeWidth = 1.5f)
        }
        else -> {
            // Glowing round particle / orb
            drawCircle(color, radius = size * 0.6f, center = Offset(x, y))
        }
    }
}
