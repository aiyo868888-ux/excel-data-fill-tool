# 价值观分析系统 - 集成使用指南

## ✅ 已完成集成

记忆系统和价值观检测已完全集成！

---

## 🚀 快速开始

### 基本使用

```javascript
const MemoryWithValues = require('./skills/simple-values/memory-with-values.js');

// 创建系统实例
const system = new MemoryWithValues();

// 添加对话，自动分析价值观
const result = system.addWithValues(
  '我认为产品应该专注单点，做到极致',
  'conversation',
  {
    speaker: '产品经理',
    context: '产品方向讨论'
  }
);

console.log('记录:', result.record);
console.log('检测到的价值观:', result.values);
```

### 查看价值观画像

```javascript
// 获取价值观画像
const profile = system.getValuesProfile();

console.log('Top 5 特征:', profile.profile.slice(0, 5));
console.log('主导维度:', profile.analysis.dominantTraits);
console.log('总结:', profile.analysis.summary);
```

---

## 📋 完整示例

### 场景：分析产品经理价值观

```javascript
const MemoryWithValues = require('./skills/simple-values/memory-with-values.js');
const system = new MemoryWithValues();

// 记录多段对话
const conversations = [
  '我认为产品应该专注单点，做到极致',
  '数据驱动决策，不能凭感觉',
  '用户体验是第一位的',
  '快速迭代，先上线再优化'
];

conversations.forEach(content => {
  system.addWithValues(content, 'conversation', {
    speaker: '产品经理',
    date: '2026-01-25'
  });
});

// 分析画像
const profile = system.getValuesProfile();

console.log('产品经理的价值观画像：');
profile.profile.slice(0, 5).forEach((item, i) => {
  console.log(`${i + 1}. ${item.dimension} - ${item.value}`);
  console.log(`   得分: ${item.score}`);
  console.log(`   示例: "${item.examples[0]}"`);
});
```

**输出：**
```
产品经理的价值观画像：
1. 产品哲学 - 极简主义
   得分: 10
   示例: "我认为产品应该专注单点，做到极致"

2. 决策风格 - 数据驱动
   得分: 8
   示例: "数据驱动决策，不能凭感觉"

3. 工作态度 - 快速交付
   得分: 12
   示例: "快速迭代，先上线再优化"
```

---

## 🎯 API 参考

### 1. addWithValues(content, category, metadata)

添加记录并自动分析价值观

**参数：**
- `content`: 对话内容
- `category`: 分类（默认 'conversation'）
- `metadata`: 元数据（speaker, context 等）

**返回：**
```javascript
{
  record: {
    id: "mktecu56kh2m4et31wr",
    content: "...",
    category: "conversation",
    metadata: {
      speaker: "产品经理",
      values: [
        { dimension: "产品哲学", value: "极简主义", score: 5 }
      ]
    }
  },
  values: [
    {
      dimension: "产品哲学",
      dimensionKey: "product_philosophy",
      value: "极简主义",
      keywords: ["专注单点"],
      score: 5
    }
  ]
}
```

### 2. getValuesProfile(limit)

获取价值观画像

**参数：**
- `limit`: 分析最近多少条记录（默认 100）

**返回：**
```javascript
{
  profile: [
    {
      dimension: "产品哲学",
      value: "极简主义",
      score: 25,
      count: 3,
      examples: ["对话1", "对话2", "对话3"]
    }
  ],
  analysis: {
    topValues: [...],
    dominantTraits: [
      {
        dimension: "产品哲学",
        score: 50,
        description: "对产品方向和优先级的看法"
      }
    ],
    summary: "主导价值观是产品哲学中的'极简主义'倾向"
  },
  totalRecords: 50,
  valuesRecords: 30
}
```

### 3. searchByValue(dimension, value)

搜索包含特定价值观的记录

**示例：**
```javascript
// 搜索所有"极简主义"的记录
const results = system.searchByValue("产品哲学", "极简主义");

results.forEach(record => {
  console.log(record.content);
  console.log(record.timestamp);
});
```

### 4. getStatsByValues()

按价值观统计

**返回：**
```javascript
{
  byDimension: {
    "产品哲学": 50,
    "决策风格": 30
  },
  byValue: {
    "产品哲学:极简主义": 25,
    "决策风格:数据驱动": 15
  },
  total: 50
}
```

---

## 💡 使用场景

### 场景 1：面试候选人

```javascript
// 记录面试对话
system.addWithValues('快速迭代，先上线再优化', 'interview', {
  candidate: '张三',
  position: '产品经理'
});

system.addWithValues('数据驱动决策', 'interview', {
  candidate: '张三',
  position: '产品经理'
});

// 分析候选人价值观
const profile = system.getValuesProfile();
console.log('候选人画像:', profile.analysis.summary);
```

### 场景 2：团队协作分析

```javascript
// 记录团队讨论
teamMembers.forEach(member => {
  system.addWithValues(member.statement, 'meeting', {
    speaker: member.name,
    role: member.role
  });
});

// 分析团队价值观分布
const stats = system.getStatsByValues();
console.log('团队价值观分布:', stats.byDimension);
```

### 场景 3：产品经理画像

```javascript
// 长期记录产品经理的发言
productManagerQuotes.forEach(quote => {
  system.addWithValues(quote, 'product', {
    speaker: '产品经理',
    date: new Date().toISOString()
  });
});

// 生成画像
const profile = system.getValuesProfile();

// 判断价值观类型
if (profile.analysis.summary.includes('极简主义')) {
  console.log('这是极简主义产品经理');
} else if (profile.analysis.summary.includes('功能主义')) {
  console.log('这是功能主义产品经理');
}
```

---

## 🔍 数据查询

### 查看所有记录

```javascript
const records = system.getRecent(10);
records.forEach(record => {
  console.log(`[${record.category}] ${record.content}`);
  if (record.metadata.values) {
    console.log(`  价值观: ${record.metadata.values.map(v => v.value).join(', ')}`);
  }
});
```

### 搜索特定内容

```javascript
// 搜索包含"数据"的记录
const results = system.search('数据');

results.forEach(record => {
  console.log(record.content);
});
```

### 按价值观筛选

```javascript
// 找出所有"数据驱动"的对话
const dataDriven = system.searchByValue('决策风格', '数据驱动');

console.log(`找到 ${dataDriven.length} 条数据驱动的对话`);
```

---

## 📊 数据可视化

### 雷达图数据

```javascript
const stats = system.getStatsByValues();

// 雷达图数据（可用于 Excel/Python 可视化）
const radarData = stats.byDimension;

console.log('雷达图数据:', JSON.stringify(radarData, null, 2));

// 输出：
// {
//   "产品哲学": 50,
//   "决策风格": 30,
//   "工作态度": 25,
//   "技术观点": 15,
//   "团队观念": 10,
//   "风险态度": 8
// }
```

### 价值观分布图

```javascript
const profile = system.getValuesProfile();

// Top 10 特征分布
const top10 = profile.profile.slice(0, 10);

top10.forEach(item => {
  console.log(`${item.dimension} - ${item.value}: ${item.score}`);
});
```

---

## ⚙️ 高级用法

### 批量导入历史记录

```javascript
const conversations = [
  { content: '专注单点', speaker: 'A', date: '2026-01-01' },
  { content: '数据驱动', speaker: 'A', date: '2026-01-02' },
  // ... 更多记录
];

conversations.forEach(conv => {
  system.addWithValues(
    conv.content,
    'imported',
    {
      speaker: conv.speaker,
      date: conv.date
    }
  );
});
```

### 对比多个人的价值观

```javascript
// 记录 A 的对话
personAQuotes.forEach(q => {
  system.addWithValues(q, 'conversation', { speaker: 'A' });
});

// 记录 B 的对话
personBQuotes.forEach(q => {
  system.addWithValues(q, 'conversation', { speaker: 'B' });
});

// 对比分析
const profileA = system.getValuesProfile();
// ... 对比分析
```

---

## 💾 数据存储

### 存储位置

```
.memory/memory.json
```

### 数据格式

```json
[
  {
    "id": "mktecu56kh2m4et31wr",
    "timestamp": "2026-01-25T07:08:09.498Z",
    "content": "我认为产品应该专注单点",
    "category": "conversation",
    "metadata": {
      "speaker": "产品经理",
      "values": [
        {
          "dimension": "产品哲学",
          "value": "极简主义",
          "score": 5
        }
      ]
    },
    "project": "my-project"
  }
]
```

---

## 🎯 最佳实践

### 1. 持续记录

```javascript
// 每次对话后记录
system.addWithValues(对话内容, 'conversation', {
  speaker: '姓名',
  context: '讨论主题'
});
```

### 2. 定期分析

```javascript
// 每周分析一次
setInterval(() => {
  const profile = system.getValuesProfile();
  console.log('本周价值观画像:', profile.analysis.summary);
}, 7 * 24 * 60 * 60 * 1000);
```

### 3. 多维度对比

```javascript
// 对比不同人的价值观
const people = ['A', 'B', 'C'];

people.forEach(person => {
  const profile = system.getValuesProfile();
  console.log(`${person} 的价值观:`, profile.analysis.summary);
});
```

---

## ✅ 总结

**集成系统 = 记忆系统 + 价值观检测**

- ✅ 自动检测价值观
- ✅ 生成价值观画像
- ✅ 支持搜索和筛选
- ✅ 可视化数据
- ✅ 多场景应用

**开始使用：**
```javascript
const system = new MemoryWithValues();
system.addWithValues('你的对话', 'conversation');
const profile = system.getValuesProfile();
console.log(profile.analysis.summary);
```
