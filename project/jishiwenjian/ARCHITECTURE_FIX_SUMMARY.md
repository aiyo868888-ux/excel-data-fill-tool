# 架构修复总结 - 内容类型与标签分离

## 问题描述

之前的实现混淆了两个概念：
1. **导航栏分类**（灵感/启发/待办）
2. **标签系统**（用户自定义的分类标签）

错误的逻辑是：导航栏通过标签名称筛选，导致只有添加了"灵感"标签的内容才会显示在灵感页面。

## 正确的架构

### 1. 内容类型（type 字段）
- **位置**：`ClipboardEntity.type` 字段
- **用途**：对应导航栏的三个 Tab（灵感/启发/待办）
- **设置时机**：用户点击悬浮窗时选择类型
- **筛选逻辑**：`WHERE type = '灵感'`

### 2. 标签系统（独立维度）
- **位置**：`TagDefinition` 表 + `clipboard_tag_relation` 关联表
- **用途**：用户自定义的分类标签（如"工作"、"读书"等）
- **设置时机**：在编辑对话框中选择标签
- **筛选逻辑**：通过关联表查询

## 修改清单

### 数据层
- ✅ `ClipboardEntity` 添加 `type: String` 字段
- ✅ `ClipboardDao` 添加 `getClipboardsByType(type: String)` 方法
- ✅ `ClipboardDao` 添加 `updateClipboardWithType()` 方法
- ✅ 数据库迁移：v6 → v7，添加 type 列

### Repository 层
- ✅ `ClipboardRepository.saveClipboard()` 接收 `type` 参数
- ✅ `ClipboardRepository.updateClipboard()` 接收 `type` 参数
- ✅ `ClipboardRepository.getClipboardsByType()` 方法
- ✅ `UnifiedContentRepository` 改用 `type` 字段筛选

### UI 层
- ✅ `ClipboardEditDialogFragment` 添加 `contentType` 字段
- ✅ `setDefaultTag()` 改为设置内容类型
- ✅ 所有保存方法传递 `contentType` 参数

## 使用示例

```kotlin
// 保存到"灵感"类型，带标签"工作"
clipboardRepository.saveClipboard(
    content = "这是一个想法",
    tags = listOf("工作"),  // 标签是独立的
    type = "灵感"           // 类型决定显示在哪个导航栏
)

// 导航栏查询
unifiedContentRepository.observeInspirations()  // type='灵感'
unifiedContentRepository.observeInsights()      // type='启发'
unifiedContentRepository.observeTodos()         // type='待办'
```

## 用户流程

1. 用户点击悬浮窗 → 选择类型（灵感/启发/待办）
2. 进入编辑对话框 → 可选择标签（工作/读书/生活等）
3. 保存后 → 内容显示在对应类型的导航栏
4. 标签用于二次筛选和组织

## 数据库迁移

```sql
-- v6 → v7
ALTER TABLE clipboards ADD COLUMN type TEXT NOT NULL DEFAULT '灵感'
```

## 测试检查点

- [ ] 选择"灵感"类型保存后，内容出现在灵感 Tab
- [ ] 选择"启发"类型保存后，内容出现在启发 Tab
- [ ] 选择"待办"类型保存后，内容出现在待办 Tab
- [ ] 标签选择不影响导航栏显示
- [ ] 可以给任意类型的内容添加任意标签
