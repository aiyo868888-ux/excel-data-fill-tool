export type NoteType = 'flash' | 'insight' | 'todo';

export interface Note {
  id: number;
  content: string;
  timestamp: string;
  type: NoteType;
}

export type PageType = 'tag-select' | 'input' | 'list';
