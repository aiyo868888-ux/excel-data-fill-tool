# 闪念笔记 - Pencil 设计原型

## 📱 设计文件

本目录包含闪念笔记应用的完整 UI 设计原型，采用 Pencil 格式。

### 设计页面

#### 核心页面（4个）

1. **[01-home-inspiration.pen](01-home-inspiration.pen)** - 💡 灵感首页
   - 笔记卡片列表（按日期分组）
   - 顶部搜索栏
   - 底部导航（首页/启发/待办）
   - 悬浮添加按钮（FAB）

2. **[07-home-insight.pen](07-home-insight.pen)** - 📖 启发首页
   - 来源筛选器（全部/书籍/播客/网络/对话/课程/其他）
   - 卡片显示来源图标 + 来源详情 + 内容预览
   - 按日期分组
   - 底部导航

3. **[05-home-todo.pen](05-home-todo.pen)** - ⚡ 待办首页
   - 快速筛选（全部/今天/已完成）
   - 任务列表（带复选框）
   - 时间显示和优先级标记
   - 已完成状态（可折叠）

4. **[06-settings.pen](06-settings.pen)** - ⚙️ 设置页面
   - 深色模式切换
   - 字体大小调整
   - 数据导出
   - 提醒设置
   - 同步配置

#### 对话框页面（3个）

5. **[02-dialog-inspiration.pen](02-dialog-inspiration.pen)** - 💡 灵感对话框
   - 大文本输入区
   - 剪切板计数器
   - 智能提示
   - 保存/取消按钮

6. **[03-dialog-insight.pen](03-dialog-insight.pen)** - 📖 启发对话框
   - 来源选择（6种类型）
   - 来源详情输入
   - 关键词标签
   - 智能提示（URL识别）

7. **[04-dialog-todo.pen](04-dialog-todo.pen)** - ⚡ 待办对话框
   - 时间快速选择（5种选项）
   - 任务输入区
   - 智能多任务识别
   - 重复任务设置

#### 补充页面（4个）

8. **[08-search-page.pen](08-search-page.pen)** - 🔍 搜索页面
   - 顶部搜索栏
   - 类型筛选器（全部/灵感/启发/待办）
   - 搜索结果列表
   - 高亮匹配文字

9. **[09-empty-state.pen](09-empty-state.pen)** - 📭 空状态页面
   - 友好的空状态插图
   - 引导文案
   - FAB 按钮引导

10. **[10-detail-page.pen](10-detail-page.pen)** - 📄 详情页面
    - 完整内容展示
    - 元信息（类型、时间）
    - 相关内容推荐
    - 返回/更多操作

11. **[11-components-library.pen](11-components-library.pen)** - 🧩 组件库
    - 按钮（主/次/文字）
    - 标签（4种类型）
    - 输入框（默认/聚焦）
    - 卡片示例

---

## 🎨 设计系统

### 配色方案

```css
/* 主色调 */
--primary: #6200EE;        /* 深紫 */
--primary-light: #7C4DFF;  /* 浅紫 */
--primary-dark: #3700B3;   /* 深紫 */

/* 次要色 */
--secondary: #03DAC6;      /* 青绿 */
--secondary-light: #66FFF9;
--secondary-dark: #00BFA5;

/* 三种类型配色 */
--inspiration: #FF6B6B;    /* 灵感红 */
--insight: #4ECDC4;        /* 启发青 */
--todo: #FFE66D;           /* 待办黄 */

/* 背景系统 */
--background: #FAFAFA;     /* 浅灰背景 */
--surface: #FFFFFF;        /* 白色表面 */
--surface-variant: #F5F5F5;

/* 文字颜色 */
--text-primary: #000000;
--text-secondary: #666666;
--text-tertiary: #999999;
--text-on-primary: #FFFFFF;
```

### 字体系统

```css
/* 字体族 */
font-family: 'Outfit', 'Noto Sans SC', sans-serif;

/* 字体层级 */
--font-title-xl: 28px / 700  /* 页面标题 */
--font-title-lg: 20px / 700  /* 对话框标题 */
--font-title-md: 15px / 600  /* 卡片标题 */
--font-body: 15px / 400      /* 正文 */
--font-caption: 13px / 500   /* 辅助文字 */
--font-small: 12px / 400     /* 小字 */
--font-tiny: 11px / 500      /* 标签文字 */
```

### 间距系统

```css
--space-xs: 4px;
--space-sm: 8px;
--space-md: 12px;
--space-lg: 16px;
--space-xl: 24px;
--space-2xl: 32px;
--space-3xl: 48px;
```

### 圆角系统

```css
--radius-sm: 8px;      /* 小圆角（标签） */
--radius-md: 12px;     /* 中圆角（按钮、输入框） */
--radius-lg: 16px;     /* 大圆角（卡片） */
--radius-xl: 24px;     /* 超大圆角（对话框） */
--radius-full: 9999px; /* 完全圆形（FAB） */
```

### 阴影系统

```css
--shadow-sm: 0 1px 2px rgba(0,0,0,0.06);
--shadow-md: 0 4px 12px rgba(0,0,0,0.08);
--shadow-lg: 0 8px 24px rgba(0,0,0,0.12);
--shadow-xl: 0 16px 48px rgba(0,0,0,0.16);
```

---

## 📐 页面规范

### 设备尺寸

- **主设备**: iPhone 15 Pro (393 x 852px)
- **状态栏**: 54px
- **底部导航**: 80px（含安全区域）
- **内容区域**: 可滚动

### 组件尺寸

**卡片**
- 宽度: 361px（左右各16px边距）
- 高度: 自适应，最小76px
- 圆角: 16px
- 左侧彩色指示条: 3px宽

**悬浮按钮（FAB）**
- 尺寸: 56 x 56px
- 位置: 右下角，距离底部100px
- 形状: 完全圆形
- 阴影: `--shadow-md`

**对话框**
- 最大高度: 80%屏幕高度
- 圆角: 顶部24px，底部0
- 背景: 半透明遮罩 `rgba(0,0,0,0.5)`
- 内容区: 可滚动

**底部导航**
- 高度: 80px（含安全区域）
- 图标: 24px
- 文字: 11px
- 分隔线: 1px实线

---

## 🎭 交互动效

### 页面切换

```css
/* 淡入 */
animation: fadeIn 250ms cubic-bezier(0.4, 0, 0.2, 1);

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
```

### 对话框

```css
/* 从底部滑入 */
animation: slideUp 250ms cubic-bezier(0.4, 0, 0.2, 1);

@keyframes slideUp {
  from {
    transform: translateY(100%);
  }
  to {
    transform: translateY(0);
  }
}
```

### 按钮反馈

```css
/* 悬停 */
transform: scale(1.05);
transition: transform 150ms;

/* 按下 */
transform: scale(0.95);
transition: transform 150ms;
```

### 智能提示

```css
/* 从上方滑入 */
animation: slideDown 250ms cubic-bezier(0.4, 0, 0.2, 1);

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
```

---

## 🔍 如何查看设计

### 方法 1: VSCode + Pencil 插件

1. 确保已安装 [Pencil Dev](https://marketplace.visualstudio.com/items?itemName=highagency.pencildev) 插件
2. 在 VSCode 中打开任意 `.pen` 文件
3. 按 `Ctrl+Shift+P`，输入 `Pencil: Toggle Design Mode`
4. 或使用快捷键 `Ctrl+Shift+\`

### 方法 2: Trae 编辑器

1. 在 Trae 中打开 `.pen` 文件
2. 右键标签页，选择 `Open with Pencil Design Editor`
3. 或按 `Ctrl+Shift+P`，输入 `Pencil: New File`

### 方法 3: 导出 HTML

如果 Pencil 插件无法使用，可以：
1. 使用已生成的 HTML 原型：`ui-design.html`
2. 在浏览器中直接预览

---

## 📝 设计原则

### 极简主义

- ✅ 留白充足（最小16px边距）
- ✅ 对称平衡（居中对齐）
- ✅ 聚焦核心（内容优先）

### 视觉层次

- ✅ 字体层级清晰（7级字体系统）
- ✅ 颜色低饱和度（避免过度鲜艳）
- ✅ 阴影轻微（不干扰内容）

### 交互直觉

- ✅ 每个操作有反馈（动画/阴影）
- ✅ 样式保持一致（圆角/间距统一）
- ✅ 动画符合物理规律（cubic-bezier缓动）

---

## 🎯 三种类型的视觉区分

### 💡 灵感

- **主色**: `#FF6B6B`（红色）
- **浅色背景**: `#FFE5E5`
- **使用场景**: 突然想到的点子、创造性想法

### 📖 启发

- **主色**: `#4ECDC4`（青色）
- **浅色背景**: `#E6F9F7`
- **使用场景**: 读书感悟、播客收获、文章学习

### ⚡ 待办

- **主色**: `#FFE66D`（黄色）
- **浅色背景**: `#FFF9E6`
- **使用场景**: 记住要做的事情、会议任务、购物清单

---

## 🚀 下一步

1. ✅ 在 Pencil 中查看设计原型
2. ⬜ 与产品经理确认交互流程
3. ⬜ 导出设计规范文档（JSON）
4. ⬜ 开始 Jetpack Compose 开发

---

## 📚 相关文档

- [产品需求文档](../../docs/闪念笔记-PRD.md)
- [技术架构文档](../../docs/闪念笔记-技术架构.md)
- [UI 设计原型 HTML](../ui-design.html)

---

**设计工具**: Pencil Dev (VSCode Extension)
**设计日期**: 2025-01-23
**设计师**: Claude (AI Designer)
