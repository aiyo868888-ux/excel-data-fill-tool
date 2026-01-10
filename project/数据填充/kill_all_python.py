import os
import signal
import psutil

# 杀死所有 Python 进程
for proc in psutil.process_iter(['pid', 'name', 'cmdline']):
    try:
        if proc.info['name'] and 'python' in proc.info['name'].lower():
            print(f"杀死进程: {proc.info['pid']} - {proc.info['name']}")
            proc.kill()
    except (psutil.NoSuchProcess, psutil.AccessDenied, psutil.ZombieProcess):
        pass

print("所有 Python 进程已停止")
