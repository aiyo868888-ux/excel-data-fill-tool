"""
Runtime hook to fix numpy source directory detection in PyInstaller
Must be loaded before numpy is imported
"""
import sys
import os

# 完全重置 sys.path，避免 numpy 检测到源代码目录
# 这必须在任何 import 之前执行
if hasattr(sys, '_MEIPASS'):
    # PyInstaller 打包环境
    # 只保留 _MEIPASS，移除所有其他路径
    _meipass = sys._MEIPASS
    sys.path = [_meipass]

    # 确保没有空字符串或当前目录引用
    # numpy 会检查这些来判断是否在源代码目录中
else:
    # 开发环境：移除空字符串
    try:
        if '' in sys.path:
            sys.path.remove('')
    except:
        pass
