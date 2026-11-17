import React from 'react';
import { ShoppingCart } from 'lucide-react';

export default function ProductCard({ product, onSelect, onClick, onAddToCart }) {
  const handleClick = () => {
    if (onClick) onClick(product);
    else if (onSelect) onSelect(product);
  };

  const formatPrice = (val) => {
    return new Intl.NumberFormat('vi-VN').format(val || 0) + 'đ';
  };

  return (
    <div
      onClick={handleClick}
      className="group bg-white rounded-2xl p-3 sm:p-4 border border-slate-100 hover:border-cyan-200 hover:shadow-xl hover:shadow-cyan-100/50 hover:-translate-y-1.5 transition-all duration-300 flex flex-col justify-between cursor-pointer"
    >
      <div>
        {/* Product Image */}
        <div className="relative aspect-square rounded-xl overflow-hidden bg-slate-100 mb-3">
          <img
            src={product.primaryImageUrl || 'https://images.unsplash.com/photo-1523381210434-271e8be1f52b?w=600'}
            alt={product.name}
            className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
            loading="lazy"
          />
          {!product.inStock && (
            <span className="absolute top-2 right-2 bg-rose-600/90 backdrop-blur-sm text-white text-[10px] font-bold px-2 py-0.5 rounded-full shadow-sm">
              Tạm hết
            </span>
          )}
        </div>

        {/* Category tag */}
        <span className="text-[10px] sm:text-[11px] font-bold text-cyan-600 uppercase tracking-wider block mb-1">
          {product.categoryName || 'Thời trang'}
        </span>

        {/* Product Name */}
        <h3 className="text-xs sm:text-sm font-bold text-slate-800 line-clamp-2 leading-snug group-hover:text-cyan-700 transition-colors mb-2">
          {product.name}
        </h3>
      </div>

      {/* Price & Action button */}
      <div className="mt-2 pt-2 border-t border-slate-50 flex items-center justify-between gap-2">
        <span className="text-sm sm:text-base font-extrabold text-slate-900">
          {formatPrice(product.basePrice)}
        </span>

        <button
          onClick={(e) => {
            e.stopPropagation();
            onAddToCart(product);
          }}
          className="w-8 h-8 rounded-xl bg-cyan-50 hover:bg-cyan-600 text-cyan-700 hover:text-white flex items-center justify-center transition-all duration-200 active:scale-95 shadow-sm"
          title="Thêm nhanh vào giỏ"
        >
          <ShoppingCart className="w-4 h-4" />
        </button>
      </div>
    </div>
  );
}
