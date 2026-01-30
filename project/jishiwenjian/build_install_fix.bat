@echo off
echo ======================================
echo 构建并安装架构修复版本
echo ======================================
echo.

set JAVA_HOME=C:\Program Files\ojdkbuild\java-17-openjdk-17.0.3.0.6-1
set PATH=%JAVA_HOME%\bin;%PATH%

cd /d "%~dp0"

echo [1/4] 清理旧构建...
call gradlew.bat clean --console=plain

echo.
echo [2/4] 构建 Debug APK（包含架构修复）...
call gradlew.bat assembleDebug --console=plain

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ 构建失败！
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo [3/4] 清理应用数据（数据库迁移到 v7）...
C:\Users\wenan\AppData\Local\Android\Sdk\platform-tools\adb.exe shell pm clear com.jishi.clipboard

echo.
echo [4/4] 安装到手机...
C:\Users\wenan\AppData\Local\Android\Sdk\platform-tools\adb.exe install -r app\build\outputs\apk\debug\app-debug.apk

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ 安装失败！请检查 ADB 连接。
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo ======================================
echo ✅ 架构修复版本安装成功！
echo ======================================
echo.
echo 🧪 请测试以下场景：
echo.
echo 1. 点击悬浮窗 → 选择"灵感" → 保存
echo    ✓ 内容应该出现在【灵感】Tab
echo.
echo 2. 点击悬浮窗 → 选择"启发" → 保存  
echo    ✓ 内容应该出现在【启发】Tab
echo.
echo 3. 点击悬浮窗 → 选择"待办" → 保存
echo    ✓ 内容应该出现在【待办】Tab
echo.
echo 4. 添加标签不影响导航栏分类
echo    ✓ 标签是独立的分类维度
echo.
echo 📝 查看日志：
echo    adb logcat ^| findstr "ClipboardEdit UnifiedContent"
echo.

pause
