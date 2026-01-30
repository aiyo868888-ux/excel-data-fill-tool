@echo off
set JAVA_HOME=C:\Program Files\ojdkbuild\java-17-openjdk-17.0.3.0.6-1
set PATH=%JAVA_HOME%\bin;%PATH%

echo ===================================
echo 重新构建并安装及时记
echo ===================================
echo.

cd /d "%~dp0"

echo 正在清理旧构建...
call gradlew.bat clean

echo.
echo 正在构建新APK...
call gradlew.bat assembleDebug

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ 构建失败!
    echo 请检查上面的错误信息
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo ✅ 构建成功!
echo.
echo 正在安装到手机...
"C:\Users\15085\AppData\Local\Android\Sdk\platform-tools\adb.exe" install -r "app\build\outputs\apk\debug\app-debug.apk"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅✅✅ 安装成功!
    echo.
    echo 📱 现在可以测试了:
    echo    1. 打开应用
    echo    2. 点击任意卡片
    echo    3. 查看底部工具栏应该只有3个按钮: # 📷 +
    echo    4. 点击 + 按钮测试工具弹窗
) else (
    echo.
    echo ❌ 安装失败
    echo 请检查:
    echo    1. 手机是否连接并开启USB调试
    echo    2. 是否授权了USB调试
)

echo.
pause
