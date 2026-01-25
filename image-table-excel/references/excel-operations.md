# Excel操作指南

## openpyxl基础

### 打开工作簿

```python
from openpyxl import load_workbook

# 打开现有文件
wb = load_workbook('data.xlsx')

# 获取活动工作表
ws = wb.active

# 获取指定工作表
ws = wb['Sheet1']
```

### 读取数据

```python
# 读取单个单元格
value = ws['A1'].value
value = ws.cell(row=1, column=1).value

# 读取范围
for row in ws.iter_rows(min_row=1, max_row=10, values_only=True):
    print(row)
```

### 写入数据

```python
# 写入单个单元格
ws['A1'] = 'Hello'
ws.cell(row=1, column=1, value='World')

# 写入多行
data = [['Name', 'Age'], ['Alice', 25], ['Bob', 30]]
for row_idx, row_data in enumerate(data, start=1):
    for col_idx, value in enumerate(row_data, start=1):
        ws.cell(row=row_idx, column=col_idx, value=value)
```

### 插入行和列

```python
# 在第5行插入1行
ws.insert_rows(5)

# 在第5行插入3行
ws.insert_rows(5, 3)

# 在第C列插入1列
ws.insert_cols(3)
```

## 图片操作

### 插入图片到单元格

```python
from openpyxl.drawing.image import Image as XLImage
from PIL import Image as PILImage

# 调整图片大小
img = PILImage.open('image.png')
img_resized = img.copy()

# 计算合适的尺寸
target_width = 100  # 像素
target_height = 100
img_resized.thumbnail((target_width, target_height))

# 保存临时文件
img_resized.save('temp_image.png')

# 插入到Excel
excel_img = XLImage('temp_image.png')
excel_img.anchor = 'A1'  # 或者使用坐标
ws.add_image(excel_img)
```

### 图片嵌入到单元格内部

将图片缩放到单元格大小：

```python
def get_cell_size(ws, row, column):
    """获取单元格的像素尺寸"""
    from openpyxl.utils import get_column_letter

    # 列宽（单位: 字符宽度）
    col_letter = get_column_letter(column)
    col_width = ws.column_dimensions[col_letter].width or 10

    # 行高（单位: 磅）
    row_height = ws.row_dimensions[row].height or 15

    # 转换为像素（近似值）
    col_width_px = col_width * 7
    row_height_px = row_height * 1.5

    return col_width_px, row_height_px

def insert_image_to_cell(ws, image_path, row, column):
    """将图片插入到单元格内"""
    from openpyxl.utils import column_index_from_string, get_column_letter

    # 获取单元格尺寸
    if isinstance(column, str):
        col_idx = column_index_from_string(column)
        col_letter = column
    else:
        col_idx = column
        col_letter = get_column_letter(column)

    col_width, row_height = get_cell_size(ws, row, col_idx)

    # 调整图片大小
    img = PILImage.open(image_path)
    img_resized = img.copy()
    img_resized.thumbnail((col_width, row_height), PILImage.Resampling.LANCZOS)

    # 保存临时文件
    temp_path = f"temp_cell_{col_letter}{row}.png"
    img_resized.save(temp_path)

    # 插入图片
    excel_img = XLImage(temp_path)
    excel_img.anchor = f"{col_letter}{row}"
    ws.add_image(excel_img)

    return temp_path
```

## 样式操作

### 设置列宽和行高

```python
# 设置列宽
ws.column_dimensions['A'].width = 20

# 设置行高
ws.row_dimensions[1].height = 30

# 自动调整列宽
for column in ws.columns:
    max_length = 0
    column_letter = get_column_letter(column[0].column)
    for cell in column:
        try:
            if len(str(cell.value)) > max_length:
                max_length = len(str(cell.value))
        except:
            pass
    adjusted_width = min(max_length + 2, 50)
    ws.column_dimensions[column_letter].width = adjusted_width
```

### 保存文件

```python
wb.save('output.xlsx')
```

## 完整示例

```python
from openpyxl import load_workbook
from openpyxl.drawing.image import Image as XLImage
from openpyxl.utils import get_column_letter

def update_excel_with_image(excel_path, data, image_path, insert_row=1):
    """更新Excel并插入图片"""

    # 打开工作簿
    wb = load_workbook(excel_path)
    ws = wb.active

    # 插入空行
    ws.insert_rows(insert_row)

    # 写入数据
    for col_idx, value in enumerate(data, start=1):
        ws.cell(row=insert_row, column=col_idx, value=value)

    # 插入图片
    img = XLImage(image_path)
    img.width = 100
    img.height = 100
    img.anchor = f"{get_column_letter(len(data) + 1)}{insert_row}"
    ws.add_image(img)

    # 保存
    wb.save(excel_path)
```

## 常见问题

### Excel文件被锁定

确保文件未被其他程序打开：

```python
import os
import time

def safe_save(wb, path, max_retries=3):
    """安全保存文件"""
    for i in range(max_retries):
        try:
            wb.save(path)
            return True
        except PermissionError:
            if i < max_retries - 1:
                time.sleep(1)
            else:
                raise
```

### 图片不显示

检查图片路径和格式：

```python
# 支持的格式: PNG, JPEG, BMP
# 图片大小建议: < 5MB
```

### 公式引用错误

插入行后公式会自动调整，但固定引用（如$A$1）不会改变。
