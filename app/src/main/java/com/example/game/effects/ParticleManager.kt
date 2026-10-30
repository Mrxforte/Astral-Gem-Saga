package com.example.game.effects

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import java.util.Random
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object ParticleManager {

    private const val MAX_PARTICLES = 400
    private val pool = Array(MAX_PARTICLES) { i ->
        MutableParticle(id = i.toLong())
    }
    private val rng = Random()

    // Particle Types:
    // 0: Glowing Orb / Stardust
    // 1: 5-Pointed Spinning Star (Yulduz)
    // 2: 4-Pointed Radiant Sparkle (Chaqnash / Olmos yulduz)
    // 3: Expanding Flash Shockwave Ring (Chaqnash halqasi)
    // 4: Central Radial Light Flash / Ray (Nur chaqnashi)
    // 5: Gem Shard
    data class MutableParticle(
        var id: Long = 0L,
        var active: Boolean = false,
        var x: Float = 0f,
        var y: Float = 0f,
        var vx: Float = 0f,
        var vy: Float = 0f,
        var colorHex: Long = 0xFFFFD700,
        var life: Float = 0f,
        var maxLife: Float = 1.0f,
        var decayRate: Float = 0.04f,
        var size: Float = 14f,
        var angle: Float = 0f,
        var rotSpeed: Float = 0f,
        var shapeType: Int = 1,
        var currentRadius: Float = 0f,
        var maxRadius: Float = 60f
    )

    private fun acquireParticle(): MutableParticle? {
        for (p in pool) {
            if (!p.active) return p
        }
        // If pool is full, reuse the oldest (lowest life)
        var oldest: MutableParticle? = null
        var lowestLife = 2f
        for (p in pool) {
            if (p.life < lowestLife) {
                lowestLife = p.life
                oldest = p
            }
        }
        return oldest
    }

    /**
     * Spawns a high-energy match burst with 5-pointed spinning stars (yulduzlar),
     * diamond sparkles, and expanding flash shockwave rings (chaqnashlar).
     */
    fun spawnMatchBurst(
        centerX: Float,
        centerY: Float,
        colorHex: Long,
        count: Int = 24,
        isSpecial: Boolean = false,
        combo: Int = 1
    ) {
        val brightColors = listOf(
            colorHex,
            0xFFFFD700, // Vibrant Gold Star
            0xFFFFF59D, // Warm Sunlight
            0xFFFFFFFF, // Pure Flash White
            0xFFFF80AB, // Neon Pink
            0xFF80D8FF  // Electric Cyan
        )

        // 1. Spawning Flash Shockwave Ring (Chaqnash halqasi)
        acquireParticle()?.apply {
            active = true
            x = centerX
            y = centerY
            vx = 0f
            vy = 0f
            this.colorHex = 0xFFFFFFFF
            life = 1.0f
            maxLife = 1.0f
            decayRate = 0.07f
            shapeType = 3 // Flash Ring
            currentRadius = 6f
            maxRadius = if (isSpecial) 90f else (55f + combo * 8f)
            size = if (isSpecial) 6f else 4f
        }

        // Secondary colored shockwave ring for big combos
        if (combo >= 2 || isSpecial) {
            acquireParticle()?.apply {
                active = true
                x = centerX
                y = centerY
                vx = 0f
                vy = 0f
                this.colorHex = colorHex
                life = 1.0f
                maxLife = 1.0f
                decayRate = 0.05f
                shapeType = 3 // Flash Ring
                currentRadius = 2f
                maxRadius = if (isSpecial) 110f else 75f
                size = 3.5f
            }
        }

        // 2. Central Blooming Flash (Nur chaqnashi)
        acquireParticle()?.apply {
            active = true
            x = centerX
            y = centerY
            vx = 0f
            vy = 0f
            this.colorHex = 0xFFFFFFFF
            life = 1.0f
            maxLife = 1.0f
            decayRate = 0.10f // Quick 0.15s flash
            shapeType = 4 // Radial Flash
            size = if (isSpecial) 48f else 32f
        }

        // 3. Multi-ray flash spikes (Chaqnash nurlari)
        val numRays = if (isSpecial) 8 else 4
        for (i in 0 until numRays) {
            acquireParticle()?.apply {
                active = true
                x = centerX
                y = centerY
                val rayAngle = (i * (2.0 * Math.PI / numRays) + rng.nextFloat() * 0.3).toFloat()
                val speed = 90f + rng.nextFloat() * 120f
                vx = cos(rayAngle) * speed
                vy = sin(rayAngle) * speed
                this.colorHex = 0xFFFFFFFF
                life = 1.0f
                maxLife = 1.0f
                decayRate = 0.08f
                shapeType = 2 // 4-pointed radiant sparkle ray
                size = 18f + rng.nextFloat() * 12f
                angle = rayAngle
                rotSpeed = 4f
            }
        }

        // 4. Sparkling 5-point Stars (Yulduzlar)
        val starCount = if (isSpecial) (count + 12) else count
        for (i in 0 until starCount) {
            val p = acquireParticle() ?: break
            p.active = true
            p.x = centerX + (rng.nextFloat() - 0.5f) * 12f
            p.y = centerY + (rng.nextFloat() - 0.5f) * 12f

            val angleRad = rng.nextDouble() * 2.0 * Math.PI
            val speed = if (isSpecial) (130f + rng.nextFloat() * 220f) else (70f + rng.nextFloat() * 150f)
            p.vx = (cos(angleRad) * speed).toFloat()
            p.vy = (sin(angleRad) * speed).toFloat()

            // Pick vibrant cheerful star colors
            p.colorHex = if (rng.nextBoolean()) 0xFFFFD700 else brightColors[rng.nextInt(brightColors.size)]
            p.life = 1.0f
            p.maxLife = 1.0f
            p.decayRate = 0.035f + rng.nextFloat() * 0.025f
            p.size = if (isSpecial) (14f + rng.nextFloat() * 12f) else (9f + rng.nextFloat() * 9f)

            // 70% 5-point stars, 30% 4-point diamond sparkles
            p.shapeType = if (rng.nextFloat() < 0.65f) 1 else 2
            p.angle = (rng.nextFloat() * 2.0 * Math.PI).toFloat()
            p.rotSpeed = (rng.nextFloat() - 0.5f) * 14f // Fast playful spin
        }

        // 5. Floating Stardust Motes
        for (i in 0 until 8) {
            val p = acquireParticle() ?: break
            p.active = true
            p.x = centerX + (rng.nextFloat() - 0.5f) * 24f
            p.y = centerY + (rng.nextFloat() - 0.5f) * 24f
            val angleRad = rng.nextDouble() * 2.0 * Math.PI
            val speed = 25f + rng.nextFloat() * 45f
            p.vx = (cos(angleRad) * speed).toFloat()
            p.vy = (sin(angleRad) * speed).toFloat() - 20f // float upward gently
            p.colorHex = 0xFFFFF9C4 // soft warm stardust
            p.life = 1.0f
            p.maxLife = 1.0f
            p.decayRate = 0.025f
            p.size = 5f + rng.nextFloat() * 4f
            p.shapeType = 0 // Orb
        }
    }

    /**
     * Backward-compatible helper for gem bursts.
     */
    fun spawnGemBurst(
        centerX: Float,
        centerY: Float,
        colorHex: Long,
        count: Int = 20,
        isSpecial: Boolean = false
    ) {
        spawnMatchBurst(
            centerX = centerX,
            centerY = centerY,
            colorHex = colorHex,
            count = count,
            isSpecial = isSpecial,
            combo = if (isSpecial) 2 else 1
        )
    }

    /**
     * Huge Supernova event: Massive star showers, rainbow flash rings, and cosmic sparks.
     */
    fun spawnSupernovaBurst(centerX: Float, centerY: Float) {
        val rainbow = listOf(
            0xFFFF1744, 0xFFFF9100, 0xFFFFEA00, 0xFF00E676,
            0xFF00E5FF, 0xFFD500F9, 0xFFFFFFFF, 0xFFFFD700
        )

        // Multiple giant expanding flash waves (Chaqnash to'lqinlari)
        for (ring in 0 until 3) {
            acquireParticle()?.apply {
                active = true
                x = centerX
                y = centerY
                vx = 0f
                vy = 0f
                colorHex = if (ring == 0) 0xFFFFFFFF else 0xFFFFD700
                life = 1.0f
                maxLife = 1.0f
                decayRate = 0.04f + ring * 0.015f
                shapeType = 3
                currentRadius = 10f * ring
                maxRadius = 180f + ring * 50f
                size = 6f
            }
        }

        // 60+ swirling colorful stars
        for (i in 0 until 70) {
            val p = acquireParticle() ?: break
            p.active = true
            p.x = centerX
            p.y = centerY
            val angle = rng.nextDouble() * 2.0 * Math.PI
            val speed = 140f + rng.nextFloat() * 320f
            p.vx = (cos(angle) * speed).toFloat()
            p.vy = (sin(angle) * speed).toFloat()
            p.colorHex = rainbow[rng.nextInt(rainbow.size)]
            p.life = 1.0f
            p.maxLife = 1.0f
            p.decayRate = 0.025f + rng.nextFloat() * 0.02f
            p.size = 14f + rng.nextFloat() * 16f
            p.shapeType = if (rng.nextBoolean()) 1 else 2 // 5-point stars and sparkles
            p.angle = (rng.nextFloat() * 2 * PI).toFloat()
            p.rotSpeed = (rng.nextFloat() - 0.5f) * 18f
        }
    }

    /**
     * Bomb explosion: Fiery expanding shockwave, radiant flash, and bursting golden stars.
     */
    fun spawnBombExplosion(centerX: Float, centerY: Float, colorHex: Long = 0xFFFF5722) {
        // Flash shockwave
        acquireParticle()?.apply {
            active = true
            x = centerX
            y = centerY
            this.colorHex = 0xFFFFD700
            life = 1.0f
            maxLife = 1.0f
            decayRate = 0.05f
            shapeType = 3
            currentRadius = 8f
            maxRadius = 130f
            size = 7f
        }
        // Center flash
        acquireParticle()?.apply {
            active = true
            x = centerX
            y = centerY
            this.colorHex = 0xFFFFFFFF
            life = 1.0f
            maxLife = 1.0f
            decayRate = 0.09f
            shapeType = 4
            size = 60f
        }
        // Explosive stars
        for (i in 0 until 35) {
            val p = acquireParticle() ?: break
            p.active = true
            p.x = centerX
            p.y = centerY
            val angle = rng.nextDouble() * 2.0 * Math.PI
            val speed = 120f + rng.nextFloat() * 240f
            p.vx = (cos(angle) * speed).toFloat()
            p.vy = (sin(angle) * speed).toFloat()
            p.colorHex = if (rng.nextBoolean()) 0xFFFFD700 else colorHex
            p.life = 1.0f
            p.decayRate = 0.04f + rng.nextFloat() * 0.025f
            p.size = 12f + rng.nextFloat() * 12f
            p.shapeType = 1 // 5-point star
            p.rotSpeed = (rng.nextFloat() - 0.5f) * 16f
        }
    }

    /**
     * Laser line beam sparkles along an entire row or column.
     */
    fun spawnLineBlastSparkles(
        startX: Float, startY: Float,
        endX: Float, endY: Float,
        colorHex: Long = 0xFF00E5FF
    ) {
        val steps = 12
        for (i in 0..steps) {
            val t = i.toFloat() / steps
            val px = startX + (endX - startX) * t
            val py = startY + (endY - startY) * t
            acquireParticle()?.apply {
                active = true
                x = px + (rng.nextFloat() - 0.5f) * 8f
                y = py + (rng.nextFloat() - 0.5f) * 8f
                vx = (rng.nextFloat() - 0.5f) * 80f
                vy = (rng.nextFloat() - 0.5f) * 80f
                this.colorHex = if (rng.nextBoolean()) 0xFFFFFFFF else colorHex
                life = 1.0f
                decayRate = 0.06f
                size = 12f + rng.nextFloat() * 8f
                shapeType = 2 // Diamond sparkle
                rotSpeed = 8f
            }
        }
    }

    /**
     * Updates physics and draws all active particles onto the Compose Canvas.
     */
    fun updateAndDraw(drawScope: DrawScope, deltaSec: Float = 0.016f) {
        for (p in pool) {
            if (!p.active) continue

            // Update physics
            p.x += p.vx * deltaSec
            p.y += p.vy * deltaSec
            p.angle += p.rotSpeed * deltaSec

            // Gravity affects stars and motes, but not expanding rings or flashes
            if (p.shapeType == 1 || p.shapeType == 0) {
                p.vy += 160f * deltaSec
                p.vx *= 0.985f // air drag
            }

            p.life -= p.decayRate
            if (p.life <= 0f) {
                p.active = false
                continue
            }

            val alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
            val baseColor = Color(p.colorHex)
            val alphaColor = baseColor.copy(alpha = alpha)

            when (p.shapeType) {
                // 1: 5-Pointed Rotating Cartoon Star (Yulduz)
                1 -> {
                    val radius = p.size * (0.4f + 0.6f * alpha)
                    drawStar5(
                        drawScope = drawScope,
                        cx = p.x,
                        cy = p.y,
                        radius = radius,
                        color = alphaColor,
                        rotationRad = p.angle
                    )
                }

                // 2: 4-Pointed Radiant Sparkle (Chaqnash / Olmos)
                2 -> {
                    val size = p.size * (0.3f + 0.7f * alpha)
                    drawSparkle4(
                        drawScope = drawScope,
                        cx = p.x,
                        cy = p.y,
                        size = size,
                        color = alphaColor,
                        rotationRad = p.angle
                    )
                }

                // 3: Expanding Flash Shockwave Ring (Chaqnash halqasi)
                3 -> {
                    val progress = 1.0f - alpha
                    val currentR = p.currentRadius + (p.maxRadius - p.currentRadius) * progress
                    val ringAlpha = (alpha * 1.2f).coerceIn(0f, 1f)
                    drawScope.drawCircle(
                        color = alphaColor.copy(alpha = ringAlpha),
                        radius = currentR,
                        center = Offset(p.x, p.y),
                        style = Stroke(width = (p.size * alpha).coerceAtLeast(1.5f))
                    )
                    // Inner bright glow ring
                    if (progress < 0.6f) {
                        drawScope.drawCircle(
                            color = Color.White.copy(alpha = (alpha * 0.9f).coerceIn(0f, 1f)),
                            radius = currentR * 0.85f,
                            center = Offset(p.x, p.y),
                            style = Stroke(width = 1.5f)
                        )
                    }
                }

                // 4: Central Radial Light Flash / Ray (Nur chaqnashi)
                4 -> {
                    val flashRadius = p.size * (1.2f - alpha * 0.2f)
                    drawScope.drawCircle(
                        color = Color.White.copy(alpha = (alpha * 0.9f).coerceIn(0f, 1f)),
                        radius = flashRadius,
                        center = Offset(p.x, p.y)
                    )
                    // Golden outer corona
                    drawScope.drawCircle(
                        color = Color(0xFFFFD700).copy(alpha = (alpha * 0.5f).coerceIn(0f, 1f)),
                        radius = flashRadius * 1.5f,
                        center = Offset(p.x, p.y)
                    )
                }

                // 0: Soft Glowing Stardust Orb
                else -> {
                    val radius = (p.size * alpha) / 2f
                    drawScope.drawCircle(
                        color = alphaColor,
                        radius = radius,
                        center = Offset(p.x, p.y)
                    )
                    // White shine speck
                    drawScope.drawCircle(
                        color = Color.White.copy(alpha = alpha * 0.7f),
                        radius = (radius * 0.4f).coerceAtLeast(1f),
                        center = Offset(p.x - radius * 0.25f, p.y - radius * 0.25f)
                    )
                }
            }
        }
    }

    /**
     * Draws a crisp 5-pointed cartoon star with inner gleam.
     */
    private fun drawStar5(
        drawScope: DrawScope,
        cx: Float,
        cy: Float,
        radius: Float,
        color: Color,
        rotationRad: Float
    ) {
        val path = Path()
        val innerRadius = radius * 0.42f
        for (i in 0 until 10) {
            val r = if (i % 2 == 0) radius else innerRadius
            val angle = rotationRad + i * (PI.toFloat() / 5f)
            val px = cx + cos(angle) * r
            val py = cy + sin(angle) * r
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        path.close()
        drawScope.drawPath(path, color)

        // Draw cute glossy white highlight in the center
        drawScope.drawCircle(
            color = Color.White.copy(alpha = (color.alpha * 0.85f).coerceIn(0f, 1f)),
            radius = (radius * 0.26f).coerceAtLeast(1f),
            center = Offset(cx - radius * 0.12f, cy - radius * 0.12f)
        )
    }

    /**
     * Draws a 4-pointed diamond sparkle star (✨) with radiant cross spikes.
     */
    private fun drawSparkle4(
        drawScope: DrawScope,
        cx: Float,
        cy: Float,
        size: Float,
        color: Color,
        rotationRad: Float
    ) {
        val path = Path()
        val rLong = size
        val rShort = size * 0.22f
        for (i in 0 until 8) {
            val r = if (i % 2 == 0) rLong else rShort
            val angle = rotationRad + i * (PI.toFloat() / 4f)
            val px = cx + cos(angle) * r
            val py = cy + sin(angle) * r
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        path.close()
        drawScope.drawPath(path, color)

        // Bright white star core
        drawScope.drawCircle(
            color = Color.White.copy(alpha = color.alpha),
            radius = (size * 0.3f).coerceAtLeast(1.5f),
            center = Offset(cx, cy)
        )
    }
}
