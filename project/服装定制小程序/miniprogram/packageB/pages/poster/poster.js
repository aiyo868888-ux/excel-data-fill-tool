// pages/poster/poster.js - 海报生成与预览页
const mockData = require('../../../mock/data.js');

Page({
  data: {
    navigationBarTitle: '商品海报分享',
    statusBarHeight: 0,
    totalNavbarHeight: 44,
    posterImage: '',        // 生成的海报图片URL
    productName: '',
    productId: '',
    loading: true
  },

  onLoad(options) {
    console.log('海报页面加载', options);

    // 计算导航栏高度
    const systemInfo = wx.getSystemInfoSync();
    const statusBarHeight = systemInfo.statusBarHeight || 0;
    const navigationBarHeight = systemInfo.platform === 'ios' ? 44 : 48;
    const totalNavbarHeight = statusBarHeight + navigationBarHeight;

    this.setData({
      statusBarHeight,
      totalNavbarHeight
    });

    const productId = options.productId;
    if (productId) {
      const product = mockData.products.find(p => p._id === productId);
      if (product) {
        this.setData({
          productName: product.name,
          productId: productId
        });
        this.generatePoster(product);
      } else {
        wx.showToast({ title: '商品不存在', icon: 'none' });
      }
    } else {
      wx.showToast({ title: '缺少商品信息', icon: 'none' });
    }
  },

  /**
   * 生成海报图片
   */
  generatePoster(product) {
    wx.showLoading({ title: '生成中...', mask: true });

    // 使用canvas生成海报
    const query = wx.createSelectorQuery();
    query.select('#posterCanvas')
      .fields({ node: true, size: true })
      .exec((res) => {
        if (!res || !res[0]) {
          wx.hideLoading();
          wx.showToast({ title: '生成失败', icon: 'none' });
          return;
        }

        const canvas = res[0].node;
        const ctx = canvas.getContext('2d');

        // 设置canvas尺寸
        const dpr = wx.getSystemInfoSync().pixelRatio;
        canvas.width = 750 * dpr;
        canvas.height = 1200 * dpr;
        ctx.scale(dpr, dpr);

        // 绘制海报
        this.drawPoster(ctx, canvas, product);
      });
  },

  /**
   * 绘制海报内容
   */
  drawPoster(ctx, canvas, product) {
    // 1. 绘制白色背景
    ctx.fillStyle = '#FFFFFF';
    ctx.fillRect(0, 0, 750, 1200);

    // 2. 绘制商品图片
    const img = canvas.createImage();
    img.src = product.images[0];
    img.onload = () => {
      // 商品图
      ctx.drawImage(img, 0, 0, 750, 750);

      // 绘制半透明遮罩（底部文字区域）
      ctx.fillStyle = 'rgba(255, 255, 255, 0.95)';
      ctx.fillRect(0, 750, 750, 450);

      // 绘制商品名称
      ctx.fillStyle = '#000000';
      ctx.font = 'bold 48px sans-serif';
      ctx.fillText(product.name, 40, 820);

      // 绘制属性
      ctx.font = '32px sans-serif';
      ctx.fillStyle = '#666666';
      ctx.fillText(`品类: ${product.type}`, 40, 880);
      ctx.fillText(`材质: ${product.material}`, 40, 930);

      // 绘制分割线
      ctx.fillStyle = '#EEEEEE';
      ctx.fillRect(40, 970, 670, 2);

      // 绘制提示文字
      ctx.fillStyle = '#999999';
      ctx.font = '28px sans-serif';
      ctx.fillText('长按识别二维码', 40, 1030);
      ctx.fillText('了解更多定制服务', 40, 1070);

      // 绘制二维码占位图
      ctx.fillStyle = '#F5F5F5';
      ctx.fillRect(500, 1000, 200, 200);
      ctx.strokeStyle = '#DDDDDD';
      ctx.strokeRect(500, 1000, 200, 200);

      ctx.fillStyle = '#CCCCCC';
      ctx.font = '24px sans-serif';
      ctx.fillText('二维码', 560, 1110);

      // 生成临时文件
      wx.canvasToTempFilePath({
        canvas: canvas,
        success: (res) => {
          wx.hideLoading();
          this.setData({
            posterImage: res.tempFilePath,
            loading: false
          });
        },
        fail: (err) => {
          wx.hideLoading();
          console.error('生成海报失败', err);
          wx.showToast({ title: '生成失败', icon: 'none' });
        }
      });
    };

    img.onerror = (err) => {
      wx.hideLoading();
      console.error('图片加载失败', err);
      wx.showToast({ title: '图片加载失败', icon: 'none' });
    };
  },

  /**
   * 保存图片到相册
   */
  onSaveToAlbum() {
    if (!this.data.posterImage) {
      wx.showToast({ title: '海报未生成', icon: 'none' });
      return;
    }

    wx.saveImageToPhotosAlbum({
      filePath: this.data.posterImage,
      success: () => {
        wx.showToast({ title: '已保存到相册', icon: 'success' });
      },
      fail: (err) => {
        if (err.errMsg && err.errMsg.includes('auth')) {
          wx.showModal({
            title: '提示',
            content: '需要授权保存图片到相册',
            confirmText: '去设置',
            success: (res) => {
              if (res.confirm) {
                wx.openSetting();
              }
            }
          });
        } else {
          wx.showToast({ title: '保存失败', icon: 'none' });
        }
      }
    });
  },

  /**
   * 分享给好友
   */
  onShareFriend() {
    wx.showShareMenu({
      withShareTicket: true,
      menus: ['shareAppMessage', 'shareTimeline']
    });
    wx.showToast({ title: '点击右上角分享', icon: 'none' });
  },

  /**
   * 返回上一页
   */
  onBack() {
    wx.navigateBack();
  },

  /**
   * 分享配置
   */
  onShareAppMessage() {
    return {
      title: `${this.data.productName} - 鼎盛服装定制`,
      path: `/pages/design/design?productId=${this.data.productId}`,
      imageUrl: this.data.posterImage || ''
    };
  }
});
