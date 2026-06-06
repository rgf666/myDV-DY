package com.example.tvdouyin

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.webkit.WebView
import kotlin.math.abs

class GestureHandler(
    private val context: Context,
    private val webView: WebView,
    private val onVideoSeek: (Int) -> Unit // Callback for seek (milliseconds)
) {

    private var initialX = 0f
    private var initialY = 0f
    private var isSeeking = false
    private val swipeThreshold = 100 // Minimum swipe distance
    private val seekIncrement = 5000 // 5 seconds per swipe

    /**
     * Handle touch events for gesture detection
     */
    fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = event.x
                initialY = event.y
                isSeeking = false
                return false
            }

            MotionEvent.ACTION_MOVE -> {
                val deltaX = event.x - initialX
                val deltaY = event.y - initialY

                // Check if horizontal swipe (seek) or vertical (volume/brightness)
                if (abs(deltaX) > abs(deltaY) && abs(deltaX) > 20) {
                    // Horizontal swipe - seek video
                    handleHorizontalSwipe(deltaX)
                    initialX = event.x // Reset for continuous swiping
                    return true
                }
            }

            MotionEvent.ACTION_UP -> {
                if (isSeeking) {
                    // Seek completed
                    isSeeking = false
                    return true
                }
            }
        }

        return false
    }

    /**
     * Handle horizontal swipe for video seeking
     */
    private fun handleHorizontalSwipe(deltaX: Float) {
        if (abs(deltaX) < swipeThreshold) return

        isSeeking = true
        val direction = if (deltaX > 0) 1 else -1 // 1 = forward, -1 = backward
        val seekTime = direction * seekIncrement

        // Seek video via JavaScript
        seekVideo(seekTime)

        // Notify callback
        onVideoSeek(seekTime)
    }

    /**
     * Seek video via JavaScript injection
     */
    private fun seekVideo(milliseconds: Int) {
        val jsCode = """
            (function() {
                const video = document.querySelector('video');
                if (video) {
                    const newTime = video.currentTime + ($milliseconds / 1000);
                    video.currentTime = Math.max(0, Math.min(newTime, video.duration));
                    
                    // Show seek indicator (optional UI feedback)
                    if (window.AndroidBridge) {
                        window.AndroidBridge.onVideoSeek($milliseconds);
                    }
                }
            })();
        """.trimIndent()

        webView.evaluateJavascript(jsCode, null)
    }

    /**
     * Setup gesture detection on a view
     */
    fun setupOnView(targetView: View) {
        targetView.setOnTouchListener { _, event ->
            onTouchEvent(event)
        }
    }

    /**
     * Get current seek position from video
     */
    fun getCurrentPosition(callback: (Int) -> Unit) {
        val jsCode = """
            (function() {
                const video = document.querySelector('video');
                if (video) {
                    return Math.floor(video.currentTime * 1000); // Convert to milliseconds
                }
                return 0;
            })();
        """.trimIndent()

        webView.evaluateJavascript(jsCode) { result ->
            val position = result?.toIntOrNull() ?: 0
            callback(position)
        }
    }

    /**
     * Get video duration
     */
    fun getDuration(callback: (Int) -> Unit) {
        val jsCode = """
            (function() {
                const video = document.querySelector('video');
                if (video) {
                    return Math.floor(video.duration * 1000); // Convert to milliseconds
                }
                return 0;
            })();
        """.trimIndent()

        webView.evaluateJavascript(jsCode) { result ->
            val duration = result?.toIntOrNull() ?: 0
            callback(duration)
        }
    }

    /**
     * Seek to specific position
     */
    fun seekTo(milliseconds: Int) {
        val jsCode = """
            (function() {
                const video = document.querySelector('video');
                if (video) {
                    video.currentTime = $milliseconds / 1000;
                }
            })();
        """.trimIndent()

        webView.evaluateJavascript(jsCode, null)
    }
}
