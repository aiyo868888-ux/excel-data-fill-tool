#!/bin/bash

# 系统优化执行脚本
# 执行优化前必须备份

set -e  # 遇到错误立即退出

BACKUP_DIR=".backup/system-optimizer-$(date +%Y%m%d)"
TIMESTAMP=$(date +%Y-%m-%d)

echo "=== 系统优化开始 ==="
echo "备份目录: $BACKUP_DIR"

# 1. 创建备份
echo ""
echo "1. 创建备份..."
mkdir -p "$BACKUP_DIR"
cp CLAUDE.md "$BACKUP_DIR/" 2>/dev/null || echo "  ⚠️  CLAUDE.md 不存在"
cp memory.md "$BACKUP_DIR/" 2>/dev/null || echo "  ⚠️  memory.md 不存在"
echo "  ✓ 备份完成"

# 2. 执行优化（示例）
echo ""
echo "2. 执行优化..."
echo "  ℹ️  具体优化步骤由 AI 根据健康检查结果决定"

# 3. 验证结果
echo ""
echo "3. 验证结果..."
if [ -f "CLAUDE.md" ]; then
  echo "  ✓ CLAUDE.md 存在"
fi
if [ -f "memory.md" ]; then
  echo "  ✓ memory.md 存在"
fi

# 4. 更新配置文件
echo ""
echo "4. 更新配置..."
cat > .system-optimizer.json << EOF
{
  "lastRun": "$TIMESTAMP",
  "issuesFound": 0,
  "issuesFixed": 0,
  "autoBackup": true,
  "backupRetentionDays": 7
}
EOF
echo "  ✓ 配置已更新"

echo ""
echo "=== 优化完成 ==="
echo ""
echo "如需回滚，执行:"
echo "  cp $BACKUP_DIR/CLAUDE.md CLAUDE.md"
echo "  cp $BACKUP_DIR/memory.md memory.md"
