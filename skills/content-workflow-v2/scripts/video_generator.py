#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
视频自动生成脚本
将文本脚本转换为完整的视频（音频 + 画面 + 字幕）
"""

import os
import sys
import argparse
from pathlib import Path
import json

# 修复 StepFun Python 环境的路径问题
if 'StepFun' in sys.executable:
    site_packages = os.path.join(os.path.dirname(sys.executable), 'Lib', 'site-packages')
    if site_packages not in sys.path:
        sys.path.append(site_packages)

def check_dependencies():
    """检查并提示安装依赖"""
    missing = []
    
    # 检查 moviepy
    try:
        import moviepy
    except ImportError:
        missing.append("moviepy")
    
    # 检查 edge-tts
    try:
        import edge_tts
    except ImportError:
        missing.append("edge-tts")
    
    # 检查 pillow
    try:
        from PIL import Image
    except ImportError:
        missing.append("pillow")
    
    if missing:
        print("=" * 60)
        print("缺少以下依赖:")
        print("=" * 60)
        for pkg in missing:
            print(f"  - {pkg}")
        print("\n请运行以下命令安装:")
        print(f"\npip install {' '.join(missing)}")
        print("\n或使用国内镜像:")
        print(f"\npip install {' '.join(missing)} -i https://pypi.tuna.tsinghua.edu.cn/simple")
        print("=" * 60)
        return False
    
    return True

def parse_script(script_file):
    """
    解析视频脚本，提取时间轴、文本、画面提示
    
    返回:
        segments: 列表，每个元素包含 {start, end, text, visual}
    """
    with open(script_file, 'r', encoding='utf-8') as f:
        content = f.read()
    
    segments = []
    current_segment = {}
    
    lines = content.split('\n')
    for line in lines:
        line = line.strip()
        
        # 解析时间轴 (0:00-1:30)
        import re
        time_match = re.search(r'\((\d+):(\d+)-(\d+):(\d+)\)', line)
        if time_match:
            start_min, start_sec, end_min, end_sec = map(int, time_match.groups())
            current_segment['start'] = start_min * 60 + start_sec
            current_segment['end'] = end_min * 60 + end_sec
        
        # 解析画面提示 [画面：xxx]
        visual_match = re.search(r'\[画面[：:]\s*([^\]]+)\]', line)
        if visual_match:
            current_segment['visual'] = visual_match.group(1)
        
        # 解析语气标记 [语气：xxx]
        tone_match = re.search(r'\[语气[：:]\s*([^\]]+)\]', line)
        if tone_match:
            current_segment['tone'] = tone_match.group(1)
        
        # 普通文本
        if line and not line.startswith('#') and not line.startswith('**[') and not time_match:
            if 'text' not in current_segment:
                current_segment['text'] = []
            current_segment['text'].append(line)
        
        # 段落结束
        if not line and current_segment:
            if 'text' in current_segment:
                current_segment['text'] = '\n'.join(current_segment['text'])
                segments.append(current_segment.copy())
                current_segment = {}
    
    return segments

def generate_audio(text, output_file, voice="zh-CN-XiaoxiaoNeural", rate="-10%"):
    """使用 Edge TTS 生成音频"""
    import asyncio
    import edge_tts
    
    async def generate():
        communicate = edge_tts.Communicate(text, voice, rate=rate)
        await communicate.save(output_file)
    
    print(f"正在生成音频: {output_file}")
    asyncio.run(generate())
    return output_file

def create_background_image(width=1920, height=1080, color=(30, 30, 40), output_file="bg.png"):
    """创建纯色背景图片"""
    from PIL import Image, ImageDraw
    
    img = Image.new('RGB', (width, height), color)
    img.save(output_file)
    return output_file

def create_text_clip(text, duration, width=1920, height=1080, fontsize=60):
    """创建文字片段"""
    from moviepy.editor import TextClip
    
    # 处理长文本，自动换行
    words = text.split()
    lines = []
    current_line = []
    max_chars_per_line = 30
    
    for word in words:
        current_line.append(word)
        if len(' '.join(current_line)) > max_chars_per_line:
            lines.append(' '.join(current_line[:-1]))
            current_line = [word]
    
    if current_line:
        lines.append(' '.join(current_line))
    
    text = '\n'.join(lines)
    
    clip = TextClip(
        text,
        fontsize=fontsize,
        color='white',
        font='Arial',  # Windows 默认字体
        method='caption',
        size=(width * 0.8, None),
        align='center'
    )
    
    clip = clip.set_duration(duration)
    clip = clip.set_position('center')
    
    return clip

def generate_video_simple(script_file, output_file, voice="zh-CN-XiaoxiaoNeural", rate="-10%"):
    """
    生成简单视频（纯色背景 + 文字 + 音频）
    
    适合快速生成，无需复杂画面
    """
    from moviepy.editor import VideoClip, AudioFileClip, CompositeVideoClip, concatenate_videoclips
    from PIL import Image, ImageDraw, ImageFont
    import numpy as np
    
    print("\n" + "=" * 60)
    print("视频生成方案：简单模式")
    print("=" * 60)
    print("包含：纯色背景 + 文字字幕 + 配音")
    print("=" * 60)
    
    # 1. 解析脚本
    print("\n[1/5] 解析脚本...")
    segments = parse_script(script_file)
    print(f"共 {len(segments)} 个段落")
    
    # 2. 生成完整音频
    print("\n[2/5] 生成音频...")
    full_text = '\n\n'.join([seg.get('text', '') for seg in segments if seg.get('text')])
    audio_file = "temp_audio.mp3"
    generate_audio(full_text, audio_file, voice, rate)
    
    # 3. 创建背景
    print("\n[3/5] 创建背景...")
    bg_file = "temp_bg.png"
    create_background_image(output_file=bg_file)
    
    # 4. 创建视频片段
    print("\n[4/5] 创建视频片段...")
    
    from moviepy.editor import ImageClip
    
    # 加载音频获取总时长
    audio = AudioFileClip(audio_file)
    total_duration = audio.duration
    
    # 创建背景视频
    bg_clip = ImageClip(bg_file).set_duration(total_duration)
    
    # 创建字幕片段（简化版：显示完整文本）
    # 注意：这里简化处理，实际应该按时间轴分段
    try:
        text_clip = create_text_clip(full_text[:200] + "...", total_duration)  # 只显示前200字
        video = CompositeVideoClip([bg_clip, text_clip])
    except Exception as e:
        print(f"警告：文字渲染失败 ({e})，使用纯背景")
        video = bg_clip
    
    # 5. 合成最终视频
    print("\n[5/5] 合成视频...")
    video = video.set_audio(audio)
    video.write_videofile(
        output_file,
        fps=24,
        codec='libx264',
        audio_codec='aac',
        temp_audiofile='temp-audio.m4a',
        remove_temp=True
    )
    
    # 清理临时文件
    if os.path.exists(audio_file):
        os.remove(audio_file)
    if os.path.exists(bg_file):
        os.remove(bg_file)
    
    print(f"\n视频生成完成: {output_file}")
    return output_file

def generate_video_advanced(script_file, output_file, assets_dir=None):
    """
    生成高级视频（自定义画面 + 字幕 + 音频）
    
    需要提供素材文件夹
    """
    print("\n" + "=" * 60)
    print("视频生成方案：高级模式")
    print("=" * 60)
    print("包含：自定义画面 + 动态字幕 + 配音")
    print("=" * 60)
    print("\n此功能需要提供素材文件夹")
    print("暂未实现，请使用简单模式")
    
    return None

def main():
    parser = argparse.ArgumentParser(description='自动生成视频文件')
    parser.add_argument('script_file', help='视频脚本文件路径')
    parser.add_argument('-o', '--output', help='输出视频文件路径（默认：与脚本同名的 .mp4）')
    parser.add_argument('-m', '--mode', choices=['simple', 'advanced'], default='simple',
                        help='生成模式 (simple: 简单模式, advanced: 高级模式)')
    parser.add_argument('-v', '--voice', default='zh-CN-XiaoxiaoNeural', help='语音选项')
    parser.add_argument('-r', '--rate', default='-10%', help='语速调整')
    parser.add_argument('--assets', help='素材文件夹路径（高级模式）')
    
    args = parser.parse_args()
    
    # 检查依赖
    if not check_dependencies():
        return
    
    # 检查脚本文件
    if not os.path.exists(args.script_file):
        print(f"错误: 文件不存在: {args.script_file}")
        return
    
    # 确定输出文件
    if args.output:
        output_file = args.output
    else:
        output_file = str(Path(args.script_file).with_suffix('.mp4'))
    
    # 生成视频
    try:
        if args.mode == 'simple':
            generate_video_simple(args.script_file, output_file, args.voice, args.rate)
        elif args.mode == 'advanced':
            if not args.assets:
                print("错误: 高级模式需要提供 --assets 参数")
                return
            generate_video_advanced(args.script_file, output_file, args.assets)
        
        print(f"\n成功！视频文件已保存到: {output_file}")
        
    except Exception as e:
        print(f"生成失败: {e}")
        import traceback
        traceback.print_exc()

if __name__ == '__main__':
    main()
