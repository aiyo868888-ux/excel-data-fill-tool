@echo off
echo ===================================
echo 即时剪贴板 - 重新构建并安装
echo ===================================
echo.

set JAVA_HOME=C:\Program Files\ojdkbuild\java-17-openjdk-17.0.3.0.6-1
set PATH=%JAVA_HOME%\bin;%PATH%

cd /d "%~dp0"

echo [1/2] 构建 Debug APK...
call gradlew.bat assembleDebug

if %ERRORLEVEL% NEQ 0 (
    echo ❌ 构建失败！
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo [2/2] 安装到手机...
"C:\Users\15085\AppData\Local\Android\Sdk\platform-tools\adb.exe" install -r "d:\claude code -11\project\jishiwenjian\app\build\outputs\apk\debug\app-debug.apk"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ 安装成功！
    echo.
    echo 📱 请在手机上测试：
    echo    1. 进入"设置"页面
    echo    2. 点击"🔧 修复默认标签"按钮
    echo    3. 重新尝试保存内容到灵感
) else (
    echo ❌ 安装失败
)

pause
