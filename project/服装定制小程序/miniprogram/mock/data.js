// 本地Mock数据
module.exports = {
  // 轮播图数据
  banners: [
    {
      id: 1,
      image: '/images/banners/banner1.jpg',
      title: '2025春夏新款上市'
    },
    {
      id: 2,
      image: '/images/banners/banner2.jpg',
      title: '定制专属你的风格'
    },
    {
      id: 3,
      image: '/images/banners/banner3.jpg',
      title: '限时优惠活动'
    }
  ],

  // 分类数据
  categories: [
    {
      _id: 'category_001',
      name: '2025秋冬款',
      parentId: '',
      icon: '/images/categories/category-1.png',
      sortOrder: 1
    },
    {
      _id: 'category_002',
      name: '冲锋衣系列',
      parentId: '',
      icon: '/images/categories/category-2.png',
      sortOrder: 2
    },
    {
      _id: 'category_003',
      name: '翻领短袖',
      parentId: 'category_002',
      icon: '/images/categories/category-3.png',
      sortOrder: 1
    },
    {
      _id: 'category_004',
      name: '圆领T恤',
      parentId: '',
      icon: '/images/categories/category-4.png',
      sortOrder: 3
    },
    {
      _id: 'category_005',
      name: '马甲',
      parentId: '',
      icon: '/images/categories/category-5.png',
      sortOrder: 4
    },
    {
      _id: 'category_006',
      name: '卫衣系列',
      parentId: '',
      icon: '/images/categories/category-6.png',
      sortOrder: 5
    },
    {
      _id: 'category_007',
      name: '高端运动',
      parentId: '',
      icon: '/images/categories/category-7.png',
      sortOrder: 6
    },
    {
      _id: 'category_008',
      name: '速干系列',
      parentId: '',
      icon: '/images/categories/category-8.png',
      sortOrder: 7
    }
  ],

  // 商品数据
  products: [
    {
      _id: 'product_001',
      name: '1618三合一冲锋衣',
      categoryId: 'category_002',
      images: ['/images/products/product-1.jpg'],
      detailImages: [
        '/images/details/244ae4366a6a5e8c25d5503f5c5a8005.jpg',
        '/images/details/5b307b82bee0b70c3dfc94f12c560194.jpg',
        '/images/details/6510047d2a6afa723ecad5d310b8729b.jpg',
        '/images/details/6513b381972df7dd7520f201e403e3aa.jpg',
        '/images/details/6b9b8345e561fd4976f6d33e716c3cd6.jpg',
        '/images/details/84f5acba93f4b369891dda2f11448036.jpg',
        '/images/details/8e34f8da3ed64542c8486eced2de3145.jpg'
      ],
      type: '三合一',
      material: '100%聚酯纤维',
      style: '通款',
      pattern: '常规款',
      pattern: '防风防水',
      price: 299,
      sales: 100,
      status: 1,
      description: '专业户外三合一冲锋衣，防风防水透气'
    },
    {
      _id: 'product_002',
      name: '纯棉圆领T恤',
      categoryId: 'category_004',
      images: ['/images/products/product-2.jpg'],
      type: '圆领',
      material: '100%棉',
      style: '通款',
      pattern: '常规款',
      pattern: '简约百搭',
      price: 89,
      sales: 230,
      status: 1,
      description: '纯棉材质，舒适透气'
    },
    {
      _id: 'product_003',
      name: '反光条骑行马甲',
      categoryId: 'category_005',
      images: ['/images/products/product-3.jpg'],
      type: '套头',
      material: '100%聚酯纤维',
      style: '运动款',
      pattern: '常规款',
      pattern: '反光条设计',
      price: 159,
      sales: 178,
      status: 1,
      description: '夜间骑行安全，反光条设计'
    },
    {
      _id: 'product_004',
      name: '速干运动短袖',
      categoryId: 'category_008',
      images: ['/images/products/product-4.jpg'],
      type: '短袖',
      material: '速干面料',
      style: '运动款',
      pattern: '常规款',
      pattern: '透气网眼',
      price: 129,
      sales: 342,
      status: 1,
      description: '快速排汗，保持干爽'
    },
    {
      _id: 'product_005',
      name: '高端商务POLO衫',
      categoryId: 'category_007',
      images: ['/images/products/product-5.jpg'],
      type: 'POLO领',
      material: '棉混纺',
      style: '商务款',
      pattern: '常规款',
      price: 199,
      sales: 89,
      status: 1,
      description: '商务休闲两相宜'
    },
    {
      _id: 'product_006',
      name: '连帽卫衣',
      categoryId: 'category_006',
      images: ['/images/products/product-6.jpg'],
      type: '连帽',
      material: '加绒棉',
      style: '宽松款',
      pattern: '常规款',
      price: 179,
      sales: 156,
      status: 1,
      description: '秋冬保暖，舒适百搭'
    },
    {
      _id: 'product_007',
      name: '翻领印花短袖',
      categoryId: 'category_003',
      images: ['/images/products/product-7.jpg'],
      type: '翻领',
      material: '100%棉',
      style: '修身款',
      pattern: '常规款',
      price: 99,
      sales: 267,
      status: 1,
      description: '时尚印花，青春活力'
    },
    {
      _id: 'product_008',
      name: '加绒保暖内衣',
      categoryId: 'category_001',
      images: ['/images/products/product-8.jpg'],
      type: '套装',
      material: '莫代尔棉',
      style: '贴身款',
      pattern: '常规款',
      price: 149,
      sales: 423,
      status: 1,
      description: '保暖透气，亲肤舒适'
    },
    {
      _id: 'product_009',
      name: '加厚保暖棉服',
      categoryId: 'category_001',
      images: ['/images/products/product-9.jpg'],
      type: '外套',
      material: '棉',
      style: '宽松款',
      pattern: '常规款',
      price: 399,
      sales: 56,
      status: 1,
      description: '加厚保暖，适合秋冬'
    },
    {
      _id: 'product_010',
      name: '时尚羽绒马甲',
      categoryId: 'category_001',
      images: ['/images/products/product-10.jpg'],
      type: '马甲',
      material: '羽绒',
      style: '轻薄款',
      pattern: '常规款',
      price: 259,
      sales: 89,
      status: 1,
      description: '轻便保暖，时尚百搭'
    },
    {
      _id: 'product_011',
      name: '防风保暖冲锋衣',
      categoryId: 'category_001',
      images: ['/images/products/product-11.jpg'],
      type: '外套',
      material: '聚酯纤维',
      style: '运动款',
      pattern: '常规款',
      price: 459,
      sales: 134,
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
