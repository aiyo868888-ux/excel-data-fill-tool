"""
音效生成器测试脚本
快速测试所有音效类型
"""

import sys
import os

# 添加src目录到路径
sys.path.insert(0, os.path.join(os.path.dirname(__file__), 'src'))

from sound_generator import SoundGenerator

def test_all_sounds():
    """测试所有音效类型"""
    gen = SoundGenerator()
    output_dir = os.path.join(os.path.dirname(__file__), 'output')
    os.makedirs(output_dir, exist_ok=True)

    print("=" * 60)
    print("   音效生成器 - 功能测试")
    print("=" * 60)
    print()

    tests = [
        ('白噪声', lambda: gen.generate_white_noise(2, 0.5)),
        ('粉红噪声', lambda: gen.generate_pink_noise(2, 0.5)),
        ('布朗噪声', lambda: gen.generate_brown_noise(2, 0.5)),
        ('雨声', lambda: gen.generate_rain(2, 0.5, 0.5)),
        ('风声', lambda: gen.generate_wind(2, 0.5, 1.0)),
        ('雷声', lambda: gen.generate_thunder(2, 0.5)),
        ('海浪', lambda: gen.generate_waves(2, 0.5, 0.1)),
        ('森林环境', lambda: gen.generate_forest(2, 0.5)),
    ]

    success_count = 0
    fail_count = 0

    for name, generator in tests:
        try:
            print(f"正在生成: {name}...", end=" ")
            audio = generator()

            # 保存文件
            filename = os.path.join(output_dir, f"test_{name}.wav")
            gen.save_wav(audio, filename)

            print(f"[OK] 成功 ({len(audio)} 采样)")
            success_count += 1
        except Exception as e:
            print(f"[FAIL] 失败: {e}")
            fail_count += 1

    print()
    print("=" * 60)
    print(f"测试完成: {success_count} 成功, {fail_count} 失败")
    print(f"文件保存在: {output_dir}")
    print("=" * 60)

if __name__ == '__main__':
    test_all_sounds()
