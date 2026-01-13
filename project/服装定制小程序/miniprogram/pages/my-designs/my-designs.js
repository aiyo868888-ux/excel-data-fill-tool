// pages/my-designs/my-designs.js - 我的设计列表页
const store = require('../../store/index.js');
const cloudService = require('../../utils/cloud.js');
const ErrorHandler = require('../../utils/error-handler.js');

Page({
  data: {
    designs: [],
    loading: true
  },

  onLoad() {
    this.loadDesigns();
  },

  onShow() {
    // 每次显示页面时重新加载
    this.loadDesigns();
  },

  /**
   * 加载设计列表
   */
  loadDesigns() {
    this.setData({ loading: true });

    try {
      const designs = wx.getStorageSync('my_designs') || [];
      console.log('加载设计列表:', designs);

      this.setData({
        designs: designs,
        loading: false
      });
    } catch (err) {
      console.error('加载设计失败:', err);
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      });
      this.setData({
        designs: [],
        loading: false
      });
    }
  },

  /**
   * 查看设计详情
   */
  onViewDesign(e) {
    const design = e.currentTarget.dataset.design;
    console.log('查看设计:', design);

    // 预览设计（可以扩展为显示设计详情）
    wx.showModal({
      title: design.productName,
      content: `包含 ${design.elements.length} 个设计元素\n创建时间: ${this.formatTime(design.createTime)}`,
      showCancel: true,
      confirmText: '编辑',
      cancelText: '关闭',
      success: (res) => {
        if (res.confirm) {
          this.editDesign(design);
        }
      }
    });
  },

  /**
   * 编辑设计
   */
  onEditDesign(e) {
    const design = e.currentTarget.dataset.design;
    this.editDesign(design);
  },

  /**
   * 执行编辑操作
   */
  editDesign(design) {
    // 保存到 Store（带版本控制）
    store.selectDesign(design.designId, design.productId);

    // 直接跳转，无需二次确认
    wx.switchTab({
      url: '/pages/editor/editor',
      success: () => {
        wx.showToast({ title: '设计已加载', icon: 'success' });
      }
    });
  },

  /**
   * 删除设计
   */
  onDeleteDesign(e) {
    const designId = e.currentTarget.dataset.id;

    wx.showModal({
      title: '确认删除',
      content: '确定要删除这个设计吗？删除后无法恢复。',
      confirmText: '删除',
      confirmColor: '#E60012',
      success: (res) => {
        if (res.confirm) {
          try {
            let designs = wx.getStorageSync('my_designs') || [];
            designs = designs.filter(d => d.designId !== designId);
            wx.setStorageSync('my_designs', designs);

            wx.showToast({
              title: '删除成功',
              icon: 'success'
            });

            // 重新加载列表
            this.loadDesigns();
          } catch (err) {
            console.error('删除失败:', err);
            wx.showToast({
              title: '删除失败',
              icon: 'none'
            });
          }
        }
      }
    });
  },

  /**
   * 去设计
   */
  onGoToDesign() {
    wx.switchTab({
      url: '/pages/index/index'
    });
  },

  /**
   * 格式化时间
   */
  formatTime(isoString) {
    if (!isoString) return '';

    const date = new Date(isoString);
    const now = new Date();
    const diff = now - date;

    // 小于1分钟
    if (diff < 60000) {
      return '刚刚';
    }

    // 小于1小时
    if (diff < 3600000) {
      const minutes = Math.floor(diff / 60000);
      return `${minutes}分钟前`;
    }

    // 小于1天
    if (diff < 86400000) {
      const hours = Math.floor(diff / 3600000);
      return `${hours}小时前`;
    }

    // 小于7天
    if (diff < 604800000) {
      const days = Math.floor(diff / 86400000);
      return `${days}天前`;
    }

    // 其他显示日期
    const month = date.getMonth() + 1;
    const day = date.getDate();
    return `${month}月${day}日`;
  }
});
