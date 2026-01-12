// pages/design/design.js - 商品详情页
const mockData = require('../../mock/data.js');

Page({
  data: {
    // 导航栏相关
    navigationBarTitle: '鼎盛服装定制供应链',
    statusBarHeight: 0,
    totalNavbarHeight: 44,

    // 商品相关
    productId: '',
    productName: '',
    productImages: [],    // 商品图片数组
    currentImageIndex: 0, // 当前显示的图片索引
    productPrice: 0,      // 商品价格
    productSales: 0,      // 商品销量
    productType: '',      // 商品类型
    productMaterial: '',  // 商品材质
    productStyle: '',     // 商品版型
    productPattern: '',   // 商品款式
    productDescription: '', // 商品描述
    detailImages: [],      // 商品详情图片数组
    hasDetailImages: true, // 是否有详情图片
    isCollected: false     // 是否已收藏
  },

  onLoad(options) {
    console.log('商品详情页加载', options);

    // 获取系统信息，计算导航栏高度
    const systemInfo = wx.getSystemInfoSync();
    const statusBarHeight = systemInfo.statusBarHeight || 0;
    const navigationBarHeight = systemInfo.platform === 'ios' ? 44 : 48;
    const totalNavbarHeight = statusBarHeight + navigationBarHeight;

    // 获取导航栏标题
    const app = getApp();
    const navigationBarTitle = app.globalData.navigationTitle || '鼎盛服装定制供应链';

    this.setData({
      statusBarHeight,
      totalNavbarHeight,
      navigationBarTitle
    });

    // 从 globalData 获取商品ID
    const productId = app.globalData.selectedProductId || options.productId;

    console.log('从 globalData 获取的 productId:', productId);

    if (productId) {
      if (this.loadProductData(productId)) {
        // 清除 globalData 中的选择
        app.globalData.selectedProductId = null;
      }
    } else {
      console.log('没有 productId，使用默认商品');
      const defaultProduct = mockData.products[0];
      if (defaultProduct) {
        this.loadProductData(defaultProduct._id);
      }
    }
  },

  onShow() {
    console.log('商品详情页 onShow');
    // 每次显示时检查是否有新的商品选择
    const app = getApp();
    if (app.globalData.selectedProductId) {
      const productId = app.globalData.selectedProductId;
      console.log('onShow: 检测到新的商品选择:', productId);

      if (this.loadProductData(productId)) {
        // 清除 globalData
        app.globalData.selectedProductId = null;
        // 获取商品名称用于提示
        const product = mockData.products.find(p => p._id === productId);
        if (product) {
          wx.showToast({ title: `已选择: ${product.name}`, icon: 'success' });
        }
      }
    }
  },

  /**
   * 加载商品数据到页面
   * @param {string} productId - 商品ID
   * @returns {boolean} 是否加载成功
   */
  loadProductData(productId) {
    if (!mockData.products || mockData.products.length === 0) {
      console.error('商品数据为空');
      wx.showToast({ title: '商品数据加载失败', icon: 'none' });
      return false;
    }

    const product = mockData.products.find(p => p._id === productId);
    if (!product) {
      console.error('未找到商品，productId:', productId);
      wx.showToast({ title: '未找到该商品', icon: 'none' });
      return false;
    }

    console.log('找到商品:', product);

    // 构建商品图片数组（可以多张）
    const productImages = product.images || [];

    // 构建详情图片数组
    const detailImages = product.detailImages || [];
    const hasDetailImages = detailImages.length > 0;

    // 如果没有详情图片，记录日志
    if (!hasDetailImages) {
      console.warn(`商品 ${productId} 暂无详情图片`);
    }

    // 检查是否已收藏
    const collections = wx.getStorageSync('my_collections') || [];
    const isCollected = collections.some(item => item.productId === productId);

    this.setData({
      productId: product._id,
      productName: product.name,
      productImages: productImages,
      productPrice: product.price,
      productSales: product.sales,
      productType: product.type,
      productMaterial: product.material,
      productStyle: product.style,
      productPattern: product.pattern || '', // 添加款式字段
      productDescription: product.description,
      detailImages: detailImages,
      hasDetailImages: hasDetailImages,
      isCollected: isCollected
    });

    return true;
  },

  /**
   * 开始自助设计
   */
  onStartDesign() {
    console.log('=== onStartDesign 被调用 ===');
    console.log('当前商品ID:', this.data.productId);

    if (!this.data.productId) {
      wx.showToast({
        title: '商品信息错误',
        icon: 'none'
      });
      return;
    }

    // 保存当前商品ID到 globalData
    const app = getApp();
    app.globalData.selectedProductId = this.data.productId;
    console.log('已保存商品ID到 globalData:', app.globalData.selectedProductId);

    // 跳转到设计编辑页（editor在TabBar中，应该使用switchTab）
    wx.switchTab({
      url: '/pages/editor/editor',
      success: () => {
        console.log('跳转到设计编辑页成功');
      },
      fail: (err) => {
        console.error('跳转失败:', err);
        wx.showToast({
          title: '跳转失败',
          icon: 'none'
        });
      }
    });
  },

  /**
   * 保存图片到相册
   */
  onSaveImage() {
    wx.showLoading({ title: '保存中...', mask: true });

    // TODO: 这里需要使用 canvas 绘制设计图并保存
    // 暂时先提示功能开发中
    setTimeout(() => {
      wx.hideLoading();
      wx.showToast({
        title: '功能开发中',
        icon: 'none',
        duration: 2000
      });
    }, 1000);
  },

  /**
   * 收藏商品
   */
  onCollect() {
    const collections = wx.getStorageSync('my_collections') || [];
    const isCollected = this.data.isCollected;

    if (isCollected) {
      // 取消收藏
      const newCollections = collections.filter(item => item.productId !== this.data.productId);
      wx.setStorageSync('my_collections', newCollections);

      this.setData({ isCollected: false });
      wx.showToast({ title: '已取消收藏', icon: 'success' });
    } else {
      // 添加到收藏
      const collectionData = {
        collectionId: `collection_${Date.now()}`,  // 修复：使用 collectionId
        productId: this.data.productId,
        productName: this.data.productName,
        productImage: this.data.productImages[0] || '',
        collectTime: new Date().toISOString(),
        type: 'product'  // 标识类型
      };

      collections.unshift(collectionData);
      wx.setStorageSync('my_collections', collections);

      this.setData({ isCollected: true });
      wx.showToast({ title: '收藏成功', icon: 'success' });
    }
  },

  /**
   * 生成海报
   */
  onCreatePoster() {
    wx.showLoading({ title: '生成中...', mask: true });

    // 跳转到海报生成页面
    setTimeout(() => {
      wx.hideLoading();
      wx.navigateTo({
        url: `/pages/poster/poster?productId=${this.data.productId}`
      });
    }, 500);
  },

  /**
   * 自定义导航栏返回
   */
  onBack() {
    wx.navigateBack({
      fail: () => {
        // 如果无法返回，跳转到首页
        wx.switchTab({
          url: '/pages/index/index'
        });
      }
    });
  },

  /**
   * 分享
   */
  onShare() {
    wx.showToast({
      title: '分享功能开发中',
      icon: 'none',
      duration: 2000
    });
  },

  /**
   * 返回首页
   */
  goHome() {
    wx.switchTab({
      url: '/pages/index/index'
    });
  },

  /**
   * 联系客服
   */
  contactService() {
    wx.showModal({
      title: '联系客服',
      content: '客服电话: 400-XXX-XXXX',
      confirmText: '拨打',
      cancelText: '取消',
      success: (res) => {
        if (res.confirm) {
          wx.makePhoneCall({
            phoneNumber: '400-XXX-XXXX',
            fail: () => {
              wx.showToast({
                title: '拨号失败',
                icon: 'none'
              });
            }
          });
        }
      }
    });
  }
});
