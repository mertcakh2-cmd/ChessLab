
package com.chesslab

object ChessMover {

    fun isUciMove(value: String): Boolean {
        return Regex(
            "^[a-h][1-8][a-h][1-8][qrbn]?$"
        ).matches(value.lowercase())
    }
}
