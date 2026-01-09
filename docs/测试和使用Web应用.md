# 🧪 Web应用测试和使用指南

## 📋 当前状态

✅ **Web应用已创建完成**
- 后端：Flask服务器 (`web_app.py`)
- 前端：现代化HTML界面 (`templates/data_filler.html`)
- 核心逻辑：数据填充工具 (`tests/数据填充工具.py`)

## 🚀 如何启动

### 方法1: 使用启动脚本（推荐）
```bash
双击：启动Web应用.bat
```

### 方法2: 命令行启动
```bash
cd "d:\claude code -11"
python web_app.py
```

看到以下信息表示成功：
```
==================================================
   数据填充工具 - Web应用
   访问地址: http://localhost:5000
======================================================
```

## 🧪 测试步骤

### 步骤1: 创建测试文件（已完成）

✅ 已创建测试报表：`temp/test_report.xlsx`

包含31个工作表（1-31日），可以用来测试。

### 步骤2: 创建测试送货商文件

创建一个简单的送货商文件用于测试：

```python
# 在 Python 中运行
import pandas as pd

# 创建测试数据
data = {
    '名称': ['商品A', '商品B', '商品C'],
    '单位': ['kg', 'kg', '个'],
    '数量': [10, 20, 30],
    '单价': [50, 30, 100],
    '总价': [500, 600, 3000]
}

# 创建Excel文件
with pd.ExcelWriter('temp/test_supplier.xlsx') as writer:
    # 工作表名称就是日期
    data.to_excel(writer, sheet_name='9', index=False)
    data.to_excel(writer, sheet_name='10', index=False)

print("Test supplier file created: temp/test_supplier.xlsx")
```

### 步骤3: 测试Web应用

1. **打开浏览器**
   ```
   http://localhost:5000
   ```

2. **上传报表文件**
   - 点击"选择报表文件"
   - 选择 `temp/test_report.xlsx`
   - 点击上传

   预期结果：
   ```
   ✅ 报表已加载: test_report.xlsx (31 个日期)
   ```

3. **上传送货商文件**
   - 点击"选择送货商文件"
   - 选择 `temp/test_supplier.xlsx`
   - 点击上传

   预期结果：
   ```
   ✅ test_supplier.xlsx (2 个日期)
   ```

4. **配置列范围**
   - 起始列：选择 Q列
   - 结束列：选择 U列

5. **开始填充**
   - 点击"🚀 开始填充数据"
   - 等待进度条完成

6. **下载结果**
   - 点击"📥 下载填充后的报表"
   - 打开下载的文件，检查数据是否正确填充

## 🐛 常见问题排查

### 问题1: "File is not a zip file"

**原因**：上传的不是有效的Excel文件

**解决**：
- 确保文件是 `.xlsx` 或 `.xls` 格式
- 不要上传损坏的文件
- 不要上传其他格式（如.csv, .txt等）

**检查方法**：
```python
import openpyxl
wb = openpyxl.load_workbook('你的文件.xlsx')
print("文件正常")
```

### 问题2: 上传后没有反应

**原因**：可能是文件太大或网络问题

**解决**：
1. 打开浏览器控制台（按F12）
2. 查看Console标签的错误信息
3. 查看Network标签的请求状态

### 问题3: 进度条卡住不动

**原因**：后端处理时间过长

**解决**：
1. 查看命令行窗口的日志输出
2. 等待处理完成（可能需要几秒钟）
3. 如果超过1分钟还没反应，可能是死锁，需要重启

### 问题4: 端口被占用

**错误**：`Address already in use`

**解决**：
```bash
# 方法1: 关闭占用端口的程序
netstat -ano | findstr :5000
taskkill /F /PID <进程ID>

# 方法2: 修改端口
# 编辑 web_app.py 最后一行
app.run(host='0.0.0.0', port=5001)  # 改为5001
```

## 📝 使用真实数据

### 使用你的实际报表

1. **准备报表文件**
   - 确保是 `.xlsx` 格式
   - 包含数字工作表（1, 2, 3, ..., 31）

2. **准备送货商文件**
   - 文件名任意
   - 工作表名称对应日期（如"9", "10", "11"）
   - 包含列：名称、单位、数量、单价、总价

3. **上传并填充**
   - 按照测试步骤操作
   - 检查下载的结果文件

## 🔍 调试技巧

### 查看后端日志
命令行窗口会显示详细的处理过程：
```
📊 加载报表文件: uploads\report_xxx.xlsx
✅ 成功加载报表
📂 读取送货商文件: supplier_xxx.xlsx
✅ 工作表 '9' -> 日期 '9': 3 条记录
🚀 开始填充：Q-U
✅ 填充完成！成功处理 2/2 个日期
```

### 查看前端日志
浏览器控制台（F12）会显示：
```
上传文件: report.xlsx
响应: {success: true, filepath: "..."}
处理数据...
进度: 20%
进度: 100%
```

### 测试单个功能

1. **测试报表加载**
   ```python
   from tests.数据填充工具 import DataFiller
   filler = DataFiller('temp/test_report.xlsx')
   filler.load_report()
   ```

2. **测试送货商文件读取**
   ```python
   filler = DataFiller.__new__(DataFiller)
   filler.wb = None
   filler.supplier_data = {}
   filler.read_supplier_file('temp/test_supplier.xlsx')
   print(filler.supplier_data)
   ```

## ✅ 成功的标志

当一切正常时，你会看到：

1. **后端日志**
   ```
   ✅ 成功加载报表
   ✅ 工作表 '9' -> 日期 '9': 3 条记录
   ✅ 填充完成！成功处理 2/2 个日期
   ✅ 报表已保存到: temp/金融岛报表_已填充_xxx.xlsx
   ```

2. **前端界面**
   - 绿色的"✅ 填充完成!"消息
   - "📥 下载填充后的报表"按钮可用

3. **下载的文件**
   - 可以在Excel中正常打开
   - 数据已正确填充到指定列
   - 格式和样式保持完整

## 🎯 下一步

1. **测试基本功能**
   - 使用测试文件验证流程
   - 确保每个步骤都能正常工作

2. **使用真实数据**
   - 上传你的实际报表
   - 上传送货商文件
   - 验证填充结果

3. **保存配置**
   - 记录常用的列范围
   - 创建书签保存Web应用地址

## 📞 获取帮助

如果遇到问题：
1. 查看浏览器控制台（F12）
2. 查看命令行窗口的日志
3. 检查 `temp/` 目录下的输出文件
4. 查看 `docs/WEB应用使用指南.md` 详细文档

---

**祝测试顺利！** 🎉
