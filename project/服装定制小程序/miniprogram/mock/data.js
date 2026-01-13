// 本地Mock数据
module.exports = {
  // 轮播图数据
  banners: [
    {
      id: 1,
      image: 'cloud://cloud1-2g7e3gch6d0592e5.636c-cloud1-2g7e3gch6d0592e5-1395469184/banners/ScreenShot_2026-01-12_102502_938.png',
      title: '2025春夏新款上市'
    },
    {
      id: 2,
      image: 'cloud://cloud1-2g7e3gch6d0592e5.636c-cloud1-2g7e3gch6d0592e5-1395469184/banners/ScreenShot_2026-01-12_102530_633.png',
      title: '定制专属你的风格'
    },
    {
      id: 3,
      image: 'cloud://cloud1-2g7e3gch6d0592e5.636c-cloud1-2g7e3gch6d0592e5-1395469184/banners/ScreenShot_2026-01-12_102550_165.png',
      title: '限时优惠活动'
    }
  ],

  // 分类数据
  categories: [
    {
      _id: 'category_001',
      name: '2025秋冬款',
      parentId: '',
      icon: 'cloud://cloud1-2g7e3gch6d0592e5.636c-cloud1-2g7e3gch6d0592e5-1395469184/categories/2025春夏.png',
      sortOrder: 1
    },
    {
      _id: 'category_002',
      name: '冲锋衣系列',
      parentId: '',
      icon: 'cloud://cloud1-2g7e3gch6d0592e5.636c-cloud1-2g7e3gch6d0592e5-1395469184/categories/2025秋冬.png',
      sortOrder: 2
    },
    {
      _id: 'category_003',
      name: '翻领短袖',
      parentId: 'category_002',
      icon: 'cloud://cloud1-2g7e3gch6d0592e5.636c-cloud1-2g7e3gch6d0592e5-1395469184/categories/翻领短袖.png',
      sortOrder: 1
    },
    {
      _id: 'category_004',
      name: '圆领T恤',
      parentId: '',
      icon: 'cloud://cloud1-2g7e3gch6d0592e5.636c-cloud1-2g7e3gch6d0592e5-1395469184/categories/圆领T恤.png',
      sortOrder: 3
    },
    {
      _id: 'category_005',
      name: '马甲',
      parentId: '',
      icon: 'cloud://cloud1-2g7e3gch6d0592e5.636c-cloud1-2g7e3gch6d0592e5-1395469184/categories/卫衣拉链带帽.png',
      sortOrder: 4
    },
    {
      _id: 'category_006',
      name: '卫衣系列',
      parentId: '',
      icon: 'cloud://cloud1-2g7e3gch6d0592e5.636c-cloud1-2g7e3gch6d0592e5-1395469184/categories/卫衣套头带帽.png',
      sortOrder: 5
    },
    {
      _id: 'category_007',
      name: '高端运动',
      parentId: '',
      icon: 'cloud://cloud1-2g7e3gch6d0592e5.636c-cloud1-2g7e3gch6d0592e5-1395469184/categories/高端运动.png',
      sortOrder: 6
    },
    {
      _id: 'category_008',
      name: '速干系列',
      parentId: '',
      icon: 'cloud://cloud1-2g7e3gch6d0592e5.636c-cloud1-2g7e3gch6d0592e5-1395469184/categories/冲锋衣系列.png',
      sortOrder: 7
    }
  ],

  // 商品数据
  products: [
    {
      _id: 'product_001',
      name: '1618三合一冲锋衣',
      categoryId: 'category_002',
      images: ['cloud://cloud1-2g7e3gch6d0592e5.636c-cloud1-2g7e3gch6d0592e5-1395469184/products/6510047d2a6afa723ecad5d310b8729b.jpg'],
      detailImages: [
        'cloud://cloud1-2g7e3gch6d0592e5.636c-cloud1-2g7e3gch6d0592e5-1395469184/products/244ae4366a6a5e8c25d5503f5c5a8005.jpg',
        'cloud://cloud1-2g7e3gch6d0592e5.636c-cloud1-2g7e3gch6d0592e5-1395469184/products/5b307b82bee0b70c3dfc94f12c560194.jpg',
        'cloud://cloud1-2g7e3gch6d0592e5.636c-cloud1-2g7e3gch6d0592e5-1395469184/products/6510047d2a6afa723ecad5d310b8729b.jpg',
        'cloud://cloud1-2g7e3gch6d0592e5.636c-cloud1-2g7e3gch6d0592e5-1395469184/products/6513b381972df7dd7520f201e403e3aa.jpg',
        'cloud://cloud1-2g7e3gch6d0592e5.636c-cloud1-2g7e3gch6d0592e5-1395469184/products/6b9b8345e561fd4976f6d33e716c3cd6.jpg',
        'cloud://cloud1-2g7e3gch6d0592e5.636c-cloud1-2g7e3gch6d0592e5-1395469184/products/84f5acba93f4b369891dda2f11448036.jpg',
        'cloud://cloud1-2g7e3gch6d0592e5.636c-cloud1-2g7e3gch6d0592e5-1395469184/products/84f5acba93f4b369891dda2f11448036.jpg'
      ],
      type: '三合一',
      material: '100%聚酯纤维',
      style: '通款',
      pattern: '常规款',
      pattern: '防风防水',
      status: 1,
      description: '专业户外三合一冲锋衣，防风防水透气',
      // 颜色图片映射（点击颜色框时切换）
      colorImages: {
        '红色': 'cloud://cloud1-2g7e3gch6d0592e5.636c-cloud1-2g7e3gch6d0592e5-1395469184/products/6510047d2a6afa723ecad5d310b8729b.jpg',
        '黑色': 'cloud://cloud1-2g7e3gch6d0592e5.636c-cloud1-2g7e3gch6d0592e5-1395469184/products/6510047d2a6afa723ecad5d310b8729b.jpg',
        '白色': 'cloud://cloud1-2g7e3gch6d0592e5.636c-cloud1-2g7e3gch6d0592e5-1395469184/products/6510047d2a6afa723ecad5d310b8729b.jpg',
        '蓝色': 'cloud://cloud1-2g7e3gch6d0592e5.636c-cloud1-2g7e3gch6d0592e5-1395469184/products/6510047d2a6afa723ecad5d310b8729b.jpg',
        '黄色': 'cloud://cloud1-2g7e3gch6d0592e5.636c-cloud1-2g7e3gch6d0592e5-1395469184/products/6510047d2a6afa723ecad5d310b8729b.jpg',
        '绿色': 'cloud://cloud1-2g7e3gch6d0592e5.636c-cloud1-2g7e3gch6d0592e5-1395469184/products/6510047d2a6afa723ecad5d310b8729b.jpg',
        '紫色': 'cloud://cloud1-2g7e3gch6d0592e5.636c-cloud1-2g7e3gch6d0592e5-1395469184/products/6510047d2a6afa723ecad5d310b8729b.jpg',
        '灰色': 'cloud://cloud1-2g7e3gch6d0592e5.636c-cloud1-2g7e3gch6d0592e5-1395469184/products/6510047d2a6afa723ecad5d310b8729b.jpg',
        '粉色': 'cloud://cloud1-2g7e3gch6d0592e5.636c-cloud1-2g7e3gch6d0592e5-1395469184/products/6510047d2a6afa723ecad5d310b8729b.jpg',
        '橙色': 'cloud://cloud1-2g7e3gch6d0592e5.636c-cloud1-2g7e3gch6d0592e5-1395469184/products/6510047d2a6afa723ecad5d310b8729b.jpg'
      },
      defaultColor: '红色'
    },
    {
      _id: 'product_002',
      name: '纯棉圆领T恤',
      categoryId: 'category_004',
      images: ['cloud://cloud1-2g7e3gch6d0592e5.636c-cloud1-2g7e3gch6d0592e5-1395469184/products/6513b381972df7dd7520f201e403e3aa.jpg'],
      type: '圆领',
      material: '100%棉',
      style: '通款',
      pattern: '常规款',
      pattern: '简约百搭',
      status: 1,
      description: '纯棉材质，舒适透气'
    },
    {
      _id: 'product_003',
      name: '反光条骑行马甲',
      categoryId: 'category_005',
      images: ['cloud://cloud1-2g7e3gch6d0592e5.636c-cloud1-2g7e3gch6d0592e5-1395469184/products/6b9b8345e561fd4976f6d33e716c3cd6.jpg'],
      type: '套头',
      material: '100%聚酯纤维',
      style: '运动款',
      pattern: '常规款',
      pattern: '反光条设计',
      status: 1,
      description: '夜间骑行安全，反光条设计'
    },
    {
      _id: 'product_004',
      name: '速干运动短袖',
      categoryId: 'category_008',
      images: ['cloud://cloud1-2g7e3gch6d0592e5.636c-cloud1-2g7e3gch6d0592e5-1395469184/products/84f5acba93f4b369891dda2f11448036.jpg'],
      type: '短袖',
      material: '速干面料',
      style: '运动款',
      pattern: '常规款',
      pattern: '透气网眼',
      status: 1,
      description: '快速排汗，保持干爽'
    },
    {
      _id: 'product_005',
      name: '高端商务POLO衫',
      categoryId: 'category_007',
      images: ['cloud://cloud1-2g7e3gch6d0592e5.636c-cloud1-2g7e3gch6d0592e5-1395469184/products/84f5acba93f4b369891dda2f11448036.jpg'],
      type: 'POLO领',
      material: '棉混纺',
      style: '商务款',
      pattern: '常规款',
      status: 1,
      description: '商务休闲两相宜'
    },
    {
      _id: 'product_006',
      name: '连帽卫衣',
      categoryId: 'category_006',
      images: ['cloud://cloud1-2g7e3gch6d0592e5.636c-cloud1-2g7e3gch6d0592e5-1395469184/products/6513b381972df7dd7520f201e403e3aa.jpg'],
      type: '连帽',
      material: '加绒棉',
      style: '宽松款',
      pattern: '常规款',
      status: 1,
      description: '秋冬保暖，舒适百搭'
    },
    {
      _id: 'product_007',
      name: '翻领印花短袖',
      categoryId: 'category_003',
      images: ['cloud://cloud1-2g7e3gch6d0592e5.636c-cloud1-2g7e3gch6d0592e5-1395469184/products/6b9b8345e561fd4976f6d33e716c3cd6.jpg'],
      type: '翻领',
      material: '100%棉',
      style: '修身款',
      pattern: '常规款',
      status: 1,
      description: '时尚印花，青春活力'
    },
    {
      _id: 'product_008',
      name: '加绒保暖内衣',
      categoryId: 'category_001',
      images: ['cloud://cloud1-2g7e3gch6d0592e5.636c-cloud1-2g7e3gch6d0592e5-1395469184/products/84f5acba93f4b369891dda2f11448036.jpg'],
      type: '套装',
      material: '莫代尔棉',
      style: '贴身款',
      pattern: '常规款',
      status: 1,
      description: '保暖透气，亲肤舒适'
    },
    {
      _id: 'product_009',
      name: '加厚保暖棉服',
      categoryId: 'category_001',
      images: ['cloud://cloud1-2g7e3gch6d0592e5.636c-cloud1-2g7e3gch6d0592e5-1395469184/products/84f5acba93f4b369891dda2f11448036.jpg'],
      type: '外套',
      material: '棉',
      style: '宽松款',
      pattern: '常规款',
      status: 1,
      description: '加厚保暖，适合秋冬'
    },
    {
      _id: 'product_010',
      name: '时尚羽绒马甲',
      categoryId: 'category_001',
      images: ['cloud://cloud1-2g7e3gch6d0592e5.636c-cloud1-2g7e3gch6d0592e5-1395469184/products/6513b381972df7dd7520f201e403e3aa.jpg'],
      type: '马甲',
      material: '羽绒',
      style: '轻薄款',
      pattern: '常规款',
      status: 1,
      description: '轻便保暖，时尚百搭'
    },
    {
      _id: 'product_011',
      name: '防风保暖冲锋衣',
      categoryId: 'category_001',
      images: ['cloud://cloud1-2g7e3gch6d0592e5.636c-cloud1-2g7e3gch6d0592e5-1395469184/products/6b9b8345e561fd4976f6d33e716c3cd6.jpg'],
      type: '外套',
      material: '聚酯纤维',
      style: '运动款',
      pattern: '常规款',
      status: 1,
      description: '防风防水，户外必备'
    }
  ],

  // 设计模板
  templates: [
    {
      _id: 'template_001',
      name: '简约风格',
      description: '适合日常穿搭',
      thumbnail: '/images/template-1.jpg',
      elements: []
    },
    {
      _id: 'template_002',
      name: '商务风格',
      description: '适合商务场合',
      thumbnail: '/images/template-2.jpg',
      elements: []
    },
    {
      _id: 'template_003',
      name: '运动风格',
      description: '适合运动休闲',
      thumbnail: '/images/template-3.jpg',
      elements: []
    }
  ]
};
