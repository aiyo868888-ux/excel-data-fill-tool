# 图片资源管理指南

## 📁 图片目录结构

```
miniprogram/images/
├── banners/           # 轮播图
│   ├── banner1.jpg
│   ├── banner2.jpg
│   └── banner3.jpg
├── categories/        # 分类图标
│   ├── category-1.png
│   ├── category-2.png
│   └── ...
├── products/          # 商品图片
│   ├── product-1.jpg
│   ├── product-2.jpg
│   └── ...
├── templates/         # 设计模板图片
│   ├── template-1.jpg
│   └── ...
├── tshirt-template.png  # T恤设计模板
└── icons/             # 图标
    ├── icon-search.png
    └── ...
```

## 🔧 快速方案：使用纯色占位图

如果暂时没有图片，可以在WXML中使用纯色占位：

```xml
<!-- 使用背景色代替图片 -->
<image src="{{item.image}}" mode="aspectFill" style="background: #f0f0f0;" />
```

## 🌐 方案：使用网络图片

修改 mock/data.js，使用网络图片URL：

```javascript
banners: [
  {
    id: 1,
    image: 'https://picsum.photos/750/300?random=1', // 随机图片
    title: '2025春夏新款上市'
  }
]
```

## 📤 添加本地图片

1. 将图片放到 `miniprogram/images/` 对应目录
2. 在代码中引用：`/images/banners/banner1.jpg`

## 🎨 设计模板图片

设计页需要T恤模板图，建议尺寸：750x1000px

放到：`/images/tshirt-template.png`
