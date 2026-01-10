import { createOpencode, createOpencodeClient } from "@opencode-ai/sdk";

async function main() {
  console.log("=== OpenCode AI SDK 示例 ===\n");

  try {
    // 方法1: 创建完整的 opencode 实例（包含服务器和客户端）
    console.log("1. 创建完整的 OpenCode 实例...");
    const opencode = await createOpencode({
      hostname: "127.0.0.1",
      port: 4096,
      config: {
        model: "anthropic/claude-3-5-sonnet-20241022",
      },
    });

    console.log(`服务器运行在: ${opencode.server.url}`);

    // 检查服务器健康状态
    console.log("\n2. 检查服务器健康状态...");
    const health = await opencode.client.global.health();
    console.log(`健康状态: ${health.data.healthy ? "健康" : "不健康"}`);
    console.log(`版本: ${health.data.version}`);

    // 列出所有项目
    console.log("\n3. 列出所有项目...");
    const projects = await opencode.client.project.list();
    console.log(`找到 ${projects.data.length} 个项目`);
    projects.data.forEach((project, index) => {
      console.log(`  ${index + 1}. ${project.name}`);
    });

    // 获取当前项目
    console.log("\n4. 获取当前项目信息...");
    const currentProject = await opencode.client.project.current();
    console.log(`当前项目: ${currentProject.data.name}`);

    // 列出可用代理
    console.log("\n5. 列出可用代理...");
    const agents = await opencode.client.app.agents();
    console.log(`找到 ${agents.data.length} 个代理`);
    agents.data.forEach((agent, index) => {
      console.log(`  ${index + 1}. ${agent.name} - ${agent.description}`);
    });

    // 关闭服务器
    console.log("\n6. 关闭服务器...");
    opencode.server.close();

    console.log("\n=== 示例完成 ===\n");

    // 方法2: 仅创建客户端（连接到已运行的服务器）
    console.log("7. 创建仅客户端实例的示例...");
    const client = createOpencodeClient({
      baseUrl: "http://localhost:4096",
    });

    console.log("客户端创建成功！");
    console.log("注意：此客户端需要有一个运行的 OpenCode 服务器在 localhost:4096");

  } catch (error) {
    console.error("发生错误:", error.message);
    console.log("\n如果服务器未运行，可以尝试以下方法：");
    console.log("1. 确保 OpenCode 服务已安装并运行");
    console.log("2. 检查端口 4096 是否被占用");
    console.log("3. 或者使用仅客户端模式连接到现有服务器");
  }
}

// 运行示例
main().catch(console.error);