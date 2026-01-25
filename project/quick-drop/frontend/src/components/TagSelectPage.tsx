import type { NoteType } from '../types';

interface TagSelectPageProps {
  onTypeSelect: (type: NoteType) => void;
}

const TAG_INFO = {
  flash: {
    emoji: '⚡',
    name: '闪念',
    desc: '快速记录瞬间的想法',
    color: 'text-flash-blue',
    glow: 'shadow-glow-blue',
  },
  insight: {
    emoji: '💡',
    name: '启发',
    desc: '捕捉灵感的火花',
    color: 'text-insight-purple',
    glow: 'shadow-glow-purple',
  },
  todo: {
    emoji: '✓',
    name: '待办',
    desc: '规划要完成的任务',
    color: 'text-todo-green',
    glow: 'shadow-glow-green',
  },
};

export default function TagSelectPage({ onTypeSelect }: TagSelectPageProps) {
  return (
    <div className="min-h-screen flex flex-col items-center justify-center p-6">
      <h1 className="text-4xl font-bold bg-gradient-to-r from-flash-blue to-insight-purple bg-clip-text text-transparent mb-12">
        闪念笔记
      </h1>

      <div className="w-full max-w-md space-y-4">
        {(Object.keys(TAG_INFO) as NoteType[]).map((type) => {
          const info = TAG_INFO[type];
          return (
            <button
              key={type}
              onClick={() => onTypeSelect(type)}
              className={`w-full p-6 rounded-2xl card-gradient text-left hover:scale-105 active:scale-95 transition-transform duration-200 ${info.glow}`}
            >
              <div className="text-3xl mb-3">{info.emoji}</div>
              <div className={`text-2xl font-bold ${info.color}`}>{info.name}</div>
              <div className="text-sm text-gray-500 mt-2">{info.desc}</div>
            </button>
          );
        })}
      </div>
    </div>
  );
}
