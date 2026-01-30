@echo off
echo ===================================
echo 即时剪贴板 - 调试版本构建
echo ===================================
echo.

set JAVA_HOME=C:\Program Files\ojdkbuild\java-17-openjdk-17.0.3.0.6-1
set PATH=%JAVA_HOME%\bin;%PATH%

cd /d "%~dp0"

echo [1/2] 构建 Debug APK (带详细日志)...
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
    echo 📱 测试步骤：
    echo    1. 打开应用
    echo    2. 进入设置，点击"修复默认标签"
    echo    3. 回到灵感页面
    echo    4. 点击➕按钮
    echo    5. 输入内容并保存
    echo.
    echo 🔍 查看日志：
    echo    运行 view_debug_log.bat 查看实时日志
    echo.
) else (
    echo ❌ 安装失败
)

pause
