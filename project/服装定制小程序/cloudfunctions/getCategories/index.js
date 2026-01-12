// 云函数入口文件
const cloud = require('wx-server-sdk')
cloud.init({ env: cloud.DYNAMIC_CURRENT_ENV })
const db = cloud.database()

// 云函数入口函数
exports.main = async (event, context) => {
  console.log('getCategories 云函数被调用', event)

  try {
    const result = await db.collection('categories')
      .orderBy('sortOrder', 'asc')
      .get()

    return {
      code: 200,
      data: result.data
    }
  } catch (err) {
    console.error('getCategories 云函数执行失败', err)
    return {
      code: 500,
      error: err.message
    }
  }
}
