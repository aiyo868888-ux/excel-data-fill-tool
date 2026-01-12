@echo off
chcp 65001 >nul
title 音效生成工具

echo ========================================
echo    音效生成工具
echo    正在启动...
echo ========================================
echo.

cd /d "%~dp0src"

python web_app.py

pause
