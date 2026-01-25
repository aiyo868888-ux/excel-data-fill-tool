/**
 * 价值观检测器
 * 通过对话分析一个人的价值观
 */

class ValuesDetector {
  constructor() {
    // 价值观维度定义
    this.dimensions = {
      // 产品哲学
      product_philosophy: {
        name: '产品哲学',
        keywords: {
          '极简主义': ['极简', '简单', '专注单点', '做减法', '少即是多', '克制'],
          '功能主义': ['功能全面', '做加法', '丰富功能', '满足所有需求'],
          '用户体验': ['用户体验', '用户至上', '体验优先', '用户感受'],
          '商业价值': ['商业化', '盈利', '商业价值', '付费', '增长'],
        },
        weight: 5
      },

      // 决策风格
      decision_style: {
        name: '决策风格',
        keywords: {
          '数据驱动': ['数据驱动', '数据分析', 'a/b测试', '数据说话', '指标'],
          '直觉决策': ['直觉', '经验判断', '感觉', '相信直觉'],
          '用户反馈': ['用户反馈', '用户声音', '用户调研', '用户意见'],
          '快速试错': ['快速试错', '迭代', '小步快跑', '敏捷'],
          '谨慎决策': ['谨慎', '充分调研', '深思熟虑', '风险评估'],
        },
        weight: 4
      },

      // 工作态度
      work_attitude: {
        name: '工作态度',
        keywords: {
          '完美主义': ['完美', '精益求精', '追求极致', '质量第一'],
          '快速交付': ['快速', '效率优先', '先上线再优化', 'done is better than perfect'],
          '稳定优先': ['稳定', '可靠', '不出错', '安全第一'],
          '创新优先': ['创新', '突破', '不墨守成规', '尝试新方法'],
        },
        weight: 4
      },

      // 技术观点
      tech_viewpoint: {
        name: '技术观点',
        keywords: {
          '技术驱动': ['技术驱动', '技术优先', '技术壁垒', '核心技术'],
          '业务优先': ['业务优先', '技术服务业务', '解决问题', '工具'],
          '技术保守': ['成熟技术', '稳定优先', '不追新', '避免风险'],
          '技术激进': ['新技术', '前沿技术', '尝鲜', '技术栈升级'],
        },
        weight: 3
      },

      // 团队观念
      teamwork: {
        name: '团队观念',
        keywords: {
          '个人英雄': ['个人英雄', '独狼', '独立完成', '一个人搞定'],
          '团队协作': ['团队协作', '团队合作', '沟通', '配合'],
          '透明开放': ['透明', '开放', '信息共享', '公开讨论'],
          '层级管理': ['层级', '流程', '规范', '审批'],
        },
        weight: 3
      },

      // 风险态度
      risk_attitude: {
        name: '风险态度',
        keywords: {
          '风险偏好': ['大胆', '敢于尝试', '不怕失败', '拥抱风险'],
          '风险厌恶': ['稳健', '保守', '风险控制', '避免失败'],
          '风险平衡': ['平衡', '适度', '可控风险', '试错'],
        },
        weight: 3
      }
    };
  }

  /**
   * 检测内容中的价值观
   */
  detect(content) {
    const results = [];
    const lowerContent = content.toLowerCase();

    // 遍历所有维度
    for (const [dimensionKey, dimension] of Object.entries(this.dimensions)) {
      for (const [valueKey, keywords] of Object.entries(dimension.keywords)) {
        // 检查是否包含关键词
        const matched = keywords.filter(kw => lowerContent.includes(kw.toLowerCase()));

        if (matched.length > 0) {
          results.push({
            dimension: dimension.name,
            dimensionKey,
            value: valueKey,
            keywords: matched,
            score: matched.length * dimension.weight,
            content
          });
        }
      }
    }

    return results;
  }

  /**
   * 生成价值观画像
   */
  generateProfile(records) {
    const profile = {};

    // 统计每个维度的得分
    records.forEach(record => {
      const key = `${record.dimensionKey}:${record.value}`;

      if (!profile[key]) {
        profile[key] = {
          dimension: record.dimension,
          value: record.value,
          score: 0,
          count: 0,
          examples: []
        };
      }

      profile[key].score += record.score;
      profile[key].count++;
      profile[key].examples.push(record.content);
    });

    // 转换为数组并排序
    const sorted = Object.values(profile).sort((a, b) => b.score - a.score);

    return sorted;
  }

  /**
   * 分析价值观倾向
   */
  analyze(profile) {
    const analysis = {
      topValues: profile.slice(0, 5),
      dominantTraits: [],
      summary: ''
    };

    // 提取主导特征
    const dimensionScores = {};
    profile.forEach(item => {
      if (!dimensionScores[item.dimension]) {
        dimensionScores[item.dimension] = 0;
      }
      dimensionScores[item.dimension] += item.score;
    });

    // 找出最强的维度
    const sortedDimensions = Object.entries(dimensionScores)
      .sort((a, b) => b[1] - a[1])
      .slice(0, 3);

    analysis.dominantTraits = sortedDimensions.map(([dim, score]) => ({
      dimension: dim,
      score,
      description: this.getDimensionDescription(dim)
    }));

    // 生成总结
    analysis.summary = this.generateSummary(profile);

    return analysis;
  }

  /**
   * 获取维度描述
   */
  getDimensionDescription(dimension) {
    const descriptions = {
      '产品哲学': '对产品方向和优先级的看法',
      '决策风格': '做决定的方式和依据',
      '工作态度': '对工作质量和速度的取舍',
      '技术观点': '技术选型的倾向',
      '团队观念': '对团队协作方式的偏好',
      '风险态度': '对风险的接受程度'
    };

    return descriptions[dimension] || dimension;
  }

  /**
   * 生成总结
   */
  generateSummary(profile) {
    if (profile.length === 0) {
      return '暂无明显价值观倾向';
    }

    const top = profile[0];
    const traits = profile.slice(0, 3).map(p => p.value).join('、');

    return `主导价值观是${top.dimension}中的"${top.value}"倾向，主要特征包括${traits}等。`;
  }
}

module.exports = ValuesDetector;
