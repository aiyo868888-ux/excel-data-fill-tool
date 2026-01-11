/**
 * 云开发服务工具类
 * 封装常用的云开发操作
 */
class CloudService {
  /**
   * 调用云函数
   * @param {string} name 云函数名称
   * @param {object} data 传递给云函数的数据
   * @returns {Promise} 云函数返回结果
   */
  async callFunction(name, data = {}) {
    try {
      console.log(`调用云函数: ${name}`, data);
      const res = await wx.cloud.callFunction({
        name,
        data
      });
      console.log(`云函数 ${name} 返回:`, res.result);
      return res.result;
    } catch (err) {
      console.error(`云函数 ${name} 调用失败`, err);
      throw err;
    }
  }

  /**
   * 上传文件到云存储
   * @param {string} cloudPath 云存储路径
   * @param {string} filePath 本地文件路径
   * @returns {Promise<string>} 文件的 fileID
   */
  async uploadFile(cloudPath, filePath) {
    try {
      console.log('上传文件:', cloudPath);
      const res = await wx.cloud.uploadFile({
        cloudPath,
        filePath
      });
      console.log('文件上传成功:', res.fileID);
      return res.fileID;
    } catch (err) {
      console.error('文件上传失败', err);
      throw err;
    }
  }

  /**
   * 下载云存储文件
   * @param {string} fileID 云文件ID
   * @returns {Promise<string>} 临时文件路径
   */
  async downloadFile(fileID) {
    try {
      const res = await wx.cloud.downloadFile({
        fileID
      });
      return res.tempFilePath;
    } catch (err) {
      console.error('文件下载失败', err);
      throw err;
    }
  }

  /**
   * 删除云存储文件
   * @param {Array<string>} fileList 文件ID列表
   * @returns {Promise}
   */
  async deleteFile(fileList) {
    try {
      const res = await wx.cloud.deleteFile({
        fileList
      });
      return res.fileList;
    } catch (err) {
      console.error('文件删除失败', err);
      throw err;
    }
  }

  /**
   * 获取数据库引用
   * @returns {object} 数据库实例
   */
  db() {
    return wx.cloud.database();
  }

  /**
   * 获取数据库命令
   * @returns {object} 数据库命令实例
   */
  command() {
    return wx.cloud.command();
  }

  /**
   * 获取集合引用
   * @param {string} name 集合名称
   * @returns {object} 集合引用
   */
  collection(name) {
    return wx.cloud.database().collection(name);
  }

  /**
   * 显示加载提示
   * @param {string} title 提示文字
   */
  showLoading(title = '加载中...') {
    wx.showLoading({ title, mask: true });
  }

  /**
   * 隐藏加载提示
   */
  hideLoading() {
    wx.hideLoading();
  }

  /**
   * 显示成功提示
   * @param {string} title 提示文字
   */
  showSuccess(title) {
    wx.showToast({ title, icon: 'success', duration: 2000 });
  }

  /**
   * 显示错误提示
   * @param {string} title 提示文字
   */
  showError(title) {
    wx.showToast({ title, icon: 'none', duration: 2000 });
  }
}

// 创建单例
const cloudService = new CloudService();

module.exports = cloudService;
