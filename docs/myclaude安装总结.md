# MyClaude 安装总结

## ✅ 已安装组件

### 🎯 Skills (7个)

| 技能 | 说明 | 来源 |
|------|------|------|
| `codeagent` | Codex/Claude/Gemini 多后端执行 | myclaude |
| `product-requirements` | 产品需求分析和管理 | myclaude |
| `prototype-prompt-generator` | 原型设计提示词生成 | myclaude |
| `design-master` | 设计大师人格 | 飞书文档 |
| `json-canvas` | JSON Canvas 画布文件 | obsidian-skills |
| `obsidian-bases` | Obsidian 数据库视图 | obsidian-skills |
| `obsidian-markdown` | Obsidian Markdown 增强 | obsidian-skills |

### ⚡ Commands (12个)

| 命令 | 功能 | 用途 |
|------|------|------|
| `/code` | 实现功能 | 快速开发 |
| `/debug` | 调试问题 | 排查错误 |
| `/test` | 编写测试 | 测试覆盖 |
| `/review` | 代码审查 | 质量保证 |
| `/optimize` | 性能优化 | 提升性能 |
| `/refactor` | 代码重构 | 改善结构 |
| `/docs` | 文档编写 | 生成文档 |
| `/ask` | 问答咨询 | 获取建议 |
| `/bugfix` | 修复 Bug | 问题解决 |
| `/enhance-prompt` | 优化提示词 | 提升效率 |
| `/think` | 深度思考 | 复杂分析 |
| `/dev` | Dev 工作流 | 完整开发流程 |

## 📁 文件位置

```
d:\claude code -11\
├── .claude/
│   ├── skills/
│   │   ├── codeagent/
│   │   ├── product-requirements/
│   │   ├── prototype-prompt-generator/
│   │   ├── design-master/
│   │   ├── json-canvas/
│   │   ├── obsidian-bases/
│   │   └── obsidian-markdown/
│   ├── commands/
│   │   ├── code.md
│   │   ├── debug.md
│   │   ├── test.md
│   │   └── ... (12 个命令)
│   └── agents/
├── CLAUDE.md (你原有的配置)
├── CLAUDE-myclaude.md (myclaude 的配置参考)
└── CLAUDE.md.backup (原配置备份)
```

## 🚀 使用示例

### 基础开发命令

```bash
# 实现一个功能
/code "实现用户登录功能"

# 调试问题
/debug "登录后页面空白"

# 编写测试
/test "为登录功能编写单元测试"

# 代码审查
/review "审查 src/auth.js"

# 性能优化
/optimize "优化数据库查询性能"

# 重构代码
/refactor "重构认证模块"
```

### Dev 工作流（推荐）

```bash
/dev "实现 JWT 认证系统"
```

**流程：**
1. 需求澄清
2. 代码分析
3. 开发计划
4. 并行执行
5. 覆盖率验证（≥90%）
6. 完成总结

### 技能激活

**自动触发场景：**

- `design-master` - UI/UX 设计、界面优化
- `product-requirements` - 需求分析、PRD 生成
- `json-canvas` - 创建思维导图、流程图
- `codeagent` - 多后端代码执行

## ⚙️ 配置说明

### 已处理冲突

- 你原有的 `CLAUDE.md` 配置已保留
- myclaude 的配置保存为 `CLAUDE-myclaude.md` 供参考
- 原配置已备份为 `CLAUDE.md.backup`

### 可选配置

如果你想启用 myclaude 的完整工作流配置，可以：

1. 查看 `CLAUDE-myclaude.md` 的内容
2. 将需要的部分合并到你当前的 `CLAUDE.md`
3. 或者直接替换（已备份原文件）

## 🎯 下一步

### 推荐尝试

1. **测试命令**：试试 `/code` 或 `/debug` 命令
2. **查看文档**：阅读 [CLAUDE-myclaude.md](CLAUDE-myclaude.md) 了解高级用法
3. **安装 BMAD**：如果需要企业级敏捷工作流，可以安装 bmad 模块

### 未安装模块

- **bmad** - 企业级 BMAD 敏捷工作流（6个专业代理）
- **requirements** - 需求驱动开发工作流

如需安装，运行：
```bash
cd "d:\claude code -11\temp\myclaude"
python install.py --module bmad --install-dir "d:\claude code -11"
```

## 📚 参考资源

- [myclaude GitHub](https://github.com/cexll/myclaude)
- [Dev 工作流文档](temp/myclaude/docs/DEV-WORKFLOW.md)
- [开发命令文档](temp/myclaude/docs/DEVELOPMENT-COMMANDS.md)

---

**安装日期**：2026-01-12
**版本**：myclaude v5.2
