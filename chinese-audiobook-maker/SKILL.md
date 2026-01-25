---
name: chinese-audiobook-maker
description: 中文有声书制作工具。使用 edge-tts 将中文文本转换为专业有声书。当需要：1) 将中文文章/书籍转换为音频，2) 生成分章节的有声书，3) 使用TTS技术创建中文语音内容，4) 合并多个音频为完整有声书。支持自定义语音、语速、音调，自动章节分割和合并。
---

# 中文有声书制作器

快速将中文文本制作成专业有声书。

## 核心功能

**文本转语音** - 使用微软 edge-tts 引擎，支持8种中文神经网络语音
**自动分章** - 按章节标记自动分割文本，生成独立音频文件
**智能合并** - 自动添加章节间停顿，合并为完整有声书
**参数可调** - 支持自定义语速、音调、音量、声音类型

## 快速开始

### 最简用法

```
用户：把这篇文章做成有声书

你：
1. 将文本保存为 `audiobook_text.txt`
2. 运行脚本生成音频
3. 返回完整有声书文件
```

### 完整工作流

#### 步骤1：准备文本
保存文本到工作目录，格式要求：
- 使用章节标记（如"第一章"、"第二章"）
- 每章节间空行分隔
- 总字符数建议 3000-5000 字

#### 步骤2：生成有声书
运行生成脚本：
```bash
cd project/yinian-clipboard-manager/temp
python generate_audiobook.py
```

#### 步骤3：获取结果
- **完整有声书**: `deep_work_complete_audiobook.mp3`
- **独立章节**: `chapters/chapter_*.mp3`

## 可用中文语音

### 男声
- `zh-CN-YunjianNeural` - 云健（专业讲座风格，推荐）
- `zh-CN-YunyangNeural` - 云阳

### 女声
- `zh-CN-XiaoxiaoNeural` - 晓晓（标准女声）
- `zh-CN-XiaoyiNeural` - 晓怡
- `zh-CN-YunxiNeural` - 云希（轻柔）
- `zh-CN-YunxiaNeural` - 云霞

### 方言
- `zh-CN-liaoning-XiaobeiNeural` - 东北小北（女声）
- `zh-CN-shaanxi-XiaoniNeural` - 陕西小妮（女声）

## 参数配置

在脚本中修改配置：
```python
VOICE = 'zh-CN-YunjianNeural'  # 选择语音
RATE = '+0%'   # 语速（-50% 到 +100%）
PITCH = '+0Hz' # 音调（-50Hz 到 +50Hz）
VOLUME = '+0%' # 音量（-50% 到 +100%）
CHAPTER_PAUSE = 2  # 章节间停顿（秒）
```

## 章节定义格式

在脚本的 `CHAPTERS` 列表中定义：
```python
CHAPTERS = [
    {"title": "00_开篇", "start_marker": "开篇\n", "end_marker": "\n第一章"},
    {"title": "01_第一章", "start_marker": "\n第一章", "end_marker": "\n第二章"},
    # ... 更多章节
    {"title": "08_结尾", "start_marker": "\n结尾", "end_marker": None},
]
```

## 输出规格

**音频格式**：MP3
**采样率**：24kHz
**声道**：单声道（适合人声）
**比特率**：32kbps（平衡质量和大小）

**预计时长**：正常语速（150-180字/分钟）
- 3000字 ≈ 17-20分钟
- 4000字 ≈ 22-27分钟
- 5000字 ≈ 28-33分钟

## 核心脚本

**主脚本**: [scripts/generate_audiobook.py](scripts/generate_audiobook.py)

功能：
1. 读取文本文件
2. 按章节标记提取各章节
3. 使用 edge-tts 生成每个章节的音频
4. 生成章节间静音文件
5. 使用 FFmpeg 合并所有章节

## 依赖工具

- **edge-tts** - 微软 TTS 引擎（已安装）
- **FFmpeg** - 音频处理工具（已安装）
- **Python 3.11+** - 运行环境

## 使用场景

### 场景1：单篇文章
直接将文章保存为文本文件运行脚本即可。

### 场景2：分章节书籍
在文本中使用明确的章节标记，脚本会自动分割。

### 场景3：自定义语音风格
修改 VOICE 参数选择不同的声音类型。

### 场景4：调整时长
修改 RATE 参数加快或减慢语速。

## 常见问题

**Q: 如何调整语速？**
A: 修改 `RATE` 参数，如 `'+20%'` 加快20%，`'-10%'` 减慢10%

**Q: 章节标记格式是什么？**
A: 默认使用"第一章"、"第二章"等中文标记，可在 CHAPTERS 中自定义

**Q: 能否添加背景音乐？**
A: 当前版本仅支持纯语音，背景音乐需要后期手动添加

**Q: 如何改变音质？**
A: 修改 FFmpeg 合并参数，调整比特率（当前32kbps）

## 输出文件位置

**工作目录**: `project/yinian-clipboard-manager/temp/`

**输出文件**:
- `deep_work_complete_audiobook.mp3` - 完整合并音频
- `chapters/` - 独立章节音频
- `concat_list.txt` - FFmpeg 合并列表（临时文件）
