#!/usr/bin/env node
/**
 * Smart Memory - 智能记忆系统
 * 基于内容分析自动触发记录
 */

const SimpleMemory = require('./memory.js');
const AutoDetector = require('./auto-detect.js');

class SmartMemory {
  constructor(projectRoot) {
    this.memory = new SimpleMemory(projectRoot);
    this.detector = new AutoDetector();
  }

  /**
   * 处理用户输入，自动判断是否记录
   * @param {string} content - 用户输入的内容
   * @returns {object} - 处理结果
   */
  processInput(content) {
    // 检测内容
    const detection = this.detector.detect(content);

    if (!detection.shouldRecord) {
      return {
        shouldRecord: false,
        reason: detection.reason
      };
    }

    // 自动记录
    const record = this.memory.add(
      content,
      detection.category,
      { autoDetected: true, confidence: detection.confidence }
    );

    return {
      shouldRecord: true,
      record,
      category: detection.category,
      confidence: detection.confidence,
      matchedKeywords: detection.matchedKeywords
    };
  }

  /**
   * 生成友好的提示消息
   */
  generatePrompt(content, detection) {
    const suggestion = this.detector.generateSuggestion(content, detection);

    return {
      message: `🤖 ${suggestion}`,
      category: detection.category,
      confidence: detection.confidence,
      questions: [
        `是否自动记录到 [${detection.category}] 分类？`,
        `匹配关键词：${detection.matchedKeywords.join(', ')}`
      ]
    };
  }

  // 代理 SimpleMemory 的所有方法
  add(...args) { return this.memory.add(...args); }
  search(...args) { return this.memory.search(...args); }
  getRecent(...args) { return this.memory.getRecent(...args); }
  getByCategory(...args) { return this.memory.getByCategory(...args); }
  delete(...args) { return this.memory.delete(...args); }
  getStats() { return this.memory.getStats(); }
}

// CLI 接口
if (require.main === module) {
  const smartMemory = new SmartMemory();
  const args = process.argv.slice(2);
  const command = args[0];

  switch (command) {
    case 'test':
      // 测试智能检测
      const testContent = args.slice(1).join(' ');
      if (!testContent) {
        console.log('用法: node smart-memory.js test "内容"');
        process.exit(1);
      }

      const detection = smartMemory.detector.detect(testContent);

      console.log('\n🤖 智能检测结果：');
      console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
      console.log(`内容: ${testContent}`);
      console.log(`应该记录: ${detection.shouldRecord ? '✅ 是' : '❌ 否'}`);

      if (detection.shouldRecord) {
        console.log(`分类: ${detection.category}`);
        console.log(`置信度: ${detection.confidence}/5`);
        console.log(`匹配关键词: ${detection.matchedKeywords.join(', ')}`);

        const prompt = smartMemory.generatePrompt(testContent, detection);
        console.log(`\n${prompt.message}`);
        console.log(`置信度: ${prompt.confidence}/5`);
      } else {
        console.log(`原因: ${detection.reason}`);
      }
      console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n');
      break;

    case 'auto':
      // 自动处理并记录
      const autoContent = args.slice(1).join(' ');
      if (!autoContent) {
        console.log('用法: node smart-memory.js auto "内容"');
        process.exit(1);
      }

      const result = smartMemory.processInput(autoContent);

      if (result.shouldRecord) {
        console.log('✅ 自动记录成功！');
        console.log(`   分类: [${result.category}]`);
        console.log(`   内容: ${autoContent}`);
        console.log(`   ID: ${result.record.id}`);
      } else {
        console.log(`⏭️  跳过记录: ${result.reason}`);
      }
      break;

    default:
      // 其他命令透传给 SimpleMemory
      const memoryArgs = ['memory.js', ...args];
      const { spawn } = require('child_process');
      const child = spawn('node', memoryArgs, {
        cwd: __dirname,
        stdio: 'inherit'
      });
      child.on('exit', (code) => process.exit(code));
  }
}

module.exports = SmartMemory;
