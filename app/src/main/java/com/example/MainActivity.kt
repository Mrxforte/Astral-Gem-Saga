package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.game.audio.AudioManager
import com.example.game.data.database.GameDatabase
import com.example.game.data.repository.GameRepository
import com.example.game.haptics.HapticsManager
import com.example.game.ui.screens.achievements.AchievementsScreen
import com.example.game.ui.screens.boss.BossBattleScreen
import com.example.game.ui.screens.editor.LevelEditorScreen
import com.example.game.ui.screens.gameplay.GameplayScreen
import com.example.game.ui.screens.home.HomeScreen
import com.example.game.ui.screens.leaderboard.LeaderboardScreen
import com.example.game.ui.screens.profile.ProfileScreen
import com.example.game.ui.screens.rewards.DailyRewardsScreen
import com.example.game.ui.screens.shop.ShopScreen
import com.example.game.ui.screens.splash.SplashScreen
import com.example.game.ui.screens.worldmap.WorldMapScreen
import com.example.game.viewmodel.AppScreen
import com.example.game.viewmodel.GameViewModel
import com.example.game.viewmodel.MainViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private lateinit var audioManager: AudioManager
    private lateinit var hapticsManager: HapticsManager
    private lateinit var gameRepository: GameRepository
    private lateinit var mainViewModel: MainViewModel
    private lateinit var gameViewModel: GameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = GameDatabase.getInstance(this)
        gameRepository = GameRepository(database.gameDao())
        audioManager = AudioManager()
        hapticsManager = HapticsManager(this)

        mainViewModel = MainViewModel(gameRepository, audioManager, hapticsManager)
        gameViewModel = GameViewModel(gameRepository, audioManager, hapticsManager)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF4EC0CA)
                ) {
                    GameAppContent(
                        mainViewModel = mainViewModel,
                        gameViewModel = gameViewModel
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::audioManager.isInitialized && !audioManager.isMuted) {
            audioManager.startBackgroundMusic()
        }
    }

    override fun onPause() {
        super.onPause()
        if (::audioManager.isInitialized) {
            audioManager.stopBackgroundMusic()
        }
    }
}

@Composable
fun GameAppContent(
    mainViewModel: MainViewModel,
    gameViewModel: GameViewModel
) {
    val currentScreen by mainViewModel.currentScreen.collectAsState()
    val selectedLevelId by mainViewModel.selectedLevelId.collectAsState()

    // Handle back button on sub-screens
    if (currentScreen != AppScreen.HOME && currentScreen != AppScreen.SPLASH) {
        BackHandler {
            if (currentScreen == AppScreen.GAMEPLAY || currentScreen == AppScreen.BOSS_BATTLE) {
                gameViewModel.quitLevel()
            }
            mainViewModel.navigateTo(AppScreen.HOME)
        }
    }

    when (currentScreen) {
        AppScreen.SPLASH -> {
            SplashScreen(
                onLoaded = { mainViewModel.navigateTo(AppScreen.HOME) }
            )
        }
        AppScreen.HOME -> {
            HomeScreen(viewModel = mainViewModel)
        }
        AppScreen.WORLD_MAP -> {
            WorldMapScreen(viewModel = mainViewModel)
        }
        AppScreen.GAMEPLAY -> {
            GameplayScreen(
                gameViewModel = gameViewModel,
                mainViewModel = mainViewModel,
                levelId = selectedLevelId
            )
        }
        AppScreen.BOSS_BATTLE -> {
            BossBattleScreen(
                gameViewModel = gameViewModel,
                mainViewModel = mainViewModel,
                bossLevelId = selectedLevelId
            )
        }
        AppScreen.LEVEL_EDITOR -> {
            LevelEditorScreen(
                gameViewModel = gameViewModel,
                mainViewModel = mainViewModel
            )
        }
        AppScreen.DAILY_REWARDS -> {
            DailyRewardsScreen(viewModel = mainViewModel)
        }
        AppScreen.SHOP -> {
            ShopScreen(viewModel = mainViewModel)
        }
        AppScreen.LEADERBOARD -> {
            LeaderboardScreen(viewModel = mainViewModel)
        }
        AppScreen.ACHIEVEMENTS -> {
            AchievementsScreen(viewModel = mainViewModel)
        }
        AppScreen.PROFILE -> {
            ProfileScreen(viewModel = mainViewModel)
        }
    }
}
