// pages/design/design.js - 商品详情页
const mockData = require('../../../mock/data.js');
const store = require('../../../store/index.js');
const CloudImageUtil = require('../../../utils/cloud-image.js');

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
    productType: '',      // 商品类型
    productMaterial: '',  // 商品材质
    productStyle: '',     // 商品版型
    productPattern: '',   // 商品款式
    productDescription: '', // 商品描述
    detailImages: [],      // 商品详情图片数组
    hasDetailImages: true, // 是否有详情图片
    isCollected: false     // 是否已收藏
  },

  async onLoad(options) {
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

    // 从 Store 获取商品ID
    const productId = store.selectedProductId || options.productId;

    console.log('从 Store 获取的 productId:', productId);

    wx.showLoading({ title: '加载中...', mask: true });

    if (productId) {
      await this.loadProductData(productId);
      // 清除 Store 中的选择
      store.clearNavigation();
    } else {
      console.log('没有 productId，使用默认商品');
      const defaultProduct = mockData.products[0];
      if (defaultProduct) {
        await this.loadProductData(defaultProduct._id);
      }
    }

    wx.hideLoading();
  },

  async onShow() {
    console.log('商品详情页 onShow');
    // 每次显示时检查是否有新的商品选择
    if (store.selectedProductId && store.isNavigationValid()) {
      const productId = store.selectedProductId;
      console.log('onShow: 检测到新的商品选择:', productId);

      await this.loadProductData(productId);
      // 清除 Store
      store.clearNavigation();
      // 获取商品名称用于提示
      const product = mockData.products.find(p => p._id === productId);
      if (product) {
        wx.showToast({ title: `已选择: ${product.name}`, icon: 'success' });
      }
    }
  },

  /**
   * 加载商品数据到页面
   * @param {string} productId - 商品ID
   * @returns {Promise<boolean>} 是否加载成功
   */
  async loadProductData(productId) {
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

    // 转换云存储图片为临时链接
    const productWithImages = await CloudImageUtil.preloadImages(product);

    // 构建商品图片数组（可以多张）
    const productImages = productWithImages.images || [];
    const productImage = productImages[0] || '';

    // 构建详情图片数组
    const detailImages = productWithImages.detailImages || [];
    const hasDetailImages = detailImages.length > 0;

    // 如果没有详情图片，记录日志
    if (!hasDetailImages) {
      console.warn(`商品 ${productId} 暂无详情图片`);
    }

    // 检查是否已收藏
    const collections = wx.getStorageSync('my_collections') || [];
    const isCollected = collections.some(item => item.productId === productId);

    this.setData({
      productId: productWithImages._id,
      productName: productWithImages.name,
      productImages: productImages,
      productImage: productImage,
      productType: productWithImages.type,
      productMaterial: productWithImages.material,
      productStyle: productWithImages.style,
      productPattern: productWithImages.pattern || '',
      productDescription: productWithImages.description,
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

    // 保存当前商品ID到 Store
    store.selectProduct(this.data.productId);
    console.log('已保存商品ID到 Store:', store.selectedProductId);

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

    // 跳转到海报生成页面（在分包B中）
    setTimeout(() => {
      wx.hideLoading();
      wx.navigateTo({
        url: `/packageB/pages/poster/poster?productId=${this.data.productId}`
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
