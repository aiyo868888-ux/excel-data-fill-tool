// pages/test/test.js
const CloudImageUtil = require('../../utils/cloud-image.js');

Page({
  data: {
    tempURL: ''
  },

  onLoad() {
    console.log('[test] 测试页面加载');
  },

  async onTestConversion() {
    console.log('[test] 开始测试图片转换');

    wx.showLoading({ title: '转换中...', mask: true });

    try {
      const fileId = 'cloud://cloud1-2g7e3gch6d0592e5.636c-cloud1-2g7e3gch6d0592e5-1395469184/banners/ScreenShot_2026-01-12_102502_938.png';

      // 测试转换
      const tempURL = await CloudImageUtil.getTempFileURL(fileId);

      console.log('[test] 转换结果:', tempURL);

      this.setData({ tempURL });

      wx.showModal({
        title: '转换成功',
        content: '临时链接：' + tempURL.substring(0, 50) + '...',
        showCancel: false
      });
    } catch (err) {
      console.error('[test] 转换失败:', err);
      wx.showModal({
        title: '转换失败',
        content: '错误：' + JSON.stringify(err),
        showCancel: false
      });
    } finally {
      wx.hideLoading();
    }
  }
});
