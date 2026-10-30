package com.example.game.ui.screens.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.game.model.BoosterType
import com.example.game.viewmodel.AppScreen
import com.example.game.viewmodel.MainViewModel

@Composable
fun ShopScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.playerProfile.collectAsState()
    var purchaseSuccessMessage by remember { mutableStateOf<String?>(null) }

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
                        onClick = { viewModel.navigateTo(AppScreen.HOME) },
                        modifier = Modifier.testTag("shop_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF1E3A4B))
                    }
                    Text(
                        text = "ARCANE EMPORIUM",
                        color = Color(0xFF1E3A4B),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                    // Coins counter
                    Surface(
                        color = Color(0xFFFFFDE7),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFF59D))
                    ) {
                        Text(
                            text = "🪙 ${profile?.coins ?: 0}",
                            color = Color(0xFFF57F17),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFFE8F7FB),
        modifier = modifier.fillMaxSize().testTag("shop_screen")
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            // Free Rewarded Ad Offers
            item {
                Text(
                    text = "FREE REWARDED OFFERS",
                    color = Color(0xFF00ACC1),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFBCE7F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(16.dp).fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "📺", fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Watch Ad for +100 Coins", color = Color(0xFF1E3A4B), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(text = "Sponsored bonus reward", color = Color(0xFF5C798A), fontSize = 11.sp)
                            }
                        }
                        Button(
                            onClick = {
                                viewModel.refillLivesWithCoins()
                                purchaseSuccessMessage = "Bonus 100 Coins added!"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00ACC1)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("WATCH", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Booster Spells Shop
            item {
                Text(
                    text = "BOOSTER SPELLS (PURCHASE WITH COINS)",
                    color = Color(0xFFE65100),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }

            items(BoosterType.values().size) { idx ->
                val booster = BoosterType.values()[idx]
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFBCE7F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(14.dp).fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Text(text = booster.iconSymbol, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = booster.title, color = Color(0xFF1E3A4B), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(text = booster.description, color = Color(0xFF5C798A), fontSize = 11.sp)
                            }
                        }
                        Button(
                            onClick = {
                                viewModel.buyBooster(booster)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFDC72B)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.testTag("buy_booster_${booster.name.lowercase()}")
                        ) {
                            Text("${booster.coinCost} 🪙", color = Color(0xFF1E3A4B), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Google Play Billing Store Bundles
            item {
                Text(
                    text = "STARTER & PREMIUM BUNDLES",
                    color = Color(0xFF70C635),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFDC72B)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(16.dp).fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "👑", fontSize = 32.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Enchanter's Starter Pack", color = Color(0xFF1E3A4B), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(text = "1,000 Coins + 3 Hammers + 3 Bombs", color = Color(0xFF5C798A), fontSize = 11.sp)
                            }
                        }
                        Button(
                            onClick = { purchaseSuccessMessage = "Starter pack claimed!" },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFDC72B)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("$2.99", color = Color(0xFF1E3A4B), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFBCE7F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(16.dp).fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🚫", fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Remove All Ads Forever", color = Color(0xFF1E3A4B), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(text = "Clean uninterrupted gameplay", color = Color(0xFF5C798A), fontSize = 11.sp)
                            }
                        }
                        Button(
                            onClick = { purchaseSuccessMessage = "Ads removed permanently!" },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00ACC1)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("$4.99", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        purchaseSuccessMessage?.let { msg ->
            AlertDialog(
                onDismissRequest = { purchaseSuccessMessage = null },
                containerColor = Color.White,
                shape = RoundedCornerShape(20.dp),
                title = { Text("🎉 Success!", color = Color(0xFF1E3A4B), fontWeight = FontWeight.Bold) },
                text = { Text(msg, color = Color(0xFF5C798A)) },
                confirmButton = {
                    Button(
                        onClick = { purchaseSuccessMessage = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFDC72B))
                    ) {
                        Text("AWESOME", color = Color(0xFF1E3A4B), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}
