import { useState } from 'react';
import type { NoteType, PageType } from './types';
import { storage } from './utils/storage';
import TagSelectPage from './components/TagSelectPage';
import InputPage from './components/InputPage';
import ListPage from './components/ListPage';
import FloatingButton from './components/FloatingButton';

function App() {
  const [currentPage, setCurrentPage] = useState<PageType>('tag-select');
  const [currentType, setCurrentType] = useState<NoteType>('flash');
  const [editingNote, setEditingNote] = useState<{ id: number; content: string } | null>(null);
  const [refreshKey, setRefreshKey] = useState(0);

  const handleTypeSelect = (type: NoteType) => {
    setCurrentType(type);
    setCurrentPage('input');
    setEditingNote(null);
  };

  const handleBack = () => {
    setCurrentPage('tag-select');
    setEditingNote(null);
  };

  const handleSave = (content: string) => {
    if (editingNote) {
      storage.updateNote(currentType, editingNote.id, content);
    } else {
      storage.saveNote(currentType, content);
    }
    setCurrentPage('list');
    setRefreshKey(prev => prev + 1);
  };

  const handleEdit = (id: number, content: string) => {
    setEditingNote({ id, content });
    setCurrentPage('input');
  };

  const handleDelete = (id: number) => {
    if (confirm('确定删除这条笔记吗？')) {
      storage.deleteNote(currentType, id);
      setRefreshKey(prev => prev + 1);
    }
  };

  return (
    <div className="min-h-screen bg-dark-bg text-white">
      {currentPage === 'tag-select' && (
        <TagSelectPage onTypeSelect={handleTypeSelect} />
      )}

      {currentPage === 'input' && (
        <InputPage
          type={currentType}
          editingNote={editingNote}
          onBack={handleBack}
          onSave={handleSave}
        />
      )}

      {currentPage === 'list' && (
        <ListPage
          type={currentType}
          refreshKey={refreshKey}
          onBack={handleBack}
          onTypeChange={(type) => {
            setCurrentType(type);
            setRefreshKey(prev => prev + 1);
          }}
          onEdit={handleEdit}
          onDelete={handleDelete}
        />
      )}

      <FloatingButton onClick={() => {
        setCurrentPage('tag-select');
        setEditingNote(null);
      }} />
    </div>
  );
}

export default App;
