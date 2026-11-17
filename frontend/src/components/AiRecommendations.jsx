import React from 'react';
import { Sparkles, RotateCw, Cpu, Clock } from 'lucide-react';
import ProductCard from './ProductCard';

export default function AiRecommendations({
  recommendationData,
  products: initialProducts,
  algorithmVersion: initialAlgo,
  isFallback: initialFallback,
  onRefresh,
  onSelectProduct,
  onProductClick,
  onAddToCart
}) {
  const products = initialProducts || recommendationData?.products || [];
  const algorithmVersion = initialAlgo || recommendationData?.algorithmVersion || 'AI-COLLABORATIVE-V1';
  const isFallback = initialFallback !== undefined ? initialFallback : recommendationData?.isFallback;
  const responseTimeMs = recommendationData?.responseTimeMs;

  const handleProductSelect = onProductClick || onSelectProduct;

  return (
    <section id="aiSection" className="w-full">
      <div className="bg-gradient-to-br from-cyan-50/80 via-white to-cyan-50/40 border-2 border-cyan-200/90 rounded-3xl p-5 sm:p-8 shadow-sm">
        
        {/* Header */}
        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 mb-6">
          <div className="flex items-center gap-3.5">
            <div className="w-11 h-11 rounded-2xl bg-gradient-to-tr from-cyan-600 to-cyan-400 flex items-center justify-center text-white ai-badge-glow shadow-md shadow-cyan-500/20 shrink-0">
              <Sparkles className="w-5 h-5 text-white" />
            </div>
            <div>
              <div className="flex flex-wrap items-center gap-2">
                <h2 className="text-lg sm:text-2xl font-black text-slate-900 tracking-tight">
                  Gợi Ý AI Dành Cho Bạn
                </h2>
                <span className={`text-[10px] sm:text-[11px] font-extrabold uppercase px-2.5 py-0.5 rounded-full border flex items-center gap-1 ${
                  isFallback
                    ? 'bg-amber-100 text-amber-800 border-amber-300'
                    : 'bg-cyan-100 text-cyan-800 border-cyan-300'
                }`}>
                  <Cpu className="w-3 h-3" />
                  <span>{algorithmVersion}</span>
                </span>
                {responseTimeMs !== undefined && (
                  <span className="text-[10px] text-slate-500 font-mono flex items-center gap-0.5 bg-slate-100 px-2 py-0.5 rounded-full">
                    <Clock className="w-2.5 h-2.5" />
                    <span>{responseTimeMs}ms</span>
                  </span>
                )}
              </div>
              <p className="text-xs sm:text-sm text-slate-500 mt-0.5">
                {isFallback
                  ? 'Gợi ý sản phẩm thịnh hành nhất khi chưa đăng nhập hoặc tắt cá nhân hóa'
                  : 'Được tính toán cá nhân hóa theo phong cách và tương tác của riêng bạn'}
              </p>
            </div>
          </div>

          <button
            onClick={onRefresh}
            className="btn-cyan-outline text-xs px-3.5 py-2 rounded-xl bg-white hover:bg-cyan-50 flex items-center gap-1.5 self-end sm:self-auto shadow-sm"
          >
            <RotateCw className="w-3.5 h-3.5" />
            <span>Làm mới gợi ý</span>
          </button>
        </div>

        {/* Product Grid */}
        {products && products.length > 0 ? (
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-3.5 sm:gap-4">
            {products.map((p) => (
              <ProductCard
                key={p.productId}
                product={p}
                onSelect={handleProductSelect}
                onClick={handleProductSelect}
                onAddToCart={onAddToCart}
              />
            ))}
          </div>
        ) : (
          <div className="text-center py-10 bg-white/60 rounded-2xl border border-dashed border-cyan-200">
            <Sparkles className="w-8 h-8 text-cyan-400 mx-auto mb-2 animate-pulse" />
            <p className="text-xs text-slate-500 font-medium">Hệ thống AI đang phân tích dữ liệu gợi ý...</p>
          </div>
        )}

      </div>
    </section>
  );
}
