# TvDouyinWebView 增强版功能说明

> **版本**: 2.0.0 (Enhanced Edition)
> **更新日期**: 2026-06-06
> **状态**: 开发完成，待测试

---

## 🎯 项目概述

TvDouyinWebView 是一个**功能完整的 Android TV 抖音精选应用**，基于 WebView 方案实现。在基础版（v1.0）之上，增加了**弹幕系统、手势操作、导航栏、Cookie 登录、设置页面**等高级功能。

### 📌 核心价值

- ✅ **无需逆向 API**：使用公开网页，合规风险低
- ✅ **完整 TV 适配**：遥控器 + 手势双操作模式
- ✅ **沉浸式体验**：弹幕 + 全屏视频
- ✅ **个性化登录**：Cookie 导入，解锁个人推荐

---

## 🆕 新增功能清单 (v1.0 → v2.0)

| 功能模块 | 状态 | 描述 |
|---------|------|------|
| 🎨 **弹幕系统** | ✅ 完成 | 视频播放时显示滚动弹幕 |
| 👆 **手势操作** | ✅ 完成 | 左右滑动快进/快退 |
| 🧭 **导航栏** | ✅ 完成 | 快速切换页面/刷新 |
| 🔐 **Cookie 登录** | ✅ 完成 | 导入 Cookie 解锁个人推荐 |
| ⚙️ **设置页面** | ✅ 完成 | 管理 Cookie 和应用配置 |

---

## 🎨 功能一：弹幕系统 (DanmakuManager.kt)

### 📌 功能描述

在视频播放时，屏幕上会显示从右到左滚动的弹幕文字，类似 Bilibili 的弹幕效果。

### ✨ 核心特性

#### 1️⃣ 弹幕显示逻辑
- **触发条件**：视频播放状态为 `playing` 时自动启动
- **停止条件**：视频暂停/结束/错误时自动停止
- **显示位置**：覆盖层（Overlay），位于 WebView 上方

#### 2️⃣ 弹幕内容生成
- **默认内容**：
  - `"抖音精选真好看"`
  - `"TV端体验太棒了"`
  - `"支持开发者"`
  - `"666"`
  - `"厉害了"`

#### 3️⃣ 弹幕样式配置
- **颜色随机**：白色、黄色、青色、绿色、橙色
- **字体大小**：`16sp ~ 24sp`（随机）
- **滚动速度**：每秒 `8~12 dp`
- **透明度**：`0.7f ~ 0.95f`（随机）
- **生命周期**：`6~10 秒`后自动消失

#### 4️⃣ 弹幕管理机制
- **最大数量**：同时显示最多 `50` 条弹幕
- **防重叠**：垂直位置随机分配（避免堆叠）
- **线程安全**：使用 `Handler` + `Runnable` 实现定时器
- **性能优化**：弹幕消失后自动移除 View，释放内存

### 🔧 技术实现

```kotlin
// DanmakuManager.kt 核心代码片段

class DanmakuManager(
    private val overlayView: ViewGroup,
    private val context: Context
) {
    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false
    private val activeDanmakus = mutableListOf<View>()
    
    // 启动弹幕
    fun start() {
        if (isRunning) return
        isRunning = true
        scheduleNextDanmaku()
    }
    
    // 停止弹幕
    fun stop() {
        isRunning = false
        handler.removeCallbacksAndMessages(null)
        clearAllDanmakus()
    }
    
    // 定时生成弹幕
    private fun scheduleNextDanmaku() {
        if (!isRunning) return
        
        handler.postDelayed({
            createDanmaku()
            scheduleNextDanmaku() // 循环调度
        }, 800..1500.random()) // 每 0.8~1.5 秒一条
    }
}
```

### 🎮 使用场景

**场景 1：观看热门视频**
```
用户打开应用 → 视频自动播放 → 弹幕开始滚动 → 沉浸式体验
```

**场景 2：互动体验**
```
朋友一起看电视 → 弹幕增加趣味性 → 类似影院效果
```

---

## 👆 功能二：手势操作 (GestureHandler.kt)

### 📌 功能描述

支持触摸屏设备的**左右滑动手势**，实现视频快进/快退功能。

### ✨ 核心特性

#### 1️⃣ 支持的手势类型
| 手势 | 功能 | 触发阈值 |
|------|------|---------|
| 👈 **左滑** | 快退 10 秒 | 距离 > 100px |
| 👉 **右滑** | 快进 10 秒 | 距离 > 100px |
| 👆 **上滑** | （预留）音量增加 | - |
| 👇 **下滑** | （预留）音量减少 | - |

#### 2️⃣ 手势检测算法
- 使用 `SimpleOnGestureListener` 监听手势
- 通过 `onFling()` 方法检测快速滑动
- 计算滑动距离和速度，判断是否触发快进/快退

#### 3️⃣ 视觉反馈
- **快进提示**：Toast 显示 "⏩ 快进 10 秒"
- **快退提示**：Toast 显示 "⏪ 快退 10 秒"
- **动画效果**：（可扩展）进度条动画

### 🔧 技术实现

```kotlin
// GestureHandler.kt 核心代码片段

class GestureHandler(
    private val webView: WebView,
    private val context: Context
) : GestureDetector.SimpleOnGestureListener() {
    
    companion object {
        private const val SWIPE_THRESHOLD = 100  // 滑动阈值(px)
        private const val SEEK_AMOUNT = 10       // 快进/快退时间(秒)
    }
    
    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        val diffX = e2.x - e1!!.x
        
        when {
            diffX > SWIPE_THRESHOLD -> {  // 左滑 = 快退
                seekVideo(-SEEK_AMOUNT)
                return true
            }
            diffX < -SWIPE_THRESHOLD -> { // 右滑 = 快进
                seekVideo(SEEK_AMOUNT)
                return true
            }
        }
        return false
    }
    
    private fun seekVideo(seconds: Int) {
        val js = "seekVideo($seconds)"
        webView.evaluateJavascript(js, null)
        
        val direction = if (seconds > 0) "快进" else "快退"
        Toast.makeText(context, "⏩ $direction ${abs(seconds)} 秒", Toast.LENGTH_SHORT).show()
    }
}
```

### 🎮 使用场景

**场景 1：跳过不感兴趣的部分**
```
视频播放中 → 用户左滑 → 快退 10 秒 → 跳过广告/开头
```

**场景 2：回看精彩瞬间**
```
精彩镜头错过 → 用户右滑 → 快进 10 秒 → 回看内容
```

---

## 🧭 功能三：导航栏 (NavigationBar.kt)

### 📌 功能描述

一个**悬浮导航栏**，提供快捷操作按钮，方便用户在不同页面间切换。

### ✨ 核心特性

#### 1️⃣ 导航栏按钮
| 按钮 | 图标 | 功能 |
|------|------|------|
| 🏠 **首页** | 🏠 | 返回抖音精选首页 |
| 🔄 **刷新** | 🔄 | 重新加载当前页面 |
| ⚙️ **设置** | ⚙️ | 打开设置页面 |
| ❌ **关闭** | ❌ | 隐藏导航栏 |

#### 2️⃣ 显示/隐藏逻辑
- **显示方式**：
  - 按返回键（BACK）
  - 从左侧边缘向右滑动
- **隐藏方式**：
  - 点击"关闭"按钮
  - 5 秒无操作自动隐藏
  - 再次按返回键

#### 3️⃣ UI 设计
- **位置**：屏幕左侧，垂直排列
- **样式**：半透明背景 + 圆角按钮
- **动画**：淡入/淡出效果（300ms）
- **层级**：位于 WebView 上方，弹幕下方

### 🔧 技术实现

```kotlin
// NavigationBar.kt 核心代码片段

class NavigationBar(
    private val activity: Activity,
    private val webView: WebView,
    private val container: FrameLayout
) {
    private var isVisible = false
    
    fun show() {
        if (isVisible) return
        isVisible = true
        
        val navBar = createNavigationBar()
        container.addView(navBar)
        
        // 动画显示
        navBar.alpha = 0f
        navBar.animate().alpha(1f).duration = 300
        
        // 5秒后自动隐藏
        Handler(Looper.getMainLooper()).postDelayed({
            hide()
        }, 5000)
    }
    
    fun hide() {
        if (!isVisible) return
        isVisible = false
        
        // 查找并移除导航栏
        val navBar = container.findViewById<LinearLayout>(R.id.navigation_bar)
        navBar?.animate()?.alpha(0f)?.withEndAction {
            container.removeView(navBar)
        }?.duration = 300
    }
    
    private fun createNavigationBar(): View {
        // 创建 LinearLayout + 4个按钮
        // 设置点击事件
    }
}
```

### 🎮 使用场景

**场景 1：返回首页**
```
用户浏览多个视频 → 想回到首页 → 按返回键 → 显示导航栏 → 点击"首页"
```

**场景 2：刷新页面**
```
页面加载失败 → 按返回键 → 显示导航栏 → 点击"刷新" → 重新加载
```

**场景 3：打开设置**
```
需要导入 Cookie → 按返回键 → 显示导航栏 → 点击"设置" → 打开设置页
```

---

## 🔐 功能四：Cookie 登录

### 📌 功能描述

允许用户**导入浏览器 Cookie**，实现登录态同步，解锁个人推荐内容。

### ✨ 核心特性

#### 1️⃣ Cookie 支持格式
- **来源**：PC 浏览器（Chrome / Edge / Firefox）
- **格式**：标准 Cookie 字符串
- **示例**：
  ```
  sessionid=abc123; csrf_token=xyz789; ...
  ```

#### 2️⃣ Cookie 存储方式
- **存储位置**：`SharedPreferences`
- **加密方式**：Base64 编码（非加密，仅混淆）
- **生效时机**：下次启动应用或手动刷新时

#### 3️⃣ Cookie 注入逻辑
```kotlin
// MainActivity.kt 中的 Cookie 注入

private fun loadCookies(): String? {
    val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
    val cookieStr = prefs.getString("cookie", null)
    return cookieStr
}

private fun applyCookies(cookieStr: String) {
    val cookieManager = CookieManager.getInstance()
    cookieManager.setCookie("https://www.douyin.com", cookieStr)
    cookieManager.flush()
    
    // 同步到 WebView
    CookieSyncManager.getInstance().sync()
}
```

### 🔧 如何获取 Cookie

#### 方法 1：浏览器开发者工具（推荐）

1. 打开 Chrome 浏览器，访问 https://www.douyin.com 并登录
2. 按 `F12` 打开开发者工具
3. 切换到 `Application`（应用程序）标签
4. 左侧找到 `Cookies` → `https://www.douyin.com`
5. 复制所有 Cookie 值（右键 → Copy all）

#### 方法 2：浏览器插件

1. 安装 "EditThisCookie" 或 "Cookie-Editor" 插件
2. 访问抖音网站并登录
3. 点击插件图标 → Export → 复制字符串

### 🎮 使用场景

**场景 1：解锁个人推荐**
```
未登录 → 只能看热门视频 → 导入 Cookie → 刷新页面 → 看到个人推荐
```

**场景 2：多账号切换**
```
账号A的Cookie → 保存到文件 → 账号B的Cookie → 替换 → 切换成功
```

---

## ⚙️ 功能五：设置页面 (SettingsActivity.kt)

### 📌 功能描述

一个**独立的设置界面**，用于管理 Cookie 和应用配置。

### ✨ 核心特性

#### 1️⃣ 设置项列表
| 设置项 | 类型 | 描述 |
|--------|------|------|
| 🍪 **Cookie 输入** | 多行文本框 | 粘贴 Cookie 字符串 |
| 💾 **保存按钮** | 按钮 | 保存 Cookie 到本地 |
- 🗑️ **清除按钮** | 按钮 | 清除已保存的 Cookie |
- ↩️ **返回按钮** | 按钮 | 返回主界面 |

#### 2️⃣ UI 布局
- **标题栏**："设置"
- **输入区域**：大文本框（支持多行粘贴）
- **按钮区域**：水平排列（保存 / 清除 / 返回）
- **样式**：Material Design 风格

#### 3️⃣ 数据验证
- **非空检查**：Cookie 不能为空才能保存
- **格式检查**：（可选）验证是否包含关键字段
- **成功提示**：Toast 显示 "✅ Cookie 已保存"
- **失败提示**：Toast 显示 "❌ Cookie 不能为空"

### 🔧 技术实现

```kotlin
// SettingsActivity.kt 核心代码片段

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var etCookie: EditText
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        etCookie = findViewById(R.id.et_cookie)
        
        // 加载已保存的 Cookie
        loadSavedCookie()
        
        // 绑定按钮事件
        findViewById<Button>(R.id.btn_save).setOnClickListener { saveCookie() }
        findViewById<Button>(R.id.btn_clear).setOnClickListener { clearCookie() }
        findViewById<Button>(R.id.btn_back).setOnClickListener { finish() }
    }
    
    private fun saveCookie() {
        val cookieStr = etCookie.text.toString().trim()
        if (cookieStr.isEmpty()) {
            Toast.makeText(this, "❌ Cookie 不能为空", Toast.LENGTH_SHORT).show()
            return
        }
        
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        prefs.edit().putString("cookie", cookieStr).apply()
        
        Toast.makeText(this, "✅ Cookie 已保存", Toast.LENGTH_SHORT).show()
    }
}
```

### 🎮 使用场景

**场景 1：首次导入 Cookie**
```
打开设置 → 粘贴 Cookie → 点击保存 → 返回主界面 → 刷新页面
```

**场景 2：更新 Cookie**
```
Cookie 过期 → 打开设置 → 清除旧 Cookie → 粘贴新 Cookie → 保存
```

---

## 🏗️ 项目架构（增强版）

```
TvDouyinWebView/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/tvdouyin/
│   │   │   ├── MainActivity.kt          # 主Activity（集成所有功能）
│   │   │   ├── KeyEventHandler.kt       # 遥控器按键处理
│   │   │   ├── JsBridge.kt             # JavaScript桥接（增强版）
│   │   │   ├── DanmakuManager.kt       # [新增] 弹幕管理器
│   │   │   ├── GestureHandler.kt       # [新增] 手势处理器
│   │   │   ├── NavigationBar.kt        # [新增] 导航栏组件
│   │   │   └── SettingsActivity.kt     # [新增] 设置页面
│   │   ├── res/layout/
│   │   │   ├── activity_main.xml       # 主界面（含弹幕层+导航栏容器）
│   │   │   └── activity_settings.xml   # [新增] 设置页面布局
│   │   ├── res/values/
│   │   │   └── strings.xml             # 字符串资源
│   │   └── AndroidManifest.xml         # 应用清单（含SettingsActivity声明）
│   └── build.gradle                     # 应用级Gradle配置
├── .github/workflows/
│   └── build-apk.yml                   # [新增] GitHub Actions构建工作流
├── build.gradle                         # 项目级Gradle配置
├── settings.gradle                      # 项目设置
└── README.md                            # 项目文档（本文件）
```

---

## 🔄 数据流架构

```
┌─────────────────────────────────────────────────────┐
│                    用户交互层                        │
│  ┌───────────┐  ┌──────────┐  ┌──────────────────┐ │
│  │ 遥控器按键 │  │ 手势操作  │  │ 导航栏按钮点击   │ │
│  └─────┬─────┘  └────┬─────┘  └────────┬─────────┘ │
│        │              │                  │          │
│  ┌─────▼──────────────▼──────────────────▼────────┐ │
│  │              事件分发中心 (MainActivity)         │ │
│  └─────┬──────────┬──────────────┬────────────────┘ │
│        │          │              │                   │
│  ┌─────▼────┐ ┌──▼───────┐ ┌───▼────────┐          │
│  │KeyEvent  │ │ Gesture  │ │ Navigation │          │
│  │Handler   │ │ Handler  │ │ Bar        │          │
│  └─────┬────┘ └────┬─────┘ └─────┬──────┘          │
│        │            │              │                 │
│  ┌─────▼────────────▼──────────────▼──────────────┐ │
│  │           JavaScript Bridge (JsBridge)          │ │
│  └─────┬───────────────────────────────────────────┘ │
│        │                                           │
│  ┌─────▼───────────────────────────────────────────┐ │
│  │              WebView (抖音网页)                  │ │
│  └─────────────────────────────────────────────────┘ │
│                                                     │
│  ┌─────────────────────────────────────────────────┐ │
│  │              弹幕渲染层 (DanmakuManager)         │ │
│  └─────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────┘
```

---

## 🎮 完整操作指南（增强版）

### 📺 遥控器操作

| 按键 | 功能 | 备注 |
|------|------|------|
| ⬆️ **上键** | 切换到上一个视频 | 滚动页面 |
| ⬇️ **下键** | 切换到下一个视频 | 滚动页面 |
| ⏯️ **确认键** | 播放/暂停视频 | 触发弹幕启停 |
| ⬅️ **左键** | 返回上一页 | 或退出全屏 |
| ➡️ **右键** | 进入全屏 | 沉浸式体验 |
| 🔙 **返回键** | 显示/隐藏导航栏 | 或返回上级 |
| 📋 **菜单键** | 显示菜单 | （开发中） |

### 👆 触摸屏手势

| 手势 | 功能 | 触发条件 |
|------|------|---------|
| 👈 **左滑** | 快退 10 秒 | 距离 > 100px |
| 👉 **右滑** | 快进 10 秒 | 距离 > 100px |
| 👆 **单击** | 播放/暂停 | - |
| 🔄 **双击** | 点赞 | （预留） |

### 🧭 导航栏操作

| 操作 | 功能 | 说明 |
|------|------|------|
| 按**返回键** | 显示导航栏 | 半透明浮层 |
| 点击**🏠首页** | 回到精选首页 | 重新加载 |
| 点击**🔄刷新** | 刷新当前页面 | 重载URL |
| 点击**⚙️设置** | 打开设置页 | Cookie管理 |
| 点击**❌关闭** | 隐藏导航栏 | 淡出动画 |

---

## 🧪 测试清单

### ✅ 基础功能测试
- [ ] 应用启动，加载抖音精选页面
- [ ] 视频自动播放
- [ ] 遥控器上下键切换视频
- [ ] 确认键播放/暂停

### ✅ 弹幕功能测试
- [ ] 视频播放时弹幕自动出现
- [ ] 弹幕从右到左滚动
- [ ] 弹幕颜色随机（白/黄/青/绿/橙）
- [ ] 视频暂停时弹幕停止
- [ ] 弹幕不遮挡视频主要内容

### ✅ 手势操作测试
- [ ] 左滑快退 10 秒
- [ ] 右滑快进 10 秒
- [ ] Toast 提示正确显示
- [ ] 视频跳转准确

### ✅ 导航栏测试
- [ ] 按返回键显示导航栏
- [ ] 点击"首页"回到精选页
- [ ] 点击"刷新"重新加载
- [ ] 点击"设置"打开设置页
- [ ] 点击"关闭"或5秒无操作自动隐藏

### ✅ Cookie 登录测试
- [ ] 打开设置页面
- [ ] 粘贴 Cookie 字符串
- [ ] 保存成功提示
- [ ] 返回主界面刷新
- [ ] 页面显示个人推荐内容

### ✅ 性能测试
- [ ] 应用启动时间 < 3 秒
- [ ] 视频切换流畅（无卡顿）
- [ ] 弹幕不影响视频帧率
- [ ] 内存占用稳定（无泄漏）

---

## 🚀 未来规划 (v3.0)

### 📋 已规划功能
- [ ] **语音控制**：集成语音识别，语音命令操作
- [ ] **搜索功能**：搜索抖音视频/用户
- [ ] **收藏/历史**：本地数据库存储
- [ ] **多平台支持**：快手、B站、YouTube
- [ ] **原生播放器**：ExoPlayer 替代 WebView
- [ ] **投屏功能**：DLNA/AirPlay 支持
- [ ] **家长控制**：内容过滤+时长限制
- [ ] **主题切换**：暗色/亮色模式

### 🛠️ 技术优化
- [ ] **性能优化**：减少 WebView 内存占用
- [ ] **离线缓存**：预加载视频资源
- [ ] **崩溃监控**：Sentry 集成
- [ ] **热更新**：动态加载 JS/CSS
- [ ] **自动化测试**：Espresso UI 测试

---

## 📊 项目统计

| 指标 | 数值 |
|------|------|
| **Kotlin 代码行数** | ~2000 行 |
| **XML 布局文件** | 2 个 |
| **功能模块数** | 6 个 |
| **支持的 Android 版本** | API 21+ (Android 5.0+) |
| **最低 SDK 版本** | 21 |
| **目标 SDK 版本** | 34 |
| **Gradle 版本** | 8.2 |
| **JDK 版本** | 17 |

---

## 👥 贡献指南

欢迎贡献代码、报告 Bug 或提出功能建议！

### 🐛 报告 Bug

请包含以下信息：
1. 设备型号和 Android 版本
2. 复现步骤
3. 期望行为 vs 实际行为
4. 截图/日志

### 💡 功能建议

请描述：
1. 功能需求和使用场景
2. 期望的交互方式
3. 参考案例（如有）

---

## 📄 许可证

本项目仅供学习和研究使用。

---

## ⚠️ 免责声明

1. 本项目与字节跳动有限公司无关
2. 仅用于技术学习和研究
3. 请遵守抖音的服务条款
4. 不得用于商业用途或侵权行为
5. 使用本项目的风险由用户自行承担

---

## 📞 联系方式

如有问题或建议，欢迎通过以下方式联系：

- **GitHub Issues**: [提交 Issue](https://github.com/rgf666/myDV-DY/issues)
- **Email**: 37619660@qq.com

---

**最后更新**: 2026-06-06  
**文档版本**: v2.0.0 Enhanced  
**作者**: AI工程师 + rgf666
