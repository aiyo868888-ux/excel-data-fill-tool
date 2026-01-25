# 闪念笔记 (QuickDrop Notes)

一个极简的闪念笔记手机应用，支持三个分类：闪念、启发、待办

## 功能特性

- ✨ **三种笔记类型**：闪念⚡、启发💡、待办✓
- 🎨 **暗黑科技风格**：深色背景 + 霓虹光晕
- 📱 **移动端优先**：专为手机设计的交互体验
- 💾 **本地存储**：数据保存在浏览器 localStorage
- ✏️ **编辑删除**：支持编辑和删除已有笔记
- 🎯 **悬浮窗按钮**：可拖动的悬浮按钮（模拟）

## 快速开始

### 安装依赖

```bash
cd frontend
npm install
```

### 启动开发服务器

Windows:
```bash
# 双击运行
start.bat

# 或在命令行中
npm run dev
```

macOS/Linux:
```bash
npm run dev
```

应用将在 `http://localhost:5173` 启动

### 构建生产版本

```bash
npm run build
```

构建产物在 `frontend/dist` 目录

## 项目结构

```
quick-drop/
├── frontend/
│   ├── src/
│   │   ├── components/          # React 组件
│   │   │   ├── TagSelectPage.tsx    # 标签选择页
│   │   │   ├── InputPage.tsx        # 输入页
│   │   │   ├── ListPage.tsx         # 列表页
│   │   │   └── FloatingButton.tsx   # 悬浮按钮
│   │   ├── utils/
│   │   │   └── storage.ts       # 本地存储工具
│   │   ├── types.ts             # TypeScript 类型定义
│   │   ├── App.tsx              # 主应用组件
│   │   ├── index.css            # 全局样式
│   │   └── main.tsx             # 应用入口
│   ├── index.html
│   ├── package.json
│   ├── vite.config.ts
│   └── tailwind.config.js
├── DESIGN.md                    # 设计文档
├── README-NOTES.md              # 项目说明
└── start.bat                    # Windows 启动脚本
```

## 使用说明

### 创建笔记

1. 点击悬浮按钮 ✏️ 或启动应用
2. 选择笔记类型（闪念/启发/待办）
3. 输入内容
4. 点击保存

### 查看笔记

1. 在列表页查看所有笔记
2. 点击底部导航切换分类
3. 点击"编辑"修改笔记
4. 点击"删除"移除笔记

### 悬浮按钮

- 悬浮按钮可拖动到任意位置
- 点击打开标签选择页
- 支持触摸屏和鼠标操作

## 技术栈

- **React 19** - UI 框架
- **TypeScript** - 类型安全
- **Vite** - 构建工具
- **Tailwind CSS** - 样式框架
- **LocalStorage** - 数据持久化

## 设计理念

### 极简主义
- 专注单点功能
- 减少操作步骤
- 清晰的视觉层次

### 暗黑科技风
- 深色背景护眼
- 霓虹光晕突出重点
- 渐变按钮引导操作

### 移动优先
- 大触摸目标
- 流畅的动画
- 直观的手势操作

## 后续计划

- [ ] 笔记搜索
- [ ] 标签系统
- [ ] 云同步
- [ ] 导出功能
- [ ] 主题切换
- [ ] PWA 支持
- [ ] 桌面悬浮窗（原生应用）

## License

MIT
