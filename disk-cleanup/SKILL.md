---
name: disk-cleanup
description: Windows 磁盘空间清理和分析工具。自动化清理临时文件、开发工具缓存、系统垃圾文件，生成磁盘使用报告。适用于：磁盘空间不足警告、系统性能优化、定期维护、开发环境清理、C盘清理。
---

# Windows 磁盘清理工具

自动化 Windows 系统磁盘清理，专注于安全、可控的临时文件和缓存清理。

## 核心原则

- **安全第一**: 始终使用 `-DryRun` 预览，确认后再执行
- **用户控制**: 默认交互式确认，使用 `-Force` 跳过（谨慎）
- **增量清理**: 从安全项目开始，逐步清理高风险缓存
- **可追溯**: 所有操作有明确输出和大小报告

## 快速开始

### 1. 生成磁盘报告

```powershell
# 生成 HTML 报告，查看当前状态和建议
.\disk-cleanup\scripts\generate_cleanup_report.ps1
```

报告包含：总容量、使用率、清理建议、可清理项目估算。

### 2. 预览清理内容

```powershell
# 预览模式（推荐）
.\disk-cleanup\scripts\cleanup_temp.ps1 -DryRun

# 预览开发工具缓存
.\disk-cleanup\scripts\cleanup_dev_caches.ps1 -DryRun

# 预览 Claude Code 临时文件
.\disk-cleanup\scripts\cleanup_claude_temp.ps1 -DryRun
```

### 3. 执行清理

```powershell
# 交互式清理（每个项目确认）
.\disk-cleanup\scripts\cleanup_temp.ps1

# 清理所有临时文件
.\disk-cleanup\scripts\cleanup_temp.ps1 -IncludeDevCaches

# 清理特定开发工具缓存
.\disk-cleanup\scripts\cleanup_dev_caches.ps1 -Tool gradle
```

## 脚本说明

### analyze_disk.ps1

**用途**: 分析磁盘空间占用，定位大文件/文件夹。

```powershell
# 查找 C 盘前 20 个最大文件
.\disk-cleanup\scripts\analyze_disk.ps1 -Path "C:\" -TopN 20

# 按文件夹统计（慢但详细）
.\disk-cleanup\scripts\analyze_disk.ps1 -Path "C:\Users" -ByFolder
```

**输出**: 文件路径、大小（GB/MB）、修改时间，按大小降序排列。

### cleanup_temp.ps1

**用途**: 清理系统临时文件（安全项）。

**清理路径**:
- `%TEMP%`
- `C:\Windows\Temp`
- 浏览器缓存（Chrome, IE）
- 用户临时目录

**参数**:
- `-DryRun`: 预览模式，不实际删除
- `-Force`: 自动确认所有删除项（谨慎）
- `-IncludeDevCaches`: 同时清理开发工具缓存

**安全性**: 所有路径都是系统临时文件，清理后自动重建，无风险。

### cleanup_dev_caches.ps1

**用途**: 清理开发工具缓存，释放大量空间但需重新下载依赖。

**支持工具**:
- `gradle`: Android/Java 项目依赖缓存（通常 1-5 GB）
- `npm`: Node.js 依赖缓存（通常 500MB-2GB）
- `pip`: Python 包缓存
- `maven`: Java 项目依赖
- `yarn`, `cargo`: 其他包管理器

```powershell
# 清理所有开发缓存
.\disk-cleanup\scripts\cleanup_dev_caches.ps1

# 仅清理 Gradle
.\disk-cleanup\scripts\cleanup_dev_caches.ps1 -Tool gradle -Force

# 预览 npm 缓存大小
.\disk-cleanup\scripts\cleanup_dev_caches.ps1 -Tool npm -DryRun
```

**影响**: 清理后首次构建/安装会重新下载依赖，不影响代码。

### cleanup_claude_temp.ps1

**用途**: 清理 Claude Code 生成的 `tmpclaude-*-cwd` 临时目录。

```powershell
# 清理当前目录及子目录
.\disk-cleanup\scripts\cleanup_claude_temp.ps1

# 指定基础路径
.\disk-cleanup\scripts\cleanup_claude_temp.ps1 -BasePath "D:\projects" -Force
```

**安全性**: 这些是会话临时目录，清理后不影响 Claude Code 正常运行。

### generate_cleanup_report.ps1

**用途**: 生成 HTML 格式的磁盘使用报告。

```powershell
# 生成到默认位置（%TEMP%）
.\disk-cleanup\scripts\generate_cleanup_report.ps1

# 指定输出路径
.\disk-cleanup\scripts\generate_cleanup_report.ps1 -OutputPath "D:\report.html"
```

**报告内容**:
- 总容量、已用空间、可用空间
- 使用率百分比进度条
- 基于使用率的清理建议
- 各类清理项目的大小估算和风险等级

## 典型工作流

### 场景 1: C 盘红色警告

```powershell
# 1. 生成报告，了解当前状态
.\disk-cleanup\scripts\generate_cleanup_report.ps1

# 2. 立即执行安全清理
.\disk-cleanup\scripts\cleanup_temp.ps1 -Force

# 3. 如果空间仍不足，清理开发缓存
.\disk-cleanup\scripts\cleanup_dev_caches.ps1 -Force

# 4. 分析大文件，手动清理
.\disk-cleanup\scripts\analyze_disk.ps1 -Path "C:\Users" -TopN 30
```

### 场景 2: 定期维护（每月）

```powershell
# 预览所有清理项
.\disk-cleanup\scripts\cleanup_temp.ps1 -DryRun -IncludeDevCaches
.\disk-cleanup\scripts\cleanup_claude_temp.ps1 -DryRun

# 确认后执行
.\disk-cleanup\scripts\cleanup_temp.ps1 -IncludeDevCaches
.\disk-cleanup\scripts\cleanup_claude_temp.ps1
```

### 场景 3: 开发环境优化

```powershell
# 清理特定工具缓存
.\disk-cleanup\scripts\cleanup_dev_caches.ps1 -Tool gradle
.\disk-cleanup\scripts\cleanup_dev_caches.ps1 -Tool npm

# 清理项目临时文件
cd "D:\my-project"
Get-ChildItem -Recurse -Filter "tmp*" | Remove-Item -Recurse -Force
```

## 安全注意事项

**高风险操作**（需谨慎使用 `-Force`）:
- 清理开发缓存后首次构建会重新下载依赖
- 确保网络稳定且有足够流量
- 建议在重要项目开发前避免清理

**禁止清理路径**（脚本自动排除）:
- `C:\Windows` 系统文件
- `C:\Program Files` 应用程序
- 用户文档、桌面、下载文件夹

**建议清理前**:
1. 关闭所有应用程序（尤其是浏览器和 IDE）
2. 保存重要工作
3. 确认备份重要数据

## 故障排查

**脚本执行错误**:
```powershell
# 检查执行策略
Get-ExecutionPolicy

# 临时允许脚本执行
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope Process
```

**清理失败**:
- 文件被占用：关闭应用程序后重试
- 权限不足：以管理员身份运行 PowerShell
- 路径不存在：脚本会自动跳过，正常现象

**清理后空间未释放**:
- 清空回收站：`Clear-RecycleBin -Force`
- 重启计算机（释放被系统占用的文件句柄）
- 运行磁盘检查：`chkdsk C: /F`

## 扩展清理

**系统内置清理工具**:
```powershell
# 运行磁盘清理
cleanmgr

# 使用预设配置
cleanmgr /sagerun:1
```

**Windows 更新缓存**（需管理员权限）:
```powershell
Stop-Service wuauserv
Remove-Item -Recurse -Force C:\Windows\SoftwareDistribution\Download\*
Start-Service wuauserv
```

**旧版 Windows 组件**:
```powershell
# 清理旧 Windows 版本
Get-ChildItem "C:\Windows\old*" | Remove-Item -Recurse -Force
```
