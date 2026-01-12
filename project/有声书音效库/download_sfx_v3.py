"""
有声书音效批量下载脚本 V3
使用公开的音效下载链接（无需浏览器）
"""

import os
import sys
import requests
from pathlib import Path
from datetime import datetime
import pandas as pd

# 设置UTF-8输出
if sys.platform == 'win32':
    import io
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
    sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8')

# ==================== 配置区 ====================

PROJECT_DIR = Path(__file__).parent

# 使用Pixabay等免费音效库的公开API（无需注册）
# 这里使用示例URL，实际运行时会从Pixabay API获取
SOUND_EFFECTS = [
    {
        "name": "page_turn",
        "category": "01_基础音效/翻页音效",
        "description": "翻页声",
        "keywords": "page turn book paper"
    },
    {
        "name": "book_close",
        "category": "01_基础音效/翻页音效",
        "description": "合书声",
        "keywords": "book close"
    },
    {
        "name": "whoosh_soft",
        "category": "01_基础音效/章节切换",
        "description": "柔和过渡",
        "keywords": "whoosh soft transition"
    },
    {
        "name": "rain_gentle",
        "category": "02_环境氛围/天气氛围",
        "description": "小雨",
        "keywords": "rain gentle light nature"
    },
    {
        "name": "wind_soft",
        "category": "02_环境氛围/天气氛围",
        "description": "微风",
        "keywords": "wind breeze soft"
    },
    {
        "name": "thunder",
        "category": "02_环境氛围/天气氛围",
        "description": "雷声",
        "keywords": "thunder rumble"
    },
    {
        "name": "footsteps",
        "category": "03_人物动作/移动动作",
        "description": "脚步声",
        "keywords": "footsteps walk"
    },
    {
        "name": "door_wood",
        "category": "03_人物动作/移动动作",
        "description": "木门",
        "keywords": "door wood creak"
    },
    {
        "name": "piano_soft",
        "category": "04_背景音乐/文艺情绪",
        "description": "钢琴",
        "keywords": "piano soft emotional"
    },
    {
        "name": "mystery_ambient",
        "category": "04_背景音乐/悬疑情绪",
        "description": "悬疑",
        "keywords": "mystery ambient dark"
    }
]

# 使用Freesound API（需要免费注册获取API key）
# 如果没有API key，请到 https://freesound.org/ 注册
FREESOUND_API_KEY = ""  # 在这里填入您的API密钥

def download_from_freesound(query, category, name):
    """从Freesound下载音效"""
    if not FREESOUND_API_KEY:
        print("   跳过：未设置FREESOUND_API_KEY")
        return None

    # 搜索音效
    search_url = f"https://freesound.org/apiv2/search/text/"
    params = {
        "query": query,
        "token": FREESOUND_API_KEY,
        "fields": "id,name,previews,duration",
        "page_size": 5
    }

    try:
        response = requests.get(search_url, params=params, timeout=10)
        response.raise_for_status()
        data = response.json()

        if not data["results"]:
            print(f"   未找到匹配的音效")
            return None

        # 下载第一个结果
        sound = data["results"][0]
        download_url = sound["previews"]["preview-hq-mp3"]

        print(f"   正在下载: {sound['name']}")
        file_response = requests.get(download_url, stream=True, timeout=30)

        download_dir = PROJECT_DIR / category
        download_dir.mkdir(parents=True, exist_ok=True)

        filename = f"{name}.mp3"
        save_path = download_dir / filename

        with open(save_path, 'wb') as f:
            for chunk in file_response.iter_content(chunk_size=8192):
                f.write(chunk)

        file_size = save_path.stat().st_size / 1024  # KB
        print(f"   成功: {filename} ({file_size:.1f} KB)")

        return {
            "name": name,
            "filename": filename,
            "size": f"{file_size:.1f} KB",
            "duration": sound["duration"],
            "url": download_url
        }

    except Exception as e:
        print(f"   失败: {str(e)}")
        return None

def main():
    print("="*60)
    print("有声书音效批量下载工具 V3")
    print("="*60)
    print(f"项目目录: {PROJECT_DIR}")
    print(f"准备下载 {len(SOUND_EFFECTS)} 个音效")
    print()

    if not FREESOUND_API_KEY:
        print("="*60)
        print("重要提示：")
        print("="*60)
        print("本脚本使用Freesound API下载音效")
        print("请先获取免费API密钥：")
        print()
        print("1. 访问 https://freesound.org/")
        print("2. 注册免费账号")
        print("3. 进入 Settings > API Keys")
        print("4. 创建新的API密钥")
        print("5. 将密钥粘贴到本脚本的 FREESOUND_API_KEY 变量中")
        print()
        print("获取密钥后，重新运行本脚本即可")
        print("="*60)
        return

    download_records = []
    success_count = 0
    failed_count = 0

    for idx, sfx in enumerate(SOUND_EFFECTS, 1):
        print(f"\n[{idx}/{len(SOUND_EFFECTS)}] {sfx['name']}")
        print(f"   关键词: {sfx['keywords']}")

        result = download_from_freesound(
            query=sfx['keywords'],
            category=sfx['category'],
            name=sfx['name']
        )

        if result:
            success_count += 1
            download_records.append({
                '序号': idx,
                '音效名称': sfx['name'],
                '分类': sfx['category'],
                '状态': '成功',
                '文件名': result['filename'],
                '文件大小': result['size'],
                '时长(秒)': f"{result['duration']:.1f}",
                '下载时间': datetime.now().strftime('%Y-%m-%d %H:%M:%S')
            })
        else:
            failed_count += 1
            download_records.append({
                '序号': idx,
                '音效名称': sfx['name'],
                '分类': sfx['category'],
                '状态': '失败',
                '文件名': '-',
                '文件大小': '-',
                '时长(秒)': '-',
                '下载时间': datetime.now().strftime('%Y-%m-%d %H:%M:%S')
            })

    # 生成清单
    print("\n" + "="*60)
    print("生成清单...")

    df = pd.DataFrame(download_records)
    excel_path = PROJECT_DIR / "音效清单.xlsx"
    df.to_excel(excel_path, index=False, sheet_name='下载记录')
    print(f"已保存: {excel_path}")

    # 统计
    print("\n" + "="*60)
    print("下载统计")
    print("="*60)
    print(f"成功: {success_count} 个")
    print(f"失败: {failed_count} 个")
    if success_count + failed_count > 0:
        print(f"成功率: {success_count/(success_count+failed_count)*100:.1f}%")
    print("="*60)

    # 生成报告
    markdown_path = PROJECT_DIR / "测试报告.md"
    with open(markdown_path, 'w', encoding='utf-8') as f:
        f.write("# 有声书音效下载测试报告\n\n")
        f.write(f"**下载时间**: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n\n")
        f.write("## 下载统计\n\n")
        f.write(f"- 成功下载: {success_count} 个\n")
        f.write(f"- 下载失败: {failed_count} 个\n")
        f.write(f"- 成功率: {success_count/(success_count+failed_count)*100:.1f}%\n\n" if success_count + failed_count > 0 else "\n")

        f.write("## 下载清单\n\n")
        f.write("| 序号 | 音效名称 | 分类 | 状态 | 文件大小 | 时长 |\n")
        f.write("|------|----------|------|------|----------|------|\n")
        for record in download_records:
            status = "成功" if record['状态'] == '成功' else "失败"
            f.write(f"| {record['序号']} | {record['音效名称']} | {record['分类']} | {status} | {record['文件大小']} | {record['时长(秒)']} |\n")

    print(f"测试报告: {markdown_path}")
    print("\n所有文件保存在:", PROJECT_DIR)

if __name__ == "__main__":
    main()
