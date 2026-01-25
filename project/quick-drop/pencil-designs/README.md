# QQ 音乐 - Apple 风格设计

## 📱 设计文件

本目录包含 QQ 音乐应用的 Apple 风格设计文件，采用浅色系极简设计。

### 设计规范文档
- [qq-music-apple-style.pencil.md](qq-music-apple-style.pencil.md) - 完整设计规范文档

### Pencil 画布文件
1. [qq-music-player.pencil](qq-music-player.pencil) - 播放首页
2. [qq-music-discover.pencil](qq-music-discover.pencil) - 发现页
3. [qq-music-playlist.pencil](qq-music-playlist.pencil) - 播放列表
4. [qq-music-search.pencil](qq-music-search.pencil) - 搜索页
5. [qq-music-library.pencil](qq-music-library.pencil) - 音乐库

## 🎨 设计系统

### 配色方案
- **背景色**: #FAFAFA (Apple 浅灰)
- **卡片色**: #FFFFFF (纯白)
- **主色调**: #FA233B (QQ音乐红)
- **次要色**: #007AFF (iOS 蓝)
- **文字主**: #000000 (85% opacity)
- **文字次**: #8E8E93 (60% opacity)

### 字体系统
- **字体**: SF Pro Display (Apple 系统字体)
- **标题层级**:
  - 大标题: 22px, Bold
  - 中标题: 17px, Semibold
  - 小标题: 15px, Regular
- **正文**: 16px, Medium
- **辅助**: 13px, Regular
- **次要**: 11px, Medium

### 组件规范

#### 按钮
- **主按钮**: 64 x 64px, 圆形, #FA233B
- **次按钮**: 48 x 48px
- **功能按钮**: 28px 图标

#### 卡片
- **大卡片**: 361 x 140px, 圆角 16px
- **中卡片**: 280 x 280px, 圆角 12px
- **小卡片**: 113 x 113px, 圆角 8px

#### 间距系统
- **页面边距**: 16px
- **卡片间距**: 8px (小), 12px (中), 16px (大)
- **元素间距**: 4px, 8px, 12px, 16px

## 📐 页面布局

### 1. 播放首页 (PlayerPage)
- 专辑封面: 280 x 280px, 居中
- 歌曲信息: 居中对齐
- 进度条: 宽度 329px
- 播放控制: 居中布局

### 2. 发现页 (DiscoverPage)
- 横幅卡片: 361 x 140px
- 歌单网格: 3 列布局
- 新歌卡片: 2 列横向滚动

### 3. 播放列表 (PlaylistPage)
- 当前播放: 高亮卡片
- 普通歌曲: 列表布局
- 分隔线: 0.5px

### 4. 搜索页 (SearchPage)
- 搜索框: 圆角 10px
- 标签云: 圆角 16px
- 历史记录: 列表布局

### 5. 音乐库 (LibraryPage)
- 收藏夹: 渐变卡片
- 专辑网格: 3 列布局
- 文件夹卡片: 361 x 60px

## 🎭 交互动效

### 页面转场
- 推入: 300ms, ease-out
- 淡入淡出: 250ms, linear
- 滑动: 350ms, spring

### 按钮反馈
- 按下: scale(0.95), 100ms
- 悬停: scale(1.02), 150ms
- 恢复: 200ms, ease-out

### 列表动画
- 进入: 滑入 + 淡入, 200ms, stagger(30ms)
- 删除: 滑出 + 淡出, 250ms

### 播放动画
- 专辑封面: 缓慢旋转 (播放时), 20s/圈
- 进度条: 实时更新, smooth

## 📱 设备适配

### iPhone 15 Pro (393 x 852px)
- 标准设计尺寸

### iPhone SE (375 x 667px)
- 缩小卡片尺寸 10%
- 减少间距 20%

### iPhone 15 Pro Max (430 x 932px)
- 增加卡片尺寸 10%
- 增加间距 15%

### iPad (768 x 1024)
- 列数从 3 列增加到 4-5 列
- 卡片尺寸等比放大

## 🎯 Apple 设计原则

### 极简主义
- ✅ 留白充足
- ✅ 对称平衡
- ✅ 聚焦核心

### 视觉层次
- ✅ 字体层级清晰
- ✅ 颜色低饱和度
- ✅ 阴影轻微

### 交互直觉
- ✅ 每个操作有反馈
- ✅ 样式保持一致
- ✅ 动画符合物理规律

## 🚀 下一步

1. 使用 Pencil 打开 `.pencil` 文件查看设计
2. 导出为图片或 HTML
3. 实现前端代码
4. 添加交互动效

## 📝 注意事项

- 所有尺寸单位: px
- 所有颜色使用 HEX 格式
- 圆角和阴影统一使用设计规范
- 字体优先使用系统字体 SF Pro
