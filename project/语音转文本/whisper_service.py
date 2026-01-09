"""
Whisper 语音识别服务封装
提供音频转文字、字幕生成等功能
"""

import whisper
import os
import threading
from datetime import timedelta


def traditional_to_simplified(text):
    """将繁体字转换为简体字"""
    try:
        import opencc
        converter = opencc.OpenCC('t2s')  # 繁体转简体
        return converter.convert(text)
    except ImportError:
        # 如果没有安装 opencc，返回原文
        return text


def add_punctuation(text):
    """
    为文本添加标点符号（基于规则）
    适用于中文语音识别结果的后处理
    """
    import re

    if not text or not text.strip():
        return text

    # 移除多余的空格，但保留换行符
    text = re.sub(r'[ \t]+', '', text)

    # 定义句末标点符号（根据语调、停顿长度判断）
    # Whisper会将长句分成多个segment，我们需要在合适的segment后添加标点

    # 标点符号列表
    punctuation_marks = ['。', '！', '？', '，', '；', '：']

    # 简单规则：每句话结束后添加句号
    # 检测句子结束的标志词
    sentence_enders = [
        '的', '了', '呢', '吧', '啊', '嘛', '呀',
        '吗', '哦', '噢', '恩', '嗯', '唉',
        '这样', '那样', '现在', '然后', '最后',
        '所以', '因此', '但是', '不过', '而且'
    ]

    # 分割成行（每个segment一行）
    lines = text.split('\n')
    result = []

    for i, line in enumerate(lines):
        line = line.strip()
        if not line:
            continue

        # 如果行太短（小于2个字符），跳过
        if len(line) < 2:
            result.append(line)
            continue

        # 移除已有的标点
        clean_line = line
        for mark in punctuation_marks:
            clean_line = clean_line.replace(mark, '')

        # 判断是否需要添加标点
        if i < len(lines) - 1:  # 不是最后一行
            # 检查是否以疑问词结尾
            if any(clean_line.endswith(w) for w in ['吗', '呢', '啊']):
                result.append(clean_line + '？')
            # 检查是否以感叹词结尾
            elif any(clean_line.endswith(w) for w in ['啊', '呀', '哇', '哦']):
                result.append(clean_line + '！')
            # 检查是否以句子结束标志词结尾
            elif any(clean_line.endswith(w) for w in sentence_enders):
                result.append(clean_line + '。')
            # 否则添加逗号（表示停顿）
            else:
                result.append(clean_line + '，')
        else:  # 最后一行
            if any(clean_line.endswith(w) for w in ['吗', '呢']):
                result.append(clean_line + '？')
            elif any(clean_line.endswith(w) for w in ['啊', '呀', '哇']):
                result.append(clean_line + '！')
            else:
                result.append(clean_line + '。')

    return '\n'.join(result)


class WhisperTranscriber:
    """Whisper 语音识别服务封装"""

    # 模型缓存（单例模式）
    _model_cache = {}
    _cache_lock = threading.Lock()

    def __init__(self, model_size='base'):
        """
        初始化转录器

        参数:
            model_size: 模型大小 (tiny/base/small/medium/large)
                        tiny: 最快，准确度较低（~1GB 内存）
                        base: 平衡（~1.5GB 内存）✅ 推荐
                        small: 较准确（~2GB 内存）
                        medium: 准确（~5GB 内存）
                        large: 最准确（~10GB 内存）
        """
        self.model_size = model_size
        self.model = None
        self.progress_callback = None

    def load_model(self, progress_callback=None):
        """加载 Whisper 模型（带缓存）"""
        with self._cache_lock:
            # 检查缓存
            if self.model_size in self._model_cache:
                self.model = self._model_cache[self.model_size]
                if progress_callback:
                    progress_callback(10, "模型已加载（缓存）")
                return

            # 加载新模型
            if progress_callback:
                progress_callback(5, f"正在加载 {self.model_size} 模型...")

            try:
                self.model = whisper.load_model(self.model_size)
                self._model_cache[self.model_size] = self.model

                if progress_callback:
                    progress_callback(10, "模型加载完成")
            except Exception as e:
                raise Exception(f"模型加载失败: {str(e)}")

    def transcribe(self, audio_path, output_formats=['txt', 'srt', 'vtt'],
                   progress_callback=None):
        """
        转录音频文件

        参数:
            audio_path: 音频文件路径
            output_formats: 输出格式列表 ['txt', 'srt', 'vtt']
            progress_callback: 进度回调函数 callback(progress, message)

        返回:
            dict: {
                'text': '纯文本内容',
                'srt_path': 'path/to/file.srt',
                'vtt_path': 'path/to/file.vtt'
            }
        """
        self.progress_callback = progress_callback

        # 1. 加载模型
        try:
            self.load_model(progress_callback)
        except Exception as e:
            raise Exception(f"模型加载失败: {str(e)}")

        # 2. 转录（预估时间）
        import time
        start_time = time.time()

        if progress_callback:
            progress_callback(20, "正在转录音频，请耐心等待...")

        try:
            print(f"[DEBUG] 开始转录，音频路径: {audio_path}")
            print(f"[DEBUG] 文件是否存在: {os.path.exists(audio_path)}")

            if not os.path.exists(audio_path):
                raise Exception(f"音频文件不存在: {audio_path}")

            file_size = os.path.getsize(audio_path)
            print(f"[DEBUG] 文件大小: {file_size} bytes ({file_size / 1024 / 1024:.2f} MB)")

            # 检查文件扩展名
            file_ext = os.path.splitext(audio_path)[1].lower()
            print(f"[DEBUG] 文件扩展名: {file_ext}")

            # 检查路径是否包含非ASCII字符（会导致Windows ffmpeg错误）
            try:
                audio_path.encode('ascii')
                path_has_unicode = False
            except UnicodeEncodeError:
                path_has_unicode = True
                print(f"[WARN] 文件路径包含非ASCII字符，可能需要创建临时副本")

            # 如果路径包含中文，创建临时副本
            if path_has_unicode:
                import shutil
                import tempfile

                # 创建临时文件（纯ASCII路径）
                temp_dir = tempfile.gettempdir()
                temp_filename = f"whisper_temp_{os.getpid()}{file_ext}"
                temp_audio_path = os.path.join(temp_dir, temp_filename)

                print(f"[INFO] 创建临时文件副本: {temp_audio_path}")
                shutil.copy2(audio_path, temp_audio_path)
                audio_path = temp_audio_path
                print(f"[INFO] 使用临时路径: {audio_path}")

            # 尝试使用 ffmpeg 加载音频
            print(f"[DEBUG] 尝试加载音频文件...")
            import torch
            audio = whisper.load_audio(audio_path)
            print(f"[DEBUG] 音频加载成功，长度: {len(audio)}")

            # 执行转录
            print(f"[DEBUG] 开始 Whisper 转录...")
            result = self.model.transcribe(
                audio,
                language='zh',  # 中文
                task='transcribe',  # 转录任务（不是翻译）
                word_timestamps=False,  # 关闭词级时间戳，提升速度
                verbose=False  # 关闭详细输出
            )

            print(f"[DEBUG] 转录完成，识别文本长度: {len(result['text'])}")

            # 清理临时文件
            if path_has_unicode and os.path.exists(temp_audio_path):
                try:
                    os.remove(temp_audio_path)
                    print(f"[INFO] 已删除临时文件")
                except:
                    pass
        except FileNotFoundError as e:
            print(f"[ERROR] 文件未找到: {str(e)}")
            raise Exception(f"音频文件未找到: {str(e)}")
        except PermissionError as e:
            print(f"[ERROR] 权限错误: {str(e)}")
            raise Exception(f"文件访问权限错误: {str(e)}")
        except OSError as e:
            print(f"[ERROR] 系统错误: {str(e)}")
            print(f"[ERROR] errno: {e.errno}")
            import traceback
            traceback.print_exc()
            raise Exception(f"文件读取错误 (errno={e.errno}): {str(e)}\n可能的原因：\n1. 文件路径包含特殊字符\n2. 文件被其他程序占用\n3. 磁盘空间不足\n4. 文件系统权限问题")
        except Exception as e:
            print(f"[ERROR] 转录失败: {str(e)}")
            import traceback
            traceback.print_exc()
            raise Exception(f"转录失败: {str(e)}")

        elapsed_time = time.time() - start_time
        if progress_callback:
            progress_callback(80, f"转录完成（耗时{elapsed_time:.1f}秒），正在生成字幕...")

        # 3. 生成输出
        outputs = {}

        # 纯文本
        if 'txt' in output_formats:
            text = result['text'].strip()
            text = traditional_to_simplified(text)  # 繁体转简体
            text = add_punctuation(text)  # 添加标点符号
            outputs['text'] = text

        # SRT 字幕
        if 'srt' in output_formats:
            srt_content = self._generate_srt(result)
            srt_content = traditional_to_simplified(srt_content)  # 繁体转简体
            srt_content = add_punctuation(srt_content)  # 添加标点符号
            srt_path = self._save_output(srt_content, 'srt')
            outputs['srt_path'] = srt_path

        # VTT 字幕
        if 'vtt' in output_formats:
            vtt_content = self._generate_vtt(result)
            vtt_content = traditional_to_simplified(vtt_content)  # 繁体转简体
            vtt_content = add_punctuation(vtt_content)  # 添加标点符号
            vtt_path = self._save_output(vtt_content, 'vtt')
            outputs['vtt_path'] = vtt_path

        if progress_callback:
            progress_callback(100, "处理完成！")

        return outputs

    def _generate_srt(self, result):
        """生成 SRT 格式字幕"""
        srt_content = []
        for i, segment in enumerate(result['segments'], 1):
            start_time = self._format_srt_time(segment['start'])
            end_time = self._format_srt_time(segment['end'])
            text = segment['text'].strip()

            srt_content.append(f"{i}")
            srt_content.append(f"{start_time} --> {end_time}")
            srt_content.append(text)
            srt_content.append("")  # 空行

        return "\n".join(srt_content)

    def _format_srt_time(self, seconds):
        """格式化 SRT 时间戳：00:00:00,000"""
        td = timedelta(seconds=seconds)
        hours, remainder = divmod(td.seconds, 3600)
        minutes, seconds = divmod(remainder, 60)
        milliseconds = td.microseconds // 1000
        return f"{hours:02d}:{minutes:02d}:{seconds:02d},{milliseconds:03d}"

    def _generate_vtt(self, result):
        """生成 VTT 格式字幕"""
        vtt_content = ["WEBVTT", ""]
        for segment in result['segments']:
            start_time = self._format_vtt_time(segment['start'])
            end_time = self._format_vtt_time(segment['end'])
            text = segment['text'].strip()

            vtt_content.append(f"{start_time} --> {end_time}")
            vtt_content.append(text)
            vtt_content.append("")

        return "\n".join(vtt_content)

    def _format_vtt_time(self, seconds):
        """格式化 VTT 时间戳：00:00:00.000"""
        td = timedelta(seconds=seconds)
        hours, remainder = divmod(td.seconds, 3600)
        minutes, seconds = divmod(remainder, 60)
        milliseconds = td.microseconds // 1000
        return f"{hours:02d}:{minutes:02d}:{seconds:02d}.{milliseconds:03d}"

    def _save_output(self, content, format_type):
        """保存输出文件"""
        # 使用绝对路径
        import time

        # 获取项目根目录（whisper_service.py所在目录）
        service_dir = os.path.dirname(os.path.abspath(__file__))

        # 构建输出目录路径
        output_dir = os.path.join(service_dir, '../../temp/whisper', format_type)
        output_dir = os.path.abspath(output_dir)

        # 确保目录存在
        os.makedirs(output_dir, exist_ok=True)

        # 生成文件名
        timestamp = int(time.time())
        filename = f'transcript_{timestamp}.{format_type}'
        filepath = os.path.join(output_dir, filename)

        print(f"[DEBUG] 保存文件: {filepath}")

        # 保存文件
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)

        # 返回相对路径（用于下载）
        return filepath

    @classmethod
    def preload_models(cls, model_sizes=['base']):
        """预加载常用模型（应用启动时调用）"""
        with cls._cache_lock:
            for size in model_sizes:
                if size not in cls._model_cache:
                    print(f"⏳ 预加载 Whisper 模型：{size}")
                    try:
                        cls._model_cache[size] = whisper.load_model(size)
                        print(f"✅ 模型 {size} 加载完成")
                    except Exception as e:
                        print(f"❌ 模型 {size} 加载失败：{e}")
