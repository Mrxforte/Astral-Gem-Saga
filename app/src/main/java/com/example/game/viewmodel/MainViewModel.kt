package com.example.game.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.game.audio.AudioManager
import com.example.game.audio.SoundType
import com.example.game.data.entity.*
import com.example.game.data.repository.GameRepository
import com.example.game.haptics.HapticsManager
import com.example.game.model.BoosterType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppScreen {
    SPLASH,
    HOME,
    WORLD_MAP,
    GAMEPLAY,
    BOSS_BATTLE,
    LEVEL_EDITOR,
    DAILY_REWARDS,
    SHOP,
    LEADERBOARD,
    ACHIEVEMENTS,
    PROFILE
}

class MainViewModel(
    val repository: GameRepository,
    val audioManager: AudioManager,
    val hapticsManager: HapticsManager
) : ViewModel() {

    private val _currentScreen = MutableStateFlow(AppScreen.SPLASH)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _selectedLevelId = MutableStateFlow(1)
    val selectedLevelId: StateFlow<Int> = _selectedLevelId.asStateFlow()

    private val _selectedWorldId = MutableStateFlow(1)
    val selectedWorldId: StateFlow<Int> = _selectedWorldId.asStateFlow()

    val playerProfile: StateFlow<PlayerProfileEntity?> = repository.playerProfileFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val levelProgressList: StateFlow<List<LevelProgressEntity>> = repository.levelProgressFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val boosterInventory: StateFlow<List<BoosterEntity>> = repository.boostersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val achievements: StateFlow<List<AchievementEntity>> = repository.achievementsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customLevels: StateFlow<List<CustomLevelEntity>> = repository.customLevelsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _dailyRewardBanner = MutableStateFlow<String?>(null)
    val dailyRewardBanner: StateFlow<String?> = _dailyRewardBanner.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeDefaultsIfNeeded()
            repository.checkAndRechargeLives()
        }
    }

    fun navigateTo(screen: AppScreen) {
        audioManager.playSound(SoundType.BUTTON_CLICK)
        hapticsManager.lightTap()
        _currentScreen.value = screen
    }

    fun openLevel(levelId: Int) {
        _selectedLevelId.value = levelId
        if (levelId % 20 == 0) {
            _currentScreen.value = AppScreen.BOSS_BATTLE
        } else {
            _currentScreen.value = AppScreen.GAMEPLAY
        }
    }

    fun openWorld(worldId: Int) {
        _selectedWorldId.value = worldId
        _currentScreen.value = AppScreen.WORLD_MAP
    }

    fun buyBooster(booster: BoosterType) {
        viewModelScope.launch {
            val success = repository.buyBoosterWithCoins(booster)
            if (success) {
                audioManager.playSound(SoundType.COIN_PICKUP)
                hapticsManager.mediumImpact()
            } else {
                audioManager.playSound(SoundType.INVALID_SWAP)
            }
        }
    }

    fun claimAchievement(achievementId: String) {
        viewModelScope.launch {
            val coins = repository.claimAchievement(achievementId)
            if (coins > 0) {
                audioManager.playSound(SoundType.STAR_EARNED)
                hapticsManager.successPattern()
            }
        }
    }

    fun claimDaily() {
        viewModelScope.launch {
            val result = repository.claimDailyReward()
            if (result != null) {
                audioManager.playSound(SoundType.CHEST_OPEN)
                hapticsManager.successPattern()
                _dailyRewardBanner.value = "Claimed: ${result.second}!"
            } else {
                _dailyRewardBanner.value = "Already claimed for today! Come back tomorrow."
            }
        }
    }

    fun dismissDailyBanner() {
        _dailyRewardBanner.value = null
    }

    fun updateProfile(name: String, avatar: String) {
        viewModelScope.launch {
            repository.updateProfile(name, avatar)
            audioManager.playSound(SoundType.BUTTON_CLICK)
        }
    }

    fun refillLivesWithCoins() {
        viewModelScope.launch {
            val profile = playerProfile.value ?: return@launch
            if (profile.coins >= 150) {
                repository.addCoins(-150)
                repository.addLife(5)
                audioManager.playSound(SoundType.COIN_PICKUP)
                hapticsManager.successPattern()
            }
        }
    }

    fun watchRewardedAdForLife() {
        viewModelScope.launch {
            repository.addLife(1)
            audioManager.playSound(SoundType.COIN_PICKUP)
            hapticsManager.successPattern()
        }
    }

    fun toggleAudio() {
        audioManager.isMuted = !audioManager.isMuted
        if (audioManager.isMuted) {
            audioManager.stopBackgroundMusic()
        } else {
            audioManager.startBackgroundMusic()
        }
    }

    fun toggleHaptics() {
        hapticsManager.isHapticsEnabled = !hapticsManager.isHapticsEnabled
    }
}
