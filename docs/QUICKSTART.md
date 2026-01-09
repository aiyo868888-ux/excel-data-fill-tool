# 🚀 快速启动指南

## 启动Web应用

### Windows用户（推荐）

1. **双击运行**
   ```
   启动Web应用.bat
   ```

2. **或在命令行运行**
   ```bash
   cd "d:\claude code -11"
   python web_app.py
   ```

### 访问应用

启动后，在浏览器中打开：
```
http://localhost:5000
```

## 使用流程

### 1️⃣ 上传报表文件
- 点击"选择报表文件"按钮
- 选择你的Excel报表文件（.xlsx）
- 如果有密码，输入密码

### 2️⃣ 上传送货商文件
- 点击"选择送货商文件"按钮
- 可以一次选择多个送货商文件
- 支持 .xlsx 和 .xls 格式

### 3️⃣ 配置列范围
- 选择起始列（如 Q列）
- 选择结束列（如 U列）

### 4️⃣ 开始填充
- 点击"🚀 开始填充数据"按钮
- 等待进度条完成
- 点击"📥 下载填充后的报表"

## 文件位置

- **输入文件**: `uploads/` 目录
- **输出文件**: `temp/` 目录
- **核心代码**: `web_app.py`
- **前端页面**: `templates/data_filler.html`

## 停止应用

在命令行窗口按 `Ctrl + C`

## 常见问题

### 端口被占用？
修改 `web_app.py` 最后一行的端口号：
```python
app.run(host='0.0.0.0', port=5001)  # 改为5001
```

### 需要安装依赖？
```bash
pip install flask pandas openpyxl msoffcrypto
```

### 详细文档
查看 `docs/WEB应用使用指南.md`

---

**祝你使用愉快！** ✨
