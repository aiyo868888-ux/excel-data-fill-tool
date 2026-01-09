# 项目创建快速参考

## 🎯 新项目创建流程

### 步骤 1：确定位置
```
d:\projects\
├── web-projects\     # Web 应用
├── tools\            # 工具脚本
├── learning\         # 学习练习
└── experiments\      # 实验项目
```

### 步骤 2：初始化目录
```bash
mkdir d:\projects\[类别]\[项目名称]
cd d:\projects\[类别]\[项目名称]
mkdir docs tests test_screenshots temp uploads
```

### 步骤 3：创建项目文件
- `CLAUDE.md` - 项目约定
- `README.md` - 项目说明
- `.gitignore` - Git 忽略规则

---

## 📁 文件存放规则

| 文件类型 | 存放位置 | 示例 |
|---------|---------|------|
| 文档 (.md) | `docs/` | `docs/API.md` |
| 测试脚本 (test_*.py) | `tests/` | `tests/test_api.py` |
| 截图 | `test_screenshots/` | `test_screenshots/result.png` |
| 临时文件 | `temp/` | `temp/cache.json` |
| 配置文件 | 根目录 | `config.json` |
| 应用入口 | 根目录 | `app.py`, `main.py` |

---

## ⚠️ 严格禁止

- ❌ 在现有项目根目录混放新项目文件
- ❌ 不创建目录结构就开始开发
- ❌ 测试文件、文档散落在根目录

---

## ✅ 必须执行

- ✅ 新项目放在独立目录
- ✅ 先创建标准目录结构
- ✅ 项目级 CLAUDE.md
- ✅ 遵循文件组织约定
