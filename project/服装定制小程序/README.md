# 鼎盛服装定制供应链小程序

## 项目简介

这是一个基于微信小程序云开发技术栈的服装定制小程序，支持用户在线设计服装款式、上传图片、添加文字等DIY功能。

## 技术栈

- **前端**: 微信原生小程序 (WXML/WXSS/JS)
- **后端**: 微信云开发
  - 云函数 (Cloud Functions)
  - 云数据库 (Cloud Database)
  - 云存储 (Cloud Storage)
- **开发工具**: 微信开发者工具

## 项目结构

```
服装定制小程序/
├── miniprogram/              # 小程序前端代码
│   ├── pages/               # 页面
│   │   ├── index/           # 首页
│   │   ├── category/        # 分类页
│   │   ├── design/          # 设计页（核心）
│   │   └── profile/         # 个人中心
│   ├── components/          # 组件
│   │   ├── design-canvas/   # 设计画布组件
│   │   ├── banner/          # 轮播图组件
│   │   ├── product-card/    # 商品卡片组件
│   │   └── category-grid/   # 分类网格组件
│   ├── utils/               # 工具函数
│   │   ├── cloud.js         # 云开发封装
│   │   ├── canvas.js        # Canvas工具
│   │   └── storage.js       # 本地存储
│   ├── styles/              # 全局样式
│   │   └── variables.wxss   # CSS变量
│   ├── images/              # 图片资源
│   ├── app.js               # 应用入口
│   ├── app.json             # 应用配置
│   └── app.wxss             # 全局样式
├── cloudfunctions/          # 云函数
│   ├── getProducts/         # 获取商品列表
│   ├── saveDesign/          # 保存设计
│   ├── getMyDesigns/        # 获取我的设计
│   └── getCategories/       # 获取分类列表
└── project.config.json      # 项目配置
```

## 核心功能

### 1. 首页 (Index)
- ✅ 轮播图展示
- ✅ 分类网格展示
- ✅ 搜索框
- ✅ 快速导航

### 2. 分类页 (Category)
- ✅ 左侧分类树
- ✅ 右侧商品列表
- ✅ 搜索与筛选
- ✅ 排序功能

### 3. 设计页 (Design) - 核心
- ✅ 选择款式模板
- ✅ 上传图片
- ✅ 添加文字
- ✅ 元素拖拽（开发中）
- ✅ 元素缩放/旋转（开发中）
- ✅ 保存设计
- ✅ 导出设计稿

### 4. 个人中心 (Profile)
- ✅ 用户信息展示
- ✅ 我的设计列表
- ✅ 个人资料编辑
- ✅ 设置选项

## 快速开始

### 1. 环境准备
- 安装 [微信开发者工具](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html)
- 注册微信小程序账号
- 开通云开发服务

### 2. 项目导入
1. 打开微信开发者工具
2. 选择"导入项目"
3. 选择本项目目录
4. 填写 AppID（测试号可使用测试号）
5. 点击"导入"

### 3. 云开发配置
1. 在开发者工具中，点击"云开发"按钮
2. 开通云开发服务（按量付费）
3. 创建以下数据库集合：
   - `products` (商品)
   - `categories` (分类)
   - `designs` (设计)
   - `templates` (模板)
   - `users` (用户)

4. 创建云存储目录：
   - `products/` (产品图片)
   - `designs/` (设计稿)
   - `templates/` (模板图片)
   - `avatars/` (用户头像)

5. 上传云函数：
   - 右键 `cloudfunctions` 目录下的云函数文件夹
   - 选择"上传并部署：云端安装依赖"

### 4. 修改配置
打开 `miniprogram/app.js`，修改云开发环境ID：
```javascript
wx.cloud.init({
  env: 'your-env-id', // 替换为你的云开发环境ID
  traceUser: true
});
```

### 5. 运行项目
1. 在开发者工具中点击"编译"
2. 查看小程序效果

## 开发计划

### Week 1 - 基础框架与首页
- [x] 项目初始化
- [x] 首页开发
- [x] 云函数搭建

### Week 2 - 分类页与个人中心
- [x] 分类页开发
- [x] 个人中心开发
- [ ] 搜索与筛选功能完善

### Week 3 - 设计页核心功能
- [x] 设计页基础结构
- [ ] 设计画布组件完善
- [ ] 元素拖拽功能
- [ ] 元素缩放/旋转

### Week 4 - 优化与测试
- [ ] 性能优化
- [ ] 兼容性测试
- [ ] Bug修复

## 注意事项

1. **云函数部署**: 云函数需要上传并部署后才能使用
2. **图片资源**: 项目中的图片资源需要自行准备，或使用占位图
3. **测试环境**: 建议先使用测试号进行开发和测试
4. **数据库权限**: 注意设置数据库的读写权限规则

## 贡献指南

欢迎提交 Issue 和 Pull Request！

## 许可证

MIT License

## 联系方式

如有问题，请提交 Issue 或联系开发团队。

---

**文档版本**: v1.0
**最后更新**: 2026-01-11
