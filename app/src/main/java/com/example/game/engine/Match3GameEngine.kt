package com.example.game.engine

import com.example.game.model.*
import java.util.Random

class Match3GameEngine(
    val levelConfig: LevelConfig
) {
    private val rows = levelConfig.rows
    private val cols = levelConfig.cols
    private val random = Random(levelConfig.seed)
    private var pieceIdCounter = 1L

    data class SwapResult(
        val isValid: Boolean,
        val isSpecialCombo: Boolean = false,
        val matchesFound: Int = 0,
        val scoreEarned: Int = 0,
        val comboChain: Int = 0,
        val boardChanged: Boolean = false,
        val boardReshuffled: Boolean = false,
        val defeatedBoss: Boolean = false,
        val destroyedCells: Set<Pair<Int, Int>> = emptySet()
    )

    data class MatchGroup(
        val cells: Set<Pair<Int, Int>>,
        val pieceType: PieceType,
        val createsSpecial: SpecialPiece = SpecialPiece.NONE,
        val specialSpawnCell: Pair<Int, Int>? = null
    )

    data class FallingPieceData(
        val pieceId: Long,
        val fromRow: Int,
        val toRow: Int,
        val col: Int
    )

    sealed class CascadeStep {
        data class Match(
            val matchedCells: Set<Pair<Int, Int>>,
            val specialsToSpawn: Map<Pair<Int, Int>, Pair<PieceType, SpecialPiece>>,
            val scoreEarned: Int,
            val comboChain: Int,
            val primaryColor: Long = 0xFFFFD700
        ) : CascadeStep()

        data class Clear(
            val board: List<List<BoardCell>>
        ) : CascadeStep()

        data class Gravity(
            val board: List<List<BoardCell>>,
            val fallingPieces: List<FallingPieceData>
        ) : CascadeStep()

        data class Refill(
            val board: List<List<BoardCell>>,
            val newPieces: List<FallingPieceData>
        ) : CascadeStep()

        data class Reshuffle(
            val board: List<List<BoardCell>>
        ) : CascadeStep()
    }

    data class SwapPlan(
        val isValid: Boolean,
        val isSpecialCombo: Boolean,
        val steps: List<CascadeStep>,
        val finalBoard: List<List<BoardCell>>,
        val totalScore: Int,
        val maxCombo: Int,
        val matchesCount: Int,
        val allDestroyedCells: Set<Pair<Int, Int>>
    )

    /**
     * Initializes the starting board:
     * - Places initial blockers and jelly from LevelConfig
     * - Fills remaining cells with random pieces ensuring NO initial match-3
     */
    fun createInitialBoard(): List<List<BoardCell>> {
        val board = MutableList(rows) { r ->
            MutableList(cols) { c ->
                val blocker = levelConfig.initialBlockers[r to c] ?: BlockerType.NONE
                val hasJelly = levelConfig.initialJelly.contains(r to c)
                BoardCell(
                    row = r,
                    col = c,
                    blocker = blocker,
                    blockerHp = blocker.maxHp,
                    isLocked = blocker == BlockerType.LOCKED_TILE,
                    hasJelly = hasJelly,
                    jellyLayers = if (hasJelly) 1 else 0
                )
            }
        }

        // Fill cells that are not solid obstacles with pieces
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val cell = board[r][c]
                if (cell.blocker == BlockerType.STONE) continue

                // Pick a piece type that does NOT create an immediate 3-match with neighbors
                val pieceType = chooseNonMatchingPiece(board, r, c)
                board[r][c] = cell.copy(
                    piece = GamePiece(
                        id = pieceIdCounter++,
                        type = pieceType,
                        special = SpecialPiece.NONE
                    )
                )
            }
        }

        // Check for dead board immediately and reshuffle if no moves exist
        if (!hasAnyValidMove(board)) {
            shuffleBoard(board)
        }

        return board.map { it.toList() }
    }

    private fun chooseNonMatchingPiece(board: List<List<BoardCell>>, r: Int, c: Int): PieceType {
        val forbidden = mutableSetOf<PieceType>()

        // Check horizontal preceding two cells
        if (c >= 2) {
            val p1 = board[r][c - 1].piece?.type
            val p2 = board[r][c - 2].piece?.type
            if (p1 != null && p1 == p2) {
                forbidden.add(p1)
            }
        }

        // Check vertical preceding two cells
        if (r >= 2) {
            val p1 = board[r - 1][c].piece?.type
            val p2 = board[r - 2][c].piece?.type
            if (p1 != null && p1 == p2) {
                forbidden.add(p1)
            }
        }

        val candidates = levelConfig.availablePieces.filter { it !in forbidden }
        return if (candidates.isNotEmpty()) {
            candidates[random.nextInt(candidates.size)]
        } else {
            levelConfig.availablePieces[random.nextInt(levelConfig.availablePieces.size)]
        }
    }

    /**
     * Executes a player-initiated swap between two adjacent cells.
     */
    fun executeSwap(
        currentBoard: List<List<BoardCell>>,
        from: Pair<Int, Int>,
        to: Pair<Int, Int>
    ): Pair<List<List<BoardCell>>, SwapResult> {
        val (r1, c1) = from
        val (r2, c2) = to

        // Must be orthogonal neighbors
        val isAdjacent = (Math.abs(r1 - r2) + Math.abs(c1 - c2)) == 1
        if (!isAdjacent) {
            return currentBoard to SwapResult(isValid = false)
        }

        val cellA = currentBoard[r1][c1]
        val cellB = currentBoard[r2][c2]

        val pieceA = cellA.piece ?: return currentBoard to SwapResult(isValid = false)
        val pieceB = cellB.piece ?: return currentBoard to SwapResult(isValid = false)

        // Blocked/locked tiles cannot swap
        if (cellA.isLocked || cellB.isLocked || cellA.blocker == BlockerType.STONE || cellB.blocker == BlockerType.STONE) {
            return currentBoard to SwapResult(isValid = false)
        }

        // Check for Special + Special combinations
        if (pieceA.special != SpecialPiece.NONE && pieceB.special != SpecialPiece.NONE) {
            return executeSpecialCombination(currentBoard, from, to, pieceA, pieceB)
        }

        // Check for Color Bomb + Normal piece combination
        if (pieceA.special == SpecialPiece.COLOR_BOMB && pieceB.special == SpecialPiece.NONE) {
            return executeColorBombWithNormal(currentBoard, from, to, pieceB.type)
        }
        if (pieceB.special == SpecialPiece.COLOR_BOMB && pieceA.special == SpecialPiece.NONE) {
            return executeColorBombWithNormal(currentBoard, to, from, pieceA.type)
        }

        // Standard swap simulation
        val tempBoard = currentBoard.map { it.toMutableList() }
        tempBoard[r1][c1] = cellA.copy(piece = pieceB)
        tempBoard[r2][c2] = cellB.copy(piece = pieceA)

        val matches = detectAllMatches(tempBoard, swappedCell = to)
        if (matches.isEmpty()) {
            // Invalid swap: restore original
            return currentBoard to SwapResult(isValid = false)
        }

        // Valid move! Process cascades until board is stable
        val cascadeResult = processCascades(tempBoard, initialMatches = matches)

        return cascadeResult.first to SwapResult(
            isValid = true,
            matchesFound = cascadeResult.second.totalMatches,
            scoreEarned = cascadeResult.second.totalScore,
            comboChain = cascadeResult.second.maxCombo,
            boardChanged = true,
            boardReshuffled = cascadeResult.second.didReshuffle,
            destroyedCells = cascadeResult.second.allDestroyedCells
        )
    }

    /**
     * Executes combinations of two special pieces:
     * - Line + Line
     * - Line + Bomb
     * - Bomb + Bomb
     * - Color Bomb + Line
     * - Color Bomb + Bomb
     * - Color Bomb + Color Bomb (Supernova!)
     */
    private fun executeSpecialCombination(
        board: List<List<BoardCell>>,
        posA: Pair<Int, Int>,
        posB: Pair<Int, Int>,
        pieceA: GamePiece,
        pieceB: GamePiece
    ): Pair<List<List<BoardCell>>, SwapResult> {
        val mutableBoard = board.map { it.toMutableList() }
        val destroyed = mutableSetOf<Pair<Int, Int>>()
        var comboScore = 2000

        when {
            // Color Bomb + Color Bomb: Supernova wipeout!
            pieceA.special == SpecialPiece.COLOR_BOMB && pieceB.special == SpecialPiece.COLOR_BOMB -> {
                for (r in 0 until rows) {
                    for (c in 0 until cols) {
                        destroyed.add(r to c)
                    }
                }
                comboScore += 15000
            }
            // Color Bomb + Line or Bomb: Convert all gems of the other's color to that special!
            pieceA.special == SpecialPiece.COLOR_BOMB || pieceB.special == SpecialPiece.COLOR_BOMB -> {
                val specialType = if (pieceA.special == SpecialPiece.COLOR_BOMB) pieceB.special else pieceA.special
                val otherColor = if (pieceA.special == SpecialPiece.COLOR_BOMB) pieceB.type else pieceA.type
                destroyed.add(posA)
                destroyed.add(posB)
                // Detonate all of that color transformed
                for (r in 0 until rows) {
                    for (c in 0 until cols) {
                        if (mutableBoard[r][c].piece?.type == otherColor) {
                            destroyed.add(r to c)
                            if (specialType == SpecialPiece.LINE_HORIZONTAL || specialType == SpecialPiece.LINE_VERTICAL) {
                                // Add row or column
                                for (cc in 0 until cols) destroyed.add(r to cc)
                            } else if (specialType == SpecialPiece.AREA_BOMB) {
                                for (dr in -1..1) for (dc in -1..1) {
                                    val nr = r + dr
                                    val nc = c + dc
                                    if (nr in 0 until rows && nc in 0 until cols) destroyed.add(nr to nc)
                                }
                            }
                        }
                    }
                }
                comboScore += 8000
            }
            // Bomb + Bomb: 5x5 massive explosion
            pieceA.special == SpecialPiece.AREA_BOMB && pieceB.special == SpecialPiece.AREA_BOMB -> {
                val centerR = (posA.first + posB.first) / 2
                val centerC = (posA.second + posB.second) / 2
                for (dr in -2..2) {
                    for (dc in -2..2) {
                        val nr = centerR + dr
                        val nc = centerC + dc
                        if (nr in 0 until rows && nc in 0 until cols) {
                            destroyed.add(nr to nc)
                        }
                    }
                }
                comboScore += 4500
            }
            // Line + Bomb: 3-row & 3-column cross wipeout
            (pieceA.special in listOf(SpecialPiece.LINE_HORIZONTAL, SpecialPiece.LINE_VERTICAL) && pieceB.special == SpecialPiece.AREA_BOMB) ||
            (pieceB.special in listOf(SpecialPiece.LINE_HORIZONTAL, SpecialPiece.LINE_VERTICAL) && pieceA.special == SpecialPiece.AREA_BOMB) -> {
                val centerR = posB.first
                val centerC = posB.second
                for (dr in -1..1) {
                    val r = centerR + dr
                    if (r in 0 until rows) {
                        for (c in 0 until cols) destroyed.add(r to c)
                    }
                }
                for (dc in -1..1) {
                    val c = centerC + dc
                    if (c in 0 until cols) {
                        for (r in 0 until rows) destroyed.add(r to c)
                    }
                }
                comboScore += 5000
            }
            // Line + Line: Clears both entire row and entire column
            else -> {
                for (c in 0 until cols) {
                    destroyed.add(posA.first to c)
                    destroyed.add(posB.first to c)
                }
                for (r in 0 until rows) {
                    destroyed.add(r to posA.second)
                    destroyed.add(r to posB.second)
                }
                comboScore += 3000
            }
        }

        // Apply destruction
        removeCells(mutableBoard, destroyed)
        val cascadeResult = processCascades(mutableBoard, initialMatches = emptyList(), bonusScore = comboScore)

        return cascadeResult.first to SwapResult(
            isValid = true,
            isSpecialCombo = true,
            matchesFound = 1,
            scoreEarned = cascadeResult.second.totalScore,
            comboChain = 2,
            boardChanged = true,
            destroyedCells = destroyed + cascadeResult.second.allDestroyedCells
        )
    }

    /**
     * Color bomb swapped with normal gem destroys all gems of that color on the board.
     */
    private fun executeColorBombWithNormal(
        board: List<List<BoardCell>>,
        bombPos: Pair<Int, Int>,
        normalPos: Pair<Int, Int>,
        targetColor: PieceType
    ): Pair<List<List<BoardCell>>, SwapResult> {
        val mutableBoard = board.map { it.toMutableList() }
        val destroyed = mutableSetOf<Pair<Int, Int>>()
        destroyed.add(bombPos)
        destroyed.add(normalPos)

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (mutableBoard[r][c].piece?.type == targetColor) {
                    destroyed.add(r to c)
                }
            }
        }

        removeCells(mutableBoard, destroyed)
        val score = destroyed.size * 120 + 1500
        val cascadeResult = processCascades(mutableBoard, initialMatches = emptyList(), bonusScore = score)

        return cascadeResult.first to SwapResult(
            isValid = true,
            isSpecialCombo = true,
            matchesFound = 1,
            scoreEarned = cascadeResult.second.totalScore,
            comboChain = 1,
            boardChanged = true,
            destroyedCells = destroyed + cascadeResult.second.allDestroyedCells
        )
    }

    data class CascadeStats(
        var totalScore: Int = 0,
        var totalMatches: Int = 0,
        var maxCombo: Int = 0,
        var didReshuffle: Boolean = false,
        val allDestroyedCells: MutableSet<Pair<Int, Int>> = mutableSetOf()
    )

    /**
     * Loops: Remove matched cells -> trigger specials -> apply gravity -> refill -> repeat
     */
    private fun processCascades(
        board: List<MutableList<BoardCell>>,
        initialMatches: List<MatchGroup>,
        bonusScore: Int = 0
    ): Pair<List<List<BoardCell>>, CascadeStats> {
        val stats = CascadeStats(totalScore = bonusScore)
        var currentMatches = initialMatches
        var combo = 1

        while (true) {
            if (currentMatches.isNotEmpty()) {
                stats.totalMatches += currentMatches.size
                stats.maxCombo = maxOf(stats.maxCombo, combo)

                // Multiplier based on combo
                val multiplier = when (combo) {
                    1 -> 1.0f
                    2 -> 1.5f
                    3 -> 2.0f
                    4 -> 3.0f
                    else -> 5.0f
                }

                // Process match groups: track cells to destroy, damage blockers, create specials
                val cellsToDestroy = mutableSetOf<Pair<Int, Int>>()
                val specialsToSpawn = mutableMapOf<Pair<Int, Int>, Pair<PieceType, SpecialPiece>>()

                for (group in currentMatches) {
                    var groupScore = group.cells.size * 60
                    stats.totalScore += (groupScore * multiplier).toInt()

                    cellsToDestroy.addAll(group.cells)

                    if (group.createsSpecial != SpecialPiece.NONE && group.specialSpawnCell != null) {
                        specialsToSpawn[group.specialSpawnCell] = group.pieceType to group.createsSpecial
                    }

                    // Damage adjacent blockers and underlying jelly
                    for (cellCoord in group.cells) {
                        damageSurroundingBlockers(board, cellCoord.first, cellCoord.second)
                        clearJelly(board, cellCoord.first, cellCoord.second)
                    }
                }

                // Detonate any special pieces that were matched
                val extraDestroyed = mutableSetOf<Pair<Int, Int>>()
                for (coord in cellsToDestroy) {
                    val piece = board[coord.first][coord.second].piece
                    if (piece != null && piece.special != SpecialPiece.NONE) {
                        extraDestroyed.addAll(activateSpecialPiece(board, coord.first, coord.second, piece.special))
                    }
                }
                cellsToDestroy.addAll(extraDestroyed)
                stats.allDestroyedCells.addAll(cellsToDestroy)

                // Remove destroyed cells (except special spawn locations)
                for (coord in cellsToDestroy) {
                    if (coord !in specialsToSpawn.keys) {
                        val cell = board[coord.first][coord.second]
                        board[coord.first][coord.second] = cell.copy(piece = null)
                    }
                }

                // Spawn newly created special pieces
                for ((coord, spec) in specialsToSpawn) {
                    val cell = board[coord.first][coord.second]
                    board[coord.first][coord.second] = cell.copy(
                        piece = GamePiece(
                            id = pieceIdCounter++,
                            type = spec.first,
                            special = spec.second
                        )
                    )
                }

                // Apply gravity to drop pieces down
                applyGravity(board)

                // Refill empty cells from top
                refillBoard(board)

                combo++
                // Find new matches formed by falling pieces
                currentMatches = detectAllMatches(board)
            } else {
                // If there are no matches, check for dead board
                if (!hasAnyValidMove(board)) {
                    shuffleBoard(board)
                    stats.didReshuffle = true
                    // Re-check after shuffle
                    currentMatches = detectAllMatches(board)
                    if (currentMatches.isEmpty()) break
                } else {
                    break
                }
            }
        }

        return board.map { it.toList() } to stats
    }

    /**
     * Detects all horizontal, vertical, L/T, 4-in-line, and 5-in-line matches.
     */
    fun detectAllMatches(
        board: List<List<BoardCell>>,
        swappedCell: Pair<Int, Int>? = null
    ): List<MatchGroup> {
        val horizontalMatches = mutableListOf<List<Pair<Int, Int>>>()
        val verticalMatches = mutableListOf<List<Pair<Int, Int>>>()

        // 1. Horizontal scans
        for (r in 0 until rows) {
            var c = 0
            while (c < cols) {
                val piece = board[r][c].piece
                if (piece != null && piece.special != SpecialPiece.COLOR_BOMB) {
                    var matchLen = 1
                    while (c + matchLen < cols &&
                        board[r][c + matchLen].piece?.type == piece.type &&
                        board[r][c + matchLen].piece?.special != SpecialPiece.COLOR_BOMB
                    ) {
                        matchLen++
                    }
                    if (matchLen >= 3) {
                        val streak = (0 until matchLen).map { r to (c + it) }
                        horizontalMatches.add(streak)
                        c += matchLen - 1
                    }
                }
                c++
            }
        }

        // 2. Vertical scans
        for (c in 0 until cols) {
            var r = 0
            while (r < rows) {
                val piece = board[r][c].piece
                if (piece != null && piece.special != SpecialPiece.COLOR_BOMB) {
                    var matchLen = 1
                    while (r + matchLen < rows &&
                        board[r + matchLen][c].piece?.type == piece.type &&
                        board[r + matchLen][c].piece?.special != SpecialPiece.COLOR_BOMB
                    ) {
                        matchLen++
                    }
                    if (matchLen >= 3) {
                        val streak = (0 until matchLen).map { (r + it) to c }
                        verticalMatches.add(streak)
                        r += matchLen - 1
                    }
                }
                r++
            }
        }

        val matchGroups = mutableListOf<MatchGroup>()
        val processedH = BooleanArray(horizontalMatches.size)
        val processedV = BooleanArray(verticalMatches.size)

        // Check for T, L, or Cross intersections between H and V
        for (i in horizontalMatches.indices) {
            val hStreak = horizontalMatches[i]
            for (j in verticalMatches.indices) {
                val vStreak = verticalMatches[j]
                val intersection = hStreak.toSet().intersect(vStreak.toSet())
                if (intersection.isNotEmpty()) {
                    val intersectCell = intersection.first()
                    val pieceType = board[intersectCell.first][intersectCell.second].piece?.type ?: PieceType.RUBY
                    val combinedCells = (hStreak + vStreak).toSet()

                    val specialType = if (hStreak.size >= 4 || vStreak.size >= 4) {
                        SpecialPiece.CROSS_BLAST
                    } else {
                        SpecialPiece.AREA_BOMB
                    }

                    matchGroups.add(
                        MatchGroup(
                            cells = combinedCells,
                            pieceType = pieceType,
                            createsSpecial = specialType,
                            specialSpawnCell = intersectCell
                        )
                    )
                    processedH[i] = true
                    processedV[j] = true
                }
            }
        }

        // Remaining horizontal matches
        for (i in horizontalMatches.indices) {
            if (!processedH[i]) {
                val streak = horizontalMatches[i]
                val pieceType = board[streak[0].first][streak[0].second].piece?.type ?: PieceType.RUBY
                val spawnCell = if (swappedCell != null && swappedCell in streak) swappedCell else streak[streak.size / 2]

                val specialType = when {
                    streak.size >= 5 -> SpecialPiece.COLOR_BOMB
                    streak.size == 4 -> SpecialPiece.LINE_HORIZONTAL
                    else -> SpecialPiece.NONE
                }

                matchGroups.add(
                    MatchGroup(
                        cells = streak.toSet(),
                        pieceType = pieceType,
                        createsSpecial = specialType,
                        specialSpawnCell = if (specialType != SpecialPiece.NONE) spawnCell else null
                    )
                )
            }
        }

        // Remaining vertical matches
        for (j in verticalMatches.indices) {
            if (!processedV[j]) {
                val streak = verticalMatches[j]
                val pieceType = board[streak[0].first][streak[0].second].piece?.type ?: PieceType.RUBY
                val spawnCell = if (swappedCell != null && swappedCell in streak) swappedCell else streak[streak.size / 2]

                val specialType = when {
                    streak.size >= 5 -> SpecialPiece.COLOR_BOMB
                    streak.size == 4 -> SpecialPiece.LINE_VERTICAL
                    else -> SpecialPiece.NONE
                }

                matchGroups.add(
                    MatchGroup(
                        cells = streak.toSet(),
                        pieceType = pieceType,
                        createsSpecial = specialType,
                        specialSpawnCell = if (specialType != SpecialPiece.NONE) spawnCell else null
                    )
                )
            }
        }

        return matchGroups
    }

    private fun activateSpecialPiece(
        board: List<List<BoardCell>>,
        r: Int,
        c: Int,
        special: SpecialPiece
    ): Set<Pair<Int, Int>> {
        val affected = mutableSetOf<Pair<Int, Int>>()
        when (special) {
            SpecialPiece.LINE_HORIZONTAL -> {
                for (col in 0 until cols) affected.add(r to col)
            }
            SpecialPiece.LINE_VERTICAL -> {
                for (row in 0 until rows) affected.add(row to c)
            }
            SpecialPiece.AREA_BOMB -> {
                for (dr in -1..1) {
                    for (dc in -1..1) {
                        val nr = r + dr
                        val nc = c + dc
                        if (nr in 0 until rows && nc in 0 until cols) {
                            affected.add(nr to nc)
                        }
                    }
                }
            }
            SpecialPiece.CROSS_BLAST -> {
                for (col in 0 until cols) affected.add(r to col)
                for (row in 0 until rows) affected.add(row to c)
            }
            SpecialPiece.COLOR_BOMB -> {
                // Randomly target the most prevalent color
                val color = levelConfig.availablePieces[random.nextInt(levelConfig.availablePieces.size)]
                for (row in 0 until rows) {
                    for (col in 0 until cols) {
                        if (board[row][col].piece?.type == color) {
                            affected.add(row to col)
                        }
                    }
                }
            }
            SpecialPiece.NONE -> {}
        }
        return affected
    }

    private fun damageSurroundingBlockers(board: List<MutableList<BoardCell>>, r: Int, c: Int) {
        val directions = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
        for ((dr, dc) in directions) {
            val nr = r + dr
            val nc = c + dc
            if (nr in 0 until rows && nc in 0 until cols) {
                val neighbor = board[nr][nc]
                if (neighbor.blocker != BlockerType.NONE && neighbor.blocker != BlockerType.JELLY) {
                    val newHp = neighbor.blockerHp - 1
                    if (newHp <= 0) {
                        board[nr][nc] = neighbor.copy(blocker = BlockerType.NONE, blockerHp = 0, isLocked = false)
                    } else {
                        val newBlocker = if (neighbor.blocker == BlockerType.TRIPLE_ICE) BlockerType.DOUBLE_ICE
                                         else if (neighbor.blocker == BlockerType.DOUBLE_ICE) BlockerType.SINGLE_ICE
                                         else neighbor.blocker
                        board[nr][nc] = neighbor.copy(blocker = newBlocker, blockerHp = newHp)
                    }
                }
            }
        }
    }

    private fun clearJelly(board: List<MutableList<BoardCell>>, r: Int, c: Int) {
        val cell = board[r][c]
        if (cell.hasJelly) {
            val remaining = cell.jellyLayers - 1
            board[r][c] = cell.copy(
                hasJelly = remaining > 0,
                jellyLayers = maxOf(0, remaining)
            )
        }
    }

    private fun applyGravity(board: List<MutableList<BoardCell>>) {
        for (c in 0 until cols) {
            var writeRow = rows - 1
            for (r in rows - 1 downTo 0) {
                val cell = board[r][c]
                if (cell.blocker == BlockerType.STONE) {
                    writeRow = r - 1
                    continue
                }
                if (cell.piece != null) {
                    if (r != writeRow) {
                        val targetCell = board[writeRow][c]
                        if (targetCell.blocker != BlockerType.STONE) {
                            board[writeRow][c] = targetCell.copy(piece = cell.piece)
                            board[r][c] = cell.copy(piece = null)
                        }
                    }
                    writeRow--
                }
            }
        }
    }

    private fun refillBoard(board: List<MutableList<BoardCell>>) {
        for (c in 0 until cols) {
            for (r in 0 until rows) {
                val cell = board[r][c]
                if (cell.piece == null && cell.blocker != BlockerType.STONE) {
                    val pieceType = levelConfig.availablePieces[random.nextInt(levelConfig.availablePieces.size)]
                    board[r][c] = cell.copy(
                        piece = GamePiece(
                            id = pieceIdCounter++,
                            type = pieceType,
                            special = SpecialPiece.NONE
                        )
                    )
                }
            }
        }
    }

    private fun removeCells(board: List<MutableList<BoardCell>>, cells: Set<Pair<Int, Int>>) {
        for (coord in cells) {
            val cell = board[coord.first][coord.second]
            board[coord.first][coord.second] = cell.copy(piece = null)
        }
    }

    /**
     * Checks if at least ONE valid move exists on the board.
     */
    fun hasAnyValidMove(board: List<List<BoardCell>>): Boolean {
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val piece = board[r][c].piece ?: continue

                // Color bomb can swap with any neighbor
                if (piece.special == SpecialPiece.COLOR_BOMB) {
                    if (c + 1 < cols && board[r][c + 1].piece != null) return true
                    if (r + 1 < rows && board[r + 1][c].piece != null) return true
                }

                // Try swapping with right neighbor
                if (c + 1 < cols && board[r][c + 1].piece != null) {
                    if (simulateSwapHasMatch(board, r, c, r, c + 1)) return true
                }
                // Try swapping with down neighbor
                if (r + 1 < rows && board[r + 1][c].piece != null) {
                    if (simulateSwapHasMatch(board, r, c, r + 1, c)) return true
                }
            }
        }
        return false
    }

    private fun simulateSwapHasMatch(board: List<List<BoardCell>>, r1: Int, c1: Int, r2: Int, c2: Int): Boolean {
        val p1 = board[r1][c1].piece ?: return false
        val p2 = board[r2][c2].piece ?: return false

        // Both specials always form a match
        if (p1.special != SpecialPiece.NONE && p2.special != SpecialPiece.NONE) return true

        // Simulate swap in local copies
        val testBoard = board.map { it.toMutableList() }
        testBoard[r1][c1] = testBoard[r1][c1].copy(piece = p2)
        testBoard[r2][c2] = testBoard[r2][c2].copy(piece = p1)

        return detectAllMatches(testBoard).isNotEmpty()
    }

    /**
     * Shuffles non-blocker pieces when dead board is detected.
     */
    fun shuffleBoard(board: List<MutableList<BoardCell>>) {
        val pieces = mutableListOf<GamePiece>()
        val slots = mutableListOf<Pair<Int, Int>>()

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val cell = board[r][c]
                if (cell.piece != null && cell.blocker != BlockerType.STONE) {
                    pieces.add(cell.piece)
                    slots.add(r to c)
                }
            }
        }

        var attempts = 0
        do {
            pieces.shuffle(random)
            for (i in slots.indices) {
                val (r, c) = slots[i]
                board[r][c] = board[r][c].copy(piece = pieces[i])
            }
            attempts++
        } while (detectAllMatches(board).isNotEmpty() && attempts < 10)
    }

    /**
     * Uses Booster on specific cell or board.
     */
    fun applyBooster(
        currentBoard: List<List<BoardCell>>,
        booster: BoosterType,
        targetCell: Pair<Int, Int>?
    ): Pair<List<List<BoardCell>>, Int> {
        val mutableBoard = currentBoard.map { it.toMutableList() }
        var scoreGained = 500

        when (booster) {
            BoosterType.HAMMER -> {
                targetCell?.let { (r, c) ->
                    val cell = mutableBoard[r][c]
                    mutableBoard[r][c] = cell.copy(
                        piece = null,
                        blocker = BlockerType.NONE,
                        blockerHp = 0,
                        isLocked = false
                    )
                    clearJelly(mutableBoard, r, c)
                    applyGravity(mutableBoard)
                    refillBoard(mutableBoard)
                    scoreGained = 250
                }
            }
            BoosterType.SHUFFLE -> {
                shuffleBoard(mutableBoard)
                scoreGained = 150
            }
            BoosterType.ROW_BLAST -> {
                targetCell?.let { (r, _) ->
                    for (c in 0 until cols) {
                        mutableBoard[r][c] = mutableBoard[r][c].copy(piece = null)
                        clearJelly(mutableBoard, r, c)
                    }
                    applyGravity(mutableBoard)
                    refillBoard(mutableBoard)
                    scoreGained = 800
                }
            }
            BoosterType.COLUMN_BLAST -> {
                targetCell?.let { (_, c) ->
                    for (r in 0 until rows) {
                        mutableBoard[r][c] = mutableBoard[r][c].copy(piece = null)
                        clearJelly(mutableBoard, r, c)
                    }
                    applyGravity(mutableBoard)
                    refillBoard(mutableBoard)
                    scoreGained = 800
                }
            }
            BoosterType.COLOR_BOMB -> {
                targetCell?.let { (r, c) ->
                    val cell = mutableBoard[r][c]
                    mutableBoard[r][c] = cell.copy(
                        piece = GamePiece(
                            id = pieceIdCounter++,
                            type = PieceType.AMETHYST,
                            special = SpecialPiece.COLOR_BOMB
                        )
                    )
                }
            }
            BoosterType.AREA_BOMB -> {
                targetCell?.let { (r, c) ->
                    val cell = mutableBoard[r][c]
                    mutableBoard[r][c] = cell.copy(
                        piece = GamePiece(
                            id = pieceIdCounter++,
                            type = cell.piece?.type ?: PieceType.RUBY,
                            special = SpecialPiece.AREA_BOMB
                        )
                    )
                }
            }
            BoosterType.EXTRA_MOVES -> {
                // Handled in ViewModel / GameState
            }
        }

        val cascade = processCascades(mutableBoard, detectAllMatches(mutableBoard), bonusScore = scoreGained)
        return cascade.first to cascade.second.totalScore
    }
}
