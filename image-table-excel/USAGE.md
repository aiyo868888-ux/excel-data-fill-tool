# 正确使用流程示例

## 完整流程（必须严格遵守）

### 步骤1：提取图片表格内容
```bash
# 使用OCR或图像识别工具提取表格
python scripts/extract_table_from_image.py image.jpg --output data.json
```

### 步骤2：查找最后数据行（关键！）
```bash
# 使用专用工具扫描整个工作表
python scripts/find_last_row.py "C:\path\to\excel.xlsx" 5
```

**输出示例**：
```
开始扫描工作表...
  工作表名称: Sheet1
  max_row: 360
  扫描范围: 第5行 - 第360行

  第6行: 2026.1.4 | 明杆闸阀 | Z41W-16P
  第7行: 2026.1.4 | 明杆闸阀 | Z41W-16P
  ...
  第49行: 2026.1.4 | 卡条 | -

============================================================
扫描完成！
  共找到 44 行有数据
  最后数据行: 第49行
  建议插入位置: 第51行（第50行为空行分隔）
============================================================

最后一行内容:
  列1: 2026.1.4
  列2: 卡条
  列3: -
  列4: -
  列5: 个
  列6: 500
```

### 步骤3：向用户确认
**必须向用户展示并等待确认**：

```
检测到最后数据行为第 49 行
内容: 2026.1.4 | 卡条 | - | - | 个 | 500

新数据将插入到第 51 行（第50行为空行分隔）
历史内容（第49行及之前）完全不受影响

请确认：
1. 最后数据行是否正确？
2. 插入位置是否合适？

输入 'y' 确认，或告知正确的最后行号：
```

### 步骤4：执行插入
```bash
# 使用 map_and_insert.py 脚本（包含完整安全检查）
python scripts/map_and_insert.py \
  --excel "C:\path\to\excel.xlsx" \
  --data data.json \
  --image image.jpg \
  --custom '{"生产厂家":"约克","用于工程部位":"汪洋"}'
```

**脚本会自动**：
1. 再次调用 `find_last_data_row()` 确认位置
2. 在最后数据行+2的位置开始写入
3. 使用追加模式，不覆盖任何现有数据
4. 在第一行插入原图

### 步骤5：验证结果
```bash
# 再次扫描，验证数据是否正确插入
python scripts/find_last_row.py "C:\path\to\excel_已填充.xlsx" 5
```

**期望输出**：
```
最后数据行: 第66行（原49行 + 16行新数据 + 1行空行）
```

## 错误示例（禁止这样做）

### ❌ 错误1：直接使用 ExcelTool
```python
# 错误：没有先查找最后数据行
ExcelTool.write_data(
    path="excel.xlsx",
    sheet_name="Sheet1",
    start_cell="A30",  # 硬编码位置，可能覆盖数据！
    data=new_data
)
```

**问题**：
- 没有扫描工作表
- 硬编码插入位置
- 直接覆盖现有数据

### ❌ 错误2：只扫描部分行
```python
# 错误：只扫描到第30行
for row in range(5, 30):  # 应该是 ws.max_row + 1
    if has_data(row):
        last_row = row
```

**问题**：
- 第30行之后的数据会被遗漏
- 导致插入位置错误

### ❌ 错误3：没有用户确认
```python
# 错误：直接插入，不等待用户确认
last_row = find_last_row()
insert_data(last_row + 2)
```

**问题**：
- 用户无法验证最后行是否正确
- 一旦出错，数据已被覆盖

## 正确示例（推荐）

### ✅ 正确1：使用完整流程
```python
from scripts.find_last_row import find_last_data_row_safe
from scripts.map_and_insert import process_and_insert

# 1. 查找最后数据行
result = find_last_data_row_safe(excel_path, start_row=5)
last_row = result['last_row']
insert_row = result['insert_row']

# 2. 向用户展示并等待确认
print(f"最后数据行: 第{last_row}行")
print(f"内容: {result['last_row_content']}")
print(f"新数据将插入到: 第{insert_row}行")
confirm = input("确认？(y/n): ")

if confirm.lower() != 'y':
    print("操作已取消")
    exit()

# 3. 执行插入（使用追加模式）
process_and_insert(
    excel_path=excel_path,
    image_paths=[image_path],
    data_file=data_file,
    user_custom={"生产厂家": "约克", "用于工程部位": "汪洋"}
)
```

### ✅ 正确2：增加调试输出
```python
def find_last_data_row(ws, start_row=5):
    last_data_row = start_row - 1
    max_row = ws.max_row
    
    print(f"[调试] 扫描范围: {start_row} - {max_row}")
    
    for row in range(start_row, max_row + 1):
        if has_data(row):
            last_data_row = row
            print(f"[调试] 第{row}行有数据")
    
    print(f"[调试] 最终确定: 第{last_data_row}行")
    return last_data_row
```

### ✅ 正确3：验证历史数据
```python
# 插入前记录历史数据
before_data = read_data(excel_path, row=28)

# 执行插入
insert_data(...)

# 插入后验证历史数据是否完好
after_data = read_data(excel_path, row=28)

if before_data != after_data:
    print("⚠️ 警告：历史数据被修改！")
    # 回滚操作
```

## 快速检查清单

在执行插入前，确认以下所有项：

- [ ] 使用了 `find_last_row.py` 工具扫描工作表
- [ ] 扫描范围包含了 `ws.max_row`
- [ ] 向用户展示了最后数据行的内容
- [ ] 等待用户确认了插入位置
- [ ] 使用了 `map_and_insert.py` 脚本（不是 ExcelTool）
- [ ] 插入位置 = 最后数据行 + 2
- [ ] 使用了追加模式（不是覆盖模式）
- [ ] 插入后验证了历史数据是否完好

**只有所有项都打勾，才能执行插入操作！**
