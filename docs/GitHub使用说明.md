# 从 GitHub 下载和使用项目

## 方法一：使用 Git 克隆（推荐）

### 1. 安装 Git
如果还没有安装 Git，请先下载安装：
- Windows: https://git-scm.com/download/win
- 下载后一路"下一步"安装即可

### 2. 克隆项目
打开命令行（CMD 或 PowerShell），执行：

```bash
# 切换到您想存放项目的目录
cd d:\projects

# 克隆项目
git clone https://github.com/aiyo868888-ux/excel-data-fill-tool.git

# 进入项目目录
cd excel-data-fill-tool
```

### 3. 安装依赖
```bash
pip install flask pandas openpyxl msoffcrypto-tool
```

### 4. 启动应用
```bash
python web_app.py
```

然后访问：http://localhost:5000

---

## 方法二：直接下载 ZIP 文件

### 1. 下载项目
1. 访问：https://github.com/aiyo868888-ux/excel-data-fill-tool
2. 点击绿色的 "Code" 按钮
3. 选择 "Download ZIP"
4. 解压到任意目录（例如：`d:\projects\excel-data-fill-tool`）

### 2. 安装依赖
打开命令行，进入项目目录：
```bash
cd d:\projects\excel-data-fill-tool
pip install flask pandas openpyxl msoffcrypto-tool
```

### 3. 启动应用
```bash
python web_app.py
```

然后访问：http://localhost:5000

---

## 如果代码出错或需要回退

### 查看所有提交历史
```bash
cd excel-data-fill-tool
git log --oneline
```

### 回退到之前的版本
```bash
# 查看提交历史
git log --oneline -10

# 回退到指定版本（例如：ffa499d）
git reset --hard ffa499d
```

### 从 GitHub 重新拉取最新代码
```bash
# 丢弃本地所有修改，使用 GitHub 上的版本
git reset --hard origin/master

# 或者强制拉取
git fetch --all
git reset --hard origin/master
```

### 恢复单个文件
```bash
# 恢复某个文件到指定版本
git checkout 992cbe9 -- tests/数据填充工具.py
```

---

## 项目结构

```
excel-data-fill-tool/
├── web_app.py              # Web 应用主程序
├── suppliers_config.json   # 供应商配置文件
├── templates/              # HTML 模板
│   ├── index.html
│   ├── data_filler.html
│   └── config.html
├── tests/
│   └── 数据填充工具.py    # 核心数据填充类
├── uploads/                # 上传文件存放目录
└── temp/                   # 临时文件目录
```

---

## 常见问题

### Q1: 提示 "ModuleNotFoundError"
**A**: 需要安装依赖：
```bash
pip install flask pandas openpyxl msoffcrypto-tool
```

### Q2: 端口 5000 被占用
**A**: 修改 `web_app.py` 最后一行：
```python
app.run(host='0.0.0.0', port=5001, debug=True)  # 改为 5001
```

### Q3: 找不到 `数据填充工具.py`
**A**: 确保文件存在，如果丢失可以从 GitHub 恢复：
```bash
git checkout 992cbe9 -- tests/数据填充工具.py
```

---

## 更新项目

如果 GitHub 上有新的提交：
```bash
cd excel-data-fill-tool
git pull origin master
```

## 提交新的修改

```bash
# 查看修改了哪些文件
git status

# 添加所有修改
git add .

# 提交
git commit -m "描述您的修改"

# 推送到 GitHub
git push origin master
```

---

## 快速开始（完整流程）

```bash
# 1. 克隆项目
git clone https://github.com/aiyo868888-ux/excel-data-fill-tool.git
cd excel-data-fill-tool

# 2. 安装依赖
pip install flask pandas openpyxl msoffcrypto-tool

# 3. 启动应用
python web_app.py

# 4. 打开浏览器
# 访问 http://localhost:5000
```

---

**GitHub 仓库地址**：https://github.com/aiyo868888-ux/excel-data-fill-tool
