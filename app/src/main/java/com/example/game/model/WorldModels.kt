package com.example.game.model

data class GameWorld(
    val id: Int,
    val name: String,
    val subtitle: String,
    val themeColorStart: Long,
    val themeColorEnd: Long,
    val startLevel: Int,
    val endLevel: Int,
    val worldIcon: String,
    val description: String,
    val ambientTheme: String
)

object WorldsCatalog {
    val ALL_WORLDS = listOf(
        GameWorld(
            id = 1,
            name = "Crystal Valley",
            subtitle = "The Glimmering Gateway",
            themeColorStart = 0xFF4EC0CA,
            themeColorEnd = 0xFF81D4FA,
            startLevel = 1,
            endLevel = 20,
            worldIcon = "💎",
            description = "A radiant mountain canyon filled with luminescent raw gems and sparkling streams.",
            ambientTheme = "Glistening Chimes"
        ),
        GameWorld(
            id = 2,
            name = "Mystic Forest",
            subtitle = "Whispers of the Sylphs",
            themeColorStart = 0xFF70C635,
            themeColorEnd = 0xFFA5D6A7,
            startLevel = 21,
            endLevel = 40,
            worldIcon = "🌲",
            description = "Ancient woodland covered in phosphorescent flora and slumbering earth spirits.",
            ambientTheme = "Verdant Echoes"
        ),
        GameWorld(
            id = 3,
            name = "Sky Islands",
            subtitle = "Castles in the Clouds",
            themeColorStart = 0xFF29B6F6,
            themeColorEnd = 0xFFB3E5FC,
            startLevel = 41,
            endLevel = 60,
            worldIcon = "☁️",
            description = "Levitating landmasses held by astral magnetic crystals soaring above the storm.",
            ambientTheme = "Zephyr Harp"
        ),
        GameWorld(
            id = 4,
            name = "Volcanic Kingdom",
            subtitle = "Furnace of the Wyrms",
            themeColorStart = 0xFFFF8A65,
            themeColorEnd = 0xFFFFCC80,
            startLevel = 61,
            endLevel = 80,
            worldIcon = "🌋",
            description = "Molten lava rivers forge blazing fire opals and test the courage of mages.",
            ambientTheme = "Flame Percussion"
        ),
        GameWorld(
            id = 5,
            name = "Frozen Realm",
            subtitle = "The Glacier Citadel",
            themeColorStart = 0xFF4DD0E1,
            themeColorEnd = 0xFFE0F7FA,
            startLevel = 81,
            endLevel = 100,
            worldIcon = "❄️",
            description = "Permafrost spires encasing centuries of forgotten spells under glacial mirrors.",
            ambientTheme = "Crystal Wind"
        ),
        GameWorld(
            id = 6,
            name = "Ocean Temple",
            subtitle = "Abyssal Sanctuaries",
            themeColorStart = 0xFF26A69A,
            themeColorEnd = 0xFF80CBC4,
            startLevel = 101,
            endLevel = 120,
            worldIcon = "🌊",
            description = "Sunken marble ruins glowing with bioluminescent tide pearls.",
            ambientTheme = "Deep Resonance"
        ),
        GameWorld(
            id = 7,
            name = "Ancient Ruins",
            subtitle = "Legacy of the Sun Kings",
            themeColorStart = 0xFFFDC72B,
            themeColorEnd = 0xFFFFF59D,
            startLevel = 121,
            endLevel = 140,
            worldIcon = "🏛️",
            description = "Golden pyramids and enchanted obelisks tuned to celestial constellations.",
            ambientTheme = "Sun Chants"
        ),
        GameWorld(
            id = 8,
            name = "Moon Kingdom",
            subtitle = "Sanctum of the Astral Tides",
            themeColorStart = 0xFFBA68C8,
            themeColorEnd = 0xFFE1BEE7,
            startLevel = 141,
            endLevel = 160,
            worldIcon = "🌙",
            description = "Quiet crater palaces bathing under the silver light of perpetual moonlight.",
            ambientTheme = "Nocturne Serenade"
        ),
        GameWorld(
            id = 9,
            name = "Dragon Lands",
            subtitle = "High Roost of the Ancients",
            themeColorStart = 0xFFFF7043,
            themeColorEnd = 0xFFFFCCBC,
            startLevel = 161,
            endLevel = 180,
            worldIcon = "🐲",
            description = "Jagged obsidian crags guarded by elder chromatic drakes guarding hoards.",
            ambientTheme = "Dragon Roar"
        ),
        GameWorld(
            id = 10,
            name = "Celestial Realm",
            subtitle = "The Starweavers' Cosmos",
            themeColorStart = 0xFF42A5F5,
            themeColorEnd = 0xFFCE93D8,
            startLevel = 181,
            endLevel = 200,
            worldIcon = "✨",
            description = "The ultimate astral domain where reality is woven from purest starlight.",
            ambientTheme = "Cosmic Harmony"
        )
    )

    fun getWorldForLevel(levelId: Int): GameWorld {
        return ALL_WORLDS.firstOrNull { levelId in it.startLevel..it.endLevel }
            ?: ALL_WORLDS.first()
    }
}
