# Smart Todo Parser - 智能待办事项解析器

**通用时间解析库 + 任务管理系统**（Python 已完成 ✅）

> [项目总结](PROJECT_SUMMARY.md) | [安装指南](INSTALL.md) | [使用示例](docs/EXAMPLES.md) | [API 文档](docs/API.md)

## 核心功能

### 1. 时间解析
- ✅ **绝对时间**："2025年1月20日下午3点"、"1/20/2025 15:00"
- ✅ **相对时间**："明天"、"后天"、"下周三"、"3天后"
- ✅ **模糊时间**："月底前"、"本周末"、"下季度初"

### 2. 任务提取
- 自动分离任务描述和时间信息
- 识别优先级关键词（紧急、重要、今天、明天）
- 提取标签（#工作、#个人）

### 3. 提醒调度
- 到期自动提醒
- 提前提醒（提前15分钟/1小时/1天）
- 循环任务（每天、每周、每月）

### 4. 任务管理
- CRUD 操作（创建、读取、更新、删除）
- 任务状态管理（待办、已完成、已过期）
- 按优先级/时间排序

## 快速开始

### Python 版本

```python
from smart_todo_parser import TodoParser

# 初始化解析器
parser = TodoParser(language='zh-CN')

# 解析单条输入
result = parser.parse("明天下午3点开会，准备PPT")
print(result)
# {
#     "task": "开会，准备PPT",
#     "datetime": "2025-01-21 15:00:00",
#     "priority": "normal",
#     "tags": [],
#     "type": "absolute"
# }

# 批量解析
todos = [
    "月底前提交报告 #工作",
    "后天早上9点面试 #求职",
    "下周三下午2点团队会议"
]
results = parser.parse_batch(todos)

# 设置提醒
parser.set_reminder(result, reminder_before=15)  # 提前15分钟
```

### JavaScript 版本

```javascript
const { TodoParser } = require('smart-todo-parser');

const parser = new TodoParser({ language: 'zh-CN' });

const result = parser.parse('明天下午3点开会，准备PPT');
console.log(result);
// {
//   task: '开会，准备PPT',
//   datetime: '2025-01-21 15:00:00',
//   priority: 'normal',
//   tags: [],
//   type: 'absolute'
// }

// 异步解析
const results = await parser.parseBatch([
    '月底前提交报告 #工作',
    '后天早上9点面试 #求职'
]);
```

## 架构设计

```
smart-todo-parser/
├── python/                    # Python 实现
│   ├── smart_todo_parser/
│   │   ├── __init__.py       # 主入口
│   │   ├── core/             # 核心模块
│   │   │   ├── parser.py     # 主解析器
│   │   │   ├── time_extractor.py  # 时间提取
│   │   │   ├── task_extractor.py  # 任务提取
│   │   │   └── priority_detector.py  # 优先级识别
│   │   ├── utils/
│   │   │   ├── date_utils.py # 日期工具
│   │   │   └── fuzzy_time.py # 模糊时间处理
│   │   ├── scheduler/        # 调度系统
│   │   │   ├── reminder.py   # 提醒器
│   │   │   └── task_manager.py  # 任务管理
│   │   └── config/
│   │       └── patterns.py   # 正则模式
│   ├── tests/                # 测试
│   │   ├── test_parser.py
│   │   ├── test_fuzzy_time.py
│   │   └── test_scheduler.py
│   └── setup.py
│
├── javascript/               # JavaScript 实现
│   ├── src/
│   │   ├── index.js
│   │   ├── core/
│   │   │   ├── Parser.js
│   │   │   ├── TimeExtractor.js
│   │   │   └── TaskExtractor.js
│   │   ├── utils/
│   │   │   ├── dateUtils.js
│   │   │   └── fuzzyTime.js
│   │   └── scheduler/
│   │       ├── Reminder.js
│   │       └── TaskManager.js
│   ├── test/
│   └── package.json
│
└── docs/                     # 文档
    ├── API.md
    ├── PATTERNS.md           # 支持的时间模式
    └── EXAMPLES.md           # 使用示例
```

## 依赖项

### Python
- `python-dateutil` - 日期解析
- `regex` - 高级正则表达式
- `pytz` - 时区处理

### JavaScript
- `chrono-node` - 时间解析
- `date-fns` - 日期操作
- `node-schedule` - 任务调度

## 测试覆盖

目标：≥90% 代码覆盖率

```bash
# Python
pytest tests/ --cov=smart_todo_parser --cov-report=html

# JavaScript
npm test
```

## 性能指标

- 解析速度：<10ms/条（简单时间）
- 内存占用：<50MB
- 支持语言：中文（zh-CN）、英文（en-US）

## Roadmap

- [ ] Phase 1: 基础时间解析（绝对 + 相对）
- [ ] Phase 2: 模糊时间支持
- [ ] Phase 3: 任务提取和优先级
- [ ] Phase 4: 提醒调度系统
- [ ] Phase 5: 持久化存储（SQLite/JSON）
- [ ] Phase 6: 多语言支持扩展

## 贡献指南

参考项目根目录的 `CLAUDE.md` 规范。

## 许可证

MIT
