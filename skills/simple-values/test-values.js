#!/usr/bin/env node
/**
 * 测试价值观检测
 */

const ValuesDetector = require('./values-detector.js');

const detector = new ValuesDetector();

console.log('🎭 价值观检测器测试\n');
console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n');

// 测试对话
const conversations = [
  '我认为产品应该专注单点，做减法，不要追求功能全面',
  '我们要数据驱动决策，不能凭感觉，要做a/b测试',
  '用户反馈很重要，要多听用户的声音',
  '快速迭代，先上线再优化，done is better than perfect',
  '技术是工具，要服务于业务，不要为了技术而技术',
  '团队协作很重要，要多沟通，不能一个人闷头干',
  '要敢于尝试新东西，不怕失败，拥抱风险',
  '用户体验是第一位的，商业化可以后面考虑',
  '质量很重要，不能为了快就降低标准',
  '我们要追求极致，做到最好'
];

console.log('📝 测试对话：\n');
conversations.forEach((conv, i) => {
  console.log(`${i + 1}. "${conv}"`);
});
console.log('\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n');

// 检测每条对话
console.log('🔍 逐条分析：\n');
const allRecords = [];

conversations.forEach((conv, i) => {
  const results = detector.detect(conv);

  console.log(`对话 ${i + 1}:`);
  if (results.length > 0) {
    results.forEach(r => {
      console.log(`  → ${r.dimension}: ${r.value}`);
      console.log(`     关键词: ${r.keywords.join(', ')}`);
      console.log(`     得分: ${r.score}`);
      allRecords.push(r);
    });
  } else {
    console.log(`  → 未检测到明显价值观`);
  }
  console.log();
});

// 生成画像
console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n');
console.log('👤 价值观画像：\n');

const profile = detector.generateProfile(allRecords);

console.log('Top 5 价值观特征：\n');
profile.slice(0, 5).forEach((item, i) => {
  console.log(`${i + 1}. ${item.dimension} - ${item.value}`);
  console.log(`   得分: ${item.score}`);
  console.log(`   出现次数: ${item.count}`);
  console.log(`   示例: "${item.examples[0]}"`);
  console.log();
});

// 分析
console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n');
const analysis = detector.analyze(profile);

console.log('🎯 主导维度：\n');
analysis.dominantTraits.forEach((trait, i) => {
  console.log(`${i + 1}. ${trait.dimension} (得分: ${trait.score})`);
  console.log(`   ${trait.description}`);
  console.log();
});

console.log('📊 总结：');
console.log(analysis.summary);
console.log();

// 价值观雷达图数据
console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n');
console.log('📈 雷达图数据（可用于可视化）：\n');

const radarData = {};
profile.forEach(item => {
  const dim = item.dimension;
  if (!radarData[dim]) {
    radarData[dim] = 0;
  }
  radarData[dim] += item.score;
});

console.log(JSON.stringify(radarData, null, 2));
console.log();
