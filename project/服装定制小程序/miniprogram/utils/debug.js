/**
 * 调试工具
 * 用于诊断云存储和图片加载问题
 */

class DebugUtil {
  /**
   * 检查云开发环境
   */
  static async checkCloudEnv() {
    console.log('=== 云开发环境检查 ===');

    try {
      // 检查云开发是否初始化
      if (!wx.cloud) {
        console.error('❌ wx.cloud 未定义');
        return false;
      }

      // 检查环境ID
      const env = wx.cloud.DYNAMIC_CURRENT_ENV;
      console.log('✅ 云环境ID:', env);

      return true;
    } catch (err) {
      console.error('❌ 云环境检查失败:', err);
      return false;
    }
  }

  /**
   * 测试单张图片转换
   */
  static async testImageConversion(fileId) {
    console.log('=== 图片转换测试 ===');
    console.log('原始 fileID:', fileId);

    try {
      const result = await wx.cloud.getTempFileURL({
        fileList: [{ fileID: fileId }]
      });

      console.log('转换结果:', result);

      if (result.fileList && result.fileList.length > 0) {
        const file = result.fileList[0];
        console.log('状态码:', file.status);
        console.log('临时链接:', file.tempFileURL);

        if (file.status === 0) {
          console.log('✅ 图片转换成功');
          return file.tempFileURL;
        } else {
          console.error('❌ 图片转换失败，状态码:', file.status);
          return null;
        }
      }

      return null;
    } catch (err) {
      console.error('❌ 图片转换异常:', err);
      console.error('错误码:', err.errCode);
      console.error('错误信息:', err.errMsg);
      return null;
    }
  }

  /**
   * 检查 mock 数据
   */
  static checkMockData() {
    console.log('=== Mock 数据检查 ===');

    try {
      const mockData = require('../../mock/data.js');

      console.log('✅ mockData 已加载');
      console.log('轮播图数量:', mockData.banners?.length || 0);
      console.log('分类数量:', mockData.categories?.length || 0);
      console.log('商品数量:', mockData.products?.length || 0);

      // 检查第一张图片
      if (mockData.banners && mockData.banners[0]) {
        const firstImage = mockData.banners[0].image;
        console.log('第一张图片:', firstImage);
        console.log('是否为云存储:', firstImage?.startsWith('cloud://'));

        return {
          hasData: true,
          firstImage
        };
      }

      console.warn('⚠️ mockData 为空');
      return { hasData: false };
    } catch (err) {
      console.error('❌ mockData 加载失败:', err);
      return { hasData: false };
    }
  }

  /**
   * 运行完整诊断
   */
  static async runDiagnostics() {
    console.log('\n========== 开始诊断 ==========\n');

    // 1. 检查云环境
    const cloudOk = await this.checkCloudEnv();

    // 2. 检查 mock 数据
    const mockData = this.checkMockData();

    // 3. 测试图片转换
    if (mockData.hasData && mockData.firstImage) {
      await this.testImageConversion(mockData.firstImage);
    }

    console.log('\n========== 诊断结束 ==========\n');

    return {
      cloudOk,
      mockDataOk: mockData.hasData
    };
  }
}

module.exports = DebugUtil;
