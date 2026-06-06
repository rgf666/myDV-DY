package com.example.tvdouyin

import android.util.Log
import android.webkit.JavascriptInterface
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class JsBridge(private val danmakuManager: DanmakuManager? = null) {

    private val TAG = "JsBridge"

    /**
     * 视频切换时调用
     */
    @JavascriptInterface
    fun onVideoChanged(direction: String, index: Int) {
        Log.d(TAG, "Video changed: direction=$direction, index=$index")
    }

    /**
     * 视频开始播放时调用
     */
    @JavascriptInterface
    fun onVideoPlaying() {
        Log.d(TAG, "Video is playing")
    }

    /**
     * 视频暂停时调用
     */
    @JavascriptInterface
    fun onVideoPaused() {
        Log.d(TAG, "Video is paused")
    }

    /**
     * 视频结束时调用
     */
    @JavascriptInterface
    fun onVideoEnded() {
        Log.d(TAG, "Video ended")
    }

    /**
     * 页面已优化（CSS注入完成）
     */
    @JavascriptInterface
    fun onPageOptimized() {
        Log.d(TAG, "Page optimized for TV")
    }

    /**
     * 进入全屏模式
     */
    @JavascriptInterface
    fun onFullscreenEnter() {
        Log.d(TAG, "Entered fullscreen mode")
    }

    /**
     * 退出全屏模式
     */
    @JavascriptInterface
    fun onFullscreenExit() {
        Log.d(TAG, "Exited fullscreen mode")
    }

    /**
     * 播放错误
     */
    @JavascriptInterface
    fun onPlayError(errorMessage: String?) {
        Log.e(TAG, "Play error: $errorMessage")
    }

    /**
     * 未找到视频
     */
    @JavascriptInterface
    fun onNoVideoFound() {
        Log.w(TAG, "No video found on page")
    }

    /**
     * 日志输出
     */
    @JavascriptInterface
    fun logMessage(message: String?) {
        Log.d(TAG, "JS Log: $message")
    }

    /**
     * 显示Toast（需要在UI线程）
     */
    @JavascriptInterface
    fun showToast(message: String?) {
        Log.d(TAG, "Toast requested: $message")
        // 注意：JavaScript调用时需要在UI线程显示Toast
        // 这里只记录日志，实际Toast需要在Activity中处理
    }

    /**
     * 接收弹幕消息（从JavaScript调用）
     */
    @JavascriptInterface
    fun onDanmakuReceived(message: String?) {
        Log.d(TAG, "Danmaku received: $message")
        message?.let {
            danmakuManager?.onDanmakuReceived(it)
        }
    }

    /**
     * 视频时间更新（用于弹幕同步）
     * @param currentTime 当前播放时间（秒）
     * @param duration 视频总时长（秒）
     */
    @JavascriptInterface
    fun onVideoTimeUpdate(currentTime: Double, duration: Double) {
        Log.d(TAG, "Video time update: $currentTime / $duration")
        // 可以在这里同步弹幕显示
        // 例如：根据currentTime显示对应时间的弹幕
    }

    /**
     * 左侧面板被点击（显示推荐视频）
     */
    @JavascriptInterface
    fun onLeftPanelClicked() {
        Log.d(TAG, "Left panel clicked - show recommendations")
    }

    /**
     * 进度条更新
     * @param current 当前播放时间（秒）
     * @param duration 视频总时长（秒）
     */
    @JavascriptInterface
    fun onProgressUpdate(current: Double, duration: Double) {
        Log.d(TAG, "Progress update: $current / $duration")
    }

    /**
     * 视频快进/快退
     * @param seekTime 快进/快退时间（毫秒）
     */
    @JavascriptInterface
    fun onVideoSeek(seekTime: Int) {
        Log.d(TAG, "Video seek: $seekTime ms")
    }

    /**
     * 接收视频评论（用于弹幕显示）
     * @param comments JSON数组格式的评论列表
     */
    @JavascriptInterface
    fun onVideoCommentsReceived(comments: String?) {
        Log.d(TAG, "Video comments received: $comments")
        // 可以解析comments JSON，然后添加到danmakuManager
    }

    /**
     * 页面加载完成
     */
    @JavascriptInterface
    fun onPageLoaded() {
        Log.d(TAG, "Page loaded completely")
    }

    /**
     * 用户登录状态变化
     */
    @JavascriptInterface
    fun onLoginStatusChanged(isLoggedIn: Boolean) {
        Log.d(TAG, "Login status changed: $isLoggedIn")
    }
}
