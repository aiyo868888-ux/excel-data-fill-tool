"""
有声书音效批量下载脚本 V2
改进版：直接访问分类页面，避免重复下载
"""

import time
import os
import sys
from datetime import datetime
from pathlib import Path

# 设置UTF-8输出（Windows兼容）
if sys.platform == 'win32':
    import io
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
    sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8')

from playwright.sync_api import sync_playwright
import pandas as pd

# ==================== 配置区 ====================

# 项目根目录
PROJECT_DIR = Path(__file__).parent

# 音效配置列表（直接指定Mixkit分类页面URL）
SOUND_EFFECTS = [
    # Mixkit免费音效直接URL（10个不同音效）
    {
        "name": "page_turn",
        "url": "https://mixkit.co/free-sound-effects/book-papers-3985/",
        "category": "01_基础音效/翻页音效",
        "description": "翻页声"
    },
    {
        "name": "book_close",
        "url": "https://mixkit.co/free-sound-effects/book-close-3987/",
        "category": "01_基础音效/翻页音效",
        "description": "合书声"
    },
    {
        "name": "whoosh_soft",
        "url": "https://mixkit.co/free-sound-effects/whoosh-soft-891/",
        "category": "01_基础音效/章节切换",
        "description": "柔和过渡"
    },
    {
        "name": "rain_gentle",
        "url": "https://mixkit.co/free-sound-effects/rain-light-nature-ambience-1236/",
        "category": "02_环境氛围/天气氛围",
        "description": "小雨"
    },
    {
        "name": "wind_soft",
        "url": "https://mixkit.co/free-sound-effects/wind-soft-ambience-1238/",
        "category": "02_环境氛围/天气氛围",
        "description": "微风"
    },
    {
        "name": "thunder",
        "url": "https://mixkit.co/free-sound-effects/thunder-1343/",
        "category": "02_环境氛围/天气氛围",
        "description": "雷声"
    },
    {
        "name": "footsteps",
        "url": "https://mixkit.co/free-sound-effects/footsteps-gravel-1587/",
        "category": "03_人物动作/移动动作",
        "description": "脚步声"
    },
    {
        "name": "door_wood",
        "url": "https://mixkit.co/free-sound-effects/door-wood-close-1554/",
        "category": "03_人物动作/移动动作",
        "description": "木门关闭"
    },
    {
        "name": "piano_emotional",
        "url": "https://mixkit.co/free-sound-effects/piano-emotional-735/",
        "category": "04_背景音乐/文艺情绪",
        "description": "情感钢琴"
    },
    {
        "name": "mystery_ambient",
        "url": "https://mixkit.co/free-sound-effects/mystery-ambient-2917/",
        "category": "04_背景音乐/悬疑情绪",
        "description": "悬疑氛围"
    }
]

# ==================== 核心功能 ====================

def download_sound_effects():
    """下载音效主函数"""

    print("="*60)
    print("有声书音效批量下载工具 V2")
    print("="*60)
    print(f"项目目录: {PROJECT_DIR}")
    print(f"准备下载 {len(SOUND_EFFECTS)} 个不同音效")
    print("="*60)
    print()

    # 下载记录
    download_records = []
    success_count = 0
    failed_count = 0

    with sync_playwright() as p:
        # 启动浏览器（headless模式）
        print("启动浏览器...")
        browser = p.chromium.launch(headless=True)
        context = browser.new_context()
        page = context.new_page()

        try:
            # 遍历音效列表
            for idx, sfx in enumerate(SOUND_EFFECTS, 1):
                print(f"\n[{idx}/{len(SOUND_EFFECTS)}] 正在下载: {sfx['name']}")
                print(f"   URL: {sfx['url']}")
                print(f"   分类: {sfx['category']}")

                try:
                    # 直接访问音效详情页
                    print("   访问音效页面...")
                    page.goto(sfx['url'], timeout=30000)
                    page.wait_for_load_state("networkidle")
                    time.sleep(2)

                    # 查找下载按钮
                    download_button_selectors = [
                        'button:has-text("Download")',
                        'a:has-text("Download")',
                        '[class*="download"]',
                        'button[aria-label*="download" i]',
                        'a:has-text("Free SFX")'
                    ]

                    download_button = None
                    for selector in download_button_selectors:
                        try:
                            btn = page.locator(selector).first
                            if btn.is_visible(timeout=2000):
                                download_button = btn
                                print(f"   找到下载按钮: {selector}")
                                break
                        except:
                            continue

                    if not download_button:
                        print(f"   未找到下载按钮，跳过")
                        failed_count += 1
                        download_records.append({
                            '序号': idx,
                            '音效名称': sfx['name'],
                            'URL': sfx['url'],
                            '分类': sfx['category'],
                            '状态': '失败',
                            '原因': '未找到下载按钮',
                            '下载时间': datetime.now().strftime('%Y-%m-%d %H:%M:%S')
                        })
                        continue

                    # 设置下载路径
                    download_dir = PROJECT_DIR / sfx['category']
                    download_dir.mkdir(parents=True, exist_ok=True)

                    # 点击下载并等待下载完成
                    print("   开始下载...")

                    with page.expect_download(timeout=30000) as download_info:
                        download_button.click()

                    download = download_info.value
                    filename = f"{sfx['name']}.mp3"
                    save_path = download_dir / filename
                    download.save_as(save_path)

                    file_size = save_path.stat().st_size / 1024  # KB

                    print(f"   下载成功: {filename} ({file_size:.1f} KB)")
                    success_count += 1

                    download_records.append({
                        '序号': idx,
                        '音效名称': sfx['name'],
                        'URL': sfx['url'],
                        '分类': sfx['category'],
                        '状态': '成功',
                        '文件名': filename,
                        '文件大小': f"{file_size:.1f} KB",
                        '下载时间': datetime.now().strftime('%Y-%m-%d %H:%M:%S')
                    })

                    # 等待一下，避免请求过快
                    time.sleep(2)

                except Exception as e:
                    print(f"   下载失败: {str(e)}")
                    failed_count += 1
                    download_records.append({
                        '序号': idx,
                        '音效名称': sfx['name'],
                        'URL': sfx['url'],
                        '分类': sfx['category'],
                        '状态': '失败',
                        '原因': str(e),
                        '下载时间': datetime.now().strftime('%Y-%m-%d %H:%M:%S')
                    })

        finally:
            browser.close()

    # 生成下载清单
    print("\n" + "="*60)
    print("生成下载清单...")
    df = pd.DataFrame(download_records)
    excel_path = PROJECT_DIR / "音效清单.xlsx"
    df.to_excel(excel_path, index=False, sheet_name='下载记录')
    print(f"   清单已保存: {excel_path}")

    # 生成统计报告
    print("\n" + "="*60)
    print("下载统计")
    print("="*60)
    print(f"成功: {success_count} 个")
    print(f"失败: {failed_count} 个")
    if success_count + failed_count > 0:
        print(f"成功率: {success_count/(success_count+failed_count)*100:.1f}%")
    print("="*60)

    # 生成Markdown报告
    markdown_path = PROJECT_DIR / "测试报告.md"
    with open(markdown_path, 'w', encoding='utf-8') as f:
        f.write("# 有声书音效下载测试报告\n\n")
        f.write(f"**下载时间**: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n\n")
        f.write("## 下载统计\n\n")
        f.write(f"- 成功下载: {success_count} 个\n")
        f.write(f"- 下载失败: {failed_count} 个\n")
        if success_count + failed_count > 0:
            f.write(f"- 成功率: {success_count/(success_count+failed_count)*100:.1f}%\n\n")

        f.write("## 下载清单\n\n")
        f.write("| 序号 | 音效名称 | 分类 | 状态 | 文件大小 |\n")
        f.write("|------|----------|------|------|----------|\n")
        for record in download_records:
            status_emoji = "成功" if record['状态'] == '成功' else "失败"
            size = record.get('文件大小', '-')
            f.write(f"| {record['序号']} | {record['音效名称']} | {record['分类']} | {status_emoji} | {size} |\n")

        if failed_count > 0:
            f.write("\n## 失败项目\n\n")
            for record in download_records:
                if record['状态'] == '失败':
                    f.write(f"- **{record['音效名称']}**: {record.get('原因', '未知错误')}\n")

        f.write("\n## 建议\n\n")
        if success_count == len(SOUND_EFFECTS):
            f.write("所有音效下载成功！\n\n")
            f.write("下一步建议：\n")
            f.write("1. 试听音效质量\n")
            f.write("2. 验证分类是否合理\n")
            f.write("3. 如满意，可以扩展下载更多音效\n")
        elif success_count > len(SOUND_EFFECTS) / 2:
            f.write("部分音效下载成功。\n\n")
            f.write("建议：\n")
            f.write("1. 手动下载失败的音效\n")
            f.write("2. 检查网络连接\n")
        else:
            f.write("下载成功率较低。\n\n")
            f.write("建议：\n")
            f.write("1. 检查网络连接\n")
            f.write("2. 尝试手动下载\n")
            f.write("3. 考虑使用其他音效源\n")

    print(f"   测试报告已保存: {markdown_path}")

    print("\n" + "="*60)
    print("下载任务完成！")
    print("="*60)
    print(f"所有文件保存在: {PROJECT_DIR}")
    print("="*60)


if __name__ == "__main__":
    download_sound_effects()
