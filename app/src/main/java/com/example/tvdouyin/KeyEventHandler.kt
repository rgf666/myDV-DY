package com.example.tvdouyin

import android.view.KeyEvent
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class KeyEventHandler(
    private val webView: WebView,
    private val activity: AppCompatActivity
) {

    private var currentVideoIndex = 0

    fun handleKeyEvent(keyCode: Int, event: KeyEvent?): Boolean {
        if (event?.action != KeyEvent.ACTION_DOWN) {
            return false
        }

        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> {
                // 上键：滚动到上一个视频
                scrollToPreviousVideo()
                showToast("上一个视频")
                return true
            }

            KeyEvent.KEYCODE_DPAD_DOWN -> {
                // 下键：滚动到下一个视频
                scrollToNextVideo()
                showToast("下一个视频")
                return true
            }

            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                // 确认键：点击视频播放/暂停
                toggleVideoPlayback()
                return true
            }

            KeyEvent.KEYCODE_DPAD_LEFT -> {
                // 左键：返回上一页或退出全屏
                if (webView.canGoBack()) {
                    webView.goBack()
                    showToast("返回上一页")
                } else {
                    showToast("已是最初页面")
                }
                return true
            }

            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                // 右键：全屏或显示菜单
                toggleFullscreen()
                return true
            }

            KeyEvent.KEYCODE_BACK -> {
                // 返回键：退出应用或返回
                if (webView.canGoBack()) {
                    webView.goBack()
                    return true
                }
            }

            KeyEvent.KEYCODE_MENU -> {
                // 菜单键：显示选项（可选实现）
                showToast("菜单功能开发中...")
                return true
            }
        }

        return false
    }

    private fun scrollToNextVideo() {
        currentVideoIndex++
        
        // 通过JavaScript滚动到下一个视频
        val jsCode = """
            (function() {
                // 查找所有视频元素或滚动容器
                const videoItems = document.querySelectorAll('[class*="video-item"], [class*="feed-item"], article, .video-card');
                
                if (videoItems.length > 0) {
                    // 计算下一个视频的位置
                    const nextIndex = Math.min($currentVideoIndex, videoItems.length - 1);
                    const targetVideo = videoItems[nextIndex];
                    
                    if (targetVideo) {
                        // 平滑滚动到目标视频
                        targetVideo.scrollIntoView({
                            behavior: 'smooth',
                            block: 'center'
                        });
                        
                        // 尝试自动播放视频
                        setTimeout(function() {
                            const video = targetVideo.querySelector('video');
                            if (video && video.paused) {
                                video.play().catch(function(e) {
                                    console.log('Auto-play prevented:', e);
                                });
                            }
                        }, 500);
                        
                        // 通知Android端
                        if (window.AndroidBridge) {
                            window.AndroidBridge.onVideoChanged('next', nextIndex);
                        }
                    }
                } else {
                    // 如果没有找到视频项，尝试滚动一屏
                    const videoHeight = window.innerHeight;
                    window.scrollBy({
                        top: videoHeight,
                        behavior: 'smooth'
                    });
                    
                    if (window.AndroidBridge) {
                        window.AndroidBridge.onVideoChanged('next', -1);
                    }
                }
            })();
        """.trimIndent()

        webView.evaluateJavascript(jsCode, null)
    }

    private fun scrollToPreviousVideo() {
        if (currentVideoIndex > 0) {
            currentVideoIndex--
        }
        
        val jsCode = """
            (function() {
                const videoItems = document.querySelectorAll('[class*="video-item"], [class*="feed-item"], article, .video-card');
                
                if (videoItems.length > 0) {
                    const prevIndex = Math.max($currentVideoIndex, 0);
                    const targetVideo = videoItems[prevIndex];
                    
                    if (targetVideo) {
                        targetVideo.scrollIntoView({
                            behavior: 'smooth',
                            block: 'center'
                        });
                        
                        setTimeout(function() {
                            const video = targetVideo.querySelector('video');
                            if (video && video.paused) {
                                video.play().catch(function(e) {
                                    console.log('Auto-play prevented:', e);
                                });
                            }
                        }, 500);
                        
                        if (window.AndroidBridge) {
                            window.AndroidBridge.onVideoChanged('previous', prevIndex);
                        }
                    }
                } else {
                    const videoHeight = window.innerHeight;
                    window.scrollBy({
                        top: -videoHeight,
                        behavior: 'smooth'
                    });
                    
                    if (window.AndroidBridge) {
                        window.AndroidBridge.onVideoChanged('previous', -1);
                    }
                }
            })();
        """.trimIndent()

        webView.evaluateJavascript(jsCode, null)
    }

    private fun toggleVideoPlayback() {
        val jsCode = """
            (function() {
                // 查找当前可视区域内的视频
                const videos = document.querySelectorAll('video');
                let activeVideo = null;
                
                // 找到正在播放或可视的视频
                for (let video of videos) {
                    const rect = video.getBoundingClientRect();
                    if (rect.top >= 0 && rect.bottom <= window.innerHeight) {
                        activeVideo = video;
                        break;
                    }
                }
                
                // 如果没有找到可视视频，取第一个
                if (!activeVideo && videos.length > 0) {
                    activeVideo = videos[0];
                }
                
                if (activeVideo) {
                    if (activeVideo.paused) {
                        activeVideo.play().then(function() {
                            if (window.AndroidBridge) {
                                window.AndroidBridge.onVideoPlaying();
                            }
                        }).catch(function(e) {
                            console.log('Play failed:', e);
                            if (window.AndroidBridge) {
                                window.AndroidBridge.onPlayError(e.message);
                            }
                        });
                    } else {
                        activeVideo.pause();
                        if (window.AndroidBridge) {
                            window.AndroidBridge.onVideoPaused();
                        }
                    }
                } else {
                    console.log('No video found');
                    if (window.AndroidBridge) {
                        window.AndroidBridge.onNoVideoFound();
                    }
                }
            })();
        """.trimIndent()

        webView.evaluateJavascript(jsCode, null)
    }

    private fun toggleFullscreen() {
        val jsCode = """
            (function() {
                const video = document.querySelector('video');
                if (video) {
                    if (!document.fullscreenElement) {
                        video.requestFullscreen().then(function() {
                            if (window.AndroidBridge) {
                                window.AndroidBridge.onFullscreenEnter();
                            }
                        }).catch(function(e) {
                            console.log('Fullscreen request failed:', e);
                        });
                    } else {
                        document.exitFullscreen().then(function() {
                            if (window.AndroidBridge) {
                                window.AndroidBridge.onFullscreenExit();
                            }
                        }).catch(function(e) {
                            console.log('Exit fullscreen failed:', e);
                        });
                    }
                }
            })();
        """.trimIndent()

        webView.evaluateJavascript(jsCode, null)
    }

    private fun showToast(message: String) {
        activity.runOnUiThread {
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        }
    }

    fun resetVideoIndex() {
        currentVideoIndex = 0
    }
}
