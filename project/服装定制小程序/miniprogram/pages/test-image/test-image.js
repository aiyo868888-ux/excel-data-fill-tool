// pages/test-image/test-image.js
Page({
  data: {
    status: '等待测试...',
    logs: [],
    testImage: ''
  },

  onLoad() {
    console.log('[test-image] 页面加载');
    this.addLog('页面加载成功');

    // 测试云开发是否初始化
    if (!wx.cloud) {
      this.addLog('❌ wx.cloud 未初始化');
      this.setData({ status: '云开发未初始化' });
      return;
    }

    this.addLog('✅ wx.cloud 已初始化');

    // 测试云函数调用
    this.testCloudFunction();
  },

  async testCloudFunction() {
    this.addLog('开始测试云函数调用...');
    this.setData({ status: '正在调用云函数...' });

    try {
      console.log('[test-image] 调用云函数 getImageURL');

      const result = await wx.cloud.callFunction({
        name: 'getImageURL',
        data: {
          fileIds: [
            'cloud://cloud1-2g7e3gch6d0592e5.636c-cloud1-2g7e3gch6d0592e5-1395469184/banners/ScreenShot_2026-01-12_102502_938.png'
          ]
        }
      });

      console.log('[test-image] 云函数返回:', result);
      this.addLog('✅ 云函数调用成功');

      if (result.result && result.result.code === 200) {
        const data = result.result.data[0];
        this.addLog(`✅ 状态: ${data.status}`);
        this.addLog(`✅ 临时链接: ${data.tempFileURL ? data.tempFileURL.substring(0, 50) + '...' : '无'}`);

        this.setData({
          status: '测试成功 ✅',
          testImage: data.tempFileURL
        });
      } else {
        this.addLog('❌ 云函数返回格式错误');
        this.setData({ status: '云函数返回错误' });
      }
    } catch (err) {
      console.error('[test-image] 云函数调用失败:', err);
      this.addLog(`❌ 云函数调用失败: ${err.errMsg || err.message}`);
      this.addLog(`❌ 错误码: ${err.errCode}`);
      this.setData({ status: '云函数调用失败 ❌' });
    }
  },

  addLog(message) {
    const timestamp = new Date().toLocaleTimeString();
    const logs = this.data.logs.concat(`[${timestamp}] ${message}`);
    this.setData({ logs });
    console.log(`[test-image] ${message}`);
  },

  onImageError(e) {
    console.error('[test-image] 图片加载失败:', e);
    this.addLog('❌ 图片加载失败');
  },

  onImageLoad() {
    console.log('[test-image] 图片加载成功');
    this.addLog('✅ 图片显示成功！');
  }
});
