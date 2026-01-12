// pages/design/design.js - 商品详情页
const mockData = require('../../mock/data.js');

Page({
  data: {
    productId: '',
    productName: '',
    productImages: [],    // 商品图片数组（轮播）
    productPrice: 0,      // 商品价格
    productSales: 0,      // 商品销量
    productType: '',      // 商品类型
    productMaterial: '',  // 商品材质
    productStyle: '',     // 商品版型
    productDescription: '', // 商品描述
    detailImages: []      // 商品详情图片数组
  },

  onLoad(options) {
    console.log('商品详情页加载', options);

    // 从 globalData 获取商品ID
    const app = getApp();
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
    const detailImages = product.detailImages || product.images || [];

    this.setData({
      productId: product._id,
      productName: product.name,
      productImages: productImages,
      productPrice: product.price,
      productSales: product.sales,
      productType: product.type,
      productMaterial: product.material,
      productStyle: product.style,
      productDescription: product.description,
      detailImages: detailImages
    });

    return true;
  },

  /**
   * 开始自助设计
   */
  onStartDesign() {
    // 保存当前商品ID到 globalData
    const app = getApp();
    app.globalData.selectedProductId = this.data.productId;

    // 跳转到设计编辑页
    wx.navigateTo({
      url: `/pages/editor/editor?productId=${this.data.productId}`
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
    // 检查是否已收藏
    const collections = wx.getStorageSync('my_collections') || [];
    const isCollected = collections.some(item => item.productId === this.data.productId);

    if (isCollected) {
      wx.showModal({
        title: '取消收藏',
        content: '确定要取消收藏吗？',
        success: (res) => {
          if (res.confirm) {
            const newCollections = collections.filter(item => item.productId !== this.data.productId);
            wx.setStorageSync('my_collections', newCollections);
            wx.showToast({ title: '已取消收藏', icon: 'success' });
          }
        }
      });
    } else {
      // 添加到收藏
      const collectionData = {
        designId: `design_${Date.now()}`,
        productId: this.data.productId,
        productName: this.data.productName,
        productImage: this.data.productImages[0] || '',
        collectTime: new Date().toISOString()
      };

      collections.unshift(collectionData);
      wx.setStorageSync('my_collections', collections);

      wx.showToast({ title: '收藏成功', icon: 'success' });
    }
  },

  /**
   * 海报分享
   */
  onSharePoster() {
    wx.showLoading({ title: '生成中...', mask: true });

    // TODO: 这里需要生成海报图片
    // 暂时先提示功能开发中
    setTimeout(() => {
      wx.hideLoading();

      wx.showModal({
        title: '分享海报',
        content: '是否保存海报到相册？',
        confirmText: '保存',
        success: (res) => {
          if (res.confirm) {
            wx.showToast({
              title: '海报生成功能开发中',
              icon: 'none',
              duration: 2000
            });
          }
        }
      });
    }, 1000);
  }
});
