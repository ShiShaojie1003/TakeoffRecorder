<div align="center">

![NzHelper](https://socialify.git.ci/bug-bit/NzHelper/image?description=1&font=Inter&forks=1&language=1&name=1&owner=1&stargazers=1&theme=Auto)

# NzHelper

[![license](https://img.shields.io/github/license/bug-bit/NzHelper.svg)](https://www.gnu.org/licenses/gpl-3.0.html)
[![Stars](https://img.shields.io/github/stars/bug-bit/NzHelper?label=stars)](https://github.com/bug-bit/NzHelper)
[![Downloads](https://img.shields.io/github/downloads/bug-bit/NzHelper/total?label=Downloads)](https://github.com/bug-bit/NzHelper/releases)
[![Release](https://img.shields.io/github/v/release/bug-bit/NzHelper)](https://github.com/bug-bit/NzHelper/releases)
![Android](https://img.shields.io/badge/Android-8.1%2B-3DDC84?logo=android&logoColor=white)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)

**一个简单、高效、易用的性生活记录工具。**

</div>

---

## 功能特性

| 功能              | 说明                              |
|-----------------|---------------------------------|
| ⏱ 计时记录          | 精确记录每次时长，支持后台计时                 |
| 🏷 标签管理         | 自定义分类，让记录更加清晰                   |
| 🔒 隐私保护         | 手势锁 + 指纹/人脸解锁                   |
| 🗑 回收站          | 支持恢复误删除的数据                      |
| 📊 数据统计         | 周报、月报趋势分析                       |
| 🏆 成就系统         | 多项成就，记录里程碑、连续天数与探索进度            |
| 🔔 通知提醒         | 实时显示计时状态                        |
| 🎨 Material You | Jetpack Compose + Material 3 设计 |
| ⚡ 轻量高效          | 本地存储，小巧省电                       |

---

## 下载安装

### APK 下载

前往 [GitHub Releases](https://github.com/bug-bit/NzHelper/releases) 下载最新版 APK。

- 支持系统：Android 8.1 (API 27) 及以上
- ABI：arm64-v8a / armeabi-v7a / x86 / x86_64

### 自行构建

```bash
git clone https://github.com/bug-bit/NzHelper.git
cd NzHelper
./gradlew assembleRelease
```

---

## 使用说明

1. 设置应用锁，保护个人隐私
2. 点击开始按钮记录过程
3. 完成后保存记录，可添加标签和备注
4. 在历史页面查看统计数据
5. 使用标签管理整理记录

> 所有数据均保存在本地，不上传服务器。

---

## 技术栈

| 技术                 | 用途       |
|--------------------|----------|
| Kotlin             | 开发语言     |
| Jetpack Compose    | UI 框架    |
| Material 3         | 设计组件     |
| Navigation Compose | 页面导航     |
| Room               | 本地数据库    |
| WorkManager        | 后台任务     |
| Moshi              | JSON 序列化 |
| OkHttp             | 网络请求     |
| Biometric          | 生物识别     |

---

## 致谢

本项目参考了 [DickHelper](https://github.com/zzzdajb/DickHelper) 的设计思路。

感谢所有 Star、Issue 和 Pull Request 支持本项目。

---

## 贡献

欢迎提交 Issue 或 Pull Request，一起完善项目。

---

## 支持

如果这个项目对你有帮助，欢迎通过爱发电支持开发者：

👉 [https://afdian.com/a/zyfzd](https://afdian.com/a/zyfzd)

你的支持是持续维护和更新的动力！

---

## 许可协议

本项目基于 GNU General Public License v3.0 开源。

---

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=bug-bit/NzHelper&type=Timeline)](https://star-history.com/#bug-bit/NzHelper&Timeline)

---

<div align="center">

Made with 🫱 by [bug-bit](https://github.com/bug-bit)

⭐ 如果觉得项目有帮助，欢迎 Star 支持！

</div>
## Xiaomi HyperOS 超级岛

计时前台服务已接入 Xiaomi HyperOS 焦点通知 / 超级岛客户端协议：

- 开始计时时创建普通前台通知，并通过 `miui.focus.param` 附加 `param_v2` 数据。
- HyperOS 2 使用焦点通知标题与状态栏 ticker；HyperOS 3 同时展示大岛、小岛计时摘要。
- 岛内“暂停/继续”复用 Android 原生 `Notification.Action`，与普通通知按钮保持同一行为。
- 暂停会更新同一个通知；停止或重置会移除前台通知并同步清除岛通知。
- 非 Xiaomi 设备会忽略厂商扩展参数，继续显示标准 Android 计时通知。

超级岛是否实际展示仍由设备系统版本、用户的焦点通知权限以及小米平台的场景审核结果决定。

### Android 原生实况通知（Live Updates）

- Android 16 及更高版本会为运行中的计时请求 promoted ongoing 通知。
- 通知使用系统 Chronometer 在状态胶囊中持续计时，不使用自定义 RemoteViews。
- 设置页的“实况通知”可检查授权状态并打开系统授权页面。
- 暂停后通知会退出 promoted ongoing 状态；停止或重置会清除通知。
- OEM 仍可设置额外展示条件；不满足条件时系统会自动降级为普通通知。
