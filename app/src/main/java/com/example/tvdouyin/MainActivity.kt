package com.example.tvdouyin

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import android.view.KeyEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.webkit.ConsoleMessage
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var keyEventHandler: KeyEventHandler
    private lateinit var danmakuManager: DanmakuManager
    private lateinit var gestureHandler: GestureHandler
    private lateinit var navigationBar: NavigationBar
    private lateinit var container: FrameLayout
    private lateinit var danmakuContainer: FrameLayout
    private lateinit var navigationContainer: FrameLayout
    private lateinit var leftRegion: View
    private lateinit var centerRegion: View
    private lateinit var rightRegion: View

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupWebView()
        setupDanmaku()
        setupGesture()
        setupNavigation()
        setupVideoControls()

        // 初始化按键处理
        keyEventHandler = KeyEventHandler(webView, this)

        // 加载抖音精选页面
        loadDouyinPage()
    }

    private fun initViews() {
        webView = findViewById(R.id.webview)
        progressBar = findViewById(R.id.progressBar)
        container = findViewById(R.id.container)
        danmakuContainer = findViewById(R.id.danmaku_container)
        navigationContainer = findViewById(R.id.navigation_container)
        leftRegion = findViewById(R.id.left_region)
        centerRegion = findViewById(R.id.center_region)
        rightRegion = findViewById(R.id.right_region)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val settings: WebSettings = webView.settings

        // 启用JavaScript（必须）
        settings.javaScriptEnabled = true

        // 设置User-Agent为PC版（避免移动端重定向）
        settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

        // DOM存储
        settings.domStorageEnabled = true

        // 数据库
        settings.databaseEnabled = true

        // 支持缩放
        settings.setSupportZoom(true)
        settings.builtInZoomControls = true
        settings.displayZoomControls = false

        // 自适应屏幕
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true

        // 缓存模式
        settings.cacheMode = WebSettings.LOAD_DEFAULT

        // 硬件加速
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        // 应用保存的Cookie
        applySavedCookie()

        // 设置WebViewClient（拦截页面跳转）
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                url?.let { view?.loadUrl(it) }
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // 页面加载完成后注入优化CSS
                injectCss()
                // 注入JavaScript桥接
                injectJavaScript()
                // 启动弹幕
                danmakuManager.start()
            }
        }

        // 设置ChromeClient（处理全屏视频和进度）
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                if (newProgress < 100) {
                    progressBar.visibility = View.VISIBLE
                } else {
                    progressBar.visibility = View.GONE
                }
            }

            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                consoleMessage?.let {
                    android.util.Log.d("WebView", "Console: ${it.message()}")
                }
                return super.onConsoleMessage(consoleMessage)
            }
        }

        // 注入JavaScript桥接
        webView.addJavascriptInterface(JsBridge(danmakuManager), "AndroidBridge")
    }

    /**
     * 应用保存的Cookie
     */
    private fun applySavedCookie() {
        if (SettingsActivity.hasSavedCookie(this)) {
            val cookieString = SettingsActivity.getSavedCookie(this)
            val cookieManager = android.webkit.CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)

            val cookies = cookieString.split(";").map { it.trim() }
            val douyinDomains = listOf(
                "https://www.douyin.com",
                "https://douyin.com"
            )

            for (domain in douyinDomains) {
                for (cookie in cookies) {
                    cookieManager.setCookie(domain, cookie)
                }
            }

            cookieManager.flush()
        }
    }

    /**
     * 设置弹幕管理器
     */
    private fun setupDanmaku() {
        danmakuManager = DanmakuManager(this, danmakuContainer)

        // 模拟弹幕（实际应该从视频评论中获取）
        val sampleComments = listOf(
            "哈哈哈哈哈",
            "这个视频太搞笑了",
            "666666",
            "前排围观",
            "抖音精选就是牛",
            "看完点赞！",
            "转发了",
            "太棒了！",
            "笑死我了",
            " everyday"
        )

        // 延迟启动弹幕模拟
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            danmakuManager.simulateDanmakuFromComments(sampleComments)
        }, 5000)
    }

    /**
     * 设置手势处理
     */
    private fun setupGesture() {
        gestureHandler = GestureHandler(this, webView) { seekTime ->
            Toast.makeText(this, "快进/快退: ${seekTime}ms", Toast.LENGTH_SHORT).show()
        }

        // 设置手势监听
        webView.setOnTouchListener { _, event ->
            gestureHandler.onTouchEvent(event)
        }
    }

    /**
     * 设置导航栏
     */
    private fun setupNavigation() {
        navigationBar = NavigationBar(this, container) { action ->
            when (action) {
                "back" -> {
                    if (webView.canGoBack()) {
                        webView.goBack()
                    }
                }
                "home" -> {
                    loadDouyinPage()
                }
                "refresh" -> {
                    webView.reload()
                }
                "settings" -> {
                    startActivity(android.content.Intent(this, SettingsActivity::class.java))
                }
                "exit" -> {
                    finish()
                }
            }
        }
    }

    /**
     * 设置视频控制区域
     */
    private fun setupVideoControls() {
        // 左侧区域 - 显示推荐面板
        leftRegion.setOnClickListener {
            showVideoRecommendations()
        }

        // 中间区域 - 播放/暂停
        centerRegion.setOnClickListener {
            toggleVideoPlayPause()
        }

        // 右侧区域 - 显示进度条
        rightRegion.setOnClickListener {
            showVideoProgress()
        }
    }

    /**
     * 显示视频推荐面板
     */
    private fun showVideoRecommendations() {
        val jsCode = """
            (function() {
                // 点击左侧区域，触发推荐面板
                const leftPanel = document.querySelector('[class*="recommend"], [class*="related"]');
                if (leftPanel) {
                    leftPanel.style.display = 'block';
                }
                
                // 或者通过JavaScript触发推荐
                if (window.AndroidBridge) {
                    window.AndroidBridge.onLeftPanelClicked();
                }
            })();
        """.trimIndent()

        webView.evaluateJavascript(jsCode, null)
        Toast.makeText(this, "显示推荐面板", Toast.LENGTH_SHORT).show()
    }

    /**
     * 切换视频播放/暂停
     */
    private fun toggleVideoPlayPause() {
        val jsCode = """
            (function() {
                const video = document.querySelector('video');
                if (video) {
                    if (video.paused) {
                        video.play();
                        if (window.AndroidBridge) {
                            window.AndroidBridge.onVideoPlaying();
                        }
                    } else {
                        video.pause();
                        if (window.AndroidBridge) {
                            window.AndroidBridge.onVideoPaused();
                        }
                    }
                }
            })();
        """.trimIndent()

        webView.evaluateJavascript(jsCode, null)
    }

    /**
     * 显示视频进度条
     */
    private fun showVideoProgress() {
        val jsCode = """
            (function() {
                // 显示进度条
                const progressBar = document.querySelector('[class*="progress"], [class*="seekbar"]');
                if (progressBar) {
                    progressBar.style.display = 'block';
                    setTimeout(() => {
                        progressBar.style.display = 'none';
                    }, 3000);
                }
                
                // 或者通过JavaScript获取进度
                const video = document.querySelector('video');
                if (video && window.AndroidBridge) {
                    const current = Math.floor(video.currentTime);
                    const duration = Math.floor(video.duration);
                    window.AndroidBridge.onProgressUpdate(current, duration);
                }
            })();
        """.trimIndent()

        webView.evaluateJavascript(jsCode, null)
        Toast.makeText(this, "显示进度条", Toast.LENGTH_SHORT).show()
    }

    private fun loadDouyinPage() {
        progressBar.visibility = View.VISIBLE
        webView.loadUrl("https://www.douyin.com/jingxuan/c++")
    }

    private fun injectCss() {
        val css = """
            javascript:(function() {
                var style = document.createElement('style');
                style.type = 'text/css';
                style.innerHTML = `
                    /* 隐藏网页版的不必要元素 */
                    .header, .footer, .sidebar, 
                    [class*="header"], [class*="footer"], 
                    [class*="sidebar"], [class*="nav"] {
                        display: none !important;
                    }
                    
                    /* 优化视频容器 */
                    .video-feed, [class*="video-feed"],
                    [class*="feed-container"] {
                        width: 100vw !important;
                        height: 100vh !important;
                        margin:0 !important;
                        padding:0 !important;
                    }
                    
                    /* 视频全屏 */
                    video {
                        width: 100% !important;
                        height: 100% !important;
                        object-fit: contain !important;
                    }
                    
                    /* 隐藏滚动条 */
                    ::-webkit-scrollbar {
                        display: none;
                    }
                    body {
                        overflow: hidden;
                        margin:0;
                        padding:0;
                    }
                    
                    /* 隐藏点赞、评论等覆盖层（可选） */
                    [class*="like"], [class*="comment"],
                    [class*="share"], [class*="interaction"] {
                        opacity: 0.5;
                    }
                `;
                document.head.appendChild(style);
                
                // 通知Android端页面已优化
                if (window.AndroidBridge) {
                    window.AndroidBridge.onPageOptimized();
                }
            })();
        """.trimIndent()

        webView.evaluateJavascript(css, null)
    }

    private fun injectJavaScript() {
        val js = """
            javascript:(function() {
                // 监听视频播放事件
                document.addEventListener('play', function(e) {
                    if (e.target.tagName === 'VIDEO') {
                        if (window.AndroidBridge) {
                            window.AndroidBridge.onVideoPlaying();
                        }
                    }
                }, true);
                
                // 监听视频暂停事件
                document.addEventListener('pause', function(e) {
                    if (e.target.tagName === 'VIDEO') {
                        if (window.AndroidBridge) {
                            window.AndroidBridge.onVideoPaused();
                        }
                    }
                }, true);
                
                // 监听视频结束事件
                document.addEventListener('ended', function(e) {
                    if (e.target.tagName === 'VIDEO') {
                        if (window.AndroidBridge) {
                            window.AndroidBridge.onVideoEnded();
                        }
                    }
                }, true);
                
                // 监听视频时间更新（用于弹幕同步）
                document.addEventListener('timeupdate', function(e) {
                    if (e.target.tagName === 'VIDEO') {
                        const video = e.target;
                        if (window.AndroidBridge) {
                            window.AndroidBridge.onVideoTimeUpdate(video.currentTime, video.duration);
                        }
                    }
                }, true);
                
                console.log('JavaScript injected successfully');
            })();
        """.trimIndent()

        webView.evaluateJavascript(js, null)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // 处理导航栏显示
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (navigationBar.isShowing()) {
                navigationBar.hide()
                return true
            } else {
                navigationBar.show()
                return true
            }
        }

        // 将按键事件交给KeyEventHandler处理
        if (keyEventHandler.handleKeyEvent(keyCode, event)) {
            return true
        }

        // 处理返回键（网页后退）
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            return true
        }

        return super.onKeyDown(keyCode, event)
    }

    override fun onBackPressed() {
        if (!navigationBar.onBackPressed()) {
            super.onBackPressed()
        }
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
        danmakuManager.stop()
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
        danmakuManager.start()
    }

    override fun onDestroy() {
        danmakuManager.stop()
        webView.destroy()
        super.onDestroy()
    }
}
