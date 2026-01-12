// pages/editor/editor.js - 自助设计编辑页
const mockData = require('../../mock/data.js');

Page({
  data: {
    productId: '',
    productName: '',
    productImage: '',
    productPrice: 0,
    productSales: 0,
    productType: '',
    productMaterial: '',
    productStyle: '',
    detailImages: [],
    elements: [],
    selectedId: null
  },

  onLoad(options) {
    console.log('设计编辑页加载', options);

    const productId = options.productId;
    if (productId) {
      const product = mockData.products.find(p => p._id === productId);
      if (product) {
        this.setData({
          productId: product._id,
          productName: product.name,
          productImage: product.images[0],
          productPrice: product.price,
          productSales: product.sales,
          productType: product.type,
          productMaterial: product.material,
          productStyle: product.style,
          detailImages: product.detailImages || product.images || []
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
  }
});
