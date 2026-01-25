#!/usr/bin/env node
/**
 * Simple Memory MCP Server
 * 为 Claude Code VS Code 扩展提供记忆功能
 */

const { Server } = require('@modelcontextprotocol/sdk/server/index.js');
const { StdioServerTransport } = require('@modelcontextprotocol/sdk/server/stdio.js');
const {
  CallToolRequestSchema,
  ListToolsRequestSchema,
} = require('@modelcontextprotocol/sdk/types.js');
const SimpleMemory = require('./memory.js');
const SmartMemory = require('./smart-memory.js');

class MemoryMCPServer {
  constructor() {
    this.memory = new SimpleMemory();
    this.smartMemory = new SmartMemory();
    this.server = new Server(
      {
        name: 'simple-memory-server',
        version: '1.0.0',
      },
      {
        capabilities: {
          tools: {},
        },
      }
    );

    this.setupHandlers();
  }

  setupHandlers() {
    // 列出可用工具
    this.server.setRequestHandler(ListToolsRequestSchema, async () => {
      return {
        tools: [
          {
            name: 'mem_add',
            description: '记录一条记忆（决策、bug修复、发现等）',
            inputSchema: {
              type: 'object',
              properties: {
                content: {
                  type: 'string',
                  description: '要记录的内容',
                },
                category: {
                  type: 'string',
                  description: '分类：decision, bugfix, feature, refactor, discovery',
                  default: 'general',
                },
              },
              required: ['content'],
            },
          },
          {
            name: 'mem_search',
            description: '搜索历史记忆',
            inputSchema: {
              type: 'object',
              properties: {
                query: {
                  type: 'string',
                  description: '搜索关键词',
                },
              },
              required: ['query'],
            },
          },
          {
            name: 'mem_list',
            description: '查看最近的记忆记录',
            inputSchema: {
              type: 'object',
              properties: {
                limit: {
                  type: 'number',
                  description: '返回数量（默认10）',
                  default: 10,
                },
              },
            },
          },
          {
            name: 'mem_stats',
            description: '查看记忆统计信息',
            inputSchema: {
              type: 'object',
              properties: {},
            },
          },
          {
            name: 'mem_detect',
            description: '智能检测内容是否应该记录',
            inputSchema: {
              type: 'object',
              properties: {
                content: {
                  type: 'string',
                  description: '要检测的内容',
                },
              },
              required: ['content'],
            },
          },
          {
            name: 'mem_auto_add',
            description: '智能检测并自动记录（如果值得记录）',
            inputSchema: {
              type: 'object',
              properties: {
                content: {
                  type: 'string',
                  description: '要记录的内容',
                },
              },
              required: ['content'],
            },
          },
        ],
      };
    });

    // 处理工具调用
    this.server.setRequestHandler(CallToolRequestSchema, async (request) => {
      const { name, arguments: args } = request.params;

      try {
        switch (name) {
          case 'mem_add': {
            const { content, category = 'general' } = args;
            const result = this.memory.add(content, category);
            return {
              content: [
                {
                  type: 'text',
                  text: `✅ 已记录 [${result.category}]: ${result.content}\nID: ${result.id}\n时间: ${result.timestamp}`,
                },
              ],
            };
          }

          case 'mem_search': {
            const { query } = args;
            const results = this.memory.search(query);
            if (results.length === 0) {
              return {
                content: [
                  {
                    type: 'text',
                    text: `未找到包含 "${query}" 的记录`,
                  },
                ],
              };
            }
            const formatted = results
              .map(
                m => `[${m.category}] ${m.timestamp}\n  ${m.content}`
              )
              .join('\n\n');
            return {
              content: [
                {
                  type: 'text',
                  text: `🔍 找到 ${results.length} 条记录:\n\n${formatted}`,
                },
              ],
            };
          }

          case 'mem_list': {
            const limit = args.limit || 10;
            const recent = this.memory.getRecent(limit);
            if (recent.length === 0) {
              return {
                content: [
                  {
                    type: 'text',
                    text: '还没有任何记录',
                  },
                ],
              };
            }
            const formatted = recent
              .map(
                m => `[${m.category}] ${m.timestamp}\n  ${m.content}`
              )
              .join('\n\n');
            return {
              content: [
                {
                  type: 'text',
                  text: `📋 最近 ${recent.length} 条记录:\n\n${formatted}`,
                },
              ],
            };
          }

          case 'mem_stats': {
            const stats = this.memory.getStats();
            const categoryList = Object.entries(stats.byCategory)
              .map(([cat, count]) => `  ${cat}: ${count}`)
              .join('\n');
            return {
              content: [
                {
                  type: 'text',
                  text: `📊 记忆统计:\n  总数: ${stats.total}\n${categoryList}`,
                },
              ],
            };
          }

          case 'mem_detect': {
            const { content } = args;
            const detection = this.smartMemory.detector.detect(content);

            if (detection.shouldRecord) {
              const prompt = this.smartMemory.generatePrompt(content, detection);
              return {
                content: [
                  {
                    type: 'text',
                    text: `🤖 ${prompt.message}\n\n分类: [${detection.category}]\n置信度: ${detection.confidence}/5 ⭐\n匹配关键词: ${detection.matchedKeywords.join(', ')}\n\n建议: 建议记录这条内容`,
                  },
                ],
              };
            } else {
              return {
                content: [
                  {
                    type: 'text',
                    text: `⏭️  不建议记录\n\n原因: ${detection.reason}${detection.confidence ? `\n置信度: ${detection.confidence}/5` : ''}`,
                  },
                ],
              };
            }
          }

          case 'mem_auto_add': {
            const { content } = args;
            const result = this.smartMemory.processInput(content);

            if (result.shouldRecord) {
              return {
                content: [
                  {
                    type: 'text',
                    text: `✅ 自动记录成功！\n\n分类: [${result.category}]\n置信度: ${result.confidence}/5 ⭐\n匹配关键词: ${result.matchedKeywords.join(', ')}\n\n内容: ${content}\n\nID: ${result.record.id}\n时间: ${result.record.timestamp}`,
                  },
                ],
              };
            } else {
              return {
                content: [
                  {
                    type: 'text',
                    text: `⏭️  跳过记录\n\n原因: ${result.reason}\n\n这条内容不值得自动记录。如果要强制记录，请使用 mem_add 工具。`,
                  },
                ],
              };
            }
          }

          default:
            throw new Error(`未知工具: ${name}`);
        }
      } catch (error) {
        return {
          content: [
            {
              type: 'text',
              text: `错误: ${error.message}`,
            },
          ],
          isError: true,
        };
      }
    });
  }

  async run() {
    const transport = new StdioServerTransport();
    await this.server.connect(transport);
    console.error('Simple Memory MCP Server running...');
  }
}

// 启动服务器
if (require.main === module) {
  const server = new MemoryMCPServer();
  server.run().catch(console.error);
}

module.exports = MemoryMCPServer;
