@echo off
echo ======================================
echo 即时剪贴板 - 安装 APK 到手机
echo ======================================
echo.

set APK_PATH="%~dp0app\build\outputs\apk\debug\app-debug.apk"

echo 正在检查 APK 文件...
if not exist %APK_PATH% (
    echo ❌ APK 文件不存在！
    echo 路径: %APK_PATH%
    echo.
    echo 请先运行 build_and_install.bat 构建 APK
    pause
    exit /b 1
)

echo ✅ APK 文件找到
echo.

echo 正在检查连接的设备...
adb devices
echo.

echo 正在安装到手机（覆盖旧版本）...
adb install -r %APK_PATH%

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ======================================
    echo ✅ 安装成功！
    echo ======================================
    echo.
    echo 📱 现在可以在手机上测试应用了！
    echo.
    echo 🧪 测试重点：
    echo   1. 底部导航栏切换
    echo   2. 标签创建和编辑
    echo   3. 剪贴板保存/编辑/删除
    echo   4. 悬浮窗功能
    echo.
) else (
    echo.
    echo ======================================
    echo ❌ 安装失败！
    echo ======================================
    echo.
    echo 可能的原因：
    echo   1. 手机未连接或未开启 USB 调试
    echo   2. 手机未授权此电脑
    echo   3. ADB 驱动未安装
    echo.
    echo 📝 解决方法：
    echo   1. 拔掉 USB 线重新连接
    echo   2. 在手机上重新授权调试
    echo   3. 运行: adb devices 检查设备
    echo.
)

pause
