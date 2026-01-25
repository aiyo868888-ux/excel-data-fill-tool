#!/bin/bash

# 系统健康检查脚本
# 检测 CLAUDE.md 和 memory.md 的问题

echo "=== 系统健康检查 ==="

# 1. 文件大小检查
echo ""
echo "1. 文件大小:"
claude_lines=$(wc -l < CLAUDE.md 2>/dev/null || echo "0")
memory_lines=$(wc -l < memory.md 2>/dev/null || echo "0")

echo "  CLAUDE.md: $claude_lines 行"
echo "  memory.md: $memory_lines 行"

if [ "$claude_lines" -gt 100 ]; then
  echo "  ⚠️  CLAUDE.md 过长，建议归档"
fi

if [ "$memory_lines" -gt 100 ]; then
  echo "  ⚠️  memory.md 过长，建议归档"
fi

# 2. 重复规则检查
echo ""
echo "2. 重复规则:"
duplicate_count=$(grep -o "保持简洁" CLAUDE.md 2>/dev/null | wc -l)
echo "  '保持简洁' 出现 $duplicate_count 次"

if [ "$duplicate_count" -ge 2 ]; then
  echo "  ⚠️  检测到重复规则"
fi

# 3. 冲突规则检查
echo ""
echo "3. 冲突规则:"
if grep -q "极简" CLAUDE.md && grep -q "≥90%.*覆盖" CLAUDE.md; then
  echo "  ⚠️  检测到潜在冲突：极简原则 vs 高测试覆盖率"
fi

# 4. 链接检查
echo ""
echo "4. 链接有效性:"
broken_links=$(grep -oP '\[.*\]\([^(]+\.md\)' CLAUDE.md 2>/dev/null | while read link; do
  file=$(echo "$link" | grep -oP '\([^(]+\.md\)' | tr -d '()')
  if [ ! -f "$file" ]; then
    echo "$link"
  fi
done)

if [ -n "$broken_links" ]; then
  echo "  ⚠️  检测到失效链接:"
  echo "$broken_links" | sed 's/^/    /'
else
  echo "  ✓ 所有链接有效"
fi

# 5. 备份检查
echo ""
echo "5. 备份状态:"
backup_dir=".backup/system-optimizer-$(date +%Y%m%d)"
if [ -d "$backup_dir" ]; then
  echo "  ✓ 今日备份已存在"
else
  echo "  ℹ️  尚未创建今日备份"
fi

echo ""
echo "=== 检查完成 ==="
