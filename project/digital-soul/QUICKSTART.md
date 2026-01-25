# 快速启动指南

## 前置要求

- Node.js >= 18
- npm >= 9

## 安装依赖

```bash
cd project/digital-soul
npm install
```

## 开发模式

### 方法 1：使用 npm 脚本（推荐）

```bash
npm run dev
```

这将：
1. 启动 Vite 开发服务器（端口 5173）
2. 编译 Electron 主进程和预加载脚本
3. 启动 Electron 应用

### 方法 2：手动启动

1. 启动 Vite 开发服务器：
```bash
npm run build:vue -- --watch
```

2. 在另一个终端编译并启动 Electron：
```bash
npm run build:electron
electron .
```

## 项目结构说明

### 核心文件

- `electron/main.ts` - Electron 主进程入口
- `electron/database/` - 数据库管理相关
- `src/main.ts` - Vue 应用入口
- `src/App.vue` - 根组件
- `src/core/` - 核心业务逻辑（数据模型）
- `src/components/` - Vue 组件
- `src/views/` - 页面视图

### 配置文件

- `package.json` - 项目依赖和脚本
- `tsconfig.json` - TypeScript 配置（前端）
- `electron/tsconfig.json` - TypeScript 配置（Electron）
- `build/vite.config.ts` - Vite 构建配置
- `.env.example` - 环境变量模板

## 功能说明

### 已实现功能

1. **对话功能** ([Home.vue](src/views/Home.vue))
   - 创建对话
   - 发送/接收消息
   - 消息持久化

2. **分身画像** ([SoulView.vue](src/views/SoulView.vue))
   - 展示价值观
   - 展示性格特质
   - 展示关注方向
   - 展示评估指标

3. **数据库系统**
   - SQLite 数据库（better-sqlite3）
   - 完整的表结构（见 [schema.sql](electron/database/schema.sql)）
   - IPC 通信接口

### 开发中功能

- AI 特征提取
- 决策模拟
- 工具集成
- 数据分析

## 数据库

数据库文件位置：`./data/soul.db`
  
表结构：
- `digital_souls` - 数字分身
- `memory_fragments` - 记忆片段
- `patterns` - 模式
- `conversations` - 对话
- `messages` - 消息
- `feedback` - 反馈
- `soul_versions` - 版本历史

## 开发提示

### 热重载

- 前端代码修改会自动热重载（Vite HMR）
- Electron 主进程修改需要重启应用

### 调试

- 打开 DevTools：`Ctrl+Shift+I`（Windows/Linux）或 `Cmd+Option+I`（Mac）
- 查看日志：在 DevTools 的 Console 中

### 环境变量

复制 `.env.example` 到 `.env` 并配置：

```bash
cp .env.example .env
```

## 常见问题

### 1. 安装依赖失败

清理缓存并重新安装：
```bash
rm -rf node_modules package-lock.json
npm install
```

### 2. Electron 启动失败

检查是否编译成功：
```bash
npm run build:electron
```

### 3. 端口被占用

修改 `build/vite.config.ts` 中的端口配置。

## 下一步

1. 配置 AI API 密钥（OpenAI/Claude）
2. 实现记忆提取引擎
3. 实现模式挖掘功能
4. 添加决策模拟功能

详细开发计划见：[项目根目录的计划文档](C:/Users/15085/.claude/plans/harmonic-napping-gadget.md)
