# SPW Playback Rain

在 Salt Player for Windows（SPW）的播放页背景上显示雨滴效果。雨滴使用 GPU 运行时着色器绘制，覆盖整个播放页，并可设置为只在播放页显示。

> 当前版本：`1.0.2`  
> 适配宿主：SPW `1.19.0`  
> 作者：`aoritian`

## 功能

- 播放页全屏雨滴背景
- 退出播放页自动隐藏（默认开启）
- 雨速、雨滴大小、密度、光照角度、边缘阴影设置
- 雨滴层位于背景之上、歌词/控件之下（由宿主布局决定）
- Skia `RuntimeEffect` GPU 渲染
- 着色器不可用时回退到简化雨滴绘制

## 安装

1. 在 [Releases](https://github.com/AoRitian/spw-playback-rain/releases) 下载最新的 `.spmod`。
2. 在 SPW 中打开 **创意工坊 → 模组管理 → 导入模组**。
3. 启用模组，并授予 **“修改界面”/class-transform** 权限。
4. 打开播放页测试。

也可以在本地构建后导入：

```text
build/libs/plugin-com.aoritian.playbackrain-1.0.2.spmod
```

## 设置

| 设置 | 默认 | 说明 |
| --- | --- | --- |
| 启用雨滴 | 开 | 总开关 |
| 仅播放页显示 | 开 | 关闭后离开播放页仍显示雨滴 |
| 雨速 | 正常 | 雨滴下落速度 |
| 雨滴大小 | 中 | 雨滴和水痕尺寸 |
| 雨滴密度 | 中 | 同时出现的雨滴数量 |
| 光照角度 | 左上 | 雨滴高光方向 |
| 边缘阴影 | 关 | 雨滴边缘暗部强度 |

## 构建

环境要求：

- Windows
- JDK 25
- 可访问 Maven/JitPack 依赖

```powershell
.\gradlew.bat test plugin
```

产物：

```text
build/libs/plugin-com.aoritian.playbackrain-1.0.2.spmod
```

`test` 中包含 Skia 着色器编译测试。

## 日志

```text
%APPDATA%\Salt Player for Windows\workshop\data\com.aoritian.playbackrain\playback-rain.log
```

关键日志：

- `rain hook installed`
- `rain overlay composed`
- `playback expanded=true/false`
- `playback visible=true/false`

## 已知限制

- 目标版本为 SPW `1.19.0`，其他版本不保证可用。
- 播放页层级依赖宿主内部布局；宿主更新后可能需要调整 Hook 目标。
- 当前不包含音频频谱联动。
- 雨滴效果使用 GPLv3 授权的 BetterLyrics 着色器移植，详见 `NOTICE` 和 `LICENSE`。

## 项目结构

```text
src/main/kotlin/com/aoritian/playbackrain/
  Config.kt              设置读取
  PlaybackRainPlugin.kt  插件生命周期和 Hook 安装
  PlaybackPresence.kt    播放页可见状态
  RainOverlay.kt         Compose 雨滴层
  RainShader.kt          Skia RuntimeEffect 着色器和回退绘制
  RainTargets.kt         SPW 1.19.0 Hook 目标
src/main/resources/
  preference_config.json 设置页定义
  NOTICE.txt             第三方效果归属
```

## 致谢与许可

雨滴算法来自 [BetterLyrics](https://github.com/jayfunc/BetterLyrics)，原效果标注来自 [Shadertoy](https://www.shadertoy.com/view/ltffzl)。

本项目以 **GPL-3.0** 发布。完整许可证见 [LICENSE](LICENSE)，第三方归属见 [NOTICE](NOTICE)。

---

## English summary

A Salt Player for Windows mod that renders a full-playback-page raindrop background with a Skia GPU shader. It can be limited to the playback page, has speed/size/density/light/shadow settings, and falls back to a simple canvas rain effect if the shader cannot be compiled.

Requires SPW `1.19.0`, Windows and JDK 25 for building. Licensed under GPL-3.0; the raindrop effect is ported from BetterLyrics/Shadertoy.
