import type { Note, NoteType } from '../types';

const STORAGE_KEYS = {
  flash: 'quickdrop_notes_flash',
  insight: 'quickdrop_notes_insight',
  todo: 'quickdrop_notes_todo',
};

export const storage = {
  getNotes(type: NoteType): Note[] {
    try {
      const data = localStorage.getItem(STORAGE_KEYS[type]);
      return data ? JSON.parse(data) : [];
    } catch (error) {
      console.error('Error reading notes:', error);
      return [];
    }
  },

  saveNote(type: NoteType, content: string): Note {
    const notes = this.getNotes(type);
    const newNote: Note = {
      id: Date.now(),
      content,
      timestamp: new Date().toLocaleString('zh-CN'),
      type,
    };
    notes.unshift(newNote);
    localStorage.setItem(STORAGE_KEYS[type], JSON.stringify(notes));
    return newNote;
  },

  updateNote(type: NoteType, noteId: number, content: string): Note | null {
    const notes = this.getNotes(type);
    const index = notes.findIndex(n => n.id === noteId);
    if (index === -1) return null;

    notes[index] = {
      ...notes[index],
      content,
      timestamp: new Date().toLocaleString('zh-CN') + ' (已编辑)',
    };
    localStorage.setItem(STORAGE_KEYS[type], JSON.stringify(notes));
    return notes[index];
  },

  deleteNote(type: NoteType, noteId: number): boolean {
    const notes = this.getNotes(type);
    const filtered = notes.filter(n => n.id !== noteId);
    if (filtered.length === notes.length) return false;

    localStorage.setItem(STORAGE_KEYS[type], JSON.stringify(filtered));
    return true;
  },
};
