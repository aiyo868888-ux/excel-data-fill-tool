# Pencil 画布激活指南

## 问题诊断

### 1. 检查 Pencil 是否激活

在 VSCode 中：
1. 打开命令面板：`Ctrl+Shift+P`
2. 输入：`Pencil:`
3. 查看是否有以下命令：
   - `Pencil: New File`
   - `Pencil: Toggle Design Mode`
   - `Pencil: Open Welcome File`

### 2. 正确创建 Pencil 文件的方法

**方法 A：使用命令面板**
1. 按 `Ctrl+Shift+P`
2. 输入 `Pencil: New File`
3. 选择保存位置
4. 文件会自动创建并打开画布

**方法 B：使用 Pencil 侧边栏**
1. 在 VSCode 左侧活动栏找到 Pencil 图标（铅笔图标）
2. 点击打开 "Pencil Recent Files" 面板
3. 点击 "+" 按钮创建新文件

**方法 C：手动创建然后打开**
1. 创建任意 `.pen` 文件
2. 在编辑器中打开
3. 右键点击编辑器
4. 选择 "Open with Pencil Design Editor"

### 3. 切换设计模式

如果文件已打开但显示黑屏：
1. 按 `Ctrl+Shift+P`
2. 输入 `Pencil: Toggle Design Mode`
3. 或使用快捷键：`Ctrl+Shift+\`

### 4. 检查 Pencil 输出

1. 在 VSCode 中打开输出面板：`Ctrl+Shift+U`
2. 在下拉菜单中选择 "Pencil"
3. 查看是否有错误信息

### 5. 重新加载插件

1. 按 `Ctrl+Shift+P`
2. 输入 `Developer: Reload Window`
3. 等待 VSCode 重新加载

### 6. 检查文件关联

确认 `.pen` 文件与 Pencil 关联：
1. 打开 VSCode 设置：`Ctrl+,`
2. 搜索：`files.associations`
3. 添加：
   ```json
   {
     "files.associations": {
       "*.pen": "pencil"
     }
   }
   ```

### 7. 尝试打开 Pencil 自带的示例

运行以下命令打开示例文件：
```
code "C:\Users\15085\.vscode\extensions\highagency.pencildev-0.6.9\node_modules\@ha\pencil-editor\render-tests\gradient.pen"
```

如果示例文件能显示，说明 Pencil 工作正常，问题在于我们的文件格式。

### 8. 查看当前问题

可能的原因：
- ✗ Pencil 插件未正确加载
- ✗ 文件格式不正确
- ✗ 画布渲染失败
- ✗ VSCode 版本不兼容

## 解决方案

### 最简单的方法：使用 Pencil 命令创建

1. 按 `Ctrl+Shift+P`
2. 输入：`Pencil: New File`
3. 保存为 `qq-music.pen`
4. 在 Pencil 画布中手动添加元素

### 或者：使用 HTML 代替

如果 Pencil 无法工作，我们可以：
1. 使用 HTML + CSS 创建可交互的设计
2. 在浏览器中预览
3. 导出为图片

## 下一步

请尝试以上方法，然后告诉我：
1. 你能看到 Pencil 的命令吗？
2. 示例文件能打开吗？
3. 创建新文件时画布显示正常吗？

这样我可以更好地帮助你解决问题。
