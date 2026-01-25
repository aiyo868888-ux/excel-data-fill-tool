# PyInstaller 打包最佳实践指南

## NumPy/Pandas 兼容性问题

### 问题描述

NumPy 2.x 与 PyInstaller 存在已知兼容性问题，导致打包后的 EXE 报错：

```
Error importing numpy: you should not try to import numpy from its source directory
```

### 解决方案

#### 方案 1：降级 NumPy 到 1.x（推荐，最简单）

```bash
pip uninstall numpy -y
pip install "numpy<2.0"  # 使用 1.26.4 等稳定版本
```

#### 方案 2：在代码中修复 sys.path

在 `web_app.py` 最顶部、任何导入之前添加：

```python
import sys
import os

if hasattr(sys, '_MEIPASS'):
    sys.path = [sys._MEIPASS]  # PyInstaller环境
else:
    if '' in sys.path:
        sys.path.remove('')
    if '.' in sys.path:
        sys.path.remove('.')
```

在入口脚本 `数据填充工具_exe.py` 中同样处理。

#### 方案 3：spec 文件配置

`数据填充工具.spec`：

```python
hiddenimports=[
    'numpy', 'numpy._core',
    'pandas', 'pandas._libs',
    'openpyxl', 'flask', 'jinja2',
    # ... 其他依赖
],
excludes=[
    'tkinter', 'matplotlib',
    'numpy.tests', 'numpy.distutils',
    'scipy',
],
```

---

## 打包流程

### 1. 清理环境

```bash
pip install "numpy<2.0"  # 确保使用 NumPy 1.x
```

### 2. 构建 EXE

```bash
cd project/数据填充
python -m PyInstaller 数据填充工具.spec --clean
```

### 3. 测试 EXE

```bash
dist/数据填充工具.exe
# 访问 http://127.0.0.1:8888 验证功能
```

---

## 关键文件

| 文件 | 作用 |
|------|------|
| `数据填充工具.spec` | PyInstaller 配置文件 |
| `数据填充工具_exe.py` | EXE 入口脚本 |
| `web_app.py` | Flask 应用（已包含 sys.path 修复） |
| `rthook_pyi_rth_numpy.py` | NumPy runtime hook（备用） |

---

## 常见问题

### Q：为什么不使用 runtime_hooks？

**A**：runtime_hooks 在 PyInstaller 6.x 中存在导入顺序问题，直接在代码中修复 sys.path 更可靠。

### Q：能否使用 NumPy 2.x？

**A**：目前不推荐。NumPy 2.x 与 PyInstaller 的兼容性问题仍在修复中（GitHub Issue #8747）。

### Q：EXE 文件太大（150MB）？

**A**：这是正常的，因为包含了 pandas、numpy、openpyxl 等大型库。可使用 UPX 压缩（已在 spec 中启用）。

---

## PyInstaller 缓存问题导致旧代码被打包

### 问题现象

EXE 显示 "Unexpected token '<', "<!doctype "... is not valid JSON" 错误，但 Python 版本正常。

### 根本原因

1. PyInstaller 缓存了旧版本的源代码
2. 缓存位置：
   - Linux/Mac: `~/.local/share/PyInstaller`
   - Windows: `%APPDATA%\PyInstaller`
3. 即使源文件已修改，PyInstaller 可能使用缓存的旧版本
4. 当 spec 文件中将源文件作为 data 文件包含时（如 `'数据填充工具.py', '.'`），更容易出现此问题

### 解决方案

```bash
# 1. 清理 PyInstaller 缓存
# Linux/Mac:
rm -rf ~/.local/share/PyInstaller

# Windows:
rmdir /s /q %APPDATA%\PyInstaller

# 2. 清理构建目录
rm -rf build dist

# 3. 重新打包
python -m PyInstaller 数据填充工具.spec --clean
```

### 预防措施

- 每次修改源代码后，打包前必须清理缓存
- 使用 `--clean` 标志强制重新构建
- 验证打包时间戳确保是最新版本
- 考虑使用版本号管理 EXE 构建版本

### 诊断方法

1. 对比 Python 版本和 EXE 版本的行为差异
2. 检查源文件修改时间和 EXE 构建时间
3. 如果 Python 版本正常但 EXE 异常，99% 是缓存问题
4. 查看错误信息中的时间戳判断是否使用了旧代码

---

## 参考资源

- [PyInstaller GitHub Issue #8747](https://github.com/pyinstaller/pyinstaller/issues/8747)
- [PyInstaller打包Flask项目完整解决方案](https://comate.baidu.com/zh/page/l1qvokldtwh)
- [StackOverflow: PyInstaller and Pandas](https://stackoverflow.com/questions/29109324/pyinstaller-and-pandas)
