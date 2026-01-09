# 工作区状态

> **重要**：这是工作区级别的状态文件，记录所有项目的信息。

## 📚 工作区概述

**工作区类型**：多项目工作区
**位置**：`d:\claude code -11\`
**包含**：7个独立项目 + 共享资源

---

## 📋 项目列表

### 1. **data-filling-portable** - 数据填充工具（便携版）⭐ 当前重点
- **路径**：`projects/data-filling-portable/`
- **类型**：Python Web 应用（Flask）
- **状态**：⚠️ 便携版 Python 3.12 缺少 select 模块
- **特点**：无需安装 Python 的便携版
- **详细状态**：见 `projects/data-filling-portable/PROJECT_STATUS.md`

### 2. **data-filling-dev** - 数据填充工具（开发版）
- **路径**：`projects/data-filling-dev/`
- **类型**：Python Web 应用（Flask）
- **状态**：✅ 正常
- **特点**：开发版本，包含构建配置

### 3. **easy-audio-player** - 音频播放器
- **路径**：`projects/easy-audio-player/`
- **类型**：Android App
- **状态**：✅ 正常
- **特点**：Gradle 项目

### 4. **mirror-practice-app** - 镜子练习 App
- **路径**：`projects/mirror-practice-app/`
- **类型**：Android App（Kotlin）
- **状态**：✅ 正常
- **特点**：使用 Kotlin DSL

### 5. **delivery-automation** - 送货自动化系统
- **路径**：`projects/delivery-automation/`
- **类型**：Python 自动化系统
- **状态**：✅ 正常
- **特点**：送货单自动化处理

### 6. **agent-discussion** - Agent 讨论工具
- **路径**：`projects/agent-discussion/`
- **类型**：Node.js Web 应用
- **状态**：✅ 正常
- **特点**：前后端一体

### 7. **image2excel-tool** - 图片转 Excel 工具
- **路径**：`projects/image2excel-tool/`
- **类型**：Python 工具
- **状态**：✅ 正常
- **特点**：图表转 Excel 表格

---

## 📚 共享资源

### Skills（所有项目可用）✅
- **位置**：`skills/`
- **内容**：Claude 技能插件
- **说明**：任何项目都可以调用这些技能

### MCP 服务器
- **位置**：`mcp-servers/`
- **内容**：外部工具实现
- **说明**：通过配置文件启用

### 文档和知识库
- `docs/` - 共享文档
- `knowledge-base/` - 知识库
- `templates/` - 共享模板
- `patterns/` - 设计模式

---

## ⚙️ 配置说明

### 工作区配置
- **文件**：`.claude/local.json`
- **作用**：工作区级提示，识别当前工作项目

### 全局配置
- **文件**：`.claude/settings.json`
- **作用**：全局 MCP 服务器（所有项目可用）

### 项目配置
- **位置**：`projects/{project}/.claude/local.json`
- **作用**：项目级提示和配置

---

## 🎯 当前工作项目

**默认**：data-filling-portable（数据填充工具便携版）

**切换项目**：打开其他项目路径下的文件即可。

---

## 📅 更新日志

- **2026-01-07 17:00**：⭐ 重大重组 - 整理为多项目工作区
  - 移动所有项目到 `projects/` 目录
  - 为每个项目创建独立配置
  - 更新根目录配置为工作区配置
  - 保留共享资源（skills, docs, knowledge-base）
  - Skills 和 MCP 在工作区级别，所有项目可用
- **2026-01-07 16:00**：创建完整的配置文件文档体系
- **2026-01-07 15:30**：创建 `.claude/local.json` 实现项目级系统提示
