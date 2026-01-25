const AutoDetector = require('./auto-detect.js');
const detector = new AutoDetector();

// 添加调试
const originalShouldIgnore = detector.shouldIgnore.bind(detector);
detector.shouldIgnore = function(content) {
  const trimmed = content.trim();

  console.log('  Checking shouldIgnore for:', trimmed);
  console.log('  Length:', trimmed.length);

  detector.ignorePatterns.forEach((pattern, i) => {
    const match = pattern.test(trimmed);
    if (match) {
      console.log(`  ✓ Matched pattern ${i}: ${pattern}`);
    }
  });

  const result = originalShouldIgnore(content);
  console.log('  shouldIgnore result:', result);
  console.log();
  return result;
};

const tests = [
  '实现了用户认证功能',
  '重构了用户模块',
  '修复了登录bug'
];

tests.forEach(test => {
  console.log('Testing:', test);
  const result = detector.detect(test);
  console.log('Final result:', result);
  console.log('---\n');
});
