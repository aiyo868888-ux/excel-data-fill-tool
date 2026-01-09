# Auto-Formatter Hook 使用指南

## 概述

PostToolUse Hook 用于在 Claude 写入代码后自动格式化整个项目，处理最后 10% 的格式问题，避免 CI 报错。

## 功能特性

- ✅ **多语言支持**：JavaScript/TypeScript、Python、Go、Rust
- ✅ **自动检测**：自动识别项目中使用的编程语言
- ✅ **全项目格式化**：确保整个项目格式一致
- ✅ **严格模式**：格式化失败时阻止操作
- ✅ **智能工具选择**：自动选择并优先使用可用的格式化工具

## 支持的格式化工具

| 语言 | 格式化工具 | 检测命令 | 格式化命令 |
|------|-----------|---------|-----------|
| JavaScript/TypeScript | Prettier | `npx prettier --check "."` | `npx prettier --write "."` |
| Python | Black | `black --check .` | `black .` |
| Python | autopep8 | `autopep8 --diff --recursive .` | `autopep8 --in-place --recursive .` |
| Go | gofmt | `gofmt -l .` | `gofmt -w .` |
| Rust | rustfmt | `cargo fmt -- --check` | `cargo fmt` |

## 安装配置

### 1. Hook 文件已创建

```
.claude/hooks/PostToolUse/
├── auto-formatter.js    # Hook 脚本
└── auto-formatter.json  # Hook 配置
```

### 2. 安装格式化工具（根据项目需要）

#### JavaScript/TypeScript 项目
```bash
npm install --save-dev prettier
# 或
yarn add -D prettier
```

#### Python 项目
```bash
pip install black
# 或
pip install autopep8
```

#### Go 项目
```bash
# gofmt 内置于 Go 工具链，无需额外安装
```

#### Rust 项目
```bash
# rustfmt 内置于 Rust 工具链，通过 cargo fmt 使用
```

### 3. 配置格式化工具（可选）

#### Prettier 配置示例
创建 `.prettierrc`:
```json
{
  "semi": true,
  "singleQuote": true,
  "tabWidth": 2,
  "trailingComma": "es5"
}
```

#### Black 配置示例
创建 `pyproject.toml`:
```toml
[tool.black]
line-length = 88
target-version = ['py38']
```

## 工作流程

1. **Claude 写入代码** → 使用 Write 工具创建或修改文件
2. **Hook 触发** → PostToolUse Hook 自动执行
3. **语言检测** → 扫描项目文件，检测使用的编程语言
4. **格式检查** → 检查代码格式是否符合规范
5. **自动格式化** → 如果发现问题，自动运行格式化工具
6. **验证结果** → 再次检查确保格式正确
7. **成功/失败** → 格式化成功则继续，失败则阻止操作

## 日志输出示例

```
🚀 Auto-Formatter Hook 启动
📂 工作目录: /path/to/project

🔍 检测到语言: javascript, python

📝 处理 JAVASCRIPT 文件...

🔧 检查格式 (Prettier)...
⚠️  发现格式问题，正在自动修复...
🔧 格式化 (Prettier)...
✅ 格式化 (Prettier) 成功
🔧 验证格式 (Prettier)...
✅ 验证格式 (Prettier) 成功

📝 处理 PYTHON 文件...
🔧 检查格式 (Black)...
✅ Black 格式正确，无需修改

✅ 所有文件格式化完成！
```

## 错误处理

### 格式化失败时的行为

当格式化失败时，Hook 会：
1. 显示详细的错误信息
2. 阻止当前操作继续
3. 提示手动修复的建议

示例错误输出：
```
❌ 格式化失败: Prettier
💡 提示: 请手动运行格式化工具并修复错误
```

### 常见问题排查

#### 1. 格式化工具未安装
```
⚠️  Prettier 未安装，跳过
```
**解决**：安装相应的格式化工具

#### 2. 格式化冲突
如果代码有语法错误，格式化可能会失败。**解决**：先修复语法错误

#### 3. 超时
默认超时时间为 60 秒。大型项目可能需要更长时间。**解决**：修改脚本中的 `timeout` 参数

## 自定义配置

### 修改格式化范围

当前配置是格式化整个项目。如需只格式化修改的文件，修改 `format` 命令。

### 添加新的语言支持

在 `LANGUAGE_CONFIG` 中添加新配置：
```javascript
ruby: {
  patterns: ['**/*.rb'],
  formatters: [
    {
      check: 'rubocop --only Style -L .',
      format: 'rubocop -a --only Style .',
      name: 'RuboCop'
    }
  ]
}
```

### 调整超时时间
```javascript
execSync(command, {
  timeout: 120000 // 修改为 120 秒
});
```

## 禁用 Hook

如果需要临时禁用 Hook：
1. 编辑 `.claude/hooks/PostToolUse/auto-formatter.json`
2. 将 `"enabled": true` 改为 `"enabled": false`

## 最佳实践

1. **CI 集成**：在 CI 中也运行相同的格式化检查，确保一致性
2. **Pre-commit Hook**：配合 git pre-commit hook 使用，在提交前格式化
3. **团队统一**：确保团队成员使用相同的格式化配置
4. **版本锁定**：锁定格式化工具的版本，避免不同版本产生差异

## 性能考虑

- 增量格式化：只检查修改的文件可以提升性能
- 并行处理：多语言项目可以并行格式化（当前实现是串行）
- 缓存机制：使用格式化工具的缓存功能（如 `--cache` 选项）

## 故障排除

### Windows 环境问题
如果 `dir` 命令不工作，可能需要调整文件查找逻辑。

### 权限问题
确保 Hook 文件有执行权限：
```bash
chmod +x .claude/hooks/PostToolUse/auto-formatter.js
```

### 调试模式
在脚本开头添加：
```javascript
console.log('DEBUG:', process.env.HOOK_INPUT);
```

## 贡献

如需支持更多语言或改进功能，欢迎修改和扩展此 Hook！
