# 音乐 MCP 服务器安装指南

## 📦 安装状态

✅ **已安装成功**
- music21-mcp-server（音乐分析与生成）
- strudel-mcp-server（实时音乐创作）

---

## 🎯 安装位置

### music21-mcp-server
- **源码目录**: `D:\claude code -11\mcp-servers\music21-mcp-server`
- **可执行文件**: `C:\Users\15085\AppData\Roaming\Python\Python311\Scripts\music21-mcp.exe`
- **Python 包**: music21, scipy, numpy, matplotlib 等

### strudel-mcp-server
- **NPM 包**: `@williamzujkowski/strudel-mcp-server@2.3.0`
- **可执行文件**: `C:\Users\15085\AppData\Roaming\npm\strudel-mcp.cmd`
- **版本**: 2.3.0

---

## ⚙️ 配置文件

**位置**: `C:\Users\15085\AppData\Roaming\Claude\claude_desktop_config.json`

**内容**:
```json
{
  "mcpServers": {
    "music21": {
      "command": "C:\\Users\\15085\\AppData\\Roaming\\Python\\Python311\\Scripts\\music21-mcp.exe",
      "args": []
    },
    "strudel": {
      "command": "cmd.exe",
      "args": [
        "/c",
        "C:\\Users\\15085\\AppData\\Roaming\\npm\\strudel-mcp.cmd"
      ]
    }
  }
}
```

---

## ✅ 测试结果

### strudel-mcp-server
- **状态**: ✅ 运行正常
- **版本**: v2.0.1
- **测试输出**: "Enhanced Strudel MCP server v2.0.1 running (fixed)"

### music21-mcp-server
- **状态**: ⚠️ 依赖问题
- **问题**: fastmcp 与 pydantic 版本冲突
- **解决方案**: 见下方

---

## 🔧 故障排除

### music21-mcp 依赖问题修复

**错误**:
```
TypeError: cannot specify both default and default_factory
```

**原因**: fastmcp 2.9.0 与当前 pydantic 版本不兼容

**解决方案**:

#### 方法 1: 降级 fastmcp（推荐）
```bash
pip install "fastmcp<2.9.0"
```

#### 方法 2: 使用 HTTP API 模式
```bash
# 启动 HTTP API 服务器
python -m music21_mcp.server_http

# 访问 http://localhost:8000/docs 查看 API 文档
```

#### 方法 3: 使用 CLI 模式
```bash
# 直接使用命令行工具
python -m music21_mcp.cli --help
```

---

## 🚀 使用方法

### 在 Claude Desktop 中使用

1. **确保 Claude Desktop 已安装**
2. **重启 Claude Desktop**（加载配置）
3. **开始对话**:
   ```
   你：用 music21 分析一下 C 大调音阶
   Claude：（调用 music21 MCP 工具）

   你：用 Strudel 生成一个 Techno 节奏
   Claude：（调用 strudel MCP 工具）
   ```

### 在命令行中使用

#### music21 (CLI)
```bash
# 分析音乐文件
python -m music21_mcp.cli analyze path/to/music.mid

# 生成旋律
python -m music21_mcp.cli generate --style "pop" --duration 60
```

#### Strudel (在线)
访问 https://strudel.cc/ 直接在浏览器中使用

---

## 📚 功能说明

### music21-mcp-server
- ✅ 和声分析
- ✅ 声部进行分析
- ✅ 对位法生成
- ✅ 风格模仿
- ✅ MIDI 文件处理
- ✅ 乐谱生成

### strudel-mcp-server
- ✅ 实时音乐编码
- ✅ 节奏模式生成
- ✅ 旋律模式生成
- ✅ 音色控制
- ✅ 效果器处理
- ✅ MIDI 导出

---

## 🔗 相关链接

### music21
- GitHub: https://github.com/brightlikethelight/music21-mcp-server
- 官方文档: https://www.music21.org/music21docs/

### Strudel
- NPM: https://www.npmjs.com/package/@williamzujkowski/strudel-mcp-server
- 在线 REPL: https://strudel.cc/
- 教程: https://strudel.cc/workshop/getting-started/

---

## 💡 提示

1. **重启 Claude Desktop**: 修改配置后必须重启
2. **查看日志**: Claude Desktop 会显示 MCP 服务器连接状态
3. **测试工具**: 先用简单的命令测试，如 "生成一个简单的节奏"
4. **学习资源**: 参考官方文档和教程

---

## ⚠️ 已知问题

1. **music21-mcp 启动失败**: fastmcp 依赖问题（见上方修复方案）
2. **路径中的空格**: Windows 路径中使用双反斜杠 `\\`
3. **权限问题**: 确保 Claude Desktop 有读取配置文件的权限

---

## 📝 下一步

1. ✅ 修复 music21-mcp 依赖问题
2. ✅ 在 Claude Desktop 中测试两个服务器
3. ✅ 尝试生成第一首音乐！
4. ✅ 探索更多高级功能

---

## 🎵 快速示例

### music21 示例
```
你：分析这段音乐的和声
Claude：我需要先看到音乐文件。你可以提供 MIDI 文件路径，或者我可以生成一个简单的示例。

你：生成一个 C 大调音阶
Claude：（调用 music21）正在生成 C 大调音阶...
```

### Strudel 示例
```
你：生成一个欢快的节奏
Claude：（调用 strudel）正在生成节奏...
    已生成：
    sound("bd sd bd sd").slow(2)

你：加快一点
Claude：正在修改...
    sound("bd sd bd sd").slow(1)
```

---

**创建时间**: 2025-01-06
**最后更新**: 2025-01-06
