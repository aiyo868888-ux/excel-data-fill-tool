@echo off
echo =====================================
echo 即时剪贴板 - 实时日志查看
echo =====================================
echo.
echo 正在清空旧日志并启动实时监控...
echo 请在手机上操作：保存一条灵感内容
echo 按 Ctrl+C 停止监控
echo.

set ADB="C:\Users\15085\AppData\Local\Android\Sdk\platform-tools\adb.exe"

:: 清空日志
%ADB% logcat -c

:: 实时查看日志（只显示相关标签）
%ADB% logcat -v time ClipboardEdit:D UnifiedContentRepository:D InspirationFragment:D ClipboardRepository:D *:S
