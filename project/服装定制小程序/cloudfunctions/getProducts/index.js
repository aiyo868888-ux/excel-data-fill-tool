// 云函数入口文件
const cloud = require('wx-server-sdk')
cloud.init({ env: cloud.DYNAMIC_CURRENT_ENV })
const db = cloud.database()
const _ = db.command

// 云函数入口函数
exports.main = async (event, context) => {
  const { categoryId, keyword, page = 1, limit = 10, sortBy } = event

  console.log('getProducts 云函数被调用', event)

  try {
    // 构建查询条件
    let query = db.collection('products').where({
      status: _.eq(1)
    })

    if (categoryId) {
      query = query.where({ categoryId })
    }

    if (keyword) {
      query = query.where({
        name: db.RegExp({
          regexp: keyword,
          options: 'i'
        })
      })
    }

    // 排序
    let orderBy = 'createTime'
    let order = 'desc'

    if (sortBy === 'price') {
      orderBy = 'price'
      order = 'asc'
    } else if (sortBy === 'sales') {
      orderBy = 'sales'
      order = 'desc'
    }

    // 分页查询
    const result = await query
      .orderBy(orderBy, order)
      .skip((page - 1) * limit)
      .limit(limit)
      .get()

    // 获取总数
    const countResult = await query.count()

    return {
      code: 200,
      data: {
        list: result.data,
        total: countResult.total,
        page,
        limit
      }
    }
  } catch (err) {
    console.error('getProducts 云函数执行失败', err)
    return {
      code: 500,
      error: err.message
    }
  }
}
