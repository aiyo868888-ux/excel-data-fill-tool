/**
 * 数据批量导入脚本
 * 使用方法：
 * 1. 在云开发控制台 → 云函数 → 创建云函数 "importData"
 * 2. 将此文件内容复制到 index.js
 * 3. 部署云函数
 * 4. 在小程序中调用此云函数
 */

const cloud = require('wx-server-sdk')
cloud.init({ env: cloud.DYNAMIC_CURRENT_ENV })
const db = cloud.database()

exports.main = async (event, context) => {
  const { type } = event // 'categories' 或 'products'

  console.log('开始导入数据:', type)

  try {
    if (type === 'categories') {
      return await importCategories()
    } else if (type === 'products') {
      return await importProducts()
    } else {
      return {
        code: 400,
        error: '不支持的导入类型，请使用 categories 或 products'
      }
    }
  } catch (err) {
    console.error('导入失败:', err)
    return {
      code: 500,
      error: err.message
    }
  }
})

/**
 * 导入分类数据
 */
async function importCategories() {
  const categories = [
    {
      _id: 'category_001',
      name: '2025秋冬款',
      parentId: '',
      icon: '/images/category-1.png',
      sortOrder: 1
    },
    {
      _id: 'category_002',
      name: '冲锋衣系列',
      parentId: '',
      icon: '/images/category-2.png',
      sortOrder: 2
    },
    {
      _id: 'category_003',
      name: '翻领短袖',
      parentId: 'category_002',
      icon: '/images/category-3.png',
      sortOrder: 1
    },
    {
      _id: 'category_004',
      name: '圆领T恤',
      parentId: '',
      icon: '/images/category-4.png',
      sortOrder: 3
    },
    {
      _id: 'category_005',
      name: '马甲',
      parentId: '',
      icon: '/images/category-5.png',
      sortOrder: 4
    },
    {
      _id: 'category_006',
      name: '卫衣系列',
      parentId: '',
      icon: '/images/category-6.png',
      sortOrder: 5
    },
    {
      _id: 'category_007',
      name: '高端运动',
      parentId: '',
      icon: '/images/category-7.png',
      sortOrder: 6
    },
    {
      _id: 'category_008',
      name: '速干系列',
      parentId: '',
      icon: '/images/category-8.png',
      sortOrder: 7
    }
  ]

  // 批量插入
  const results = []
  for (const category of categories) {
    try {
      const result = await db.collection('categories').add({
        data: {
          ...category,
          createTime: new Date()
        }
      })
      results.push({ success: true, id: result._id })
      console.log('分类添加成功:', category.name)
    } catch (err) {
      results.push({ success: false, name: category.name, error: err.message })
    }
  }

  return {
    code: 200,
    message: `成功导入 ${results.filter(r => r.success).length}/${results.length} 个分类`,
    results
  }
}

/**
 * 导入商品数据
 */
async function importProducts() {
  const products = [
    {
      _id: 'product_001',
      name: '1618三合一冲锋衣',
      categoryId: 'category_002',
      images: ['/images/product-1.jpg'],
      type: '三合一',
      material: '100%聚酯纤维',
      style: '通款',
      price: 299,
      sales: 100,
      status: 1,
      description: '专业户外三合一冲锋衣，防风防水'
    },
    {
      _id: 'product_002',
      name: 'KY211户外三合一奥粒绒冲锋衣',
      categoryId: 'category_002',
      images: ['/images/product-2.jpg'],
      type: '三合一',
      material: '100%聚酯纤维',
      style: '通款',
      price: 399,
      sales: 85,
      status: 1,
      description: '奥粒绒内胆，保暖透气'
    },
    {
      _id: 'product_003',
      name: '5568石墨烯奥粒绒三合一冲锋衣',
      categoryId: 'category_002',
      images: ['/images/product-3.jpg'],
      type: '三合一',
      material: '石墨烯发热',
      style: '通款',
      price: 499,
      sales: 62,
      status: 1,
      description: '石墨烯发热技术，快速升温'
    },
    {
      _id: 'product_004',
      name: '纯棉圆领T恤',
      categoryId: 'category_004',
      images: ['/images/product-4.jpg'],
      type: '圆领',
      material: '100%棉',
      style: '通款',
      price: 89,
      sales: 230,
      status: 1,
      description: '纯棉材质，舒适透气'
    },
    {
      _id: 'product_005',
      name: '反光条骑行马甲',
      categoryId: 'category_005',
      images: ['/images/product-5.jpg'],
      type: '套头',
      material: '100%聚酯纤维',
      style: '运动款',
      price: 159,
      sales: 178,
      status: 1,
      description: '夜间骑行安全，反光条设计'
    },
    {
      _id: 'product_006',
      name: '加绒卫衣圆领',
      categoryId: 'category_006',
      images: ['/images/product-6.jpg'],
      type: '圆领',
      material: '棉+聚酯纤维',
      style: '宽松版',
      price: 199,
      sales: 345,
      status: 1,
      description: '内里加绒，保暖舒适'
    },
    {
      _id: 'product_007',
      name: '速干运动T恤',
      categoryId: 'category_008',
      images: ['/images/product-7.jpg'],
      type: '圆领',
      material: '100%聚酯纤维',
      style: '修身版',
      price: 129,
      sales: 412,
      status: 1,
      description: '速干透气，适合运动'
    },
    {
      _id: 'product_008',
      name: '商务休闲马甲',
      categoryId: 'category_005',
      images: ['/images/product-8.jpg'],
      type: '套头',
      material: '棉+锦纶',
      style: '商务款',
      price: 189,
      sales: 156,
      status: 1,
      description: '商务休闲两用，多口袋设计'
    }
  ]

  // 批量插入
  const results = []
  for (const product of products) {
    try {
      const result = await db.collection('products').add({
        data: {
          ...product,
          createTime: new Date(),
          updateTime: new Date()
        }
      })
      results.push({ success: true, id: result._id })
      console.log('商品添加成功:', product.name)
    } catch (err) {
      results.push({ success: false, name: product.name, error: err.message })
    }
  }

  return {
    code: 200,
    message: `成功导入 ${results.filter(r => r.success).length}/${results.length} 个商品`,
    results
  }
}
