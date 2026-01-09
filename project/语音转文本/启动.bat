@echo off
cd /d "%~dp0"
echo ========================================
echo   语音转文字 - 启动中...
echo ========================================
echo.
echo 访问地址: http://localhost:5002
echo.
python web_app.py
pause
