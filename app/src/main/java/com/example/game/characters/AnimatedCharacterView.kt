package com.example.game.characters

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.model.CharacterMood
import com.example.game.model.CompanionCatalog
import com.example.game.model.CompanionCharacter
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnimatedCharacterView(
    character: CompanionCharacter = CompanionCatalog.ALL_COMPANIONS.first(),
    mood: CharacterMood = CharacterMood.IDLE,
    speechText: String? = null,
    sizeDp: Int = 110,
    showSpeechBubble: Boolean = true,
    onTap: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "character_motion")

    // Gentle breathing / floating bob
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "char_float"
    )

    // Wand sparkle pulse
    val sparklePulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "char_sparkle"
    )

    // Orbit angle for magic orbs
    val orbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "char_orbit"
    )

    // Blinking timer state
    var isBlinking by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            delay((2800..4500).random().toLong())
            isBlinking = true
            delay(140)
            isBlinking = false
        }
    }

    // Tap jump reaction state
    var isJumped by remember { mutableStateOf(false) }
    val jumpAnim by animateFloatAsState(
        targetValue = if (isJumped) -16f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "char_jump"
    )

    LaunchedEffect(isJumped) {
        if (isJumped) {
            delay(300)
            isJumped = false
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .testTag("animated_character_${character.id}")
            .clickable(enabled = onTap != null) {
                isJumped = true
                onTap?.invoke()
            }
    ) {
        // Speech Bubble if text provided
        if (showSpeechBubble && !speechText.isNullOrBlank()) {
            Surface(
                color = Color(0xFF231846),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(character.secondaryColorHex)),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .padding(bottom = 6.dp)
                    .widthIn(max = 220.dp)
            ) {
                Text(
                    text = speechText,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        // Animated Character Illustration
        Box(
            modifier = Modifier
                .size(sizeDp.dp)
                .offset(y = (floatOffset + jumpAnim).dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCompanionCharacter(
                    character = character,
                    mood = mood,
                    isBlinking = isBlinking,
                    sparklePulse = sparklePulse,
                    orbitAngle = orbitAngle
                )
            }
        }
    }
}

private fun DrawScope.drawCompanionCharacter(
    character: CompanionCharacter,
    mood: CharacterMood,
    isBlinking: Boolean,
    sparklePulse: Float,
    orbitAngle: Float
) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val scale = size.minDimension / 100f

    val primaryColor = Color(character.primaryColorHex)
    val secondaryColor = Color(character.secondaryColorHex)
    val skinColor = Color(0xFFFFDFC4)
    val robeColor = primaryColor
    val hatColor = Color(0xFF281347)

    // 1. Soft magical aura glow around character
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(primaryColor.copy(alpha = 0.35f), Color.Transparent),
            center = Offset(cx, cy),
            radius = 48f * scale
        ),
        radius = 48f * scale,
        center = Offset(cx, cy)
    )

    // 2. Character Robe Body
    val robePath = Path().apply {
        moveTo(cx - 16f * scale, cy + 12f * scale)
        lineTo(cx + 16f * scale, cy + 12f * scale)
        lineTo(cx + 24f * scale, cy + 38f * scale)
        lineTo(cx - 24f * scale, cy + 38f * scale)
        close()
    }
    drawPath(
        path = robePath,
        brush = Brush.linearGradient(
            colors = listOf(robeColor, robeColor.copy(alpha = 0.8f), Color(0xFF140728)),
            start = Offset(cx, cy + 10f * scale),
            end = Offset(cx, cy + 40f * scale)
        )
    )
    // Robe Gold Trim
    drawRoundRect(
        color = secondaryColor,
        topLeft = Offset(cx - 24f * scale, cy + 36f * scale),
        size = Size(48f * scale, 3f * scale),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
    )

    // 3. Round Head / Face
    val headRadius = 18f * scale
    val headCenterY = cy - 2f * scale
    drawCircle(
        color = skinColor,
        radius = headRadius,
        center = Offset(cx, headCenterY)
    )

    // Rosy Cheeks
    drawCircle(
        color = Color(0xFFFF8A80).copy(alpha = 0.5f),
        radius = 3.5f * scale,
        center = Offset(cx - 10f * scale, headCenterY + 4f * scale)
    )
    drawCircle(
        color = Color(0xFFFF8A80).copy(alpha = 0.5f),
        radius = 3.5f * scale,
        center = Offset(cx + 10f * scale, headCenterY + 4f * scale)
    )

    // 4. Eyes (React to mood and blinking)
    val eyeY = headCenterY
    if (isBlinking || mood == CharacterMood.CELEBRATING || mood == CharacterMood.VICTORY) {
        // Joyful happy eye arches `^ ^`
        val leftArch = Path().apply {
            moveTo(cx - 10f * scale, eyeY + 1f * scale)
            quadraticTo(cx - 7f * scale, eyeY - 3f * scale, cx - 4f * scale, eyeY + 1f * scale)
        }
        val rightArch = Path().apply {
            moveTo(cx + 4f * scale, eyeY + 1f * scale)
            quadraticTo(cx + 7f * scale, eyeY - 3f * scale, cx + 10f * scale, eyeY + 1f * scale)
        }
        drawPath(leftArch, color = Color(0xFF1F1235), style = Stroke(width = 2.2f * scale, cap = StrokeCap.Round))
        drawPath(rightArch, color = Color(0xFF1F1235), style = Stroke(width = 2.2f * scale, cap = StrokeCap.Round))
    } else if (mood == CharacterMood.SURPRISED) {
        // Wide circle eyes in amazement
        drawCircle(color = Color(0xFF1F1235), radius = 4f * scale, center = Offset(cx - 7f * scale, eyeY))
        drawCircle(color = Color(0xFF1F1235), radius = 4f * scale, center = Offset(cx + 7f * scale, eyeY))
        drawCircle(color = Color.White, radius = 1.8f * scale, center = Offset(cx - 6f * scale, eyeY - 1f * scale))
        drawCircle(color = Color.White, radius = 1.8f * scale, center = Offset(cx + 8f * scale, eyeY - 1f * scale))
    } else if (mood == CharacterMood.SAD) {
        // Drooping sad eyes
        val leftArch = Path().apply {
            moveTo(cx - 10f * scale, eyeY - 1f * scale)
            quadraticTo(cx - 7f * scale, eyeY + 2f * scale, cx - 4f * scale, eyeY - 1f * scale)
        }
        val rightArch = Path().apply {
            moveTo(cx + 4f * scale, eyeY - 1f * scale)
            quadraticTo(cx + 7f * scale, eyeY + 2f * scale, cx + 10f * scale, eyeY - 1f * scale)
        }
        drawPath(leftArch, color = Color(0xFF1F1235), style = Stroke(width = 2f * scale, cap = StrokeCap.Round))
        drawPath(rightArch, color = Color(0xFF1F1235), style = Stroke(width = 2f * scale, cap = StrokeCap.Round))
    } else {
        // Normal lively eyes with bright catchlights
        drawCircle(color = Color(0xFF1F1235), radius = 3.2f * scale, center = Offset(cx - 7f * scale, eyeY))
        drawCircle(color = Color(0xFF1F1235), radius = 3.2f * scale, center = Offset(cx + 7f * scale, eyeY))
        drawCircle(color = Color.White, radius = 1.2f * scale, center = Offset(cx - 6f * scale, eyeY - 1f * scale))
        drawCircle(color = Color.White, radius = 1.2f * scale, center = Offset(cx + 8f * scale, eyeY - 1f * scale))
    }

    // 5. Mouth
    val mouthY = headCenterY + 8f * scale
    if (mood == CharacterMood.EXCITED || mood == CharacterMood.CELEBRATING || mood == CharacterMood.VICTORY) {
        // Open wide cheerful mouth :D
        drawArc(
            color = Color(0xFFD81B60),
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(cx - 4.5f * scale, mouthY - 2f * scale),
            size = Size(9f * scale, 7f * scale)
        )
    } else if (mood == CharacterMood.SURPRISED) {
        // Small 'O' mouth
        drawCircle(
            color = Color(0xFFD81B60),
            radius = 2.2f * scale,
            center = Offset(cx, mouthY + 1f * scale)
        )
    } else if (mood == CharacterMood.SAD) {
        // Downward curve
        val sadMouth = Path().apply {
            moveTo(cx - 4f * scale, mouthY + 2f * scale)
            quadraticTo(cx, mouthY - 1f * scale, cx + 4f * scale, mouthY + 2f * scale)
        }
        drawPath(sadMouth, color = Color(0xFF880E4F), style = Stroke(width = 1.5f * scale, cap = StrokeCap.Round))
    } else {
        // Warm smile
        val smileMouth = Path().apply {
            moveTo(cx - 4f * scale, mouthY)
            quadraticTo(cx, mouthY + 3.5f * scale, cx + 4f * scale, mouthY)
        }
        drawPath(smileMouth, color = Color(0xFF880E4F), style = Stroke(width = 1.5f * scale, cap = StrokeCap.Round))
    }

    // 6. Sorcerer Hat / Crown
    val hatBrimY = headCenterY - 12f * scale
    // Brim
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(secondaryColor, hatColor),
            center = Offset(cx, hatBrimY),
            radius = 28f * scale
        ),
        topLeft = Offset(cx - 26f * scale, hatBrimY - 4f * scale),
        size = Size(52f * scale, 8f * scale)
    )
    // Pointed Cone
    val hatConePath = Path().apply {
        moveTo(cx - 16f * scale, hatBrimY)
        quadraticTo(cx - 8f * scale, hatBrimY - 24f * scale, cx + 18f * scale, hatBrimY - 34f * scale) // bent tip
        quadraticTo(cx + 8f * scale, hatBrimY - 18f * scale, cx + 16f * scale, hatBrimY)
        close()
    }
    drawPath(
        path = hatConePath,
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFF381557), hatColor, Color(0xFF170929)),
            start = Offset(cx, hatBrimY - 34f * scale),
            end = Offset(cx, hatBrimY)
        )
    )
    // Star emblem on hat tip
    drawCircle(
        color = secondaryColor.copy(alpha = sparklePulse.coerceIn(0.5f, 1f)),
        radius = 4f * scale,
        center = Offset(cx + 18f * scale, hatBrimY - 34f * scale)
    )

    // 7. Floating Crystal Wand / Catalyst in Hand
    val wandX = cx + 24f * scale
    val wandY = cy + 14f * scale
    // Wand Stick
    drawLine(
        color = Color(0xFFBCAAA4),
        start = Offset(wandX - 4f * scale, wandY + 16f * scale),
        end = Offset(wandX + 8f * scale, wandY - 12f * scale),
        strokeWidth = 2.5f * scale,
        cap = StrokeCap.Round
    )
    // Glowing Crystal Gem on Wand Tip
    val crystalTip = Offset(wandX + 8f * scale, wandY - 12f * scale)
    drawCircle(
        color = secondaryColor.copy(alpha = 0.5f),
        radius = 8f * scale * sparklePulse,
        center = crystalTip
    )
    drawCircle(
        color = Color.White,
        radius = 3.5f * scale,
        center = crystalTip
    )

    // 8. Orbiting Sparkles around character
    for (i in 0 until 3) {
        val orbRad = (orbitAngle + i * 120.0) * PI / 180.0
        val ox = cx + 34f * scale * cos(orbRad).toFloat()
        val oy = (cy + 4f * scale) + 16f * scale * sin(orbRad).toFloat()
        drawCircle(
            color = secondaryColor.copy(alpha = 0.85f),
            radius = 2.2f * scale,
            center = Offset(ox, oy)
        )
    }
}
