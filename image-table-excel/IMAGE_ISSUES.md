# 图片插入问题说明

## 问题描述

### 问题1：新图片没有插入
**现象**：使用技能插入数据后，图片没有被插入到Excel中

**原因**：
- ExcelTool 不支持图片插入功能
- 只能插入文本、数字等数据，无法插入图片对象

### 问题2：原有图片显示为公式
**现象**：Excel中原有图片显示为 `=DISPIMG("ID_XXX",1)` 公式

**原因**：
- 这是 **WPS Office 的专有图片函数**
- WPS 使用 `DISPIMG()` 函数来嵌入图片，而不是标准的图片对象
- openpyxl 库无法识别这种WPS专有格式

## 技术背景

### Excel vs WPS 的图片处理差异

| 特性 | 标准Excel | WPS Office |
|------|-----------|------------|
| 图片存储方式 | 图片对象（Image Object） | `DISPIMG()` 函数 |
| openpyxl支持 | ✅ 完全支持 | ❌ 不支持 |
| 跨平台兼容性 | ✅ 好 | ⚠️ 仅WPS可用 |
| 图片读取 | 可以读取图片对象 | 只能读取公式文本 |
| 图片插入 | `ws.add_image()` | 需要WPS API |

### DISPIMG 函数说明

```excel
=DISPIMG("ID_3E3B5123D28F41E4A5B7EC0AF5667E86", 1)
```

- **第一个参数**：图片ID（WPS内部存储的图片标识）
- **第二个参数**：显示模式（1=适应单元格）
- **特点**：
  - 图片数据存储在WPS的内部数据库中
  - 公式引用图片ID来显示图片
  - 在标准Excel中无法显示，只显示公式文本
  - 在 openpyxl 中读取时显示为公式对象

## 解决方案

### 方案1：使用 openpyxl 插入标准图片（推荐）

**前提条件**：
- 需要安装 openpyxl 库
- 需要使用 `map_and_insert.py` 脚本（不能用ExcelTool）

**步骤**：

1. 安装依赖
```bash
pip install openpyxl pillow
```

2. 使用脚本插入
```bash
python scripts/map_and_insert.py \
  --excel "文件路径.xlsx" \
  --data data.json \
  --image image.jpg \
  --custom '{"生产厂家":"约克"}'
```

3. 脚本会自动：
   - 在第一行新数据的M列插入图片
   - 使用标准的图片对象（非DISPIMG函数）
   - 设置行高以适应图片

**代码示例**：
```python
from openpyxl import load_workbook
from openpyxl.drawing.image import Image as XLImage

# 打开Excel
wb = load_workbook('文件.xlsx')
ws = wb.active

# 插入图片
img = XLImage('image.jpg')
img.anchor = 'M346'  # 锚定到M346单元格
ws.add_image(img)

# 设置行高
ws.row_dimensions[346].height = 200

# 保存
wb.save('文件.xlsx')
```

### 方案2：手动插入图片

**适用场景**：
- 无法安装 openpyxl
- 文件由WPS创建，需要保持WPS格式

**步骤**：

1. 打开Excel文件（使用Excel或WPS）
2. 定位到第一行新数据的M列（如M346）
3. 插入 → 图片 → 选择图片文件
4. 调整图片大小和位置
5. 保存文件

### 方案3：转换文件格式

**目的**：将WPS格式转换为标准Excel格式

**步骤**：

1. 在WPS中打开文件
2. 另存为 → 选择"Excel 工作簿 (*.xlsx)"
3. 勾选"兼容模式"或"标准格式"
4. 保存后，DISPIMG函数会被转换为标准图片对象
5. 然后可以使用 openpyxl 处理

**注意**：转换后原有的DISPIMG图片可能丢失，需要重新插入

## 当前技能的限制

### ExcelTool 的限制
- ❌ 不支持图片插入
- ❌ 不支持图片读取
- ❌ 不支持DISPIMG函数
- ✅ 仅支持数据读写

### openpyxl 的限制
- ❌ 不支持DISPIMG函数（WPS专有）
- ❌ 无法读取DISPIMG引用的图片
- ✅ 支持标准Excel图片对象
- ✅ 可以插入新图片

### WPS 兼容性问题
- ⚠️ WPS创建的文件可能包含专有格式
- ⚠️ openpyxl插入的图片在WPS中可能显示异常
- ⚠️ 建议使用标准Excel或转换格式

## 最佳实践

### 1. 文件格式建议
- ✅ 使用标准Excel创建模板文件
- ✅ 避免使用WPS专有功能
- ✅ 如果必须用WPS，转换为标准格式后再处理

### 2. 图片插入建议
- ✅ 优先使用 `map_and_insert.py` 脚本
- ✅ 确保安装了 openpyxl 和 pillow 库
- ✅ 使用标准图片格式（JPG、PNG）

### 3. 工作流程建议
```
1. 检查Excel文件格式
   - 是否由WPS创建？
   - 是否包含DISPIMG函数？

2. 如果是WPS文件
   - 转换为标准Excel格式
   - 或者接受手动插入图片

3. 使用技能插入数据
   - 使用 map_and_insert.py 脚本
   - 自动插入图片

4. 验证结果
   - 检查图片是否正确显示
   - 检查数据是否正确
```

## 临时解决方案（本次测试）

由于当前环境限制（无法安装openpyxl），图片插入需要手动完成：

### 手动插入步骤
1. 打开文件：`C:\Users\15085\Desktop\中原证券\中原证券进场材料统计表2026.xlsx`
2. 定位到：第346行M列（M346单元格）
3. 插入图片：`D:\jieyue\test_image.jpg`
4. 调整图片：
   - 右键图片 → 大小和属性
   - 设置高度：约5厘米
   - 保持宽高比
5. 保存文件

### 图片位置说明
- **第346行**：第一行新数据（YTM25M-1424-S-R）
- **M列**：第13列（备注列之后）
- **建议**：图片可以跨越多行显示

## 后续改进计划

### 短期改进
1. [ ] 在技能文档中明确说明图片插入限制
2. [ ] 提供手动插入图片的详细步骤
3. [ ] 添加WPS兼容性警告

### 中期改进
1. [ ] 创建独立的图片插入工具
2. [ ] 支持批量图片插入
3. [ ] 自动检测文件格式（Excel vs WPS）

### 长期改进
1. [ ] 研究WPS API，支持DISPIMG函数
2. [ ] 开发WPS插件，直接在WPS中使用
3. [ ] 提供图片压缩和优化功能

## 常见问题

### Q1: 为什么原有图片显示为公式？
**A**: 因为文件由WPS创建，使用了WPS专有的DISPIMG函数。openpyxl无法识别这种格式，只能读取为公式文本。

### Q2: 如何让openpyxl识别WPS图片？
**A**: 无法直接识别。需要将文件转换为标准Excel格式，或者在WPS中重新插入为标准图片对象。

### Q3: 新插入的图片为什么没有显示？
**A**: 因为使用了ExcelTool，它不支持图片插入。需要使用openpyxl库的map_and_insert.py脚本，或者手动插入。

### Q4: 如何判断文件是Excel还是WPS创建的？
**A**: 
- 查看图片单元格：如果显示`=DISPIMG(...)`，则是WPS创建
- 查看文件属性：WPS文件可能包含"Kingsoft"标识
- 尝试用openpyxl读取：如果图片显示为公式，则是WPS格式

### Q5: 能否同时支持Excel和WPS？
**A**: 很难。两者的图片存储机制完全不同。建议统一使用标准Excel格式。

## 参考资料

- [openpyxl 官方文档 - 图片处理](https://openpyxl.readthedocs.io/en/stable/charts/introduction.html)
- [Excel vs WPS 兼容性问题](https://support.microsoft.com/zh-cn/office)
- [WPS DISPIMG 函数说明](https://www.wps.cn/learning/)

---

**总结**：
- DISPIMG是WPS专有格式，openpyxl无法支持
- 建议使用标准Excel格式
- 图片插入需要使用openpyxl库，不能用ExcelTool
- 如果无法安装openpyxl，需要手动插入图片
