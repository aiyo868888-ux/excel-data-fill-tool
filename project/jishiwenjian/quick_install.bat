@echo off
echo 正在安装 APK 到手机...
"C:\Users\15085\AppData\Local\Android\Sdk\platform-tools\adb.exe" install -r "d:\claude code -11\project\jishiwenjian\app\build\outputs\apk\debug\app-debug.apk"
if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ 安装成功！请在手机上测试应用
) else (
    echo.
    echo ❌ 安装失败
)
