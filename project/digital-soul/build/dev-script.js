/**
 * 开发启动脚本
 * 同时启动 Vite 开发服务器和 Electron
 */

const { spawn } = require('child_process')
const { createServer, build } = require('vite')
const electron = require('electron')
const path = require('path')
const fs = require('fs')

let viteServer = null
let electronProcess = null

async function startVite() {
  console.log('[Dev] Starting Vite dev server...')

  const server = await createServer({
    configFile: path.resolve(__dirname, 'vite.config.ts')
  })

  await server.listen()

  viteServer = server
  console.log('[Dev] Vite dev server started on http://localhost:5173')
}

async function startElectron() {
  console.log('[Dev] Starting Electron...')

  // 构建预加载脚本
  console.log('[Dev] Building preload script...')
  const { build } = require('esbuild')

  await build({
    entryPoints: [path.resolve(__dirname, 'preload.ts')],
    bundle: true,
    outfile: path.resolve(__dirname, '../dist-electron/preload.js'),
    platform: 'node',
    target: 'node18',
    external: ['electron'],
    format: 'cjs'
  })

  // 编译 Electron 主进程
  console.log('[Dev] Building Electron main process...')
  const { execSync } = require('child_process')

  try {
    execSync('tsc -p electron/tsconfig.json', {
      cwd: path.resolve(__dirname, '..'),
      stdio: 'inherit'
    })
  } catch (error) {
    console.error('[Dev] Failed to build Electron main process')
    process.exit(1)
  }

  // 设置环境变量
  process.env.NODE_ENV = 'development'

  // 启动 Electron
  electronProcess = spawn(electron, ['.'], {
    cwd: path.resolve(__dirname, '..'),
    stdio: 'inherit'
  })

  electronProcess.on('close', (code) => {
    console.log(`[Dev] Electron exited with code ${code}`)
    process.exit(code)
  })
}

async function main() {
  try {
    // 先启动 Vite
    await startVite()

    // 再启动 Electron
    await startElectron()

    console.log('[Dev] Development environment ready!')
  } catch (error) {
    console.error('[Dev] Failed to start development environment:', error)
    process.exit(1)
  }
}

// 清理函数
function cleanup() {
  console.log('[Dev] Cleaning up...')

  if (viteServer) {
    viteServer.close()
  }

  if (electronProcess) {
    electronProcess.kill()
  }
}

// 退出时清理
process.on('SIGINT', cleanup)
process.on('SIGTERM', cleanup)
process.on('exit', cleanup)

// 启动
main()
