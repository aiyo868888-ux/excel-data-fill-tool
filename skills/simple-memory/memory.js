#!/usr/bin/env node
/**
 * Simple Memory - 极简记忆系统
 * 产品哲学：专注单点，做到极致
 */

const fs = require('fs');
const path = require('path');
const os = require('os');

class SimpleMemory {
  constructor(projectRoot) {
    this.projectRoot = projectRoot || process.cwd();
    this.memoryDir = path.join(this.projectRoot, '.memory');
    this.memoryFile = path.join(this.memoryDir, 'memory.json');
    this.configFile = path.join(this.projectRoot, '.memory-config.json');
    this.ensureMemoryDir();
  }

  ensureMemoryDir() {
    if (!fs.existsSync(this.memoryDir)) {
      fs.mkdirSync(this.memoryDir, { recursive: true });
    }
  }

  getConfig() {
    if (fs.existsSync(this.configFile)) {
      return JSON.parse(fs.readFileSync(this.configFile, 'utf8'));
    }
    return {
      autoRecord: true,
      maxRecords: 1000,
      categories: ['decision', 'bugfix', 'feature', 'refactor', 'discovery']
    };
  }

  getMemories() {
    if (!fs.existsSync(this.memoryFile)) {
      return [];
    }
    try {
      return JSON.parse(fs.readFileSync(this.memoryFile, 'utf8'));
    } catch {
      return [];
    }
  }

  saveMemories(memories) {
    const config = this.getConfig();
    const trimmed = memories.slice(-config.maxRecords);
    fs.writeFileSync(this.memoryFile, JSON.stringify(trimmed, null, 2), 'utf8');
    return trimmed;
  }

  add(content, category = 'general', metadata = {}) {
    const memories = this.getMemories();
    const memory = {
      id: Date.now().toString(36) + Math.random().toString(36).substr(2),
      timestamp: new Date().toISOString(),
      content,
      category,
      metadata,
      project: path.basename(this.projectRoot)
    };
    memories.push(memory);
    this.saveMemories(memories);
    return memory;
  }

  search(query) {
    const memories = this.getMemories();
    const lowerQuery = query.toLowerCase();

    return memories.filter(m => {
      const content = m.content.toLowerCase();
      const category = m.category.toLowerCase();
      return content.includes(lowerQuery) || category.includes(lowerQuery);
    });
  }

  getRecent(limit = 10) {
    const memories = this.getMemories();
    return memories.slice(-limit).reverse();
  }

  getByCategory(category) {
    const memories = this.getMemories();
    return memories.filter(m => m.category === category);
  }

  delete(id) {
    const memories = this.getMemories();
    const filtered = memories.filter(m => m.id !== id);
    this.saveMemories(filtered);
  }

  getStats() {
    const memories = this.getMemories();
    const byCategory = {};
    memories.forEach(m => {
      byCategory[m.category] = (byCategory[m.category] || 0) + 1;
    });

    return {
      total: memories.length,
      byCategory,
      latest: memories.length > 0 ? memories[memories.length - 1] : null
    };
  }
}

// CLI 接口
if (require.main === module) {
  const memory = new SimpleMemory();
  const args = process.argv.slice(2);
  const command = args[0];

  switch (command) {
    case 'add':
      const content = args[1];
      const category = args[2] || 'general';
      if (content) {
        const result = memory.add(content, category);
        console.log('✅ 已记录:', result.id);
        console.log('   内容:', content);
      } else {
        console.log('❌ 请提供记录内容');
      }
      break;

    case 'search':
      const query = args[1];
      if (query) {
        const results = memory.search(query);
        console.log(`🔍 搜索 "${query}" 找到 ${results.length} 条记录:`);
        results.forEach(m => {
          console.log(`\n[${m.category}] ${m.timestamp}`);
          console.log(`  ${m.content}`);
        });
      } else {
        console.log('❌ 请提供搜索关键词');
      }
      break;

    case 'list':
      const limit = parseInt(args[1]) || 10;
      const recent = memory.getRecent(limit);
      console.log(`📋 最近 ${recent.length} 条记录:`);
      recent.forEach(m => {
        console.log(`\n[${m.category}] ${m.timestamp}`);
        console.log(`  ${m.content}`);
      });
      break;

    case 'stats':
      const stats = memory.getStats();
      console.log('📊 记忆统计:');
      console.log(`   总数: ${stats.total}`);
      console.log('   分类:', stats.byCategory);
      if (stats.latest) {
        console.log(`   最新: ${stats.latest.content}`);
      }
      break;

    case 'delete':
      const id = args[1];
      if (id) {
        memory.delete(id);
        console.log('✅ 已删除:', id);
      } else {
        console.log('❌ 请提供记录 ID');
      }
      break;

    default:
      console.log(`
Simple Memory - 极简记忆系统

用法:
  node memory.js add <内容> [分类]     - 记录内容
  node memory.js search <关键词>       - 搜索记录
  node memory.js list [数量]           - 查看最近记录
  node memory.js stats                 - 查看统计
  node memory.js delete <id>           - 删除记录

示例:
  node memory.js add "修复了登录bug" bugfix
  node memory.js search 登录
  node memory.js list 5
      `);
  }
}

module.exports = SimpleMemory;
