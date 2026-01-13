"""
Custom hook to fix numpy source directory detection
This will be loaded by PyInstaller before numpy is imported
"""

import sys
import os

# 在numpy被导入之前，完全重置sys.path
if hasattr(sys, '_MEIPASS'):
    # 只保留_MEIPASS，删除所有其他路径
    # 这样numpy就无法检测到源代码目录
    sys.path = [sys._MEIPASS]
