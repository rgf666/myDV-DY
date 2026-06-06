package com.example.tvdouyin

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast

class NavigationBar(
    private val context: Context,
    private val container: FrameLayout,
    private val onNavigate: (String) -> Unit // Callback for navigation
) {

    private var isVisible = false
    private var navigationView: View? = null

    /**
     * Show navigation bar
     */
    fun show() {
        if (isVisible) return

        isVisible = true
        createNavigationView()
    }

    /**
     * Hide navigation bar
     */
    fun hide() {
        if (!isVisible) return

        isVisible = false
        navigationView?.let {
            container.removeView(it)
            navigationView = null
        }
    }

    /**
     * Toggle navigation bar visibility
     */
    fun toggle() {
        if (isVisible) hide() else show()
    }

    /**
     * Check if navigation bar is visible
     */
    fun isShowing(): Boolean = isVisible

    /**
     * Create navigation bar view
     */
    private fun createNavigationView() {
        // Create a simple navigation bar with buttons
        val navView = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            setBackgroundColor(android.graphics.Color.parseColor("#CC00000"))
            setPadding(20, 20, 20, 20)
        }

        // Back button
        val backButton = createNavButton("返回") {
            onNavigate("back")
            hide()
        }

        // Home button
        val homeButton = createNavButton("首页") {
            onNavigate("home")
            hide()
        }

        // Refresh button
        val refreshButton = createNavButton("刷新") {
            onNavigate("refresh")
            hide()
        }

        // Settings button
        val settingsButton = createNavButton("设置") {
            onNavigate("settings")
            hide()
        }

        // Exit button
        val exitButton = createNavButton("退出") {
            onNavigate("exit")
            hide()
        }

        // Add buttons to navigation bar
        navView.addView(backButton)
        navView.addView(homeButton)
        navView.addView(refreshButton)
        navView.addView(settingsButton)
        navView.addView(exitButton)

        // Layout parameters
        val layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = android.view.Gravity.BOTTOM
        }

        navView.layoutParams = layoutParams
        container.addView(navView)
        navigationView = navView

        // Auto-hide after 5 seconds
        autoHide()
    }

    /**
     * Create navigation button
     */
    private fun createNavButton(text: String, onClick: () -> Unit): Button {
        return Button(context).apply {
            this.text = text
            setOnClickListener { onClick() }
            setBackgroundColor(android.graphics.Color.parseColor("#FF3700B3"))
            setTextColor(android.graphics.Color.WHITE)
            setPadding(30, 10, 30, 10)
        }
    }

    /**
     * Auto-hide navigation bar after delay
     */
    private fun autoHide() {
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        handler.postDelayed({
            if (isVisible) {
                hide()
            }
        }, 5000) // 5 seconds
    }

    /**
     * Handle back button press
     */
    fun onBackPressed(): Boolean {
        if (isVisible) {
            hide()
            return true
        }
        return false
    }
}
