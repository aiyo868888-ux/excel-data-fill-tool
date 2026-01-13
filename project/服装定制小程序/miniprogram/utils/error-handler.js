/**
 * 统一错误处理工具类
 * 提供错误捕获、用户友好提示和错误上报功能
 */

class ErrorHandler {
  /**
   * 处理错误
   * @param {Error} error - 错误对象
   * @param {string} context - 错误上下文（如：函数名、模块名）
   * @param {boolean} showToast - 是否显示错误提示（默认true）
   */
  static handle(error, context = '', showToast = true) {
    // 记录错误日志
    console.error(`[ErrorHandler] ${context}:`, error);

    // 上报错误（可选，可接入监控平台）
    this._report(error, context);

    // 显示用户友好的错误提示
    if (showToast) {
      const message = this._translateError(error);
      wx.showToast({
        title: message,
        icon: 'none',
        duration: 2000
      });
    }
  }

  /**
   * 异步错误处理包装器
   * @param {Function} fn - 异步函数
   * @param {string} context - 错误上下文
   * @returns {Function} 包装后的函数
   */
  static async wrap(fn, context = '') {
    try {
      return await fn();
    } catch (error) {
      this.handle(error, context);
      throw error; // 重新抛出，让调用者决定如何处理
    }
  }

  /**
   * 将错误信息翻译为用户友好的提示
   * @private
   * @param {Error} error - 错误对象
   * @returns {string} 用户友好的错误提示
   */
  static _translateError(error) {
    // 获取错误消息
    let errorMsg = '';
    if (typeof error === 'string') {
      errorMsg = error;
    } else if (error.message) {
      errorMsg = error.message;
    } else if (error.errMsg) {
      errorMsg = error.errMsg;
    } else {
      errorMsg = JSON.stringify(error);
    }

    // 错误消息映射表
    const errorMap = {
      // 网络相关
      'network': '网络连接失败，请检查网络设置',
      'timeout': '请求超时，请重试',
      'fail to request': '网络请求失败',

      // 存储相关
      'storage': '存储空间不足',
      'quota': '存储空间已满',
      'getStorage': '读取数据失败',
      'setStorage': '保存数据失败',

      // 权限相关
      'permission': '权限不足',
      'authorize': '需要授权才能使用此功能',
      'deny': '您拒绝了授权',

      // 云函数相关
      'cloud function': '云函数调用失败',
      'cloud database': '数据库操作失败',
      'cloud upload': '文件上传失败',

      // 参数相关
      'invalid': '参数错误',
      'not found': '未找到相关数据',
      'required': '缺少必要参数',

      // 默认提示
      'default': '操作失败，请重试'
    };

    // 模糊匹配错误类型
    for (const [key, message] of Object.entries(errorMap)) {
      if (errorMsg.toLowerCase().includes(key)) {
        return message;
      }
    }

    // 未匹配到，返回默认提示
    return errorMap.default;
  }

  /**
   * 上报错误到监控平台
   * @private
   * @param {Error} error - 错误对象
   * @param {string} context - 错误上下文
   */
  static _report(error, context) {
    try {
      // 获取系统信息
      const systemInfo = wx.getSystemInfoSync();

      // 构建错误报告
      const errorReport = {
        context,
        message: error.message || error.toString(),
        stack: error.stack,
        time: new Date().toISOString(),
        system: {
          platform: systemInfo.platform,
          system: systemInfo.system,
          version: systemInfo.version,
          SDKVersion: systemInfo.SDKVersion
        },
        page: getCurrentPages().length > 0 ?
          getCurrentPages()[getCurrentPages().length - 1].route : ''
      };

      // TODO: 上报到监控平台（如：微信云开发监控、 Sentry）
      // wx.cloud.callFunction({
      //   name: 'reportError',
      //   data: errorReport
      // });

      console.log('[ErrorHandler] 错误已记录:', errorReport);
    } catch (err) {
      console.error('[ErrorHandler] 错误上报失败:', err);
    }
  }

  /**
   * 显示加载状态
   * @param {string} title - 提示文字
   */
  static showLoading(title = '加载中...') {
    wx.showLoading({ title, mask: true });
  }

  /**
   * 隐藏加载状态
   */
  static hideLoading() {
    wx.hideLoading();
  }

  /**
   * 显示成功提示
   * @param {string} title - 提示文字
   */
  static showSuccess(title) {
    wx.showToast({ title, icon: 'success', duration: 2000 });
  }

  /**
   * 显示错误提示
   * @param {string} title - 提示文字
   */
  static showError(title) {
    wx.showToast({ title, icon: 'none', duration: 2000 });
  }
}

module.exports = ErrorHandler;
