#!/usr/bin/env node
/**
 * 测试集成系统
 */

const MemoryWithValues = require('./memory-with-values.js');

const system = new MemoryWithValues();

console.log('🧪 记忆系统 + 价值观检测集成测试\n');
console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n');

// 模拟对话场景
const conversations = [
  {
    content: '我认为产品应该专注单点，做到极致',
    speaker: '产品经理',
    context: '产品方向讨论'
  },
  {
    content: '我们要数据驱动决策，不能凭感觉',
    speaker: '产品经理',
    context: '决策方式讨论'
  },
  {
    content: '快速迭代，先上线再优化',
    speaker: '技术负责人',
    context: '开发节奏讨论'
  },
  {
    content: '用户体验是第一位的',
    speaker: '产品经理',
    context: '需求优先级讨论'
  },
  {
    content: '技术要服务于业务，不要为了技术而技术',
    speaker: 'CTO',
    context: '技术选型讨论'
  },
  {
    content: '团队协作很重要，要多沟通',
    speaker: '项目经理',
    context: '团队管理讨论'
  },
  {
    content: '要敢于尝试新东西，不怕失败',
    speaker: '创始人',
    context: '创新讨论'
  },
  {
    content: '质量很重要，不能为了快就降低标准',
    speaker: '技术负责人',
    context: '质量vs速度讨论'
  }
];

console.log('📝 添加对话记录：\n');

conversations.forEach((conv, i) => {
  console.log(`${i + 1}. [${conv.speaker}] "${conv.content}"`);

  // 添加到记忆系统
  const result = system.addWithValues(
    conv.content,
    'conversation',
    {
      speaker: conv.speaker,
      context: conv.context
    }
  );

  // 显示检测到的价值观
  if (result.values.length > 0) {
    console.log(`   → 检测到价值观:`);
    result.values.forEach(v => {
      console.log(`     • ${v.dimension}: ${v.value} (得分 ${v.score})`);
    });
  } else {
    console.log(`   → 未检测到明显价值观`);
  }
  console.log();
});

console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n');
console.log('👤 价值观画像分析：\n');

// 获取价值观画像
const profile = system.getValuesProfile();

console.log(`分析记录数: ${profile.totalRecords}`);
console.log(`包含价值观: ${profile.valuesRecords}条\n`);

console.log('Top 5 价值观特征：\n');
profile.profile.slice(0, 5).forEach((item, i) => {
  console.log(`${i + 1}. ${item.dimension} - ${item.value}`);
  console.log(`   得分: ${item.score}`);
  console.log(`   出现次数: ${item.count}`);
  console.log(`   示例: "${item.examples[0]}"`);
  console.log();
});

console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n');
console.log('🎯 主导维度：\n');

profile.analysis.dominantTraits.forEach((trait, i) => {
  console.log(`${i + 1}. ${trait.dimension} (得分: ${trait.score})`);
  console.log(`   ${trait.description}`);
  console.log();
});

console.log('📊 总结：');
console.log(profile.analysis.summary);
console.log();

console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n');
console.log('📈 按维度统计：\n');

const stats = system.getStatsByValues();

console.log('各维度得分：');
Object.entries(stats.byDimension)
  .sort((a, b) => b[1] - a[1])
  .forEach(([dim, score]) => {
    console.log(`  ${dim}: ${score}`);
  });

console.log('\n各特征得分：');
Object.entries(stats.byValue)
  .sort((a, b) => b[1] - a[1])
  .slice(0, 5)
  .forEach(([value, score]) => {
    console.log(`  ${value}: ${score}`);
  });

console.log('\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n');
console.log('✅ 测试完成！\n');

console.log('💡 使用提示：\n');
console.log('1. 查看所有记录:');
console.log('   system.getRecent(10)\n');

console.log('2. 搜索特定价值观的记录:');
console.log('   system.searchByValue("产品哲学", "极简主义")\n');

console.log('3. 搜索内容:');
console.log('   system.search("数据")\n');

console.log('4. 查看统计:');
console.log('   system.getStats()\n');
