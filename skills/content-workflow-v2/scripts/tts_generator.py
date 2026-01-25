#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
音频自动生成脚本
支持多种 TTS 服务：Edge TTS (免费)、Azure Speech、OpenAI TTS
"""

import os
import sys
import argparse
from pathlib import Path
import re

# 修复 StepFun Python 环境的路径问题
if 'StepFun' in sys.executable:
    site_packages = os.path.join(os.path.dirname(sys.executable), 'Lib', 'site-packages')
    if site_packages not in sys.path:
        sys.path.append(site_packages)

def install_dependencies():
    """检查并安装依赖"""
    try:
        import edge_tts
        return True
    except ImportError:
        print("=" * 60)
        print("检测到缺少 edge-tts 依赖")
        print("=" * 60)
        print("\n请手动安装依赖，运行以下命令：\n")
        print("pip install edge-tts")
        print("\n或者：\n")
        print("python -m pip install edge-tts")
        print("\n如果上述命令失败，请使用国内镜像：\n")
        print("pip install edge-tts -i https://pypi.tuna.tsinghua.edu.cn/simple")
        print("\n" + "=" * 60)
        sys.exit(1)

def clean_text_for_tts(text):
    """
    清理文本，移除所有标记，优化为适合 TTS 的格式
    
    参数:
        text: 原始文本
    
    返回:
        cleaned_text: 清理后的文本
    """
    # 移除 Markdown 标题标记
    text = re.sub(r'^#+\s+', '', text, flags=re.MULTILINE)
    
    # 移除代码块标记
    text = re.sub(r'```[^`]*```', '', text, flags=re.DOTALL)
    text = re.sub(r'`[^`]+`', '', text)
    
    # 移除链接
    text = re.sub(r'\[([^\]]+)\]\([^\)]+\)', r'\1', text)
    
    # 移除粗体、斜体标记
    text = re.sub(r'\*\*([^\*]+)\*\*', r'\1', text)
    text = re.sub(r'\*([^\*]+)\*', r'\1', text)
    text = re.sub(r'__([^_]+)__', r'\1', text)
    text = re.sub(r'_([^_]+)_', r'\1', text)
    
    # 移除分隔线
    text = re.sub(r'^[-=*]{3,}$', '', text, flags=re.MULTILINE)
    
    # 移除 HTML 标签
    text = re.sub(r'<[^>]+>', '', text)
    
    # 移除特殊标记（如 [语气：xxx]、[停顿 X 秒] 等）
    text = re.sub(r'\[.*?\]', '', text)
    text = re.sub(r'\*\*\[.*?\]\*\*', '', text)
    
    # 移除时间轴标记（如 (0:00-1:30)）
    text = re.sub(r'\(\d+:\d+[-–]\d+:\d+\)', '', text)
    
    # 移除多余的空行（保留段落间的单个空行）
    text = re.sub(r'\n{3,}', '\n\n', text)
    
    # 移除行首行尾空白
    lines = [line.strip() for line in text.split('\n')]
    text = '\n'.join(lines)
    
    # 移除空行
    lines = [line for line in lines if line]
    text = '\n\n'.join(lines)
    
    return text.strip()

def preview_and_confirm_text(text, max_preview_lines=50):
    """
    预览文本并等待用户确认
    
    参数:
        text: 要预览的文本
        max_preview_lines: 最大预览行数
    
    返回:
        confirmed: 是否确认（True/False）
        modified_text: 修改后的文本（如果用户选择编辑）
    """
    lines = text.split('\n')
    total_lines = len(lines)
    
    print("\n" + "=" * 60)
    print("文本预览（清理后）")
    print("=" * 60)
    
    # 显示前 N 行
    preview_lines = min(max_preview_lines, total_lines)
    for i, line in enumerate(lines[:preview_lines], 1):
        print(f"{i:3d} | {line}")
    
    if total_lines > max_preview_lines:
        print(f"\n... (省略 {total_lines - max_preview_lines} 行) ...\n")
    
    print("=" * 60)
    print(f"总行数: {total_lines}")
    print(f"总字数: {len(text)}")
    print("=" * 60)
    
    while True:
        print("\n请选择操作：")
        print("  1. 确认 - 使用此文本生成音频")
        print("  2. 预览完整文本")
        print("  3. 保存到文件并编辑")
        print("  4. 取消")
        
        choice = input("\n请输入选项 (1-4): ").strip()
        
        if choice == '1':
            return True, text
        elif choice == '2':
            print("\n" + "=" * 60)
            print("完整文本")
            print("=" * 60)
            for i, line in enumerate(lines, 1):
                print(f"{i:3d} | {line}")
            print("=" * 60)
        elif choice == '3':
            temp_file = "temp_tts_text.txt"
            with open(temp_file, 'w', encoding='utf-8') as f:
                f.write(text)
            print(f"\n文本已保存到: {temp_file}")
            print("请编辑此文件，完成后按回车继续...")
            input()
            
            # 重新读取文件
            with open(temp_file, 'r', encoding='utf-8') as f:
                modified_text = f.read()
            
            print(f"\n已读取修改后的文本（{len(modified_text)} 字）")
            return True, modified_text
        elif choice == '4':
            return False, None
        else:
            print("无效选项，请重新选择")

def generate_audio_edge(text, output_file, voice="zh-CN-XiaoxiaoNeural", rate="-10%"):
    """
    使用 Edge TTS 生成音频（免费，无需 API 密钥）
    
    参数:
        text: 要转换的文本
        output_file: 输出音频文件路径
        voice: 语音选项
        rate: 语速调整
    """
    import asyncio
    import edge_tts
    
    # 生成音频
    async def generate():
        communicate = edge_tts.Communicate(text, voice, rate=rate)
        await communicate.save(output_file)
    
    print(f"\n正在使用 Edge TTS 生成音频...")
    print(f"语音: {voice}")
    print(f"语速: {rate}")
    print(f"输出: {output_file}")
    
    asyncio.run(generate())
    
    print(f"音频生成完成: {output_file}")
    return output_file

def generate_audio_openai(text, output_file, api_key=None, voice="alloy", model="tts-1"):
    """
    使用 OpenAI TTS 生成音频（需要 API 密钥）
    """
    try:
        from openai import OpenAI
    except ImportError:
        print("正在安装 openai...")
        os.system(f"{sys.executable} -m pip install openai")
        from openai import OpenAI
    
    if not api_key:
        api_key = os.getenv("OPENAI_API_KEY")
        if not api_key:
            raise ValueError("请设置 OPENAI_API_KEY 环境变量或通过参数传入")
    
    print(f"\n正在使用 OpenAI TTS 生成音频...")
    print(f"模型: {model}")
    print(f"语音: {voice}")
    
    client = OpenAI(api_key=api_key)
    
    response = client.audio.speech.create(
        model=model,
        voice=voice,
        input=text
    )
    
    response.stream_to_file(output_file)
    
    print(f"音频生成完成: {output_file}")
    return output_file

def list_available_voices():
    """列出所有可用的语音"""
    import asyncio
    import edge_tts
    
    async def get_voices():
        voices = await edge_tts.list_voices()
        zh_voices = [v for v in voices if v["Locale"].startswith("zh-")]
        
        print("\n可用的中文语音：\n")
        for v in zh_voices:
            print(f"  {v['ShortName']}")
            print(f"    性别: {v['Gender']}")
            print(f"    语言: {v['Locale']}")
            print()
    
    asyncio.run(get_voices())

def main():
    parser = argparse.ArgumentParser(description='自动生成音频文件')
    parser.add_argument('text_file', help='输入文本文件路径')
    parser.add_argument('-o', '--output', help='输出音频文件路径（默认：与文本文件同名的 .mp3）')
    parser.add_argument('-s', '--service', choices=['edge', 'openai'], default='edge',
                        help='TTS 服务 (默认: edge)')
    parser.add_argument('-v', '--voice', help='语音选项')
    parser.add_argument('-r', '--rate', default='-10%', help='语速调整 (仅 Edge TTS，默认: -10%%)')
    parser.add_argument('--list-voices', action='store_true', help='列出所有可用语音')
    parser.add_argument('--api-key', help='API 密钥 (OpenAI TTS)')
    parser.add_argument('--no-confirm', action='store_true', help='跳过确认，直接生成')
    
    args = parser.parse_args()
    
    # 列出语音
    if args.list_voices:
        if args.service == 'edge':
            list_available_voices()
        else:
            print("OpenAI TTS 可用语音: alloy, echo, fable, onyx, nova, shimmer")
        return
    
    # 检查文本文件
    if not os.path.exists(args.text_file):
        print(f"错误: 文件不存在: {args.text_file}")
        return
    
    # 确定输出文件
    if args.output:
        output_file = args.output
    else:
        output_file = str(Path(args.text_file).with_suffix('.mp3'))
    
    # 安装依赖
    if args.service == 'edge':
        install_dependencies()
    
    # 读取并清理文本
    print(f"\n正在读取文件: {args.text_file}")
    with open(args.text_file, 'r', encoding='utf-8') as f:
        original_text = f.read()
    
    print(f"原始文本: {len(original_text)} 字")
    
    # 清理文本
    print("正在清理文本...")
    cleaned_text = clean_text_for_tts(original_text)
    print(f"清理后文本: {len(cleaned_text)} 字")
    
    # 预览并确认（除非使用 --no-confirm）
    if not args.no_confirm:
        confirmed, final_text = preview_and_confirm_text(cleaned_text)
        if not confirmed:
            print("\n已取消生成")
            return
    else:
        final_text = cleaned_text
    
    # 生成音频
    try:
        if args.service == 'edge':
            voice = args.voice or 'zh-CN-XiaoxiaoNeural'
            generate_audio_edge(final_text, output_file, voice, args.rate)
        elif args.service == 'openai':
            voice = args.voice or 'alloy'
            generate_audio_openai(final_text, output_file, args.api_key, voice)
        
        print(f"\n成功！音频文件已保存到: {output_file}")
        
    except Exception as e:
        print(f"生成失败: {e}")
        import traceback
        traceback.print_exc()

if __name__ == '__main__':
    main()
