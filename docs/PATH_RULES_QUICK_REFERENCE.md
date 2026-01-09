# 文件路径规则快速参考

## 🎯 一句话总结

**除了应用入口、核心配置、项目级文档外，所有新文件必须放入子目录！**

---

## 📋 快速查询表

### 文件类型 → 目标目录

| 文件类型/扩展名 | 目标位置 | 示例 |
|----------------|---------|------|
| `.py` (主入口) | 根目录 | `web_app.py` ✅ |
| `.py` (测试) | `tests/` | `tests/test_api.py` ✅ |
| `.py` (新功能) | `projects/` 或 `d:\projects\` | `projects/new_tool/main.py` ✅ |
| `.md` (文档) | `docs/` | `docs/GUIDE.md` ✅ |
| `.md` (项目级) | 根目录 | `README.md` ✅ |
| `.json` (配置) | 根目录 | `suppliers_config.json` ✅ |
| `.json` (数据) | `temp/` 或 `uploads/` | `temp/data.json` ✅ |
| `.png` (截图) | `test_screenshots/` | `test_screenshots/result.png` ✅ |
| `.bat`, `.sh` | 根目录（仅启动脚本） | `启动应用.bat` ✅ |

---

## ⚠️ 常见错误

| ❌ 错误做法 | ✅ 正确做法 |
|-----------|-----------|
| `test_debug.py` | `tests/test_debug.py` |
| `debug_log.md` | `docs/debug_log.md` |
| `temp_data.json` | `temp/temp_data.json` |
| `screenshot.png` | `test_screenshots/screenshot.png` |
| `new_feature.py` | `projects/new_feature/main.py` |
| `user_file.xlsx` | `uploads/user_file.xlsx` |

---

## 🔍 快速决策流程图

```
创建新文件前
    │
    ├─ 是主入口？（web_app.py, app.py）
    │   └─ ✅ 放根目录
    │
    ├─ 是核心配置？（suppliers_config.json, .env）
    │   └─ ✅ 放根目录
    │
    ├─ 是项目级文档？（README.md, CLAUDE.md）
    │   └─ ✅ 放根目录
    │
    ├─ 是文档？（.md 文件）
    │   └─ ✅ 放 docs/
    │
    ├─ 是测试/调试？（test_*.py, debug_*.py）
    │   └─ ✅ 放 tests/
    │
    ├─ 是临时文件？
    │   └─ ✅ 放 temp/
    │
    ├─ 是截图？
    │   └─ ✅ 放 test_screenshots/
    │
    ├─ 是用户上传？
    │   └─ ✅ 放 uploads/
    │
    └─ 是新功能/独立项目？
        └─ ✅ 放 projects/ 或 d:\projects\
```

---

## 📝 实施检查清单

每次生成新文件时：

- [ ] 确认文件类型
- [ ] 查阅上表确定目标目录
- [ ] 使用完整路径（包含目录前缀）
- [ ] 确认目录存在（如需要先创建）

**示例**：
```python
# ✅ 正确
Write(file_path='d:\\claude code -11\\docs\\NEW_GUIDE.md', content=...)

# ❌ 错误
Write(file_path='d:\\claude code -11\\NEW_GUIDE.md', content=...)
```

---

## 🎯 当前项目结构

```
d:\claude code -11\
├── web_app.py              # ✅ 主入口
├── suppliers_config.json   # ✅ 配置
├── CLAUDE.md               # ✅ 项目文档
├── README.md               # ✅ 项目文档
├── docs/                   # 📁 所有文档
├── tests/                  # 📁 所有测试
├── temp/                   # 📁 临时文件
├── uploads/                # 📁 用户文件
├── test_screenshots/       # 📁 截图
└── projects/               # 📁 子项目
```

---

**记住**：根目录保持简洁，只放核心文件！
