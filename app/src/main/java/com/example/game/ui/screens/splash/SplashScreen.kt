package com.example.game.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onLoaded: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "splash_gem")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gem_pulse"
    )

    var progress by remember { mutableStateOf(0.1f) }

    LaunchedEffect(Unit) {
        while (progress < 1.0f) {
            delay(120)
            progress += 0.12f
        }
        delay(200)
        onLoaded()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF4EC0CA), // Flappy Sky Cyan
                        Color(0xFF70D6E3),
                        Color(0xFFB3F5FC),
                        Color(0xFFE8F7FB)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Bright Sunny Floating Emblem
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(130.dp)
                    .scale(pulse)
                    .background(
                        Color.White.copy(alpha = 0.9f),
                        shape = CircleShape
                    )
                    .border(4.dp, Color(0xFFFDC72B), CircleShape)
            ) {
                Text(
                    text = "💎",
                    fontSize = 68.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "MAGIC MATCH",
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E3A4B),
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "QUEST",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE65100),
                letterSpacing = 6.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "The Cheerful Gem Adventure",
                fontSize = 15.sp,
                color = Color(0xFF5C798A),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(44.dp))

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(10.dp)
                    .testTag("splash_progress"),
                color = Color(0xFFFDC72B),
                trackColor = Color.White.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Preparing sunny puzzle realms...",
                fontSize = 13.sp,
                color = Color(0xFF1E3A4B),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onLoaded,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF70C635),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF55A822)),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .testTag("splash_enter_button")
                    .height(52.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Text("PLAY NOW", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }
        }
    }
}
