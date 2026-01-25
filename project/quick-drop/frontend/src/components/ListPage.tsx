import { useState, useEffect } from 'react';
import type { Note, NoteType } from '../types';
import { storage } from '../utils/storage';

interface ListPageProps {
  type: NoteType;
  refreshKey: number;
  onBack: () => void;
  onTypeChange: (type: NoteType) => void;
  onEdit: (id: number, content: string) => void;
  onDelete: (id: number) => void;
}

const TAG_TITLES = {
  flash: '⚡ 闪念',
  insight: '💡 启发',
  todo: '✓ 待办',
};

const BOTTOM_TABS = [
  { type: 'flash' as const, emoji: '⚡', label: '闪念' },
  { type: 'insight' as const, emoji: '💡', label: '启发' },
  { type: 'todo' as const, emoji: '✓', label: '待办' },
];

export default function ListPage({ type, refreshKey, onBack, onTypeChange, onEdit, onDelete }: ListPageProps) {
  const [notes, setNotes] = useState<Note[]>([]);

  useEffect(() => {
    setNotes(storage.getNotes(type));
  }, [type, refreshKey]);

  return (
    <div className="min-h-screen flex flex-col">
      <div className="flex items-center justify-between p-4 border-b border-gray-800">
        <button
          onClick={onBack}
          className="text-gray-400 hover:text-white transition-colors"
        >
          ← 返回
        </button>
        <span className="text-xl font-bold">{TAG_TITLES[type]}</span>
        <div className="w-12"></div>
      </div>

      <div className="flex-1 overflow-y-auto scrollbar-hide p-4 space-y-3">
        {notes.length === 0 ? (
          <div className="text-center text-gray-600 py-12">
            暂无{TAG_TITLES[type]}记录
          </div>
        ) : (
          notes.map((note) => (
            <div
              key={note.id}
              className="card-gradient p-4 rounded-xl hover:scale-[1.02] active:scale-[0.98] transition-transform"
            >
              <div className="text-white leading-relaxed whitespace-pre-wrap break-words">
                {note.content}
              </div>
              <div className="flex items-center justify-between mt-3">
                <div className="text-xs text-gray-600">{note.timestamp}</div>
                <div className="flex gap-2">
                  <button
                    onClick={() => onEdit(note.id, note.content)}
                    className="text-xs text-flash-blue hover:underline"
                  >
                    编辑
                  </button>
                  <button
                    onClick={() => onDelete(note.id)}
                    className="text-xs text-red-400 hover:underline"
                  >
                    删除
                  </button>
                </div>
              </div>
            </div>
          ))
        )}
      </div>

      <div className="border-t border-gray-800 p-2 pb-safe">
        <div className="flex justify-around">
          {BOTTOM_TABS.map((tab) => (
            <button
              key={tab.type}
              onClick={() => onTypeChange(tab.type)}
              className={`flex-1 py-3 rounded-xl text-center transition-all ${
                type === tab.type
                  ? 'btn-gradient'
                  : 'hover:bg-white/5'
              }`}
            >
              <div className="text-2xl">{tab.emoji}</div>
              <div className="text-xs mt-1">{tab.label}</div>
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}
