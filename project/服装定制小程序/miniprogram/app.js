// app.js
App({
  onLaunch() {
    console.log('小程序启动');

    // 初始化云开发
    if (!wx.cloud) {
      console.error('请使用 2.2.3 或以上的基础库以使用云能力');
    } else {
      wx.cloud.init({
        env: 'clothing-dev-7g7fjfyt4ff2ed7f', // 云开发环境ID
        traceUser: true
      });
      console.log('云开发初始化成功');
    }
  },

  globalData: {
    userInfo: null,
    version: '1.0.0-cloud',
    selectedProductId: null,      // 用于传递选中的商品ID
    selectedCategoryId: null,     // 用于传递选中的分类ID
    navigationTitle: '鼎盛服装定制供应链'  // 导航栏标题（可从后台配置）
  }
});
