package com.chesslab

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.*

class MainActivity : Activity() {

    private lateinit var chess: ChessBoardView
    private lateinit var status: TextView
    private lateinit var history: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.rgb(18, 22, 29)
        window.navigationBarColor = Color.rgb(18, 22, 29)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(14, 14, 14, 10)
            setBackgroundColor(Color.rgb(18, 22, 29))
        }

        val title = TextView(this).apply {
            text = "CHESSLAB"
            textSize = 26f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
        }

        status = TextView(this).apply {
            textSize = 15f
            gravity = Gravity.CENTER
            setTextColor(Color.LTGRAY)
            setPadding(0, 10, 0, 10)
        }

        chess = ChessBoardView(this) {
            refresh()
        }

        history = TextView(this).apply {
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(Color.LTGRAY)
            setPadding(4, 10, 4, 8)
        }

        val controls = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        controls.addView(
            button("Undo") {
                chess.undo()
                refresh()
            },
            weighted()
        )

        controls.addView(
            button("New game") {
                chess.reset()
                refresh()
            },
            weighted()
        )

        controls.addView(
            button("Flip") {
                chess.flip()
            },
            weighted()
        )

        root.addView(title, LinearLayout.LayoutParams(-1, 48))
        root.addView(status, LinearLayout.LayoutParams(-1, 40))
        root.addView(chess, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(history, LinearLayout.LayoutParams(-1, 72))
        root.addView(controls, LinearLayout.LayoutParams(-1, 54))

        setContentView(root)

        refresh()
    }

    private fun weighted() =
        LinearLayout.LayoutParams(0, -1, 1f).apply {
            setMargins(3, 2, 3, 2)
        }

    private fun button(
        label: String,
        action: () -> Unit
    ) = Button(this).apply {
        text = label
        isAllCaps = false
        setOnClickListener { action() }
    }

    private fun refresh() {
        status.text = chess.gameStatus()
        history.text = chess.moveHistory()
    }
}
