#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
数据填充工具 - EXE 启动入口
用于 PyInstaller 打包成独立的可执行程序
"""

import sys
import os

# ========== 关键：必须在任何导入之前修复 sys.path ==========
# 这是解决 numpy 源代码目录检查错误的关键
if hasattr(sys, '_MEIPASS'):
    # PyInstaller 打包环境
    # 只保留 _MEIPASS，完全重置 sys.path
    # 这样 numpy 就不会检测到源代码目录
    sys.path = [sys._MEIPASS]
else:
    # 开发环境：移除空字符串，避免误导 numpy
    try:
        if '' in sys.path:
            sys.path.remove('')
    except:
        pass
# ========== sys.path 修复结束 ==========

# 必须在导入 pandas/numpy 之前设置这些环境变量
# 禁用 numpy 的源代码目录检查
os.environ['PYTHONDONTWRITEBYTECODE'] = '1'
os.environ['PYTHONIOENCODING'] = 'utf-8'

import webbrowser
import time
import threading

# 添加资源路径（PyInstaller 打包后）
def resource_path(relative_path):
    """
    获取资源文件的绝对路径

    Args:
        relative_path: 相对路径

    Returns:
        资源文件的绝对路径
    """
    if hasattr(sys, '_MEIPASS'):
        # PyInstaller 打包后的临时目录
        base_path = sys._MEIPASS
    else:
        # 开发环境下的当前目录
        base_path = os.path.abspath(".")
    return os.path.join(base_path, relative_path)

def open_browser():
    """延迟打开浏览器"""
    time.sleep(1.5)
    try:
        webbrowser.open('http://localhost:8888')
    except Exception as e:
        print(f"警告：无法自动打开浏览器: {e}")
        print("请手动访问: http://localhost:8888")

if __name__ == '__main__':
    # 获取资源路径
    resource_dir = resource_path('')

    # 设置环境变量，告诉 Flask 应用资源文件的位置
    os.environ['FLASK_RESOURCE_DIR'] = resource_dir

    # sys.path 已经在文件开头修复过了，这里不需要再次修改

    # 创建必要的目录（在项目目录下）
    for dir_name in ['uploads', 'temp', 'sessions', 'logs']:
        os.makedirs(dir_name, exist_ok=True)

    print("=" * 70)
    print("   数据填充工具 - EXE 版本")
    print("   正在启动服务器...")
    print("=" * 70)
    print()
    print(f"资源目录: {resource_dir}")
    print(f"工作目录: {os.getcwd()}")
    print("访问地址: http://localhost:8888")
    print("按 Ctrl+C 停止服务器")
    print()
    print("=" * 70)
    print()

    # 启动浏览器线程
    browser_thread = threading.Thread(target=open_browser, daemon=True)
    browser_thread.start()

    # 导入并启动 Flask 应用
    try:
        # 直接从当前目录导入（不要切换目录）
        # 通过 sys.path 已经指定了资源目录
        from web_app import app
        app.run(host='127.0.0.1', port=8888, debug=False)
    except KeyboardInterrupt:
        print("\n\n服务器已停止")
    except Exception as e:
        print(f"\n\n错误: {e}")
        import traceback
        traceback.print_exc()
        input("\n按回车键退出...")
