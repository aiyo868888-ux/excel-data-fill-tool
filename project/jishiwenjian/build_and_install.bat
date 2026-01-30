@echo off
echo ======================================
echo 即时剪贴板 - 构建并安装到手机
echo ======================================
echo.

set JAVA_HOME=C:\Program Files\ojdkbuild\java-17-openjdk-17.0.3.0.6-1
set PATH=%JAVA_HOME%\bin;%PATH%

cd /d "%~dp0"

echo [1/3] 清理旧的构建文件...
call gradlew.bat clean

echo.
echo [2/3] 构建 Debug APK...
call gradlew.bat assembleDebug

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ 构建失败！请检查错误信息。
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo [3/3] 安装到已连接的手机...
call gradlew.bat installDebug

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ 安装失败！请确保：
    echo   1. 手机已通过 USB 连接到电脑
    echo   2. 手机已开启 USB 调试模式
    echo   3. 手机已授权此电脑进行调试
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo ======================================
echo ✅ 构建并安装成功！
echo ======================================
echo.
echo 📱 请在手机上测试以下功能：
echo   1. 底部导航栏切换是否正常
echo   2. 标签创建和关联是否正常
echo   3. 剪贴板保存和编辑是否正常
echo   4. 悬浮窗功能是否正常
echo.
echo APK 位置: app\build\outputs\apk\debug\app-debug.apk
echo.

pause
