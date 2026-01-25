#!/bin/bash

echo "===================================="
echo "imageToExcel Web应用"
echo "===================================="
echo ""

# 检查Python
if ! command -v python3 &> /dev/null
then
    echo "错误: 未找到Python，请先安装Python 3.8+"
    exit 1
fi

# 安装依赖
echo "[1/2] 检查并安装依赖..."
pip install -q -r requirements.txt

# 启动应用
echo "[2/2] 启动Web服务器..."
echo ""
echo "访问地址: http://localhost:5000"
echo "按 Ctrl+C 停止服务器"
echo "===================================="
echo ""

python3 app.py
