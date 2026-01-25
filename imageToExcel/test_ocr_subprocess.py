"""
测试子进程OCR调用
"""
import subprocess
import sys
import os
from pathlib import Path

# 设置环境变量
env = os.environ.copy()
env['USE_ONEDNN'] = '0'
env['DISABLE_MODEL_SOURCE_CHECK'] = 'True'

extract_script = Path(r'd:\claude code -11\image-table-excel\scripts\extract_table_from_image.py')
img_path = Path(r'd:\claude code -11\imageToExcel\test_image.jpg')
output_file = Path(r'd:\claude code -11\imageToExcel\uploads\test_output.json')

print("=" * 60)
print("测试子进程OCR调用")
print("=" * 60)
print(f"脚本: {extract_script}")
print(f"图片: {img_path}")
print(f"输出: {output_file}")
print()
print("环境变量:")
print(f"  USE_ONEDNN={env.get('USE_ONEDNN')}")
print(f"  DISABLE_MODEL_SOURCE_CHECK={env.get('DISABLE_MODEL_SOURCE_CHECK')}")
print()
print("运行命令...")
print("-" * 60)

cmd = [sys.executable, str(extract_script), str(img_path), '--output', str(output_file)]

result = subprocess.run(cmd, capture_output=True, text=False,
                      env=env, timeout=120)

# 直接写入文件避免编码问题
with open('ocr_test_output.txt', 'wb') as f:
    f.write(b"=== STDOUT ===\n")
    f.write(result.stdout)
    f.write(b"\n=== STDERR ===\n")
    f.write(result.stderr)
    f.write(f"\n=== Return Code: {result.returncode} ===\n".encode())
    f.write(f"Output file exists: {output_file.exists()}\n".encode())

print(f"结果已保存到: ocr_test_output.txt")
print(f"返回码: {result.returncode}")
print(f"输出文件存在: {output_file.exists()}")

if output_file.exists():
    import json
    with open(output_file, 'r', encoding='utf-8') as f:
        data = json.load(f)
    print(f"提取到 {len(data)} 行数据")
    print("数据预览:")
    for i, row in enumerate(data[:3]):
        print(f"  Row {i+1}: {row}")
