/**
 * 智能内容检测器
 * 自动判断是否应该记录内容
 */

class AutoDetector {
  constructor() {
    // 模式定义
    this.patterns = {
      bugfix: {
        keywords: ['修复了', '解决了', 'fix', 'bug', '错误', '问题', '异常', '失败'],
        weight: 5  // 优先级
      },
      decision: {
        keywords: ['决定', '选择', '采用', '使用', '不用', '放弃', 'prefer', 'decide'],
        weight: 4
      },
      feature: {
        keywords: ['实现', '添加', '新增', '完成了', 'develop', 'implement', 'feature'],
        weight: 3
      },
      refactor: {
        keywords: ['重构', '优化', '改进', '改善', 'refactor', 'optimize'],
        weight: 3
      },
      discovery: {
        keywords: ['发现', '注意到', '原来', '竟然', '发现是', 'found', 'noticed'],
        weight: 2
      },
      config: {
        keywords: ['配置', '设置', 'timeout', '环境变量', 'config', 'setting'],
        weight: 2
      }
    };

    // 忽略模式（这些内容不记录）
    this.ignorePatterns = [
      /^help$/i,
      /^谢谢/i,
      /^好的/i,
      /^可以/i,
      /^知道/i,
      /^\s*$/,
      /^(what|how|why|where|when|who)\s/i  // 问句开头，后面有空格
    ];
  }

  /**
   * 检测内容是否应该记录
   * @param {string} content - 用户输入的内容
   * @returns {object} - { shouldRecord: boolean, category: string, confidence: number }
   */
  detect(content) {
    // 1. 检查是否应该忽略
    if (this.shouldIgnore(content)) {
      return {
        shouldRecord: false,
        reason: 'matched ignore pattern'
      };
    }

    // 2. 检测分类和置信度
    const detection = this.detectCategory(content);

    // 3. 判断是否值得记录
    if (detection.confidence >= 3) {
      return {
        shouldRecord: true,
        category: detection.category,
        confidence: detection.confidence,
        matchedKeywords: detection.matchedKeywords
      };
    }

    return {
      shouldRecord: false,
      reason: 'low confidence',
      confidence: detection.confidence
    };
  }

  /**
   * 检测内容的分类
   */
  detectCategory(content) {
    const lowerContent = content.toLowerCase();
    let bestMatch = {
      category: 'general',
      confidence: 0,
      matchedKeywords: []
    };

    // 遍历所有模式
    for (const [category, pattern] of Object.entries(this.patterns)) {
      let matchedCount = 0;
      const matchedKeywords = [];

      // 检查每个关键词
      for (const keyword of pattern.keywords) {
        if (lowerContent.includes(keyword.toLowerCase())) {
          matchedCount++;
          matchedKeywords.push(keyword);
        }
      }

      // 计算置信度 = 匹配数量 * 权重
      const confidence = matchedCount * pattern.weight;

      if (confidence > bestMatch.confidence) {
        bestMatch = {
          category,
          confidence,
          matchedKeywords
        };
      }
    }

    return bestMatch;
  }

  /**
   * 检查是否应该忽略
   */
  shouldIgnore(content) {
    const trimmed = content.trim();

    // 检查忽略模式
    for (const pattern of this.ignorePatterns) {
      if (pattern.test(trimmed)) {
        return true;
      }
    }

    // 检查内容长度（太短的不记录）
    // 使用 Array.from 正确处理中文等多字节字符
    if (Array.from(trimmed).length < 5) {
      return true;
    }

    return false;
  }

  /**
   * 生成记录建议
   */
  generateSuggestion(content, detection) {
    const suggestions = {
      bugfix: `检测到 Bug 修复：${content}`,
      decision: `检测到技术决策：${content}`,
      feature: `检测到新功能实现：${content}`,
      refactor: `检测到代码优化：${content}`,
      discovery: `检测到新发现：${content}`,
      config: `检测到配置变更：${content}`,
      general: `检测到重要内容：${content}`
    };

    return suggestions[detection.category] || suggestions.general;
  }
}

module.exports = AutoDetector;
