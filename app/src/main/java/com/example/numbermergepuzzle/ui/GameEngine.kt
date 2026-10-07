package com.example.numbermergepuzzle

import kotlin.random.Random

internal enum class MoveDirection {
    LEFT, RIGHT, UP, DOWN
}

internal data class PuzzleState(
    val board: List<List<Int>>,
    val score: Int,
    val hasWon: Boolean,
    val isGameOver: Boolean
)

internal object GameEngine {

    fun newGame(): PuzzleState {
        var board = List(4) { List(4) { 0 } }
        board = addRandomTile(board)
        board = addRandomTile(board)

        return PuzzleState(
            board = board,
            score = 0,
            hasWon = false,
            isGameOver = false
        )
    }

    fun move(
        game: PuzzleState,
        direction: MoveDirection
    ): PuzzleState {
        val updatedBoard = MutableList(4) { MutableList(4) { 0 } }
        var pointsGained = 0

        for (line in 0 until 4) {
            val coordinates = (0 until 4).map { step ->
                when (direction) {
                    MoveDirection.LEFT -> line to step
                    MoveDirection.RIGHT -> line to (3 - step)
                    MoveDirection.UP -> step to line
                    MoveDirection.DOWN -> (3 - step) to line
                }
            }

            val values = coordinates.map { (row, column) ->
                game.board[row][column]
            }.filter { it != 0 }

            val merged = mutableListOf<Int>()
            var index = 0

            while (index < values.size) {
                if (index + 1 < values.size &&
                    values[index] == values[index + 1]
                ) {
                    val newValue = values[index] * 2
                    merged.add(newValue)
                    pointsGained += newValue
                    index += 2
                } else {
                    merged.add(values[index])
                    index++
                }
            }

            while (merged.size < 4) {
                merged.add(0)
            }

            coordinates.forEachIndexed { step, (row, column) ->
                updatedBoard[row][column] = merged[step]
            }
        }

        val movedBoard = updatedBoard.map { it.toList() }

        if (movedBoard == game.board) {
            return game
        }

        val boardWithNewTile = addRandomTile(movedBoard)
        val reached2048 = boardWithNewTile.any { row ->
            row.contains(2048)
        }

        return PuzzleState(
            board = boardWithNewTile,
            score = game.score + pointsGained,
            hasWon = game.hasWon || reached2048,
            isGameOver = !hasAvailableMove(boardWithNewTile)
        )
    }

    private fun addRandomTile(
        board: List<List<Int>>
    ): List<List<Int>> {
        val emptyCells = mutableListOf<Pair<Int, Int>>()

        for (row in 0 until 4) {
            for (column in 0 until 4) {
                if (board[row][column] == 0) {
                    emptyCells.add(row to column)
                }
            }
        }

        if (emptyCells.isEmpty()) {
            return board
        }

        val (row, column) = emptyCells.random()
        val updatedBoard = board.map { it.toMutableList() }

        updatedBoard[row][column] =
            if (Random.nextInt(10) == 0) 4 else 2

        return updatedBoard.map { it.toList() }
    }

    private fun hasAvailableMove(
        board: List<List<Int>>
    ): Boolean {
        for (row in 0 until 4) {
            for (column in 0 until 4) {
                if (board[row][column] == 0) {
                    return true
                }

                if (column < 3 &&
                    board[row][column] == board[row][column + 1]
                ) {
                    return true
                }

                if (row < 3 &&
                    board[row][column] == board[row + 1][column]
                ) {
                    return true
                }
            }
        }

        return false
    }
}