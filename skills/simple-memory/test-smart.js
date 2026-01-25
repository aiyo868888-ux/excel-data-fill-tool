#!/usr/bin/env node
/**
 * 测试智能记忆系统
 */

const SmartMemory = require('./smart-memory.js');

const smartMemory = new SmartMemory();

console.log('🧪 Smart Memory 智能检测测试\n');
console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n');

// 测试用例
const testCases = [
  {
    content: '修复了登录超时的 bug',
    expected: 'bugfix',
    description: 'Bug 修复'
  },
  {
    content: '决定使用 PostgreSQL 而不是 MySQL',
    expected: 'decision',
    description: '技术决策'
  },
  {
    content: '实现了用户认证功能',
    expected: 'feature',
    description: '新功能'
  },
  {
    content: '重构了用户模块',
    expected: 'refactor',
    description: '代码重构'
  },
  {
    content: '发现原来 timeout 设置太短了',
    expected: 'discovery',
    description: '发现学习'
  },
  {
    content: '配置了 timeout 为 30 分钟',
    expected: 'config',
    description: '配置变更'
  },
  {
    content: '帮我写个函数',
    expected: 'ignore',
    description: '临时请求（应忽略）'
  },
  {
    content: '好的',
    expected: 'ignore',
    description: '短句（应忽略）'
  }
];

let passCount = 0;
let failCount = 0;

testCases.forEach((testCase, index) => {
  const detection = smartMemory.detector.detect(testCase.content);

  const passed = testCase.expected === 'ignore'
    ? !detection.shouldRecord
    : detection.shouldRecord && detection.category === testCase.expected;

  const status = passed ? '✅ PASS' : '❌ FAIL';

  if (passed) {
    passCount++;
  } else {
    failCount++;
  }

  console.log(`测试 ${index + 1}: ${testCase.description}`);
  console.log(`内容: "${testCase.content}"`);
  console.log(`预期: ${testCase.expected}`);
  console.log(`实际: ${detection.shouldRecord ? detection.category : 'ignore'}`);

  if (detection.shouldRecord) {
    console.log(`置信度: ${detection.confidence}/5 ⭐`);
    console.log(`匹配关键词: ${detection.matchedKeywords.join(', ')}`);
  } else {
    console.log(`原因: ${detection.reason}`);
  }

  console.log(`${status}\n`);
});

console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
console.log(`\n📊 测试结果:`);
console.log(`   ✅ 通过: ${passCount}/${testCases.length}`);
console.log(`   ❌ 失败: ${failCount}/${testCases.length}`);
console.log(`   准确率: ${Math.round((passCount / testCases.length) * 100)}%\n`);

// 测试自动记录
console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
console.log('\n🔄 测试自动记录功能\n');

const autoTestCases = [
  '修复了数据库连接问题',
  '选择 React 作为前端框架',
  '怎么配置环境变量？'
];

autoTestCases.forEach(content => {
  console.log(`输入: "${content}"`);
  const result = smartMemory.processInput(content);

  if (result.shouldRecord) {
    console.log(`✅ 自动记录成功！`);
    console.log(`   分类: [${result.category}]`);
    console.log(`   ID: ${result.record.id}`);
  } else {
    console.log(`⏭️  跳过记录: ${result.reason}`);
  }
  console.log();
});

// 查看统计
console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
console.log('\n📊 记忆统计:');
const stats = smartMemory.getStats();
console.log(`   总数: ${stats.total}`);
Object.entries(stats.byCategory).forEach(([cat, count]) => {
  console.log(`   ${cat}: ${count}`);
});
console.log();
