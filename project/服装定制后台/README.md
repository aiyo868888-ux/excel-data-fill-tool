# 服装定制小程序 - 后台管理系统

## 📋 项目简介

这是一个为**服装定制小程序**打造的本地后台管理系统，提供可视化的数据管理界面。

### 核心功能

- ✅ **数据概览**：实时查看各集合数据统计
- ✅ **商品管理**：商品的增删改查操作
- ✅ **分类管理**：分类的增删改查操作
- ✅ **云开发连接**：直接管理微信云开发数据库

### 技术架构

```
前端界面 (Vue 3 + Element Plus)
         ↓
API 接口 (Express + Node.js)
         ↓
云开发 SDK (wx-server-sdk)
         ↓
微信云开发数据库
```

---

## 🚀 快速开始

### 方式1：一键安装（推荐）

双击运行 **`一键安装.bat`**，按照提示操作：

```
1. 输入你的云开发环境ID（如 cloud1-xxx）
2. 等待自动安装依赖
3. 安装完成后，双击运行 `启动服务.bat`
4. 访问 http://localhost:3000
```

---

### 方式2：手动安装

#### 步骤1：配置环境ID

创建 `server/.env` 文件：

```env
CLOUD_ENV_ID=cloud1-xxx
PORT=3000
```

#### 步骤2：安装依赖

```bash
# 安装后端依赖
cd server
npm install

# 安装前端依赖
cd ../admin
npm install
```

#### 步骤3：启动服务

```bash
# 构建前端
cd admin
npm run build

# 启动后端
cd ../server
npm start
```

访问地址：**http://localhost:3000**

---

## 📖 使用说明

### 1. 首次使用

1. 打开系统后，点击右上角 **"检查云开发连接"** 按钮
2. 确认能正常连接到云数据库
3. 开始添加数据

### 2. 添加分类

1. 进入 **"分类管理"** 页面
2. 点击 **"添加分类"**
3. 填写分类信息并保存

**示例数据：**
```json
{
  "name": "冲锋衣系列",
  "parentId": "",
  "sortOrder": 1,
  "icon": "/images/category-1.png"
}
```

### 3. 添加商品

1. 进入 **"商品管理"** 页面
2. 点击 **"添加商品"**
3. 填写商品信息并保存

**示例数据：**
```json
{
  "name": "1618三合一冲锋衣",
  "categoryId": "分类ID",
  "type": "三合一",
  "material": "100%聚酯纤维",
  "style": "通款",
  "price": 299,
  "status": 1,
  "description": "专业户外三合一冲锋衣"
}
```

---

## 📁 目录结构

```
服装定制后台/
├── server/                 # 后端服务
│   ├── routes/            # API 路由
│   │   ├── products.js    # 商品管理
│   │   ├── categories.js  # 分类管理
│   │   ├── cloud.js       # 云开发状态
│   │   └── upload.js      # 文件上传
│   ├── config/            # 配置文件
│   │   └── cloud.js       # 云开发配置
│   ├── app.js             # 主入口
│   ├── .env               # 环境变量
│   └── package.json
│
├── admin/                 # 前端界面
│   ├── src/
│   │   ├── views/         # 页面组件
│   │   │   ├── Dashboard.vue
│   │   │   ├── Products.vue
│   │   │   └── Categories.vue
│   │   ├── router/        # 路由配置
│   │   ├── App.vue        # 根组件
│   │   └── main.js        # 入口文件
│   ├── index.html
│   ├── vite.config.js
│   └── package.json
│
├── 一键安装.bat           # 自动安装脚本
├── 启动服务.bat           # 启动服务脚本
└── 启动指南.md            # 详细说明文档
```

---

## 🔑 获取环境ID

### 方法1：云开发控制台

1. 打开[云开发控制台](https://console.cloud.tencent.com/tcb)
2. 查看环境名称旁边的环境ID
3. 格式通常为：`cloud1-xxx`

### 方法2：微信开发者工具

1. 打开微信开发者工具
2. 点击顶部 **"云开发"** 按钮
3. 在弹出的窗口中查看环境ID

---

## 🐛 常见问题

### Q1: npm install 失败

**解决方案：**

```bash
npm install --registry=https://registry.npmmirror.com
```

---

### Q2: 端口被占用

**错误提示：** `Error: listen EADDRINUSE: address already in use :::3000`

**解决方案：**

修改 `server/.env` 文件中的端口号：

```env
PORT=3001
```

---

### Q3: 云开发连接失败

**可能原因：**
1. 环境ID配置错误
2. 云开发环境未开通
3. 云开发环境欠费或冻结

**解决方案：**
1. 检查 `server/.env` 中的 `CLOUD_ENV_ID` 是否正确
2. 确认云开发环境状态正常
3. 修改后需要重启服务

---

## 🔧 开发模式

如果需要实时编辑前端代码：

```bash
# 终端1：启动后端
cd server
npm start

# 终端2：启动前端（开发模式）
cd admin
npm run dev
```

- 后端地址：http://localhost:3000
- 前端地址：http://localhost:8080

---

## 📊 功能规划

### 已实现 ✅

- [x] 数据概览
- [x] 商品管理
- [x] 分类管理
- [x] 云开发连接检查

### 待开发 ⏳

- [ ] 图片上传到云存储
- [ ] 设计模板管理
- [ ] 用户设计管理
- [ ] 订单管理
- [ ] 数据导出功能
- [ ] 批量导入功能

---

## 📞 技术支持

如遇到问题，请查看 **[启动指南.md](启动指南.md)** 获取详细的故障排除步骤。

---

## 📝 更新日志

### v1.0.0（2026-01-11）

- 🎉 初始版本发布
- ✨ 实现商品和分类管理功能
- ✨ 支持云开发数据库连接
- 📝 完善文档和使用说明

---

**开发时间**：2026-01-11
**技术栈**：Node.js + Express + Vue 3 + Element Plus + 微信云开发

**准备好了吗？开始使用吧！** 🚀
