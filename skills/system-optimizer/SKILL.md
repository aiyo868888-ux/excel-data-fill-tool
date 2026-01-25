---
name: system-optimizer
description: 配置优化工具。当用户说"检查配置"、"优化系统"或需要修复 CLAUDE.md/memory.md 问题时触发。执行健康检查、问题分析、备份修复、记录变更的完整流程。
---

# 系统优化器

CLAUDE.md 和 memory.md 的健康检查和优化工具。

## 快速使用

用户说"检查配置"时：
1. 执行健康检查
2. 生成问题报告
3. 询问是否修复

## 工作流程

### 1. 健康检查

```bash
# 文件大小
wc -l CLAUDE.md memory.md

# 检测重复
grep -n "保持简洁" CLAUDE.md | wc -l

# 检测冲突
grep -n "极简\|完整测试" CLAUDE.md
```

**判断标准**：
- 文件 >100 行：需归档
- 重复 ≥2 处：需合并
- 存在冲突：需添加解决规则

### 2. 问题分类

**优先级**：
1. 冲突 - 规则矛盾
2. 重复 - 内容重复
3. 缺失 - 缺少异常处理
4. 冗余 - 不必要复杂度

### 3. 修复流程

**必须备份**：
```bash
mkdir -p .backup/system-optimizer-$(date +%Y%m%d)
cp CLAUDE.md memory.md .backup/system-optimizer-$(date +%Y%m%d)/
```

**修改原则**：
- 最小改动
- 保留意图
- 增量改进

**验证与回滚**：
```bash
# 验证
cat CLAUDE.md | head -20

# 回滚（如有问题）
cp .backup/system-optimizer-*/CLAUDE.md CLAUDE.md
```

### 4. 记录变更

更新 memory.md "系统反馈"章节：
```markdown
- YYYY-MM-DD: 问题简述
  - 问题：具体描述
  - 解决：采用的方案
```

## 输出格式

**小优化（≤3个问题）**：
```
✓ 检测到 2 个重复规则，已合并
✓ 更新 memory.md
```

**大优化（>3个问题）**：完整报告（问题清单、详细变更、后续建议）

## 脚本工具

- [scripts/health-check.sh](scripts/health-check.sh) - 健康检查
- [scripts/optimize.sh](scripts/optimize.sh) - 执行优化

## 归档策略

单章节 >50 条时：
```bash
grep -A 10000 "## 系统反馈" memory.md | tail -n +3 > memory-system-feedback.md
sed -i '31,$d' memory.md
```

## 约束条件

1. 修改前必须备份
2. 最小改动原则
3. 保留 7 天备份
4. 所有变更记录到 memory.md
5. 冲突解决：极简 > 完整测试

