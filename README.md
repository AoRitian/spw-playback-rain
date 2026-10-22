# SPW Playback Rain

在 Salt Player for Windows（SPW）的播放页背景上显示雨滴效果。雨滴使用 GPU 运行时着色器绘制，覆盖整个播放页，并可设置为只在播放页显示。

> 当前版本：`1.0.3`  
> 适配宿主：SPW `1.19.1`  
> 作者：`aoritian`

## 功能

- 播放页全屏雨滴背景
- 退出播放页自动隐藏（默认开启）
- 雨速、雨滴大小、密度、光照角度、边缘阴影设置
- Skia `RuntimeEffect` GPU 渲染
- 着色器不可用时回退到简化雨滴绘制

## 安装

1. 从 Releases 下载最新的 `.spmod`
2. 在 SPW 中打开创意工坊 → 模组管理 → 导入模组
3. 启用模组，并授予“修改界面”权限
4. 打开播放页测试

本地构建产物：

```text
build/libs/plugin-com.aoritian.playbackrain-1.0.3.spmod
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

- 适配 SPW `1.19.1` 的混淆类名；宿主更新后可能需要调整 Hook 目标。
- 播放页层级依赖宿主内部布局。
- 当前不包含音频频谱联动。
- 雨滴效果使用 GPLv3 授权，详细归属见 `NOTICE` 和 `LICENSE`。
