# 有声书制作工作流

## 完整工作流程

### 阶段1：文本准备（5-10分钟）

#### 1.1 获取源文本
- 文章：从网页、文档复制
- 书籍：确保有版权或授权
- 自写内容：直接使用

#### 1.2 清理文本
```python
# 移除多余空行
import re
clean_text = re.sub(r'\n{3,}', '\n\n', text)

# 移除特殊字符（如果影响TTS）
clean_text = re.sub(r'[^\w\s\u4e00-\u9fff，。！？、；：""''（）《》]', '', text)
```

#### 1.3 添加章节标记
如果文本没有章节划分，手动添加：
```
开篇
[开篇内容]

第一章
[第一章内容]

第二章
[第二章内容]

...
```

#### 1.4 保存文本文件
```bash
# 保存到工作目录
cd project/yinian-clipboard-manager/temp
# 保存为 audiobook_text.txt
```

### 阶段2：参数配置（2-3分钟）

#### 2.1 选择语音
根据内容类型选择：
- 专业内容 → 云健（男声）
- 文学作品 → 云希（女声）
- 通用内容 → 晓晓（女声）

#### 2.2 调整参数
编辑 `generate_audiobook.py` 中的配置：
```python
VOICE = 'zh-CN-YunjianNeural'
RATE = '+0%'   # 根据内容密度调整
PITCH = '+0Hz'
VOLUME = '+0%'
CHAPTER_PAUSE = 2  # 章节间停顿秒数
```

#### 2.3 定义章节结构
在脚本中修改 `CHAPTERS` 列表：
```python
CHAPTERS = [
    {"title": "00_开篇", "start_marker": "开篇\n", "end_marker": "\n第一章"},
    {"title": "01_第一章", "start_marker": "\n第一章", "end_marker": "\n第二章"},
    # ...
    {"title": "NN_结尾", "start_marker": "\n结尾", "end_marker": None},
]
```

### 阶段3：生成音频（5-15分钟）

#### 3.1 运行生成脚本
```bash
cd project/yinian-clipboard-manager/temp
python generate_audiobook.py
```

#### 3.2 监控进度
脚本会显示：
- 章节提取进度
- 音频生成进度
- 合并进度

#### 3.3 检查中间文件
```bash
# 查看章节文件
ls -lh chapters/

# 播放测试章节
#（使用音频播放器）
```

### 阶段4：质量验证（2-5分钟）

#### 4.1 检查时长
```bash
# 使用 ffprobe
ffprobe deep_work_complete_audiobook.mp3

# 预期时长计算：
# 总字数 / 150-180 = 分钟数
```

#### 4.2 试听检查
- 开头30秒：检查音质、语速
- 中间段落：检查流畅度
- 结尾部分：检查完整性

#### 4.3 问题诊断

**问题1：语速不合适**
→ 修改 RATE 参数，重新生成

**问题2：章节分割错误**
→ 检查 start_marker 和 end_marker

**问题3：音频有杂音**
→ 可能是网络问题，重新生成该章节

**问题4：音量不一致**
→ 使用 FFmpeg 音量标准化

### 阶段5：后期处理（可选）

#### 5.1 音量标准化
```bash
ffmpeg -i input.mp3 -filter:a "loudnorm" output.mp3
```

#### 5.2 添加元数据
```bash
ffmpeg -i input.mp3 \
  -metadata title="深度工作" \
  -metadata artist="向阳乔木" \
  -metadata genre="Audiobook" \
  output.mp3
```

#### 5.3 添加背景音乐（高级）
```bash
# 叠加背景音乐
ffmpeg -i voice.mp3 -i music.mp3 \
  -filter_complex "[0:a][1:a]amix=inputs=2:duration=first:weights=1 0.3" \
  output.mp3
```

## 快速参考

### 文本准备清单
- [ ] 清理多余空行
- [ ] 添加章节标记
- [ ] 检查特殊字符
- [ ] 保存为 UTF-8 编码

### 参数配置清单
- [ ] 选择合适的语音
- [ ] 设置语速（通常 +0%）
- [ ] 设置音调（通常 +0Hz）
- [ ] 定义章节结构

### 生成验证清单
- [ ] 所有章节生成成功
- [ ] 时长符合预期
- [ ] 音质清晰可听
- [ ] 章节间停顿适当

### 输出文件
- `audiobook_text.txt` - 源文本
- `generate_audiobook.py` - 生成脚本
- `chapters/*.mp3` - 独立章节
- `deep_work_complete_audiobook.mp3` - 完整音频
- `silence.mp3` - 静音文件（临时）

## 常见工作流问题

### Q1: 如何处理超长文本？
**方法1**: 分批生成
- 将文本分成多个文件
- 分别生成音频
- 手动合并

**方法2**: 提高语速
```python
RATE = '+20%'  # 加快20%
```

### Q2: 章节标记找不到？
检查标记格式：
- ✅ `"开篇\n"` - 带换行符
- ✅ `"\n第一章"` - 前置换行
- ❌ `"开篇"` - 不带换行可能误匹配

### Q3: 如何批量制作？
创建批处理脚本：
```bash
#!/bin/bash
for file in texts/*.txt; do
  python generate_audiobook.py "$file"
done
```

### Q4: 如何备份工作文件？
```bash
# 创建备份目录
mkdir -p backup/$(date +%Y%m%d)

# 备份所有文件
cp -r chapters/ backup/$(date +%Y%m%d)/
cp deep_work_complete_audiobook.mp3 backup/$(date +%Y%m%d)/
```

## 性能优化

### 加速生成
1. **减少章节数量** - 合并短章节
2. **提高并发** - 修改脚本使用 asyncio.gather()
3. **使用缓存** - 避免重复生成相同章节

### 减小文件大小
1. **降低比特率** - 32kbps → 24kbps
2. **降低采样率** - 24kHz → 16kHz
3. **压缩格式** - MP3 → OPUS（更小）

## 进阶技巧

### 技巧1：情感标记
在文本中使用标点影响语调：
```
正常陈述：这是一个重要的概念。
强调：这是一个重要的概念！
疑问：这是一个重要的概念？
停顿：这是一个重要的概念，...
```

### 技巧2：数字处理
TTS对数字的朗读：
```
✅ "二零二四年" → 2024年（自然）
❌ "2024年" → 可能读作"二零二四"或"两千零二十四"

✅ "百分之五十" → 50%（清晰）
❌ "50%" → 可能读错
```

### 技巧3：英文处理
中英文混排时的建议：
```
✅ "Python" → 保持英文
✅ "Python编程" → 自然
❌ "python编程" → 可能读作 p-y-t-h-o-n
```

### 技巧4：特殊符号
```
✅ 使用全角标点：，。！？
❌ 避免半角标点：,.!?
```

## 工作流模板

### 模板1：快速制作（10分钟内）
```bash
# 1. 保存文本
cp my_article.txt project/yinian-clipboard-manager/temp/audiobook_text.txt

# 2. 使用默认配置生成
cd project/yinian-clipboard-manager/temp
python generate_audiobook.py

# 3. 获取结果
# 完成！
```

### 模板2：精细调整（30分钟）
```bash
# 1. 准备文本（清理、分段）
# 2. 选择语音（测试3种）
# 3. 调整参数（语速、音调）
# 4. 生成测试版本（部分章节）
# 5. 试听并微调
# 6. 生成完整版本
# 7. 质量验证
# 8. 后期处理（可选）
```

### 模板3：批量制作（自动化）
```python
# batch_generate.py
import asyncio
from pathlib import Path

texts = list(Path("texts").glob("*.txt"))

for text_file in texts:
    # 复制文本
    # 修改配置（根据文件名）
    # 运行生成
    # 移动输出文件
    pass
```
