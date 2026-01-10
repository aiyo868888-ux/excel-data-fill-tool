"""
清理占用8888端口的所有进程
"""
import psutil
import sys
import time

# 设置输出编码
if sys.platform == 'win32':
    import codecs
    sys.stdout = codecs.getwriter('utf-8')(sys.stdout.buffer, 'strict')

def kill_port_8888():
    """终止所有占用8888端口的进程"""
    print("=" * 70)
    print("🔍 检查占用8888端口的进程...")
    print("=" * 70)

    killed = []
    for conn in psutil.net_connections():
        if conn.laddr.port == 8888 and conn.status == 'LISTEN':
            try:
                process = psutil.Process(conn.pid)
                print(f"\n🔍 发现进程:")
                print(f"   PID: {conn.pid}")
                print(f"   名称: {process.name()}")
                print(f"   状态: {process.status()}")

                # 终止进程
                process.kill()
                killed.append(conn.pid)
                print(f"   ✅ 已终止")

            except (psutil.NoSuchProcess, psutil.AccessDenied) as e:
                print(f"   ⚠️  无法终止: {e}")

    if killed:
        print(f"\n✅ 共终止 {len(killed)} 个进程")
        print("⏳ 等待2秒让端口释放...")
        time.sleep(2)

        # 再次检查
        remaining = []
        for conn in psutil.net_connections():
            if conn.laddr.port == 8888 and conn.status == 'LISTEN':
                remaining.append(conn.pid)

        if remaining:
            print(f"⚠️  仍有 {len(remaining)} 个进程占用端口")
            return False
        else:
            print("✅ 端口8888已完全释放")
            return True
    else:
        print("✅ 没有进程占用8888端口")
        return True

if __name__ == '__main__':
    success = kill_port_8888()
    sys.exit(0 if success else 1)
