package com.chesslab

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.move.Move

object DetectIllegalMove {

    fun isLegal(
        board: Board,
        move: Move
    ): Boolean {
        return board.legalMoves().contains(move)
    }
}
