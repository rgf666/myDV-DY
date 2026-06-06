# TV抖音精选 - Android TV 应用

一个基于 WebView 的 Android TV 应用，用于在电视上观看抖音精选视频。

## 项目简介

本项目是一个简单的 Android TV 应用，通过 WebView 加载抖音精选网页版，适配遥控器操作，实现上下切换视频的功能。

### 核心功能

- ✅ 使用 WebView 加载抖音精选页面
- ✅ 遥控器上下键切换视频
- ✅ 确认键播放/暂停视频
- ✅ 自动隐藏网页多余元素，优化TV显示
- ✅ JavaScript桥接，实现网页与Android交互

## 技术架构

```
TvDouyinWebView/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/tvdouyin/
│   │   │   ├── MainActivity.kt          # 主Activity，WebView配置
│   │   │   ├── KeyEventHandler.kt       # 遥控器按键处理逻辑
│   │   │   └── JsBridge.kt             # JavaScript桥接类
│   │   ├── res/layout/
│   │   │   └── activity_main.xml        # 主界面布局
│   │   ├── res/values/
│   │   │   └── strings.xml              # 字符串资源
│   │   └── AndroidManifest.xml          # 应用清单
│   └── build.gradle                     # 应用级Gradle配置
├── build.gradle                          # 项目级Gradle配置
├── settings.gradle                       # 项目设置
└── gradle/wrapper/                      # Gradle包装器
```

## 开发环境要求

- **Android Studio**: Hedgehog | 2023.1.1 或更高版本
- **JDK**: 11 或更高版本
- **Android SDK**: API 21+ (Android 5.0+)
- **Kotlin**: 1.9.0+
- **Gradle**: 8.2

## 快速开始

### 1. 克隆或下载项目

将项目文件复制到本地目录。

### 2. 使用 Android Studio 打开项目

1. 启动 Android Studio
2. 选择 "Open" 或 "Open an Existing Project"
3. 导航到 `TvDouyinWebView` 文件夹
4. 点击 "OK"

### 3. 同步 Gradle

Android Studio 会自动开始 Gradle 同步。如果没有，可以：
- 点击菜单 `File` → `Sync Project with Gradle Files`
- 或点击工具栏的 `Sync` 按钮

### 4. 运行应用

#### 方式一：在模拟器上运行

1. 创建 Android TV 模拟器：
   - 打开 AVD Manager (`Tools` → `Device Manager`)
   - 点击 `Create Device`
   - 选择 TV 硬件配置（如 1080p TV）
   - 选择系统镜像（推荐 API 30+）
   - 完成创建

2. 运行应用：
   - 选择创建的 TV 模拟器
   - 点击运行按钮 ▶

#### 方式二：在真实 TV 设备上运行

1. 启用开发者选项：
   - 在 TV 上：`设置` → `关于` → 连续点击 `版本号` 7次
   - 返回 `设置`，找到 `开发者选项`
   - 启用 `USB调试`

2. 连接设备：
   - 使用 USB 连接 TV 和电脑
   - 或通过网络 ADB 连接：
     ```bash
     adb connect <TV_IP地址>:5555
     ```

3. 运行应用：
   - 在 Android Studio 中选择连接的 TV 设备
   - 点击运行按钮 ▶

## 遥控器操作说明

| 按键 | 功能 |
|------|------|
| 上键 (UP) | 切换到上一个视频 |
| 下键 (DOWN) | 切换到下一个视频 |
| 确认键 (OK/ENTER) | 播放/暂停视频 |
| 左键 (LEFT) | 返回上一页 |
| 右键 (RIGHT) | 进入/退出全屏 |
| 返回键 (BACK) | 退出应用或返回 |
| 菜单键 (MENU) | 显示菜单（开发中） |

## 核心代码说明

### MainActivity.kt

主Activity，负责：
- 配置 WebView 设置（启用JS、设置UA等）
- 加载抖音精选页面
- 注入CSS优化TV显示
- 注入JavaScript实现交互
- 处理生命周期

关键配置：
```kotlin
// 设置PC版UA，避免移动端重定向
settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) ..."

// 启用JavaScript（必须）
settings.javaScriptEnabled = true

// 启用DOM存储
settings.domStorageEnabled = true
```

### KeyEventHandler.kt

遥控器按键事件处理，负责：
- 上下键切换视频
- 确认键播放/暂停
- 左右键导航
- 调用JavaScript与网页交互

核心方法：
```kotlin
fun handleKeyEvent(keyCode: Int, event: KeyEvent?): Boolean
private fun scrollToNextVideo()
private fun scrollToPreviousVideo()
private fun toggleVideoPlayback()
```

### JsBridge.kt

JavaScript桥接类，负责：
- 接收网页端的事件通知
- 记录日志
- 实现网页与Android的双向通信

## 注意事项

### ⚠️ 合规性说明

1. **本应用仅用于学习和研究**，不用于商业目的
2. 通过 WebView 访问公开的网页内容，类似于浏览器访问
3. 请遵守抖音的服务条款和使用政策
4. 不要用于任何侵权行为

### 技术限制

1. **网页结构变化**：抖音网页版更新可能导致CSS选择器失效，需要维护
2. **性能**：WebView 性能不如原生视频播放器
3. **网络依赖**：必须联网才能使用
4. **UA检测**：抖音可能会检测 WebView，导致访问受限

### 优化建议

1. **启用硬件加速**：
   ```xml
   android:hardwareAccelerated="true"
   ```

2. **缓存策略**：
   ```kotlin
   settings.cacheMode = WebSettings.LOAD_DEFAULT
   ```

3. **预加载**：可以预加载下一个视频，提升体验

## 常见问题

### Q: 应用启动后显示空白页面？

**A**: 检查网络连接，确保可以访问 `https://www.douyin.com`

### Q: 视频无法自动播放？

**A**: 浏览器安全策略禁止自动播放，需要用户交互。本应用通过遥控器按键触发播放。

### Q: 遥控器按键无响应？

**A**: 确保 WebView 获得了焦点：
```kotlin
webView.isFocusable = true
webView.isFocusableInTouchMode = true
webView.requestFocus()
```

### Q: 如何调试 WebView？

**A**: 在 `build.gradle` 中启用调试：
```kotlin
buildTypes {
    debug {
        debuggable true
    }
}
```

然后在 Chrome 中访问 `chrome://inspect` 进行调试。

## 打包发布

### 生成签名APK

1. 创建签名密钥：
   ```bash
   keytool -genkey -v -keystore my-release-key.keystore 
   -alias alias_name -keyalg RSA -keysize 2048 -validity 10000
   ```

2. 配置 `build.gradle`：
   ```gradle
   android {
       signingConfigs {
           release {
               storeFile file('my-release-key.keystore')
               storePassword 'password'
               keyAlias 'alias_name'
               keyPassword 'password'
           }
       }
       buildTypes {
           release {
               signingConfig signingConfigs.release
           }
       }
   }
   ```

3. 生成APK：
   ```bash
   ./gradlew assembleRelease
   ```

### 安装到TV

```bash
# 通过ADB安装
adb install app-release.apk

# 启动应用
adb shell am start -n com.example.tvdouyin/.MainActivity
```

## 未来改进方向

- [ ] 添加视频收藏功能
- [ ] 实现历史记录
- [ ] 支持搜索功能
- [ ] 优化TV界面UI
- [ ] 添加设置页面
- [ ] 支持多种视频平台
- [ ] 实现原生视频播放器（ExoPlayer）

## 许可证

本项目仅供学习和研究使用。

## 联系方式

如有问题或建议，欢迎反馈。

---

**免责声明**：本项目与字节跳动有限公司无关，仅用于技术学习和研究。
