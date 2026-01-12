"""
有声书音效批量下载脚本
从Mixkit免费音效库下载音效并自动分类
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

# 音效配置列表（10个测试音效）
SOUND_EFFECTS = [
    # 基础音效（3个）
    {
        "name": "page_turn_gentle",
        "keywords": "page turn book",
        "category": "01_基础音效/翻页音效",
        "description": "温柔翻页"
    },
    {
        "name": "book_close",
        "keywords": "book close",
        "category": "01_基础音效/翻页音效",
        "description": "合书声"
    },
    {
        "name": "transition_soft",
        "keywords": "transition whoosh",
        "category": "01_基础音效/章节切换",
        "description": "柔和过渡"
    },

    # 环境氛围（3个）
    {
        "name": "rain_gentle",
        "keywords": "rain gentle light",
        "category": "02_环境氛围/天气氛围",
        "description": "小雨"
    },
    {
        "name": "wind_soft",
        "keywords": "wind breeze soft",
        "category": "02_环境氛围/天气氛围",
        "description": "微风"
    },
    {
        "name": "thunder",
        "keywords": "thunder rumble",
        "category": "02_环境氛围/天气氛围",
        "description": "雷声"
    },

    # 人物动作（2个）
    {
        "name": "footsteps",
        "keywords": "footsteps walk",
        "category": "03_人物动作/移动动作",
        "description": "脚步声"
    },
    {
        "name": "door_open",
        "keywords": "door open creak",
        "category": "03_人物动作/移动动作",
        "description": "开门声"
    },

    # 背景音乐（2个）
    {
        "name": "piano_soft",
        "keywords": "piano soft emotional",
        "category": "04_背景音乐/文艺情绪",
        "description": "钢琴"
    },
    {
        "name": "mystery_ambient",
        "keywords": "mystery ambient dark",
        "category": "04_背景音乐/悬疑情绪",
        "description": "悬疑氛围"
    }
]

# Mixkit网站URL
BASE_URL = "https://mixkit.co/free-sound-effects/"

# ==================== 核心功能 ====================

def download_sound_effects():
    """下载音效主函数"""

    print("="*60)
    print("🎵 有声书音效批量下载工具")
    print("="*60)
    print(f"📂 项目目录: {PROJECT_DIR}")
    print(f"🎯 准备下载 {len(SOUND_EFFECTS)} 个音效")
    print("="*60)
    print()

    # 下载记录
    download_records = []
    success_count = 0
    failed_count = 0

    with sync_playwright() as p:
        # 启动浏览器
        print("🚀 启动浏览器...")
        browser = p.chromium.launch(headless=False)  # headless=False 可以看到浏览器操作
        context = browser.new_context()
        page = context.new_page()

        try:
            # 访问Mixkit首页
            print(f"🌐 访问 {BASE_URL}")
            page.goto(BASE_URL, timeout=30000)
            page.wait_for_load_state("networkidle")
            time.sleep(2)

            # 遍历音效列表
            for idx, sfx in enumerate(SOUND_EFFECTS, 1):
                print(f"\n[{idx}/{len(SOUND_EFFECTS)}] 📥 正在下载: {sfx['name']}")
                print(f"   关键词: {sfx['keywords']}")
                print(f"   分类: {sfx['category']}")

                try:
                    # 查找搜索框并输入关键词
                    search_selectors = [
                        'input[placeholder*="search" i]',
                        'input[type="search"]',
                        'input[name="q"]',
                        '#search-input'
                    ]

                    search_box = None
                    for selector in search_selectors:
                        try:
                            search_box = page.locator(selector).first
                            if search_box.is_visible(timeout=2000):
                                break
                        except:
                            continue

                    if not search_box or not search_box.is_visible():
                        print("   ⚠️  未找到搜索框，尝试直接浏览分类...")
                        # 如果没有搜索框，返回首页重新尝试
                        page.goto(BASE_URL)
                        page.wait_for_load_state("networkidle")
                        time.sleep(2)
                    else:
                        # 清空搜索框并输入关键词
                        search_box.fill("")
                        search_box.fill(sfx['keywords'])
                        time.sleep(1)

                        # 查找并点击搜索按钮
                        search_button_selectors = [
                            'button[type="submit"]',
                            'button[aria-label*="search" i]',
                            '.search-button'
                        ]

                        for btn_selector in search_button_selectors:
                            try:
                                search_btn = page.locator(btn_selector).first
                                if search_btn.is_visible(timeout=1000):
                                    search_btn.click()
                                    break
                            except:
                                continue

                        time.sleep(3)

                    # 等待搜索结果加载
                    print("   ⏳ 等待搜索结果...")

                    # 尝试多种可能的音效卡片选择器
                    card_selectors = [
                        'a[href*="/free-sound-effects/"]',
                        '.sound-effect-card',
                        'article',
                        '[class*="sound"]',
                        '[class*="effect"]'
                    ]

                    sound_link = None
                    for selector in card_selectors:
                        try:
                            links = page.locator(selector).all()
                            # 过滤掉导航链接，只保留音效详情链接
                            for link in links:
                                href = link.get_attribute('href') or ''
                                if '/free-sound-effects/' in href and len(href.split('/')) > 4:
                                    sound_link = link
                                    break
                            if sound_link:
                                break
                        except:
                            continue

                    if not sound_link:
                        print(f"   ❌ 未找到音效链接，跳过")
                        failed_count += 1
                        download_records.append({
                            '序号': idx,
                            '音效名称': sfx['name'],
                            '分类': sfx['category'],
                            '关键词': sfx['keywords'],
                            '状态': '失败',
                            '原因': '未找到音效链接',
                            '下载时间': datetime.now().strftime('%Y-%m-%d %H:%M:%S')
                        })
                        # 返回首页
                        page.goto(BASE_URL)
                        page.wait_for_load_state("networkidle")
                        time.sleep(2)
                        continue

                    # 点击进入音效详情页
                    print("   🔗 进入音效详情页...")
                    sound_link.click()
                    page.wait_for_load_state("networkidle")
                    time.sleep(2)

                    # 查找下载按钮
                    download_button_selectors = [
                        'button:has-text("Download")',
                        'a:has-text("Download")',
                        '[class*="download"]',
                        'button[aria-label*="download" i]'
                    ]

                    download_button = None
                    for selector in download_button_selectors:
                        try:
                            btn = page.locator(selector).first
                            if btn.is_visible(timeout=2000):
                                download_button = btn
                                break
                        except:
                            continue

                    if not download_button:
                        print(f"   ❌ 未找到下载按钮，跳过")
                        failed_count += 1
                        download_records.append({
                            '序号': idx,
                            '音效名称': sfx['name'],
                            '分类': sfx['category'],
                            '关键词': sfx['keywords'],
                            '状态': '失败',
                            '原因': '未找到下载按钮',
                            '下载时间': datetime.now().strftime('%Y-%m-%d %H:%M:%S')
                        })
                        # 返回首页
                        page.goto(BASE_URL)
                        page.wait_for_load_state("networkidle")
                        time.sleep(2)
                        continue

                    # 设置下载路径
                    download_dir = PROJECT_DIR / sfx['category']
                    download_dir.mkdir(parents=True, exist_ok=True)

                    # 点击下载并等待下载完成
                    print("   📥 开始下载...")

                    with page.expect_download(timeout=30000) as download_info:
                        download_button.click()

                    download = download_info.value
                    filename = f"{sfx['name']}_{idx:02d}.mp3"
                    save_path = download_dir / filename
                    download.save_as(save_path)

                    file_size = save_path.stat().st_size / 1024  # KB

                    print(f"   ✅ 下载成功: {filename} ({file_size:.1f} KB)")
                    success_count += 1

                    download_records.append({
                        '序号': idx,
                        '音效名称': sfx['name'],
                        '分类': sfx['category'],
                        '关键词': sfx['keywords'],
                        '状态': '成功',
                        '文件名': filename,
                        '文件大小': f"{file_size:.1f} KB",
                        '下载时间': datetime.now().strftime('%Y-%m-%d %H:%M:%S')
                    })

                    # 返回首页，准备下一个下载
                    page.goto(BASE_URL)
                    page.wait_for_load_state("networkidle")
                    time.sleep(2)

                except Exception as e:
                    print(f"   ❌ 下载失败: {str(e)}")
                    failed_count += 1
                    download_records.append({
                        '序号': idx,
                        '音效名称': sfx['name'],
                        '分类': sfx['category'],
                        '关键词': sfx['keywords'],
                        '状态': '失败',
                        '原因': str(e),
                        '下载时间': datetime.now().strftime('%Y-%m-%d %H:%M:%S')
                    })
                    # 返回首页
                    page.goto(BASE_URL)
                    page.wait_for_load_state("networkidle")
                    time.sleep(2)

        finally:
            browser.close()

    # 生成下载清单
    print("\n" + "="*60)
    print("📊 生成下载清单...")
    df = pd.DataFrame(download_records)
    excel_path = PROJECT_DIR / "音效清单.xlsx"
    df.to_excel(excel_path, index=False, sheet_name='下载记录')
    print(f"   ✅ 清单已保存: {excel_path}")

    # 生成统计报告
    print("\n" + "="*60)
    print("📋 下载统计")
    print("="*60)
    print(f"✅ 成功: {success_count} 个")
    print(f"❌ 失败: {failed_count} 个")
    print(f"📊 成功率: {success_count/(success_count+failed_count)*100:.1f}%")
    print("="*60)

    # 生成Markdown报告
    markdown_path = PROJECT_DIR / "测试报告.md"
    with open(markdown_path, 'w', encoding='utf-8') as f:
        f.write("# 有声书音效下载测试报告\n\n")
        f.write(f"**下载时间**: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n\n")
        f.write("## 📊 下载统计\n\n")
        f.write(f"- ✅ 成功下载: {success_count} 个\n")
        f.write(f"- ❌ 下载失败: {failed_count} 个\n")
        f.write(f"- 📊 成功率: {success_count/(success_count+failed_count)*100:.1f}%\n\n")

        f.write("## 📋 下载清单\n\n")
        f.write("| 序号 | 音效名称 | 分类 | 状态 | 文件大小 |\n")
        f.write("|------|----------|------|------|----------|\n")
        for record in download_records:
            status_emoji = "✅" if record['状态'] == '成功' else "❌"
            size = record.get('文件大小', '-')
            f.write(f"| {record['序号']} | {record['音效名称']} | {record['分类']} | {status_emoji} {record['状态']} | {size} |\n")

        if failed_count > 0:
            f.write("\n## ❌ 失败项目\n\n")
            for record in download_records:
                if record['状态'] == '失败':
                    f.write(f"- **{record['音效名称']}**: {record.get('原因', '未知错误')}\n")

        f.write("\n## 💡 建议\n\n")
        if success_count == len(SOUND_EFFECTS):
            f.write("✅ 所有音效下载成功！可以扩展为批量下载更多音效。\n\n")
            f.write("下一步建议：\n")
            f.write("1. 试听音效质量\n")
            f.write("2. 验证分类是否合理\n")
            f.write("3. 如满意，可以批量下载50-100个音效\n")
        elif success_count > len(SOUND_EFFECTS) / 2:
            f.write("⚠️ 部分音效下载失败。建议：\n")
            f.write("1. 手动下载失败的音效\n")
            f.write("2. 检查网络连接\n")
            f.write("3. 调整选择器配置\n")
        else:
            f.write("❌ 下载成功率较低。建议：\n")
            f.write("1. 检查网络连接\n")
            f.write("2. 尝试手动下载验证网站可用性\n")
            f.write("3. 考虑使用其他音效源（如Freesound API）\n")

    print(f"   ✅ 测试报告已保存: {markdown_path}")

    print("\n" + "="*60)
    print("🎉 下载任务完成！")
    print("="*60)
    print(f"📂 所有文件保存在: {PROJECT_DIR}")
    print("="*60)


if __name__ == "__main__":
    download_sound_effects()
