@echo off
chcp 65001 >nul
REM 禁用oneDNN以解决PaddleOCR兼容性问题
set USE_ONEDNN=0
set DISABLE_MODEL_SOURCE_CHECK=True

echo ============================================================
echo imageToExcel Web Application
echo ============================================================
echo.
echo Environment: USE_ONEDNN=0 (禁用硬件加速)
echo Starting server...
echo Access URL: http://localhost:5000
echo.
echo Press Ctrl+C to stop
echo ============================================================
echo.

cd /d "%~dp0"

python app.py

pause
