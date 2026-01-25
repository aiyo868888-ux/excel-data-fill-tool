# 安装指南

## 环境要求

- Python 3.8 或更高版本
- pip（Python 包管理器）

## 安装步骤

### 方法 1: 从源码安装（推荐）

```bash
# 1. 进入项目目录
cd project/smart-todo-parser/python

# 2. 创建虚拟环境（推荐）
python -m venv venv

# 3. 激活虚拟环境
# Windows:
venv\Scripts\activate
# Linux/Mac:
source venv/bin/activate

# 4. 安装依赖
pip install -e .

# 5. 验证安装
python -c "from smart_todo_parser import TodoParser; print('✓ 安装成功')"
```

### 方法 2: 仅安装依赖

```bash
cd project/smart-todo-parser/python
pip install -r requirements.txt
```

## 运行示例

```bash
# 运行快速开始示例
python example.py
```

## 运行测试

```bash
# 运行所有测试
pytest tests/ -v

# 运行测试并生成覆盖率报告
pytest tests/ --cov=smart_todo_parser --cov-report=html

# 查看覆盖率报告
# 打开 htmlcov/index.html
```

## 依赖项

### 核心依赖
- `python-dateutil` - 日期解析
- `regex` - 高级正则表达式
- `pytz` - 时区处理

### 开发依赖
- `pytest` - 测试框架
- `pytest-cov` - 覆盖率报告

## 常见问题

### Q: 安装失败，提示找不到模块

**A:** 确保已激活虚拟环境，并使用正确的 pip：

```bash
# Windows
venv\Scripts\pip install -e .

# Linux/Mac
venv/bin/pip install -e .
```

### Q: 运行示例时出错

**A:** 检查 Python 版本：

```bash
python --version  # 应该 >= 3.8
```

### Q: 测试失败

**A:** 确保安装了测试依赖：

```bash
pip install pytest pytest-cov
```

## 下一步

1. 阅读 [README.md](README.md) 了解项目概述
2. 查看 [docs/EXAMPLES.md](docs/EXAMPLES.md) 学习用法
3. 参考 [docs/API.md](docs/API.md) 了解 API

## 卸载

```bash
pip uninstall smart-todo-parser
```

## 更新

```bash
cd project/smart-todo-parser/python
git pull  # 如果使用 Git
pip install -e . --force-reinstall
```
