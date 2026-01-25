# Disk Cleanup Skill - 使用指南

## 概述

`disk-cleanup` 是一个 Windows 磁盘清理自动化 skill，提供安全、可控的临时文件和开发工具缓存清理功能。

## 快速开始

### 1. 生成磁盘报告
```powershell
.\disk-cleanup\scripts\disk_cleanup.ps1 -Report
```
自动生成 HTML 报告，在浏览器中打开，显示磁盘使用率、清理建议。

### 2. 预览清理（推荐第一步）
```powershell
# 预览系统临时文件清理
.\disk-cleanup\scripts\disk_cleanup.ps1 -Cleanup -DryRun

# 预览包含开发缓存的清理
.\disk-cleanup\scripts\disk_cleanup.ps1 -Cleanup -IncludeDev -DryRun
```

### 3. 执行清理
```powershell
# 清理系统临时文件（安全）
.\disk-cleanup\scripts\disk_cleanup.ps1 -Cleanup -Force

# 清理所有临时文件和开发缓存
.\disk-cleanup\scripts\disk_cleanup.ps1 -Cleanup -IncludeDev -Force

# 仅清理特定开发工具
.\disk-cleanup\scripts\disk_cleanup.ps1 -DevCaches -Tool gradle -Force
```

## 脚本功能

| 脚本 | 功能 | 风险 |
|------|------|------|
| `analyze_disk.ps1` | 分析大文件/文件夹 | 无（只读） |
| `cleanup_temp.ps1` | 清理系统临时文件 | 低（自动重建） |
| `cleanup_dev_caches.ps1` | 清理 Gradle/npm/pip/Maven 缓存 | 中（需重新下载依赖） |
| `cleanup_claude_temp.ps1` | 清理 Claude Code 临时文件 | 低（会话目录） |
| `generate_cleanup_report.ps1` | 生成 HTML 报告 | 无（只读） |

## 典型场景

### 场景 1: C 盘红色警告（>90%）
```powershell
# 1. 生成报告
.\disk-cleanup\scripts\disk_cleanup.ps1 -Report

# 2. 立即清理所有安全项目
.\disk-cleanup\scripts\disk_cleanup.ps1 -Cleanup -Force

# 3. 如果仍不够，清理开发缓存
.\disk-cleanup\scripts\disk_cleanup.ps1 -DevCaches -Force

# 4. 分析大文件，手动清理
.\disk-cleanup\scripts\disk_cleanup.ps1 -Analyze
```

### 场景 2: 定期维护（每月）
```powershell
# 预览
.\disk-cleanup\scripts\disk_cleanup.ps1 -Cleanup -IncludeDev -DryRun
.\disk-cleanup\scripts\disk_cleanup.ps1 -DevCaches -DryRun

# 确认后执行
.\disk-cleanup\scripts\disk_cleanup.ps1 -Cleanup -IncludeDev
```

### 场景 3: Android 开发环境优化
```powershell
# 清理 Gradle 缓存（通常 1-5GB）
.\disk-cleanup\scripts\disk_cleanup.ps1 -DevCaches -Tool gradle -DryRun
.\disk-cleanup\scripts\disk_cleanup.ps1 -DevCaches -Tool gradle -Force
```

## 安全提醒

**清理前**:
1. 关闭所有应用程序（浏览器、IDE）
2. 保存重要工作
3. 建议先使用 `-DryRun` 预览

**清理开发缓存后**:
- 首次构建/安装会重新下载依赖
- 确保网络稳定
- 不影响代码和配置

**禁止清理路径**（脚本自动排除）:
- `C:\Windows` 系统文件
- `C:\Program Files` 应用程序
- 用户文档、桌面、下载文件夹

## 故障排查

**脚本执行错误**:
```powershell
# 临时允许脚本执行
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope Process
```

**清理后空间未释放**:
```powershell
# 清空回收站
Clear-RecycleBin -Force

# 重启计算机
Restart-Computer
```

**详细 FAQ**: 参考 [references/FAQ.md](references/FAQ.md)

## Skill 文件结构

```
disk-cleanup/
├── SKILL.md                    # Skill 主文档
├── scripts/                    # PowerShell 脚本
│   ├── disk_cleanup.ps1       # 主入口
│   ├── analyze_disk.ps1
│   ├── cleanup_temp.ps1
│   ├── cleanup_dev_caches.ps1
│   ├── cleanup_claude_temp.ps1
│   └── generate_cleanup_report.ps1
└── references/
    └── FAQ.md                  # 常见问题解答
```

## 集成到 Claude Code

此 skill 可直接被 Claude Code 识别并使用。当你说"清理磁盘"、"分析 C 盘占用"等关键词时，Claude 会自动调用此 skill 中的脚本。

示例对话：
- "帮我清理 C 盘临时文件"
- "分析磁盘空间占用"
- "生成磁盘使用报告"
- "清理 Gradle 缓存"
