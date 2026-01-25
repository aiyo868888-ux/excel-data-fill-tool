# 音频自动生成脚本使用指南

## 概述

这个脚本可以自动将文本转换为音频文件，支持两种 TTS 服务：

1. **Edge TTS**（推荐）- 完全免费，无需 API 密钥，音质优秀
2. **OpenAI TTS** - 需要 API 密钥，音质最佳

---

## 快速开始

### 1. 安装 Python

**检查是否已安装**：
```bash
python --version
```

如果未安装，访问：https://www.python.org/downloads/

---

### 2. 使用 Edge TTS（免费，推荐）

**基础用法**：
```bash
python "D:\claude code -11\skills\content-workflow-v2\scripts\tts_generator.py" "D:\jieyue\AI时代的价值_TTSMaker文本.md"
```

**指定输出文件**：
```bash
python "D:\claude code -11\skills\content-workflow-v2\scripts\tts_generator.py" "D:\jieyue\AI时代的价值_TTSMaker文本.md" -o "D:\jieyue\AI时代的价值_播客.mp3"
```

**选择不同的声音**：
```bash
# 女声（温和）
python tts_generator.py input.txt -v zh-CN-XiaoxiaoNeural

# 男声（沉稳）
python tts_generator.py input.txt -v zh-CN-YunyangNeural

# 男声（年轻）
python tts_generator.py input.txt -v zh-CN-YunxiNeural

# 女声（活泼）
python tts_generator.py input.txt -v zh-CN-XiaoyiNeural
```

**调整语速**：
```bash
# 慢 10%（推荐）
python tts_generator.py input.txt -r "-10%"

# 慢 20%
python tts_generator.py input.txt -r "-20%"

# 正常语速
python tts_generator.py input.txt -r "+0%"

# 快 10%
python tts_generator.py input.txt -r "+10%"
```

**列出所有可用语音**：
```bash
python tts_generator.py --list-voices
```

---

### 3. 使用 OpenAI TTS（需要 API 密钥）

**设置 API 密钥**：

**Windows**：
```powershell
$env:OPENAI_API_KEY = "your-api-key-here"
```

**macOS/Linux**：
```bash
export OPENAI_API_KEY="your-api-key-here"
```

**生成音频**：
```bash
python tts_generator.py input.txt -s openai -v alloy
```

**可用语音**：
- `alloy` - 中性
- `echo` - 男声
- `fable` - 英式口音
- `onyx` - 深沉男声
- `nova` - 女声
- `shimmer` - 温柔女声

---

## 完整参数说明

```bash
python tts_generator.py [文本文件] [选项]

必需参数:
  文本文件              输入的文本文件路径

可选参数:
  -o, --output         输出音频文件路径（默认：与文本文件同名的 .mp3）
  -s, --service        TTS 服务 (edge 或 openai，默认: edge)
  -v, --voice          语音选项
  -r, --rate           语速调整（仅 Edge TTS，默认: -10%）
  --list-voices        列出所有可用语音
  --api-key            API 密钥（OpenAI TTS）
```

---

## 使用示例

### 示例 1：最简单的用法（Edge TTS）

```bash
python tts_generator.py "我的文本.txt"
```

**结果**：
- 自动安装依赖（首次运行）
- 使用默认女声（Xiaoxiao）
- 语速慢 10%
- 生成 `我的文本.mp3`

---

### 示例 2：自定义所有参数

```bash
python tts_generator.py "我的文本.txt" -o "输出.mp3" -v zh-CN-YunyangNeural -r "-15%"
```

**结果**：
- 使用男声（Yunyang）
- 语速慢 15%
- 输出到 `输出.mp3`

---

### 示例 3：使用 OpenAI TTS

```bash
python tts_generator.py "我的文本.txt" -s openai -v nova --api-key "sk-xxx"
```

**结果**：
- 使用 OpenAI TTS
- 使用 nova 女声
- 需要有效的 API 密钥

---

## 常见问题

### Q1: 首次运行很慢？

**A**: 首次运行会自动安装依赖（edge-tts），需要几分钟。之后运行会很快。

---

### Q2: 如何选择最佳语音？

**A**: 
1. 运行 `python tts_generator.py --list-voices` 查看所有语音
2. 推荐：
   - **Xiaoxiao**（女声）- 温和、适合叙事
   - **Yunyang**（男声）- 沉稳、适合专业内容

---

### Q3: 生成的音频在哪里？

**A**: 
- 如果未指定 `-o` 参数，音频文件会保存在与文本文件相同的目录
- 文件名与文本文件相同，扩展名为 `.mp3`

---

### Q4: Edge TTS 和 OpenAI TTS 哪个更好？

**A**: 
- **Edge TTS**：免费，音质优秀，中文支持好，推荐日常使用
- **OpenAI TTS**：音质最佳，但需要付费，适合专业制作

---

### Q5: 如何批量生成多个文件？

**A**: 
**Windows PowerShell**：
```powershell
Get-ChildItem "D:\jieyue\*.txt" | ForEach-Object {
    python tts_generator.py $_.FullName
}
```

**macOS/Linux**：
```bash
for file in /path/to/*.txt; do
    python tts_generator.py "$file"
done
```

---

## 故障排除

### 问题：找不到 Python

**解决**：
1. 安装 Python：https://www.python.org/downloads/
2. 安装时勾选 "Add Python to PATH"
3. 重启终端

---

### 问题：安装依赖失败

**解决**：
```bash
# 手动安装
pip install edge-tts

# 或使用国内镜像
pip install edge-tts -i https://pypi.tuna.tsinghua.edu.cn/simple
```

---

### 问题：OpenAI API 密钥无效

**解决**：
1. 检查密钥是否正确
2. 确认账户有余额
3. 检查 API 权限

---

## 高级用法

### 1. 添加到系统 PATH

**Windows**：
将脚本目录添加到系统 PATH，之后可以直接运行：
```bash
tts_generator.py input.txt
```

---

### 2. 创建快捷方式

**Windows 批处理文件**（`generate_audio.bat`）：
```batch
@echo off
python "D:\claude code -11\skills\content-workflow-v2\scripts\tts_generator.py" %*
```

使用：
```bash
generate_audio.bat "我的文本.txt"
```

---

### 3. 集成到其他脚本

**Python 中调用**：
```python
import subprocess

subprocess.run([
    "python",
    "tts_generator.py",
    "input.txt",
    "-o", "output.mp3",
    "-v", "zh-CN-XiaoxiaoNeural"
])
```

---

## 性能优化

### 1. 大文件处理

如果文本很长（> 5000 字），建议：
1. 分段处理
2. 使用后期软件拼接（Audacity）

### 2. 加速生成

- Edge TTS：已经很快，无需优化
- OpenAI TTS：可以使用 `tts-1` 模型（比 `tts-1-hd` 快）

---

## 更新日志

**v1.0**（2026-01-24）
- ✅ 支持 Edge TTS（免费）
- ✅ 支持 OpenAI TTS
- ✅ 自动安装依赖
- ✅ 多语音选择
- ✅ 语速调节

---

**脚本位置**：`D:\claude code -11\skills\content-workflow-v2\scripts\tts_generator.py`

**需要帮助？** 查看脚本源码中的注释，或运行 `python tts_generator.py -h`
