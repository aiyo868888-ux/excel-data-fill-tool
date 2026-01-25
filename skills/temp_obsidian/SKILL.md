---
name: obsidian-manager
description: Obsidian 笔记管理和知识库构建工具。创建和管理 Obsidian Flavored Markdown 笔记，支持双向链接、标签、Frontmatter 属性、模板、Dataview 查询、Canvas 画布。适用于：创建每日笔记、Zettelkasten 卡片、项目管理、会议记录、概念地图、知识图谱构建、批量操作笔记。
---

# Obsidian Manager

Obsidian 笔记管理和知识库构建，专注于有效的知识组织和链接策略。

## 核心原则

- **原子笔记**: 每个笔记一个概念，保持专注和可链接
- **双向链接**: 使用 `[[wikilinks]]` 构建知识网络
- **结构化数据**: 用 Frontmatter 和属性支持检索
- **渐进式总结**: 笔记随时间迭代，不断优化

## 快速开始

### 创建笔记

**基础笔记:**
```markdown
# Note Title

Content here with [[Another Note]] links.
```

**带 Frontmatter:**
```markdown
---
type: concept
created: 2026-01-21
tags: [knowledge, learning]
---

# Concept Name
```

### 使用模板

从 `assets/templates/` 复制模板并根据需要自定义：
- `daily-note.md` - 每日笔记
- `zettelkasten.md` - Zettelkasten 永久笔记
- `project.md` - 项目管理
- `meeting.md` - 会议记录
- `concept-map.md` - 概念地图

## Obsidian 语法要点

### Wiki Links

```markdown
# 基础链接
[[Note Title]]

# 带别名
[[Note Title|Display Text]]

# 链接到标题
[[Note Title#Heading]]

# 嵌入文件
![[Image.png]]
![[Note Title#^block-id]]
```

### Frontmatter 属性

```yaml
---
type: note
status: active
created: 2026-01-21
tags: [important, reference]
project: "My Project"
deadline: 2026-02-01
---
```

### Callouts

```markdown
> [!INFO] Tip
> Content here

> [!WARNING] Warning
> Foldable content
```

### Dataview 查询

```dataview
LIST
FROM #project
WHERE status = "active"
SORT created DESC
```

## 工作流程

### 1. 每日笔记

- 创建带日期的笔记：`2026-01-21.md`
- 记录当天重点、想法、完成项
- 结束时回顾并链接到相关项目笔记

### 2. Zettelkasten 永久笔记

- 一卡一概念
- 用自己的话解释
- 链接到至少 2-3 个相关笔记
- 定期回顾和更新链接

### 3. 项目管理

- 创建项目笔记并设置状态
- 分解任务为复选框列表
- 链接相关资源和会议笔记
- 定期更新进度

### 4. 概念地图

- 识别核心概念
- 映射父子关系和横向关联
- 使用 Dataview 自动显示关系
- 复杂概念用 Canvas 可视化

## 笔记类型与结构

### Daily Note
```markdown
---
date: 2026-01-21
tags: [daily-note]
---

## Focus
- [ ] Priority 1
- [ ] Priority 2

## Notes
Thoughts and observations

## Completed
- [x] Task done
```

### Zettelkasten Note
```markdown
---
type: note
created: 2026-01-21
tags: [zettelkasten]
---

# Core Concept

## 💡 Idea
One atomic idea

## 🔄 Related
- [[Related Concept 1]]
- [[Related Concept 2]]
```

### Project Note
```markdown
---
type: project
status: active
deadline: 2026-02-01
---

# Project Name

## Objectives

## Tasks
- [ ] Task 1
- [ ] Task 2

## Progress
Updates and milestones
```

## 高级功能

### Dataview 查询示例

**列出所有活动项目:**
```dataview
TABLE file.name, deadline, status
FROM #project
WHERE status = "active"
SORT deadline ASC
```

**查找孤立笔记:**
```dataview
LIST
FROM ""
WHERE !contains(file.links, [[]])
```

**任务汇总:**
```dataview
TASK
FROM #project
WHERE !completed
GROUP BY file.link
```

### Canvas 概念图

创建 `.canvas` 文件可视化复杂关系：
- 节点表示笔记
- 边表示链接关系
- 支持分组和颜色编码

### 标签策略

**层级标签:**
```markdown
#project/active
#project/completed
#research/primary
#research/secondary
```

**功能标签:**
```markdown
#todo
#question
#idea
#reference
```

## 批量操作

### 更新 Frontmatter

批量修改笔记属性：

```powershell
# 示例：将所有 draft 状态改为 published
Get-ChildItem -Recurse -Filter "*.md" | ForEach-Object {
    $content = Get-Content $_.FullName -Raw
    $content = $content -replace 'status: draft', 'status: published'
    Set-Content $_.FullName $content
}
```

### 标签迁移

重命名或重组标签：

```markdown
# 旧标签
#important

# 新标签（更具体）
#important/urgent
#important/not-urgent
```

## 最佳实践

### 笔记命名

- 使用描述性标题
- 避免特殊字符
- 保持一致性
- 示例：`Deep Work - Concept.md` 而非 `note1.md`

### 链接策略

- 每个笔记至少链接 2-3 个其他笔记
- 使用有意义的链接文本
- 定期检查孤立笔记
- 避免过度链接（每个链接应有明确目的）

### Frontmatter 使用

- 必需：`type`, `created`
- 推荐：`tags`, `status`
- 可选：特定于笔记类型的属性

### 知识库维护

**每周:**
- 回顾每日笔记
- 提取关键见解到永久笔记
- 更新项目状态

**每月:**
- 审查所有项目笔记
- 归档完成的项目
- 识别知识缺口

**每季度:**
- 评估整体结构
- 重组标签和分类
- 清理孤立内容

## 参考文档

- **Obsidian 语法**: See [OBSIDIAN-SYNTAX.md](references/OBSIDIAN-SYNTAX.md) for complete syntax reference
- **工作流程**: See [WORKFLOWS.md](references/WORKFLOWS.md) for detailed workflow guides
- **模板**: See [assets/templates/](assets/templates/) for note templates

## 常见场景

**"创建每日笔记"**
- 从模板创建
- 填入日期
- 记录当天活动

**"查找所有项目笔记"**
```dataview
LIST
FROM #project
```

**"创建 Zettelkasten 笔记"**
- 使用 Zettelkasten 模板
- 写一个核心想法
- 链接到 2-3 个相关笔记

**"生成概念地图"**
- 创建概念笔记
- 添加 Dataview 查询
- 考虑使用 Canvas

**"批量更新笔记标签"**
- 使用脚本批量操作
- 保持标签一致性
- 更新相关 Frontmatter
