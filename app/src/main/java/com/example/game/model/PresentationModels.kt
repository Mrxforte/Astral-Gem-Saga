package com.example.game.model

enum class CharacterMood {
    IDLE,
    THINKING,
    HAPPY,
    EXCITED,
    SURPRISED,
    CELEBRATING,
    SAD,
    VICTORY,
    COMBAT
}

data class CompanionCharacter(
    val id: String,
    val name: String,
    val title: String,
    val worldId: Int,
    val avatarEmoji: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long,
    val greetingQuote: String,
    val comboQuote: String,
    val victoryQuote: String
)

object CompanionCatalog {
    val ALL_COMPANIONS = listOf(
        CompanionCharacter(
            id = "lumi",
            name = "Lumi",
            title = "Apprentice Crystal Mage",
            worldId = 1,
            avatarEmoji = "🧙‍♂️",
            primaryColorHex = 0xFFAB47BC,
            secondaryColorHex = 0xFFFFD54F,
            greetingQuote = "The crystal runes are humming with ancient power!",
            comboQuote = "Spectacular magic! Keep cascading!",
            victoryQuote = "A brilliant triumph! The crystal light shines upon us!"
        ),
        CompanionCharacter(
            id = "mira",
            name = "Mira",
            title = "Forest Sylph Guardian",
            worldId = 2,
            avatarEmoji = "🧝‍♀️",
            primaryColorHex = 0xFF43A047,
            secondaryColorHex = 0xFF81C784,
            greetingQuote = "The ancient trees whisper of hidden treasures!",
            comboQuote = "Nature's fury unleashed! Beautiful cascade!",
            victoryQuote = "The grove is saved! Blessed be the forest spirits!"
        ),
        CompanionCharacter(
            id = "orion",
            name = "Orion",
            title = "Sky Citadel Zephyr",
            worldId = 3,
            avatarEmoji = "🦅",
            primaryColorHex = 0xFF0288D1,
            secondaryColorHex = 0xFF81D4FA,
            greetingQuote = "Cast off the anchor, we're soaring through the clouds!",
            comboQuote = "Sky-shattering combo! Absolutely legendary!",
            victoryQuote = "Clear skies ahead! That was an aerial masterpiece!"
        ),
        CompanionCharacter(
            id = "pyra",
            name = "Pyra",
            title = "Volcanic Drake Sorceress",
            worldId = 4,
            avatarEmoji = "🔥",
            primaryColorHex = 0xFFD84315,
            secondaryColorHex = 0xFFFFAB91,
            greetingQuote = "Feel the magma heat? Let's burn through these obstacles!",
            comboQuote = "Blazing inferno! Unstoppable destruction!",
            victoryQuote = "Forged in dragonfire! None can withstand our flame!"
        ),
        CompanionCharacter(
            id = "frost",
            name = "Frost",
            title = "Glacier Sentinel",
            worldId = 5,
            avatarEmoji = "❄️",
            primaryColorHex = 0xFF00ACC1,
            secondaryColorHex = 0xFFB2EBF2,
            greetingQuote = "The eternal frost preserves all secrets. Tread lightly.",
            comboQuote = "Absolute zero! Shattering the glacier!",
            victoryQuote = "Pristine perfection. The ice mirrors your victory."
        ),
        CompanionCharacter(
            id = "tidal",
            name = "Tidal",
            title = "Abyssal Tide Spirit",
            worldId = 6,
            avatarEmoji = "🌊",
            primaryColorHex = 0xFF00796B,
            secondaryColorHex = 0xFF80CBC4,
            greetingQuote = "Dive deep beneath the waves where tide pearls glow.",
            comboQuote = "A tidal wave of matches! Incredible surge!",
            victoryQuote = "The ocean calms. The sunken temple opens its doors!"
        ),
        CompanionCharacter(
            id = "rune",
            name = "Rune",
            title = "Sunstone Chronomancer",
            worldId = 7,
            avatarEmoji = "🏛️",
            primaryColorHex = 0xFFF57C00,
            secondaryColorHex = 0xFFFFCC80,
            greetingQuote = "The constellations align with the pyramid obelisks!",
            comboQuote = "Time itself bends to your marvelous matches!",
            victoryQuote = "The solar prophecy is fulfilled! Majestic work!"
        ),
        CompanionCharacter(
            id = "selene",
            name = "Selene",
            title = "Astral Moon Priestess",
            worldId = 8,
            avatarEmoji = "🌙",
            primaryColorHex = 0xFF512DA8,
            secondaryColorHex = 0xFFD1C4E9,
            greetingQuote = "Bask in the silver luminescence of the crescent moon.",
            comboQuote = "Moonlight cascade! Shimmering astral brilliance!",
            victoryQuote = "The night sanctuary is in harmony once more."
        ),
        CompanionCharacter(
            id = "drako",
            name = "Drako",
            title = "Elder Wyrm Champion",
            worldId = 9,
            avatarEmoji = "🐲",
            primaryColorHex = 0xFFAD1457,
            secondaryColorHex = 0xFFF48FB1,
            greetingQuote = "Sharpen your claws! The elder dragons test your mettle!",
            comboQuote = "A catastrophic draconic blast! Fear the roar!",
            victoryQuote = "You have proven worthy of the dragon throne!"
        ),
        CompanionCharacter(
            id = "celestia",
            name = "Celestia",
            title = "Cosmic Starweaver",
            worldId = 10,
            avatarEmoji = "✨",
            primaryColorHex = 0xFF283593,
            secondaryColorHex = 0xFF9FA8DA,
            greetingQuote = "Weave the starlight into constellations of destiny!",
            comboQuote = "Supernova explosion! Pure cosmic poetry!",
            victoryQuote = "The universe sings of your celestial transcendence!"
        )
    )

    fun getCompanionForWorld(worldId: Int): CompanionCharacter {
        return ALL_COMPANIONS.firstOrNull { it.worldId == worldId } ?: ALL_COMPANIONS.first()
    }
}

data class BeamEffect(
    val id: Long,
    val row: Int? = null,
    val col: Int? = null,
    val isHorizontal: Boolean,
    val colorHex: Long = 0xFFFFFFFF,
    val progress: Float = 0f,
    val maxLifeMs: Long = 450L,
    val startTimestamp: Long = System.currentTimeMillis()
)

data class ShockwaveEffect(
    val id: Long,
    val centerRow: Int,
    val centerCol: Int,
    val colorHex: Long = 0xFFFFD700,
    val radiusCells: Float = 2.5f,
    val maxLifeMs: Long = 500L,
    val startTimestamp: Long = System.currentTimeMillis()
)

data class SupernovaEvent(
    val phase: Int = 1, // 1: Charge, 2: Singularity Flash, 3: Shockwave, 4: Settle
    val progress: Float = 0f,
    val posA: Pair<Int, Int>,
    val posB: Pair<Int, Int>,
    val startTimestamp: Long = System.currentTimeMillis()
)

data class ColorTether(
    val id: Long,
    val fromRow: Int,
    val fromCol: Int,
    val toRow: Int,
    val toCol: Int,
    val colorHex: Long,
    val life: Float = 1f
)

enum class GraphicsQuality {
    LOW,
    MEDIUM,
    HIGH,
    ULTRA
}
