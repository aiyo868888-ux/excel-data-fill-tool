// pages/editor/editor.js - 自助设计编辑页
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
    productImage: '',

    // 颜色选择器
    colorList: [
      { name: '红色', color: '#E60012' },
      { name: '黑色', color: '#000000' },
      { name: '白色', color: '#FFFFFF' },
      { name: '蓝色', color: '#0066CC' },
      { name: '黄色', color: '#FFCC00' },
      { name: '绿色', color: '#009944' },
      { name: '紫色', color: '#9933CC' },
      { name: '灰色', color: '#999999' },
      { name: '粉色', color: '#FF6699' },
      { name: '橙色', color: '#FF6600' }
    ],
    selectedColorIndex: 0,
    selectedColorName: '红色',
    selectedColorValue: '#E60012',

    // 设计元素
    elements: [],
    selectedId: null
  },

  onLoad(options) {
    console.log('设计编辑页加载', options);

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

    const productId = options.productId;
    if (productId) {
      const product = mockData.products.find(p => p._id === productId);
      if (product) {
        this.setData({
          productId: product._id,
          productName: product.name,
          productImage: product.images[0]
        });
      }
    }
  },

  /**
   * 上传图片
   */
  onUploadImage() {
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const tempFilePath = res.tempFilePaths[0];

        // 添加图片元素到画布
        const element = {
          id: `img_${Date.now()}`,
          type: 'image',
          url: tempFilePath,
          x: 100,
          y: 100,
          width: 200,
          height: 200,
          rotation: 0
        };

        this.setData({
          elements: [...this.data.elements, element],
          selectedId: element.id
        });

        wx.showToast({ title: '图片已添加', icon: 'success' });
      }
    });
  },

  /**
   * 添加文字
   */
  onAddText() {
    wx.showModal({
      title: '添加文字',
      editable: true,
      placeholderText: '请输入文字内容',
      success: (res) => {
        if (res.confirm && res.content) {
          const element = {
            id: `text_${Date.now()}`,
            type: 'text',
            content: res.content,
            x: 200,
            y: 200,
            fontSize: 36,
            color: '#000000',
            rotation: 0
          };

          this.setData({
            elements: [...this.data.elements, element],
            selectedId: element.id
          });
        }
      }
    });
  },

  /**
   * 选中元素
   */
  onSelectElement(e) {
    const id = e.currentTarget.dataset.id;
    this.setData({ selectedId: id });
  },

  /**
   * 删除元素
   */
  onDeleteElement() {
    if (!this.data.selectedId) return;

    wx.showModal({
      title: '确认删除',
      content: '确定要删除这个元素吗？',
      success: (res) => {
        if (res.confirm) {
          const elements = this.data.elements.filter(el => el.id !== this.data.selectedId);
          this.setData({
            elements,
            selectedId: null
          });
          wx.showToast({ title: '已删除', icon: 'success' });
        }
      }
    });
  },

  /**
   * 完成设计
   */
  onComplete() {
    wx.showModal({
      title: '完成设计',
      content: '是否保存当前设计？',
      success: (res) => {
        if (res.confirm) {
          // 保存设计数据
          const designData = {
            designId: `design_${Date.now()}`,
            productId: this.data.productId,
            productName: this.data.productName,
            elements: this.data.elements,
            createTime: new Date().toISOString()
          };

          // 保存到本地
          let designs = wx.getStorageSync('my_designs') || [];
          designs.unshift(designData);
          wx.setStorageSync('my_designs', designs);

          wx.showToast({ title: '保存成功', icon: 'success' });

          // 返回商品详情页
          setTimeout(() => {
            wx.navigateBack();
          }, 1500);
        }
      }
    });
  },

  /**
   * 选择颜色
   */
  onSelectColor(e) {
    const { index, color, name } = e.currentTarget.dataset;

    this.setData({
      selectedColorIndex: parseInt(index),
      selectedColorName: name,
      selectedColorValue: color
    });

    // 如果有选中的文字元素，改变其颜色
    if (this.data.selectedId) {
      const elements = this.data.elements.map(el => {
        if (el.id === this.data.selectedId && el.type === 'text') {
          return { ...el, color: color };
        }
        return el;
      });

      this.setData({ elements });
    }

    wx.showToast({
      title: `已选择${name}`,
      icon: 'none',
      duration: 1000
    });
  },

  /**
   * 自定义导航栏返回
   */
  onBack() {
    wx.navigateBack({
      fail: () => {
        wx.switchTab({
          url: '/pages/index/index'
        });
      }
    });
  },

  /**
   * 顶部工具栏按钮（预留功能）
   */
  onChangeStyle() {
    wx.showToast({ title: '款式切换功能开发中', icon: 'none' });
  },

  onNewDesign() {
    wx.showModal({
      title: '新建',
      content: '确定清空当前设计？',
      success: (res) => {
        if (res.confirm) {
          this.setData({ elements: [], selectedId: null });
          wx.showToast({ title: '已清空', icon: 'success' });
        }
      }
    });
  },

  onShowHelp() {
    wx.showModal({
      title: '使用帮助',
      content: '选择颜色→添加素材/文字→调整位置→完成设计',
      showCancel: false
    });
  },

  onSaveDesign() {
    // 调用完成方法
    this.onComplete();
  },

  onEditDesign() {
    wx.showToast({ title: '编辑模式开发中', icon: 'none' });
  },

  /**
   * 选择素材
   */
  onSelectMaterial() {
    wx.showToast({ title: '素材库功能开发中', icon: 'none' });
  }
});
