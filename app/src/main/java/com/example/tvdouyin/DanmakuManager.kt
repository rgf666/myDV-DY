package com.example.tvdouyin

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import java.util.Random
import kotlin.math.abs

class DanmakuManager(private val context: Context, private val container: FrameLayout) {

    private val danmakuList = mutableListOf<TextView>()
    private val random = Random()
    private var isRunning = false
    private var nextDanmakuY = 0
    private val danmakuHeight = 60 // Height of each danmaku row
    private val maxDanmakuRows = 5 // Maximum number of danmaku rows

    /**
     * Start danmaku display
     */
    fun start() {
        isRunning = true
    }

    /**
     * Stop danmaku display
     */
    fun stop() {
        isRunning = false
        clearAll()
    }

    /**
     * Add a danmaku message
     */
    fun addDanmaku(message: String, color: Int = Color.WHITE) {
        if (!isRunning) return

        val textView = TextView(context).apply {
            text = message
            setTextColor(color)
            textSize = 16f
            setShadowLayer(2f, 1f, 1f, Color.BLACK)
        }

        // Random position
        val yPosition = getNextDanmakuPosition()
        val layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = yPosition
            leftMargin = container.width // Start from right edge
            gravity = Gravity.TOP
        }

        textView.layoutParams = layoutParams
        container.addView(textView)
        danmakuList.add(textView)

        // Animate danmaku
        animateDanmaku(textView)
    }

    /**
     * Get next danmaku Y position (avoid overlap)
     */
    private fun getNextDanmakuPosition(): Int {
        nextDanmakuY += danmakuHeight
        if (nextDanmakuY > container.height - danmakuHeight * maxDanmakuRows) {
            nextDanmakuY = 0
        }
        return nextDanmakuY
    }

    /**
     * Animate danmaku from right to left
     */
    private fun animateDanmaku(textView: TextView) {
        textView.post {
            val startX = container.width.toFloat()
            val endX = -textView.width.toFloat()
            val duration = 5000L + random.nextInt(3000) // 5-8 seconds

            textView.animate()
                .translationX(endX)
                .setDuration(duration)
                .withEndAction {
                    container.removeView(textView)
                    danmakuList.remove(textView)
                }
                .start()
        }
    }

    /**
     * Simulate danmaku from video comments
     */
    fun simulateDanmakuFromComments(comments: List<String>) {
        if (comments.isEmpty()) return

        // Add danmaku every 1-3 seconds
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                if (!isRunning) return

                val randomComment = comments.random()
                val randomColor = getRandomColor()
                addDanmaku(randomComment, randomColor)

                // Schedule next danmaku
                handler.postDelayed(this, (1000 + random.nextInt(2000)).toLong())
            }
        }

        handler.post(runnable)
    }

    /**
     * Get random color for danmaku
     */
    private fun getRandomColor(): Int {
        val colors = listOf(
            Color.WHITE,
            Color.YELLOW,
            Color.CYAN,
            Color.GREEN,
            Color.parseColor("#FF9800") // Orange
        )
        return colors.random()
    }

    /**
     * Clear all danmaku
     */
    fun clearAll() {
        danmakuList.forEach { textView ->
            container.removeView(textView)
        }
        danmakuList.clear()
        nextDanmakuY = 0
    }

    /**
     * Parse danmaku from JavaScript
     */
    fun onDanmakuReceived(message: String) {
        addDanmaku(message)
    }
}
