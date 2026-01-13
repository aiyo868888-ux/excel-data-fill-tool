"""
PyInstaller runtime hook for numpy
Fixes the "import from source directory" error
"""
import sys
import os

# 移除可能触发 numpy 源代码检测的路径
if hasattr(sys, '_MEIPASS'):
    # 在 PyInstaller 环境中，清理 sys.path
    # 移除可能导致 numpy 认为在源代码目录的路径
    sys.path = [p for p in sys.path if 'numpy' not in p or p.endswith('.zip') or '_MEI' in p]
