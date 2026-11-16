package com.chesslab

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.move.Move

class ChessBoardView(
    context: Context,
    private val changed: () -> Unit
) : View(context) {

    private val board = Board()
    private val brush = Paint(Paint.ANTI_ALIAS_FLAG)

    private var selected: String? = null
    private var legal: List<Move> = emptyList()

    private val history = mutableListOf<String>()
    private val positions = mutableListOf<String>()

    private var flipped = false

    private val pieces = mapOf(
        "WHITE_KING" to "♔",
        "WHITE_QUEEN" to "♕",
        "WHITE_ROOK" to "♖",
        "WHITE_BISHOP" to "♗",
        "WHITE_KNIGHT" to "♘",
        "WHITE_PAWN" to "♙",

        "BLACK_KING" to "♚",
        "BLACK_QUEEN" to "♛",
        "BLACK_ROOK" to "♜",
        "BLACK_BISHOP" to "♝",
        "BLACK_KNIGHT" to "♞",
        "BLACK_PAWN" to "♟"
    )

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val size = minOf(width, height).toFloat()
        val left = (width - size) / 2f
        val top = (height - size) / 2f
        val cell = size / 8f

        val last = history.lastOrNull()

        for (r in 0..7) {
            for (c in 0..7) {

                val fx = if (flipped) 7 - c else c
                val fy = if (flipped) 7 - r else r

                val square =
                    "${(97 + fx).toChar()}${8 - fy}"

                val x = left + c * cell
                val y = top + r * cell

                brush.color =
                    if ((fx + fy) % 2 == 0)
                        Color.rgb(238, 238, 210)
                    else
                        Color.rgb(118, 150, 86)

                canvas.drawRect(
                    x, y,
                    x + cell, y + cell,
                    brush
                )

                if (
                    last != null &&
                    (square == last.substring(0, 2) ||
                    square == last.substring(2, 4))
                ) {
                    brush.color =
                        Color.argb(90, 255, 215, 0)

                    canvas.drawRect(
                        x, y,
                        x + cell, y + cell,
                        brush
                    )
                }

                if (selected == square) {
                    brush.color =
                        Color.argb(100, 30, 144, 255)

                    canvas.drawRect(
                        x, y,
                        x + cell, y + cell,
                        brush
                    )
                }

                if (
                    legal.any {
                        it.to.toString().lowercase() == square
                    }
                ) {
                    brush.color =
                        Color.argb(125, 20, 20, 20)

                    canvas.drawCircle(
                        x + cell / 2,
                        y + cell / 2,
                        cell * .12f,
                        brush
                    )
                }

                val piece = board.getPiece(
                    Square.valueOf(square.uppercase())
                ).toString()

                val glyph = pieces[piece] ?: ""

                if (glyph.isNotEmpty()) {

                    brush.textAlign = Paint.Align.CENTER
                    brush.textSize = cell * .76f

                    brush.typeface =
                        android.graphics.Typeface.create(
                            "serif",
                            0
                        )

                    brush.color =
                        if (piece.startsWith("WHITE"))
                            Color.WHITE
                        else
                            Color.rgb(25, 28, 35)

                    brush.setShadowLayer(
                        1.5f, 0f, 1f, Color.DKGRAY
                    )

                    canvas.drawText(
                        glyph,
                        x + cell / 2,
                        y + cell * .77f,
                        brush
                    )

                    brush.clearShadowLayer()
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        if (event.action != MotionEvent.ACTION_UP)
            return true

        val size = minOf(width, height).toFloat()
        val left = (width - size) / 2f
        val top = (height - size) / 2f
        val cell = size / 8f

        val c = ((event.x - left) / cell).toInt()
        val r = ((event.y - top) / cell).toInt()

        if (c !in 0..7 || r !in 0..7)
            return true

        val fx = if (flipped) 7 - c else c
        val fy = if (flipped) 7 - r else r

        val square =
            "${(97 + fx).toChar()}${8 - fy}"

        if (selected != null) {

            val move = legal.firstOrNull {
                it.from.toString().lowercase() == selected &&
                it.to.toString().lowercase() == square
            }

            if (move != null) {

                positions.add(board.fen)

                board.doMove(move)

                history.add(
                    move.toString().lowercase()
                )

                selected = null
                legal = emptyList()

                invalidate()
                changed()

                return true
            }
        }

        val piece = board.getPiece(
            Square.valueOf(square.uppercase())
        ).toString()

        if (
            piece != "NONE" &&
            piece.startsWith(board.sideToMove.toString())
        ) {
            selected = square

            legal = board.legalMoves().filter {
                it.from.toString().lowercase() == square
            }

        } else {
            selected = null
            legal = emptyList()
        }

        invalidate()
        return true
    }

    fun undo() {
        if (positions.isNotEmpty()) {

            board.loadFromFen(
                positions.removeAt(positions.lastIndex)
            )

            if (history.isNotEmpty()) {
                history.removeAt(history.lastIndex)
            }

            selected = null
            legal = emptyList()

            invalidate()
        }
    }

    fun reset() {
        board.loadFromFen(Board().fen)

        positions.clear()
        history.clear()

        selected = null
        legal = emptyList()

        invalidate()
    }

    fun flip() {
        flipped = !flipped
        invalidate()
    }

    fun gameStatus(): String =
        when {
            board.isMated -> "Checkmate"
            board.isDraw -> "Draw"
            board.isKingAttacked ->
                "${board.sideToMove} is in check"

            else ->
                board.sideToMove.toString()
                    .lowercase()
                    .replaceFirstChar { it.uppercase() } +
                    " to move"
        }

    fun moveHistory(): String =
        if (history.isEmpty()) {
            "Tap a piece, then a highlighted square"
        } else {
            history.mapIndexed { i, move ->
                if (i % 2 == 0)
                    "${i / 2 + 1}. $move"
                else
                    move
            }.joinToString("   ")
        }
}
