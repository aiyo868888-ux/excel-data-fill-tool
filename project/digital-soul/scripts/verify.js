/**
 * 项目验证脚本
 */

const fs = require('fs')
const path = require('path')

const projectRoot = path.join(__dirname, '..')

console.log('🔍 验证 Digital Soul 项目结构...\n')

const checks = []

// 检查关键文件
const keyFiles = [
  'package.json',
  'tsconfig.json',
  'electron/tsconfig.json',
  'electron/main.ts',
  'electron/database/schema.sql',
  'electron/database/sqlite-manager.ts',
  'src/main.ts',
  'src/App.vue',
  'build/vite.config.ts',
  'index.html'
]

keyFiles.forEach(file => {
  const filePath = path.join(projectRoot, file)
  const exists = fs.existsSync(filePath)
  checks.push({ file, exists, type: 'file' })
  console.log(`${exists ? '✅' : '❌'} ${file}`)
})

// 检查关键目录
const keyDirs = [
  'src',
  'src/core',
  'src/core/models',
  'src/components',
  'src/views',
  'electron',
  'electron/database',
  'build',
  'tests'
]

keyDirs.forEach(dir => {
  const dirPath = path.join(projectRoot, dir)
  const exists = fs.existsSync(dirPath)
  checks.push({ dir, exists, type: 'dir' })
  console.log(`${exists ? '✅' : '❌'} ${dir}/`)
})

// 检查依赖
console.log('\n📦 检查依赖安装...')
const pkgPath = path.join(__dirname, '..', 'package.json')
const pkg = JSON.parse(fs.readFileSync(pkgPath, 'utf-8'))

const deps = [...Object.keys(pkg.dependencies || {}), ...Object.keys(pkg.devDependencies || {})]
const nodeModulesPath = path.join(projectRoot, 'node_modules')

let installedCount = 0
deps.forEach(dep => {
  const depPath = path.join(nodeModulesPath, dep)
  const exists = fs.existsSync(depPath)
  if (exists) installedCount++
})

console.log(`✅ 已安装 ${installedCount}/${deps.length} 个依赖包`)

// 总结
const allPassed = checks.every(c => c.exists)
console.log('\n' + '='.repeat(50))
if (allPassed && installedCount === deps.length) {
  console.log('✅ 项目结构完整，可以启动开发环境！')
  console.log('\n运行以下命令启动：')
  console.log('  npm run dev')
} else {
  console.log('⚠️ 项目结构不完整，请检查上述错误')
  if (installedCount < deps.length) {
    console.log('\n提示：部分依赖未安装，请运行：')
    console.log('  npm install')
  }
}
console.log('='.repeat(50))
