# Claude Code Skills 安装记录

## 已安装的技能仓库

### 1. baoyu-skills (宝玉的技能集)
**位置**: `d:\claude code -11\baoyu-skills`
**来源**: https://github.com/JimLiu/baoyu-skills
**Star**: 1.7k ⭐

#### 包含的11个技能

**Content Skills (内容生成)**:
1. `baoyu-xhs-images` - 小红书信息图生成器
   - 9种风格 × 6种布局
   - 生成1-10张卡通风格信息图

2. `baoyu-infographic` - 专业信息图生成
   - 20种布局 × 17种风格
   - 支持金字塔、漏斗、思维导图等

3. `baoyu-cover-image` - 文章封面图生成
   - 21种视觉风格
   - 支持标题/无标题选项

4. `baoyu-slide-deck` - PPT幻灯片生成
   - 14种专业风格
   - 自动合并为.pptx文件

5. `baoyu-comic` - 知识漫画创作
   - 9种风格 × 6种布局
   - 支持经典、戏剧、温暖等风格

6. `baoyu-article-illustrator` - 文章插图生成
   - 18种风格
   - 智能分析内容并插入插图

7. `baoyu-post-to-x` - 发布到X/Twitter
8. `baoyu-post-to-wechat` - 发布到微信公众号

**AI Generation Skills**:
9. `baoyu-image-gen` - OpenAI/Google图片生成
10. `baoyu-danger-gemini-web` - Gemini Web交互

**Utility Skills**:
11. `baoyu-danger-x-to-markdown` - X推文转Markdown
12. `baoyu-compress-image` - 图片压缩

---

### 2. yunshu_skillshub (云舒的技能集)
**位置**: `d:\claude code -11\yunshu_skillshub`
**来源**: https://github.com/yunshu0909/yunshu_skillshub
**Star**: 29 ⭐

#### 包含的4个技能

1. **image-assistant** (配图助手)
   - 把文章转成16:9信息图提示词
   - 需求澄清、配图规划、文案定稿
   - 支持批量生成和迭代润色
   - 触发: `/image-assistant` 或 "做个图/配图"

2. **thought-mining** (思维挖掘助手)
   - 对话式帮你整理零散想法
   - 思维挖掘、选题确定、观点验证
   - 写作辅助和最终审核
   - 触发: `/thought-mining` 或 "我想写文章/整理想法"

3. **prd-doc-writer** (PRD文档撰写助手)
   - 故事驱动的PRD撰写
   - 用户旅程地图、ASCII线框图
   - Mermaid图表（流程图/状态图/时序图）
   - 触发: `/prd-doc-writer` 或 "写PRD/梳理需求"

4. **req-change-workflow** (需求变更工作流)
   - 标准化需求变更流程
   - 需求澄清、现状基线、影响评估
   - 最小化实现、回归测试、文档维护
   - 触发: `/req-change-workflow` 或 "改需求/需求变更"

---

## 使用方法

### 方式一：直接触发（推荐）
在Claude Code中直接描述需求，相关技能会自动触发：

```
# 内容创作相关
"帮我做几张小红书信息图"
"写个PRD文档"
"整理一下我的想法"

# 直接调用技能
/image-assistant
/thought-mining
/prd-doc-writer
/req-change-workflow
```

### 方式二：手动安装到Claude目录
如果想将技能添加到Claude的默认技能目录：

```bash
# Claude Code默认技能目录
cd ~/.claude/skills/

# 复制技能
cp -r "d:\claude code -11\baoyu-skills\skills\baoyu-xhs-images" ~/.claude/skills/
cp -r "d:\claude code -11\yunshu_skillshub\image-assistant" ~/.claude/skills/

# 或使用符号链接（推荐）
ln -s "d:\claude code -11\baoyu-skills\skills\baoyu-xhs-images" ~/.claude/skills/
```

---

## 技能对比

| 类别 | baoyu-skills | yunshu_skillshub |
|------|-------------|-----------------|
| **数量** | 11个技能 | 4个技能 |
| **重点** | 视觉内容生成 | 产品管理工作流 |
| **输出** | 图片、PPT、漫画 | 文档、需求、思维整理 |
| **风格** | 多样化视觉风格 | 结构化工作流 |

---

## 环境配置要求

### 某些技能需要API密钥

**baoyu-image-gen** 需要：
```bash
# ~/.baoyu-skills/.env
OPENAI_API_KEY=sk-xxx
GOOGLE_API_KEY=xxx
```

**baoyu-post-to-x/wechat** 需要：
- Google Chrome浏览器
- 首次使用需要扫码登录

---

## 快速开始示例

```bash
# 1. 生成小红书信息图
/baoyu-xhs-images article.md --style warm --layout balanced

# 2. 整理零散想法
/thought-mining
> 我最近在思考关于AI产品设计的想法...

# 3. 写PRD文档
/prd-doc-writer
> 我想做一个智能笔记应用

# 4. 处理需求变更
/req-change-workflow
> 需要调整用户登录流程

# 5. 使用配图助手
/image-assistant
> 把这篇文章做成3张信息图
```

---

## 更新技能

```bash
# baoyu-skills
cd "d:\claude code -11\baoyu-skills"
git pull

# yunshu_skillshub
cd "d:\claude code -11\yunshu_skillshub"
git pull
```

---

## 相关链接

- [baoyu-skills GitHub](https://github.com/JimLiu/baoyu-skills)
- [yunshu_skillshub GitHub](https://github.com/yunshu0909/yunshu_skillshub)
- [Claude Code 官方文档](https://claude.ai/claude-code)

---

**安装日期**: 2026-01-21
**Claude Code 版本**: Latest
**Python环境**: Python 3.11
