import React from 'react';

export default function CategoryFilter({ categories, selectedCategoryId, onSelectCategory }) {
  return (
    <div className="flex items-center gap-2 overflow-x-auto pb-2 scrollbar-none">
      <button
        onClick={() => onSelectCategory(null)}
        className={`whitespace-nowrap px-4 py-2 rounded-xl text-xs sm:text-sm font-semibold transition-all duration-200 ${
          selectedCategoryId === null
            ? 'bg-cyan-600 text-white shadow-sm shadow-cyan-600/30'
            : 'bg-white border border-slate-200 text-slate-700 hover:bg-slate-50 hover:border-slate-300'
        }`}
      >
        Tất cả trang phục
      </button>

      {categories.map((cat) => (
        <button
          key={cat.categoryId}
          onClick={() => onSelectCategory(cat.categoryId)}
          className={`whitespace-nowrap px-4 py-2 rounded-xl text-xs sm:text-sm font-semibold transition-all duration-200 ${
            selectedCategoryId === cat.categoryId
              ? 'bg-cyan-600 text-white shadow-sm shadow-cyan-600/30'
              : 'bg-white border border-slate-200 text-slate-700 hover:bg-slate-50 hover:border-slate-300'
          }`}
        >
          {cat.name}
        </button>
      ))}
    </div>
  );
}
