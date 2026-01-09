# 语音转文本模块

基于 OpenAI Whisper 的音频转文字功能，支持生成带时间戳的字幕文件。

## 📁 文件结构

```
project/语音转文本/
├── __init__.py              # Python 包初始化文件
├── whisper_service.py       # Whisper 服务封装（核心功能）
├── templates/
│   └── whisper.html        # Web 界面
├── static/
│   └── whisper.js          # 前端交互逻辑
└── README.md               # 本文件
```

## 🎯 功能特性

- ✅ 图形界面操作（美观的紫色渐变设计）
- ✅ 拖拽上传音频文件
- ✅ 实时进度显示
- ✅ 多种输出格式（TXT/SRT/VTT）
- ✅ 带时间戳的字幕文件
- ✅ 后台任务处理
- ✅ 并发任务控制（最多3个）
- ✅ 完全免费（本地运行）

## 🌐 访问地址

**本项目独立运行**：
- 主页：http://localhost:5002
- 语音识别：http://localhost:5002

**注意**：与"数据填充工具"是两个独立项目，使用不同端口

## 🚀 启动方式

### 方法 1：双击启动脚本
```
启动.bat
```

### 方法 2：命令行启动
```bash
cd project/语音转文本
python web_app.py
```

## 📝 支持的音频格式

MP3, WAV, M4A, MP4, OGG, FLAC, AAC, WMA

## ⚙️ 技术栈

- **后端**：Flask 2.0+
- **AI 模型**：openai-whisper（Base 模型）
- **前端**：Tailwind CSS + 原生 JavaScript
- **音频处理**：pydub

## 🚀 使用方法

1. 打开浏览器访问 http://localhost:5001/whisper

2. 配置选项：
   - 选择识别模型（推荐 Base）
   - 勾选输出格式（TXT/SRT/VTT）

3. 上传音频：
   - 拖拽音频文件到上传区域
   - 或点击选择文件

4. 等待处理：
   - 查看实时进度条
   - 观察处理日志

5. 下载结果：
   - 预览识别文本
   - 下载字幕文件

## 📊 性能指标

- **模型大小**：Base（约 139MB）
- **内存占用**：约 1.5GB
- **处理速度**：5分钟音频 ≈ 2-3分钟
- **文件大小限制**：最大 500MB

## 🔧 核心类：WhisperTranscriber

### 方法

- `load_model(progress_callback=None)` - 加载 Whisper 模型（带缓存）
- `transcribe(audio_path, output_formats, progress_callback=None)` - 转录音频
- `_generate_srt(result)` - 生成 SRT 字幕
- `_generate_vtt(result)` - 生成 VTT 字幕
- `preload_models(model_sizes=['base'])` - 预加载模型

### 使用示例

```python
from project.语音转文本.whisper_service import WhisperTranscriber

# 创建转录器
transcriber = WhisperTranscriber('base')

# 加载模型
transcriber.load_model()

# 转录音频
result = transcriber.transcribe(
    'audio.mp3',
    output_formats=['txt', 'srt', 'vtt']
)

# 获取结果
text = result['text']          # 纯文本
srt_path = result['srt_path']  # SRT 字幕路径
vtt_path = result['vtt_path']  # VTT 字幕路径
```

## 📦 依赖项

```
flask>=2.0.0
openai-whisper>=20231117
pydub>=0.25.1
```

## 💡 注意事项

1. **首次使用**：Whisper 模型需要下载（约 139MB）
2. **系统要求**：建议至少 4GB 可用内存
3. **FFmpeg 依赖**：需要安装 FFmpeg（用于音频处理）

## 🔗 相关文件

- 主应用：`web_app.py`
- 主页模板：`templates/index.html`
- 配置管理：`templates/config.html`

## 📅 更新日志

- **2026-01-09**：模块从根目录迁移到 `project/语音转文本/`
- **2026-01-09**：完成 Whisper 语音识别功能集成
