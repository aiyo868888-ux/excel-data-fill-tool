/**
 * 云存储图片工具
 * 用于将云存储 fileID 转换为临时可访问链接
 * 支持缓存机制，避免重复转换
 */

class CloudImageUtil {
  // 缓存临时链接（避免重复转换）
  static cache = new Map();
  static cacheExpireTime = 2 * 60 * 60 * 1000; // 2小时缓存

  /**
   * 批量转换云存储地址为临时链接
   * @param {Array<string>} fileIds - 云存储 fileID 数组
   * @returns {Promise<Array<string>>} 临时链接数组
   */
  static async getTempFileURLs(fileIds) {
    if (!fileIds || fileIds.length === 0) return [];

    try {
      const result = await wx.cloud.getTempFileURL({
        fileList: fileIds.map(id => ({ fileID: id }))
      });

      if (result.fileList && result.fileList.length > 0) {
        return result.fileList.map(file => file.tempFileURL);
      }

      return [];
    } catch (err) {
      console.error('获取临时链接失败:', err);
      return fileIds; // 失败时返回原地址
    }
  }

  /**
   * 转换单个云存储地址（带缓存）
   * @param {string} fileId - 云存储 fileID
   * @returns {Promise<string>} 临时链接
   */
  static async getTempFileURL(fileId) {
    if (!fileId) return '';

    // 如果不是云存储地址，直接返回
    if (!fileId.startsWith('cloud://')) {
      return fileId;
    }

    // 检查缓存
    const now = Date.now();
    const cached = this.cache.get(fileId);
    if (cached && cached.expireTime > now) {
      console.log('[CloudImageUtil] 使用缓存的临时链接');
      return cached.tempURL;
    }

    try {
      const result = await wx.cloud.getTempFileURL({
        fileList: [{ fileID: fileId }]
      });

      if (result.fileList && result.fileList.length > 0 && result.fileList[0].status === 0) {
        const tempURL = result.fileList[0].tempFileURL;

        // 缓存临时链接
        this.cache.set(fileId, {
          tempURL,
          expireTime: now + this.cacheExpireTime
        });

        return tempURL;
      }

      return fileId;
    } catch (err) {
      console.error('获取临时链接失败:', err);
      return fileId;
    }
  }

  /**
   * 预加载所有云存储图片（带缓存）
   * @param {Object} data - 包含云存储图片的数据对象
   * @param {Array<string>} fields - 需要转换的字段路径
   * @returns {Promise<Object>} 转换后的数据对象
   */
  static async preloadImages(data, fields = ['image', 'images', 'icon', 'avatarUrl']) {
    const fileIds = [];
    const fieldMap = [];

    // 收集所有云存储 fileID
    const collectFileIds = (obj, prefix = '') => {
      if (Array.isArray(obj)) {
        obj.forEach((item, index) => {
          collectFileIds(item, `${prefix}[${index}]`);
        });
      } else if (obj && typeof obj === 'object') {
        Object.keys(obj).forEach(key => {
          const fieldPath = prefix ? `${prefix}.${key}` : key;

          // 检查是否是目标字段
          if (fields.includes(key)) {
            const value = obj[key];
            if (typeof value === 'string' && value.startsWith('cloud://')) {
              fileIds.push(value);
              fieldMap.push({ path: fieldPath, fileId: value });
            }
          } else if (Array.isArray(value)) {
            // 处理数组中的图片
            value.forEach((item, index) => {
              if (typeof item === 'string' && item.startsWith('cloud://')) {
                fileIds.push(item);
                fieldMap.push({ path: `${fieldPath}[${index}]`, fileId: item });
              }
            });
          } else if (typeof value === 'object') {
            collectFileIds(value, fieldPath);
          }
        });
      }
    };

    collectFileIds(data);

    // 批量获取临时链接
    const tempURLs = await this.getTempFileURLs(fileIds);

    // 替换原始数据
    const result = JSON.parse(JSON.stringify(data));
    fieldMap.forEach(({ path, fileId }, index) => {
      const tempURL = tempURLs[index];
      if (tempURL) {
        // 使用路径设置值
        const keys = path.split('.');
        let obj = result;
        for (let i = 0; i < keys.length - 1; i++) {
          // 处理数组索引
          const key = keys[i].replace(/\[(\d+)\]/, '.$1');
          obj = obj[key];
        }
        obj[keys[keys.length - 1]] = tempURL;
      }
    });

    return result;
  }

  /**
   * 清除过期缓存
   */
  static clearExpiredCache() {
    const now = Date.now();
    for (const [key, value] of this.cache.entries()) {
      if (value.expireTime <= now) {
        this.cache.delete(key);
      }
    }
  }

  /**
   * 清除所有缓存
   */
  static clearAllCache() {
    this.cache.clear();
  }
}

module.exports = CloudImageUtil;
