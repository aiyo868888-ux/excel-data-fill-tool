#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
深度工作有声书生成器
使用 edge-tts 将文本转换为专业讲座型有声书
"""

import edge_tts
import asyncio
import subprocess
from pathlib import Path
from typing import List, Dict
import re

# 配置参数
VOICE = 'zh-CN-YunjianNeural'  # 云健 - 男声，专业讲座风格
RATE = '+0%'  # 标准语速
PITCH = '+0Hz'  # 中性音调（edge-tts使用Hz格式）
VOLUME = '+0%'  # 标准音量
CHAPTER_PAUSE = 2  # 章节间停顿秒数

# 章节定义
CHAPTERS = [
    {"title": "00_开篇", "start_marker": "开篇\n", "end_marker": "\n第一章"},
    {"title": "01_第一章", "start_marker": "\n第一章", "end_marker": "\n第二章"},
    {"title": "02_第二章", "start_marker": "\n第二章", "end_marker": "\n第三章"},
    {"title": "03_第三章", "start_marker": "\n第三章", "end_marker": "\n第四章"},
    {"title": "04_第四章", "start_marker": "\n第四章", "end_marker": "\n结尾"},
    {"title": "05_结尾", "start_marker": "\n结尾", "end_marker": None},
]


def read_full_text(file_path: str) -> str:
    """读取完整文本文件"""
    with open(file_path, 'r', encoding='utf-8') as f:
        return f.read()


def extract_chapters(full_text: str) -> List[Dict[str, str]]:
    """从完整文本中提取各个章节"""
    chapters = []

    for i, chapter_def in enumerate(CHAPTERS):
        start_idx = full_text.find(chapter_def["start_marker"])

        if start_idx == -1:
            print(f"[WARN] 找不到章节 {chapter_def['title']} 的开始标记")
            continue

        # 调整开始位置（跳过标记本身）
        if i == 0:
            # 第一章保留标题
            start_idx = start_idx
        else:
            # 其他章节跳过标题行
            start_idx = start_idx + len(chapter_def["start_marker"])

        # 查找结束位置
        if chapter_def["end_marker"] is None:
            # 最后一章，读到文件末尾
            end_idx = len(full_text)
        else:
            end_idx = full_text.find(chapter_def["end_marker"], start_idx)
            if end_idx == -1:
                print(f"[WARN] 找不到章节 {chapter_def['title']} 的结束标记，使用文件末尾")
                end_idx = len(full_text)

        # 提取章节文本
        chapter_text = full_text[start_idx:end_idx].strip()

        # 清理文本（移除多余的空行）
        chapter_text = re.sub(r'\n{3,}', '\n\n', chapter_text)

        chapters.append({
            "title": chapter_def["title"],
            "text": chapter_text
        })

        print(f"[OK] 提取章节: {chapter_def['title']} ({len(chapter_text)} 字符)")

    return chapters


async def generate_chapter_audio(chapter: Dict[str, str], output_dir: Path) -> Path:
    """生成单个章节的音频文件"""
    output_file = output_dir / f"chapter_{chapter['title']}.mp3"

    print(f"正在生成: {chapter['title']}...")

    try:
        communicate = edge_tts.Communicate(
            chapter['text'],
            voice=VOICE,
            rate=RATE,
            pitch=PITCH,
            volume=VOLUME
        )

        await communicate.save(str(output_file))
        print(f"[OK] 完成: {chapter['title']} -> {output_file.name}")

        return output_file

    except Exception as e:
        print(f"[FAIL] 生成失败: {chapter['title']} - {e}")
        raise


def generate_silence(duration: int, output_file: Path) -> Path:
    """使用 FFmpeg 生成静音文件"""
    print(f"生成 {duration} 秒静音...")

    try:
        cmd = [
            'ffmpeg',
            '-f', 'lavfi',
            '-i', f'anullsrc=r=44100:cl=mono',
            '-t', str(duration),
            '-q:a', '9',
            '-y',  # 覆盖已存在文件
            str(output_file)
        ]

        subprocess.run(cmd, check=True, capture_output=True)
        print(f"[OK] 静音文件生成完成: {output_file.name}")
        return output_file

    except subprocess.CalledProcessError as e:
        print(f"[FAIL] 静音文件生成失败: {e}")
        raise


def merge_chapters(chapter_files: List[Path], silence_file: Path, output_file: Path):
    """使用 FFmpeg 合并所有章节"""
    print(f"\n开始合并章节...")

    # 创建合并列表（章节+静音交替）
    concat_list = []

    for i, chapter_file in enumerate(chapter_files):
        # 添加章节
        concat_list.append(str(chapter_file))

        # 在章节间添加静音（最后一个章节后不加）
        if i < len(chapter_files) - 1:
            concat_list.append(str(silence_file))

    # 创建 concat 列表文件
    concat_file = output_file.parent / "concat_list.txt"
    with open(concat_file, 'w', encoding='utf-8') as f:
        for file_path in concat_list:
            # FFmpeg concat 在 Windows 上需要相对路径或简单转义
            # 使用相对路径更可靠
            relative_path = Path(file_path).relative_to(output_file.parent)
            # Windows 路径需要转义反斜杠和冒号
            safe_path = str(relative_path).replace('\\', '/')
            f.write(f"file '{safe_path}'\n")

    try:
        cmd = [
            'ffmpeg',
            '-f', 'concat',
            '-safe', '0',
            '-i', str(concat_file),
            '-c', 'copy',
            '-y',
            str(output_file)
        ]

        print(f"执行合并命令（共 {len(chapter_files)} 个章节）...")
        subprocess.run(cmd, check=True, capture_output=True)
        print(f"[OK] 合并完成: {output_file.name}")

        # 清理临时文件
        concat_file.unlink()

    except subprocess.CalledProcessError as e:
        print(f"[FAIL] 合并失败: {e}")
        print(f"错误输出: {e.stderr.decode('utf-8', errors='ignore')}")
        raise


def get_audio_duration(file_path: Path) -> float:
    """获取音频文件时长（秒）"""
    try:
        cmd = [
            'ffprobe',
            '-v', 'error',
            '-show_entries', 'format=duration',
            '-of', 'default=noprint_wrappers=1:nokey=1',
            str(file_path)
        ]

        result = subprocess.run(cmd, check=True, capture_output=True, text=True)
        duration = float(result.stdout.strip())
        return duration

    except Exception as e:
        print(f"[WARN] 无法获取音频时长: {e}")
        return 0


async def main():
    """主函数"""
    print("=" * 60)
    print("深度工作有声书生成器")
    print("=" * 60)

    # 设置路径
    script_dir = Path(__file__).parent
    text_file = Path("d:/claude code -11/project/yinian-clipboard-manager/temp/intellectual_outsourcing_article.txt")
    chapters_dir = script_dir / "chapters"
    output_file = script_dir / "intellectual_outsourcing_audiobook.mp3"

    # 创建章节目录
    chapters_dir.mkdir(exist_ok=True)

    # 检查文本文件
    if not text_file.exists():
        print(f"错误：找不到文本文件 {text_file}")
        return

    # 步骤1：读取文本
    print(f"\n步骤1：读取文本文件...")
    full_text = read_full_text(str(text_file))
    print(f"[OK] 文本文件读取完成 (总字符数: {len(full_text)})")

    # 步骤2：提取章节
    print(f"\n步骤2：提取章节...")
    chapters = extract_chapters(full_text)
    print(f"[OK] 共提取 {len(chapters)} 个章节")

    if not chapters:
        print("错误：没有找到任何章节")
        return

    # 步骤3：生成章节音频
    print(f"\n步骤3：生成章节音频...")
    print(f"使用语音: {VOICE}")
    print(f"语速: {RATE}, 音调: {PITCH}, 音量: {VOLUME}")

    chapter_files = []
    for chapter in chapters:
        audio_file = await generate_chapter_audio(chapter, chapters_dir)
        chapter_files.append(audio_file)

    # 步骤4：生成静音文件
    print(f"\n步骤4：生成章节间隔静音...")
    silence_file = chapters_dir / "silence.mp3"
    generate_silence(CHAPTER_PAUSE, silence_file)

    # 步骤5：合并章节
    print(f"\n步骤5：合并所有章节...")
    merge_chapters(chapter_files, silence_file, output_file)

    # 步骤6：输出统计信息
    print(f"\n" + "=" * 60)
    print("生成完成！")
    print("=" * 60)

    total_duration = get_audio_duration(output_file)
    print(f"\n最终文件: {output_file.name}")
    print(f"总时长: {total_duration / 60:.1f} 分钟")
    print(f"章节数: {len(chapter_files)}")
    print(f"\n各章节时长:")
    for i, chapter_file in enumerate(chapter_files):
        duration = get_audio_duration(chapter_file)
        print(f"  {chapters[i]['title']}: {duration / 60:.1f} 分钟")

    print(f"\n章节文件保存在: {chapters_dir}")
    print(f"完整有声书: {output_file}")
    print("\n[OK] 所有任务完成!")


if __name__ == "__main__":
    asyncio.run(main())
