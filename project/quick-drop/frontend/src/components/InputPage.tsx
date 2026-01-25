import { useState, useEffect, useRef } from 'react';
import type { NoteType } from '../types';

interface InputPageProps {
  type: NoteType;
  editingNote: { id: number; content: string } | null;
  onBack: () => void;
  onSave: (content: string) => void;
}

const TAG_TITLES = {
  flash: '⚡ 闪念',
  insight: '💡 启发',
  todo: '✓ 待办',
};

export default function InputPage({ type, editingNote, onBack, onSave }: InputPageProps) {
  const [content, setContent] = useState('');
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  useEffect(() => {
    if (editingNote) {
      setContent(editingNote.content);
    } else {
      setContent('');
    }
  }, [editingNote]);

  useEffect(() => {
    textareaRef.current?.focus();
  }, []);

  const handleSave = () => {
    if (content.trim()) {
      onSave(content.trim());
    }
  };

  return (
    <div className="min-h-screen flex flex-col p-4">
      <div className="flex items-center justify-between mb-4">
        <button
          onClick={onBack}
          className="text-gray-400 hover:text-white transition-colors text-lg"
        >
          ← 返回
        </button>
        <span className="text-xl font-bold">{TAG_TITLES[type]}</span>
        <div className="w-12"></div>
      </div>

      <textarea
        ref={textareaRef}
        value={content}
        onChange={(e) => setContent(e.target.value)}
        placeholder="在这里输入..."
        className="flex-1 w-full p-4 rounded-2xl card-gradient text-white resize-none focus:outline-none focus:ring-2 focus:ring-flash-blue placeholder-gray-600 text-base leading-relaxed"
      />

      <button
        onClick={handleSave}
        disabled={!content.trim()}
        className="w-full p-4 mt-4 rounded-2xl btn-gradient font-bold text-lg disabled:opacity-50 disabled:cursor-not-allowed hover:opacity-90 active:scale-95 transition-all"
      >
        {editingNote ? '更新' : '保存'}
      </button>
    </div>
  );
}
