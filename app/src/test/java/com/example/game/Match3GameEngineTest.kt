package com.example.game

import com.example.game.engine.Match3GameEngine
import com.example.game.levels.generator.LevelGenerator
import com.example.game.model.*
import org.junit.Assert.*
import org.junit.Test

class Match3GameEngineTest {

    @Test
    fun testLevelGenerator_producesSolvableLevels() {
        for (lvl in 1..25) {
            val config = LevelGenerator.generateLevel(lvl)
            assertTrue("Level $lvl should have at least 5 rows", config.rows >= 5)
            assertTrue("Level $lvl should have at least 5 cols", config.cols >= 5)
            assertTrue("Level $lvl should have positive moves", config.moveLimit > 0)
            assertTrue("Level $lvl should have objectives", config.objectives.isNotEmpty())
            assertTrue("Level $lvl should pass solvability validation", LevelGenerator.validateLevelSolvability(config))
        }
    }

    @Test
    fun testBossLevel_generatedAtIntervals() {
        val bossConfig = LevelGenerator.generateLevel(20)
        assertNotNull("Level 20 must have a boss", bossConfig.boss)
        assertEquals("Level 20 boss target count matches HP", bossConfig.boss?.maxHp, bossConfig.objectives.first().targetCount)
    }

    @Test
    fun testInitialBoard_hasCorrectDimensions() {
        val config = LevelGenerator.generateLevel(1)
        val engine = Match3GameEngine(config)
        val board = engine.createInitialBoard()

        assertEquals(config.rows, board.size)
        assertEquals(config.cols, board[0].size)
    }

    @Test
    fun testMatchDetection_horizontalThree() {
        val config = LevelGenerator.generateLevel(1)
        val engine = Match3GameEngine(config)

        val customBoard = List(config.rows) { r ->
            List(config.cols) { c ->
                val type = if (r == 0 && c in 0..2) PieceType.RUBY else PieceType.SAPPHIRE
                BoardCell(
                    row = r,
                    col = c,
                    piece = GamePiece(id = (r * 10 + c).toLong(), type = type)
                )
            }
        }

        val matches = engine.detectAllMatches(customBoard)
        assertTrue("Matches should be found", matches.isNotEmpty())
    }

    @Test
    fun testWorldsCatalog_allTenWorldsExist() {
        assertEquals(10, WorldsCatalog.ALL_WORLDS.size)
        val w1 = WorldsCatalog.getWorldForLevel(1)
        assertEquals("Crystal Valley", w1.name)
        val w2 = WorldsCatalog.getWorldForLevel(25)
        assertEquals("Mystic Forest", w2.name)
    }
}
