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

        const tempURL = data.tempFileURL;
        this.addLog(`✅ 临时链接长度: ${tempURL ? tempURL.length : 0}`);

        // 显示完整链接的前100个字符
        if (tempURL) {
          this.addLog(`✅ 临时链接前缀: ${tempURL.substring(0, 80)}...`);
        }

        this.setData({
          status: '测试成功 ✅',
          testImage: tempURL
        });

        this.addLog(`✅ setData 完成，testImage 长度: ${tempURL ? tempURL.length : 0}`);

        // 验证数据是否设置成功
        setTimeout(() => {
          this.addLog(`🔍 验证: this.data.testImage 长度 = ${this.data.testImage ? this.data.testImage.length : 0}`);
        }, 100);
      } else {
        this.addLog('❌ 云函数返回格式错误');
        this.addLog(`返回内容: ${JSON.stringify(result.result)}`);
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
    this.addLog(`❌ 错误详情: ${JSON.stringify(e.detail)}`);

    // 检查图片URL
    const imgURL = this.data.testImage;
    if (imgURL) {
      this.addLog(`❌ 失败的URL: ${imgURL.substring(0, 100)}...`);
      this.addLog(`❌ URL长度: ${imgURL.length}`);
    } else {
      this.addLog('❌ testImage 为空');
    }
  },

  onImageLoad(e) {
    console.log('[test-image] 图片加载成功', e);
    this.addLog('✅ 图片显示成功！');
    this.addLog(`✅ 图片尺寸: ${e.detail.width}x${e.detail.height}`);
  }
});
