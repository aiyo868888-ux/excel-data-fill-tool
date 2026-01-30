@echo off
echo ========================================
echo 完整构建测试 - 架构修复版本
echo ========================================
echo.

set JAVA_HOME=C:\Program Files\ojdkbuild\java-17-openjdk-17.0.3.0.6-1
set PATH=%JAVA_HOME%\bin;%PATH%

cd /d "%~dp0"

echo [1/5] 清理旧构建文件...
call gradlew.bat clean --console=plain
if %ERRORLEVEL% NEQ 0 (
    echo ❌ 清理失败！
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo [2/5] 编译 Kotlin 代码...
call gradlew.bat compileDebugKotlin --console=plain
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Kotlin 编译失败！请检查错误信息。
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo [3/5] 构建 Debug APK...
call gradlew.bat assembleDebug --console=plain
if %ERRORLEVEL% NEQ 0 (
    echo ❌ APK 构建失败！
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo ✅ 构建成功！
echo.
echo APK 位置: app\build\outputs\apk\debug\app-debug.apk
echo.

echo [4/5] 检查设备连接...
C:\Users\wenan\AppData\Local\Android\Sdk\platform-tools\adb.exe devices

echo.
echo [5/5] 安装到手机...
echo 选择安装方式：
echo   1 - 清理数据后安装（推荐，触发数据库迁移）
echo   2 - 直接覆盖安装（保留数据，自动迁移）
echo   3 - 跳过安装
echo.
set /p choice="请输入选项 (1/2/3): "

if "%choice%"=="1" (
    echo.
    echo 正在清理应用数据...
    C:\Users\wenan\AppData\Local\Android\Sdk\platform-tools\adb.exe shell pm clear com.jishi.clipboard
    echo.
    echo 正在安装...
    C:\Users\wenan\AppData\Local\Android\Sdk\platform-tools\adb.exe install -r app\build\outputs\apk\debug\app-debug.apk
) else if "%choice%"=="2" (
    echo.
    echo 正在安装...
    C:\Users\wenan\AppData\Local\Android\Sdk\platform-tools\adb.exe install -r app\build\outputs\apk\debug\app-debug.apk
) else (
    echo.
    echo ⏭️ 跳过安装步骤
)

echo.
echo ========================================
echo ✅ 完成！
echo ========================================
echo.
echo 🧪 测试场景：
echo   1. 点击悬浮窗 → 选择"灵感" → 保存
echo      ✓ 内容显示在【灵感】Tab
echo.
echo   2. 点击悬浮窗 → 选择"启发" → 保存  
echo      ✓ 内容显示在【启发】Tab
echo.
echo   3. 点击悬浮窗 → 选择"待办" → 保存
echo      ✓ 内容显示在【待办】Tab
echo.
echo   4. 添加标签"工作"到"灵感"类型
echo      ✓ 内容仍显示在【灵感】Tab（标签不影响）
echo.
echo 📝 查看日志：
echo    adb logcat ^| findstr "ClipboardEdit UnifiedContent"
echo.

pause
