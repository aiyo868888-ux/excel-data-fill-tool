# 如何在 Pencil 中查看 QQ 音乐设计

## 📌 重要说明

`.pencil` 文件是 Pencil 插件的画布文件，需要在 VSCode 中通过 Pencil 插件打开才能查看和编辑。

## 🔧 打开方法

### 方法 1: 在 VSCode 资源管理器中打开

1. 打开 VSCode
2. 在左侧文件资源管理器中，导航到：
   ```
   project/quick-drop/pencil-designs/
   ```
3. 点击任意 `.pencil` 文件，例如：
   - `qq-music-player.pencil` (播放首页)
   - `qq-music-discover.pencil` (发现页)
   - `qq-music-playlist.pencil` (播放列表)
   - `qq-music-search.pencil` (搜索页)
   - `qq-music-library.pencil` (音乐库)

4. Pencil 画布会在 VSCode 的侧边栏或新面板中自动打开

### 方法 2: 使用命令面板

1. 在 VSCode 中按 `Ctrl+Shift+P` (Windows) 或 `Cmd+Shift+P` (Mac)
2. 输入 "Pencil: Open Canvas"
3. 选择要打开的 `.pencil` 文件

### 方法 3: 右键菜单

1. 在文件资源管理器中右键点击 `.pencil` 文件
2. 选择 "Open with Pencil" 或 "使用 Pencil 打开"

## 📱 Pencil 界面说明

打开后你会看到：

### 左侧面板
- **Layers**: 图层列表，显示所有元素
- **Properties**: 属性面板，可调整选中元素的样式

### 中间画布
- 设计预览区域
- 可以拖拽、缩放、旋转元素

### 右侧面板
- **Components**: 组件库
- **Assets**: 资源管理

## 🎨 设计文件说明

### 1. qq-music-player.pencil - 播放首页
- 专辑封面 (280x280px)
- 歌曲信息
- 进度条
- 播放控制按钮
- 功能按钮
- 底部导航栏

### 2. qq-music-discover.pencil - 发现页
- 渐变横幅卡片
- 3列歌单网格
- 新歌横向滚动
- 底部导航栏

### 3. qq-music-playlist.pencil - 播放列表
- 当前播放高亮卡片
- 歌曲列表
- 迷你进度条

### 4. qq-music-search.pencil - 搜索页
- 圆角搜索框
- 热门标签云
- 历史搜索记录

### 5. qq-music-library.pencil - 音乐库
- 收藏夹渐变卡片
- 最近播放网格
- 本地音乐文件夹

## ✏️ 编辑设计

在 Pencil 中你可以：

1. **选择元素**: 点击画布上的元素
2. **移动元素**: 拖拽元素到新位置
3. **调整大小**: 拖拽元素的控制点
4. **修改属性**: 在右侧属性面板修改颜色、字体等
5. **添加元素**: 从左侧组件库拖拽新元素到画布
6. **删除元素**: 选中元素后按 `Delete` 键

## 📤 导出设计

在 Pencil 中可以导出为：

1. **PNG 图片**: `File` → `Export` → `PNG`
2. **HTML 文件**: `File` → `Export` → `HTML`
3. **PDF 文件**: `File` → `Export` → `PDF`
4. **SVG 文件**: `File` → `Export` → `SVG`

## 🔍 如果无法打开

### 检查 Pencil 插件
1. 在 VSCode 中打开扩展面板 (`Ctrl+Shift+X`)
2. 搜索 "Pencil"
3. 确认 "highagency.pencildev" 插件已安装并启用

### 重新安装 Pencil 插件
如果插件未安装：
1. 在 VSCode 扩展面板中搜索 "Pencil"
2. 找到 "Pencil Dev" (by HighAgency)
3. 点击 "Install" 安装
4. 安装完成后重启 VSCode

### 检查文件关联
确保 `.pencil` 文件与 Pencil 插件关联：
1. 在 VSCode 设置中搜索 "files.associations"
2. 添加 `.pencil` 文件关联

## 📚 相关文档

- [qq-music-apple-style.pencil.md](qq-music-apple-style.pencil.md) - 完整设计规范
- [README.md](README.md) - 设计文件索引

## 💡 提示

- 如果画布显示空白，尝试调整缩放级别
- 如果元素位置不对，可以查看图层列表调整层级
- 保存修改：`Ctrl+S` (Windows) 或 `Cmd+S` (Mac)
- 撤销操作：`Ctrl+Z` (Windows) 或 `Cmd+Z` (Mac)

## 🆘 需要帮助？

如果遇到问题：
1. 查看 Pencil 插件文档
2. 检查 VSCode 输出面板的错误信息
3. 尝试重启 VSCode
4. 重新安装 Pencil 插件
