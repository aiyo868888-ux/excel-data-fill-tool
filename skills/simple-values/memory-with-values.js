#!/usr/bin/env node
/**
 * 记忆系统 + 价值观检测集成
 */

const SmartMemory = require('../simple-memory/smart-memory.js');
const ValuesDetector = require('./values-detector.js');

class MemoryWithValues {
  constructor(projectRoot) {
    this.memory = new SmartMemory(projectRoot);
    this.detector = new ValuesDetector();
  }

  /**
   * 添加记录并自动分析价值观
   */
  addWithValues(content, category = 'conversation', metadata = {}) {
    // 1. 检测价值观
    const values = this.detector.detect(content);

    // 2. 添加价值观信息到元数据
    const enhancedMetadata = {
      ...metadata,
      values: values.map(v => ({
        dimension: v.dimension,
        value: v.value,
        score: v.score
      }))
    };

    // 3. 添加到记忆
    const record = this.memory.add(content, category, enhancedMetadata);

    return {
      record,
      values
    };
  }

  /**
   * 获取价值观画像
   */
  getValuesProfile(limit = 100) {
    // 获取最近的记录
    const records = this.memory.getRecent(limit);

    // 提取所有价值观数据
    const allValues = [];
    records.forEach(record => {
      // values 存储在 metadata 中
      if (record.metadata && record.metadata.values && record.metadata.values.length > 0) {
        record.metadata.values.forEach(v => {
          allValues.push({
            ...v,
            content: record.content,
            timestamp: record.timestamp
          });
        });
      }
    });

    // 生成画像
    const profile = this.detector.generateProfile(allValues);
    const analysis = this.detector.analyze(profile);

    return {
      profile,
      analysis,
      totalRecords: records.length,
      valuesRecords: allValues.length
    };
  }

  /**
   * 搜索包含特定价值观的记录
   */
  searchByValue(dimension, value) {
    const records = this.memory.getRecent(1000);

    return records.filter(record => {
      if (!record.metadata || !record.metadata.values) return false;

      return record.metadata.values.some(v =>
        v.dimension === dimension && v.value === value
      );
    });
  }

  /**
   * 按价值观统计
   */
  getStatsByValues() {
    const profile = this.getValuesProfile();
    const stats = {
      byDimension: {},
      byValue: {},
      total: profile.totalRecords
    };

    // 统计维度
    profile.profile.forEach(item => {
      if (!stats.byDimension[item.dimension]) {
        stats.byDimension[item.dimension] = 0;
      }
      stats.byDimension[item.dimension] += item.score;
    });

    // 统计具体值
    profile.profile.forEach(item => {
      const key = `${item.dimension}:${item.value}`;
      if (!stats.byValue[key]) {
        stats.byValue[key] = 0;
      }
      stats.byValue[key] += item.score;
    });

    return stats;
  }

  // 代理所有 SmartMemory 的方法
  add(...args) { return this.memory.add(...args); }
  search(...args) { return this.memory.search(...args); }
  getRecent(...args) { return this.memory.getRecent(...args); }
  getByCategory(...args) { return this.memory.getByCategory(...args); }
  delete(...args) { return this.memory.delete(...args); }
  getStats() { return this.memory.getStats(); }
}

module.exports = MemoryWithValues;
