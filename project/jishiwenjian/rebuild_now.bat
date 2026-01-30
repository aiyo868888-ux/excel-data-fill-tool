@echo off
echo ========================================
echo 重新构建 - 已修复 MainActivity 错误
echo ========================================
echo.

set JAVA_HOME=C:\Program Files\ojdkbuild\java-17-openjdk-17.0.3.0.6-1
set PATH=%JAVA_HOME%\bin;%PATH%

cd /d "%~dp0"

echo 正在构建...
call gradlew.bat assembleDebug --console=plain

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ 构建失败！
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo ========================================
echo ✅ 构建成功！
echo ========================================
echo.
echo APK: app\build\outputs\apk\debug\app-debug.apk
echo.
echo 安装命令：
echo   1. 清理数据：adb shell pm clear com.jishi.clipboard
echo   2. 安装APK：adb install -r app\build\outputs\apk\debug\app-debug.apk
echo.

pause
