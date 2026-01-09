# 项目状态记录

> **重要**：每次对话开始时，Claude 必须先读取这个文件！

## 📋 项目概述

**项目名称**：Excel数据填充工具（便携版）
**项目目标**：创建一个无需安装Python的便携版工具，用户双击启动即可使用
**当前状态**：✅ 基本完成，正在完善便携版

---

## ✅ 已确认的重要决策

### 1. 用户需求
- **核心需求**：便携版（Portable），不需要在目标电脑上安装Python
- **原因**：目标电脑可能没有管理员权限，无法安装软件
- **交付形式**：一个文件夹，复制到任何Windows电脑都能运行

### 2. 数据填充逻辑（已确定，不要修改）
**文件**：`数据填充工具.py`

- **数据粘贴起始行**：第1行（line 1153: `start_row = 1`）
- **表头行**：第2行需要填入"金额"文字
- **乘法公式**：第3行到倒数第4行（line 1193-1226）
- **SUM公式位置**：倒数第3行（line 1249）
- **SUM公式范围**：第3行到倒数第4行求和（line 1256-1261）

⚠️ **警告**：这些逻辑已经测试通过，不要再修改！

### 3. 便携版技术方案（已确定）
- **Python版本**：Python 3.12.10 嵌入式版（python-3.12.10-embed-amd64.zip）
- **位置**：`portable/python/` 文件夹
- **依赖包**：已从系统Python复制到 `portable/python/Lib/site-packages/`
- **路径配置**：`portable/python/python312._pth` 包含：
  ```
  python312.zip
  Lib/site-packages
  .
  ```

### 4. 文件组织（已确定）
**发布版本文件夹**：`填充工具-发布版本/`

```
填充工具-发布版本/
├── portable/                    # 便携版（用户需要的最终产品）
│   ├── python/                  # 嵌入式Python（156 MB）
│   ├── 启动便携版.bat           # 双击启动
│   ├── web_app.py
│   ├── 数据填充工具.py
│   ├── suppliers_config.json
│   ├── templates/
│   ├── uploads/
│   ├── temp/
│   └── 测试依赖.py
├── python-3.12.10-embed-amd64.zip  # 嵌入式Python源文件（11 MB）
├── web_app.py                   # 标准版程序
├── 数据填充工具.py
├── suppliers_config.json
├── requirements.txt
├── templates/
├── uploads/
├── temp/
├── 启动工具.bat                 # 标准版启动脚本
├── 安装依赖.bat                 # 标准版依赖安装
└── README.txt                   # 用户文档
```

---

## 🔧 技术细节记录

### Python 嵌入式版问题记录
**问题**：`ModuleNotFoundError: No module named 'select'`

**原因**：
- Python 3.12 嵌入版缺少标准库模块
- 系统 Python 是 3.11 版本，与便携版 3.12 不兼容

**已尝试的解决方案**：
1. ✅ 从 `C:\Program Files\Python311\DLLs\select.pyd` 复制到便携版
2. ✅ 从 `C:\Program Files\Python311\Lib\selectors.py` 复制到便携版 Lib/
3. ✅ 修改 `python312._pth` 添加 `.` 行（当前目录）

**当前状态**：
- select.pyd 已复制到 `portable/python/`
- selectors.py 已复制到 `portable/python/Lib/`
- python312._pth 已修改
- **仍有错误**：`ImportError: DLL load failed while importing select`（版本不兼容）

**待解决**：
- 需要从 Python 3.12 安装中获取正确的 select.pyd
- 或者使用 Python 3.11 嵌入版重新制作便携版

### 依赖包清单
**文件**：`requirements.txt`

```
flask==3.0.0
openpyxl==3.1.2
pandas==2.1.4
msoffcrypto-tool==5.0.0
pywin32==311
werkzeug==3.0.1
```

**已安装的依赖**（portable/python/Lib/site-packages/）：
- blinker, certifi, charset_normalizer, click
- et_xmlfile, flask, idna, itsdangerous
- jinja2, markupsafe, numpy, openpyxl
- pandas, pip, pytz, requests, urllib3, werkzeug
- 以及其他依赖

---

## 🎯 下一步工作

### 高优先级
1. **解决便携版 select 模块问题**
   - 方案A：下载 Python 3.11 嵌入版，重新制作
   - 方案B：安装 Python 3.12 到系统，复制 select.pyd

2. **测试便携版启动**
   - 确认所有依赖正常导入
   - 测试 web_app.py 能否运行
   - 验证 Excel 填充功能

### 中优先级
3. **完善用户文档**
   - 更新 README.txt
   - 添加便携版使用说明
   - 添加常见问题解答

4. **清理文件**
   - 删除不必要的临时文件
   - 确保文件夹整洁

---

## 🚫 不要再做的事情

### 避免重复讨论
- ❌ 数据填充逻辑已经确定，不要再问"从哪一行开始"
- ❌ 用户明确要便携版，不要再推荐"标准安装"
- ❌ 文件组织已确定，不要再创建新的文档文件

### 避免重复测试
- ❌ 不要再测试标准版的 pip install
- ❌ 不要再创建新的启动脚本变种

---

## 📞 用户明确表达过的要求

1. **"我要的就是便携版啊！"**
   - 解释：用户要的是无需安装Python的版本
   - 行动：专注解决便携版技术问题

2. **"哎，你每次一个文件，弄晕了"**
   - 解释：文件太多会让用户困惑
   - 行动：保持文件简洁，不要创建过多文档

3. **"你看是不是这个文件夹里的内容，是否已经齐备了"**
   - 解释：用户希望确认便携版是否完整
   - 行动：检查并确保便携版可以运行

4. **"你不能直接把这些文件复制进去吗"**
   - 解释：用户希望直接解决问题，不要绕弯子
   - 行动：直接复制文件，不要用复杂脚本

---

## 🔍 调试技巧

### 检查便携版Python
```bash
cd "d:\claude code -11\填充工具-发布版本\portable"
python/python.exe -c "import flask, pandas, openpyxl; print('OK')"
```

### 检查依赖包
```bash
ls portable/python/Lib/site-packages
```

### 测试Flask应用
```bash
cd portable
python/python.exe web_app.py
```

---

## 📅 更新日志

- **2026-01-07 14:00**：创建此文件，记录项目状态
- **2026-01-07 14:30**：确认便携版为唯一目标
- **2026-01-07 15:00**：记录 select 模块问题
- **2026-01-07 15:30**：⭐ 重要改进：创建 `.claude/local.json` 实现项目级系统提示
  - 避免影响其他项目
  - 实现配置隔离
  - 更新文档：`docs/config-files-guide/01-配置文件对比与易错点.md`
- **2026-01-07 16:00**：⭐ 创建完整的配置文件文档体系
  - 新增：`docs/config-files-guide/02-systemPrompt使用指南.md`
  - 新增：`docs/config-files-guide/03-local-json使用指南.md`
  - 更新：`docs/config-files-guide/01-配置文件对比与易错点.md`
    - 添加 systemPrompt vs local.json 对比章节
    - 添加 2 个新错误案例
  - 新增：`docs/config-files-guide/README.md`（目录索引）

---

## 💡 给 Claude 的提示

每次对话开始时：
1. **先读取这个文件**（PROJECT_STATUS.md）
2. **了解用户真正需求**：便携版
3. **不要重复讨论已确定的事情**
4. **专注解决当前技术问题**
5. **保持简洁，不要创建过多文件**

**记住**：用户要的是**能用的便携版**，不是更多的文档和讨论！
