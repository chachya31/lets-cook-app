import React from 'react';
import { Routes, Route } from 'react-router-dom';

const App: React.FC = () => {
  return (
    <div>
      <h1>自炊支援アプリ</h1>
      <Routes>
        <Route path="/" element={<div>ホーム画面</div>} />
      </Routes>
    </div>
  );
};

export default App;
