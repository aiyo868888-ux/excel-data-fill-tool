// Pencil 文件格式 (JSON)
const pencilDesign = {
  version: "1.0",
  type: "pencil-design",
  canvas: {
    width: 375,
    height: 812,
    background: "#0a0a0f",
    name: "闪念笔记 - 标签选择页"
  },
  elements: [
    {
      type: "text",
      id: "title",
      content: "闪念笔记",
      style: {
        x: 187.5,
        y: 80,
        fontSize: 48,
        fontWeight: "bold",
        textAlign: "center",
        fill: "url(#gradient-main)"
      }
    },
    {
      type: "card",
      id: "card-flash",
      content: {
        emoji: "⚡",
        title: "闪念",
        description: "快速记录瞬间的想法"
      },
      style: {
        x: 24,
        y: 160,
        width: 327,
        height: 120,
        borderRadius: 16,
        background: "linear-gradient(145deg, #1a1a2e, #161625)",
        boxShadow: "0 0 20px rgba(59, 130, 246, 0.5)"
      }
    },
    {
      type: "card",
      id: "card-insight",
      content: {
        emoji: "💡",
        title: "启发",
        description: "捕捉灵感的火花"
      },
      style: {
        x: 24,
        y: 296,
        width: 327,
        height: 120,
        borderRadius: 16,
        background: "linear-gradient(145deg, #1a1a2e, #161625)",
        boxShadow: "0 0 20px rgba(168, 85, 247, 0.5)"
      }
    },
    {
      type: "card",
      id: "card-todo",
      content: {
        emoji: "✓",
        title: "待办",
        description: "规划要完成的任务"
      },
      style: {
        x: 24,
        y: 432,
        width: 327,
        height: 120,
        borderRadius: 16,
        background: "linear-gradient(145deg, #1a1a2e, #161625)",
        boxShadow: "0 0 20px rgba(34, 197, 94, 0.5)"
      }
    }
  ],
  definitions: {
    gradients: [
      {
        id: "gradient-main",
        stops: [
          { offset: "0%", color: "#3b82f6" },
          { offset: "100%", color: "#a855f7" }
        ]
      }
    ]
  }
};

console.log(JSON.stringify(pencilDesign, null, 2));
