import React from 'react';
import { Sparkles, ArrowDown, Zap } from 'lucide-react';

export default function HeroBanner({ onExplore, onExploreClick, onAiClick, aiFeaturedProduct }) {
  const handleExplore = onExploreClick || onExplore;
  return (
    <section className="relative bg-gradient-to-r from-cyan-950 via-cyan-900 to-cyan-700 text-white overflow-hidden py-12 sm:py-16 md:py-24 px-4 sm:px-6 lg:px-8">
      {/* Background subtle grid pattern */}
      <div className="absolute inset-0 opacity-10 bg-[radial-gradient(#fff_1px,transparent_1px)] [background-size:20px_20px]"></div>

      <div className="max-w-7xl mx-auto relative z-10 grid grid-cols-1 md:grid-cols-12 gap-8 lg:gap-12 items-center">
        
        {/* Left Column Text */}
        <div className="md:col-span-7 text-center md:text-left">
          <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-cyan-500/20 border border-cyan-400/30 text-cyan-200 text-xs font-semibold mb-4 backdrop-blur-md">
            <Sparkles className="w-3.5 h-3.5 text-cyan-300" />
            <span>AI Fashion Stylist 2026</span>
          </div>

          <h1 className="text-3xl sm:text-4xl lg:text-5xl font-black tracking-tight leading-tight mb-4 text-white">
            Phong Cách Thời Thượng <br className="hidden sm:inline" />
            <span className="text-transparent bg-clip-text bg-gradient-to-r from-cyan-300 to-cyan-100">
              Cá Nhân Hóa Bởi AI
            </span>
          </h1>

          <p className="text-cyan-100/90 text-sm sm:text-base leading-relaxed mb-8 max-w-xl mx-auto md:mx-0">
            Khám phá bộ sưu tập trang phục may đo chuẩn form, đa dạng phong cách từ thường nhật đến công sở và dạ tiệc, được hệ thống AI gợi ý chính xác theo gu thời trang của riêng bạn.
          </p>

          <div className="flex flex-wrap items-center justify-center md:justify-start gap-3 sm:gap-4">
            <button
              onClick={handleExplore}
              className="btn-cyan bg-white text-cyan-950 hover:bg-cyan-50 font-bold px-6 py-3 rounded-xl shadow-lg shadow-black/15"
            >
              <span>Xem bộ sưu tập</span>
              <ArrowDown className="w-4 h-4" />
            </button>

            <button
              onClick={onAiClick}
              className="btn-cyan-outline border-cyan-300/60 text-cyan-100 hover:bg-cyan-800/40 px-6 py-3 rounded-xl backdrop-blur-sm"
            >
              <Sparkles className="w-4 h-4 text-cyan-300" />
              <span>Gợi ý cho riêng bạn</span>
            </button>
          </div>
        </div>

        {/* Right Column Visual / Card */}
        <div className="md:col-span-5 hidden md:flex justify-center items-center">
          <div className="w-80 h-96 rounded-3xl bg-gradient-to-b from-cyan-500/20 via-cyan-400/10 to-transparent border border-cyan-300/30 p-5 backdrop-blur-xl relative flex flex-col justify-end shadow-2xl overflow-hidden group">
            {aiFeaturedProduct?.primaryImageUrl && (
              <img
                src={aiFeaturedProduct.primaryImageUrl}
                alt={aiFeaturedProduct.name}
                className="absolute inset-0 w-full h-full object-cover -z-10 opacity-60 group-hover:scale-105 transition-transform duration-500"
              />
            )}

            {/* Top Match Badge */}
            <div className="absolute top-4 right-4 bg-cyan-500 text-white text-[11px] font-extrabold px-3 py-1 rounded-full flex items-center gap-1.5 shadow-md shadow-cyan-950/20">
              <Zap className="w-3.5 h-3.5 fill-current" />
              <span>AI Top Pick 99%</span>
            </div>

            {/* Bottom Card Preview */}
            <div className="bg-white/95 backdrop-blur-md rounded-2xl p-4 text-slate-800 shadow-xl border border-white/40">
              <span className="text-[10px] font-extrabold uppercase tracking-wider text-cyan-600 block mb-1">
                {aiFeaturedProduct?.categoryName || 'BST Nổi Bật 2026'}
              </span>
              <h4 className="text-sm font-bold text-slate-900 leading-snug line-clamp-1">
                {aiFeaturedProduct?.name || 'Set Váy Linen Vintage & Blazer Công Sở'}
              </h4>
              <div className="flex items-center justify-between mt-3 pt-2 border-t border-slate-100">
                <div>
                  <span className="text-base font-black text-cyan-700">
                    {aiFeaturedProduct?.basePrice
                      ? new Intl.NumberFormat('vi-VN').format(aiFeaturedProduct.basePrice) + 'đ'
                      : '450.000đ'}
                  </span>
                </div>
                <span className="text-[11px] font-semibold text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded-md">
                  Gợi ý AI
                </span>
              </div>
            </div>
          </div>
        </div>

      </div>
    </section>
  );
}
