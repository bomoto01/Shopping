import React from 'react';
import { ShieldCheck, Truck, Headphones, RotateCcw, Heart, Mail, Phone, MapPin } from 'lucide-react';

export default function Footer({ onOpenAuth }) {
  return (
    <footer className="bg-slate-900 text-slate-300 border-t-4 border-cyan-500 mt-20">
      {/* Features highlight banner */}
      <div className="bg-slate-800/80 border-b border-slate-700/60 py-8 px-4 sm:px-6 lg:px-8">
        <div className="max-w-7xl mx-auto grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          <div className="flex items-center gap-4 p-4 rounded-xl bg-slate-900/40 border border-cyan-500/10">
            <div className="w-12 h-12 rounded-xl bg-cyan-500/10 flex items-center justify-center text-cyan-400 shrink-0">
              <Truck className="w-6 h-6" />
            </div>
            <div>
              <h4 className="font-semibold text-white text-sm">Giao Hàng Miễn Phí</h4>
              <p className="text-xs text-slate-400">Đơn hàng trên 500.000đ</p>
            </div>
          </div>

          <div className="flex items-center gap-4 p-4 rounded-xl bg-slate-900/40 border border-cyan-500/10">
            <div className="w-12 h-12 rounded-xl bg-cyan-500/10 flex items-center justify-center text-cyan-400 shrink-0">
              <RotateCcw className="w-6 h-6" />
            </div>
            <div>
              <h4 className="font-semibold text-white text-sm">Đổi Trả Dễ Dàng</h4>
              <p className="text-xs text-slate-400">Trong vòng 7 ngày làm việc</p>
            </div>
          </div>

          <div className="flex items-center gap-4 p-4 rounded-xl bg-slate-900/40 border border-cyan-500/10">
            <div className="w-12 h-12 rounded-xl bg-cyan-500/10 flex items-center justify-center text-cyan-400 shrink-0">
              <ShieldCheck className="w-6 h-6" />
            </div>
            <div>
              <h4 className="font-semibold text-white text-sm">Chính Hãng 100%</h4>
              <p className="text-xs text-slate-400">Cam kết chất lượng cao cấp</p>
            </div>
          </div>

          <div className="flex items-center gap-4 p-4 rounded-xl bg-slate-900/40 border border-cyan-500/10">
            <div className="w-12 h-12 rounded-xl bg-cyan-500/10 flex items-center justify-center text-cyan-400 shrink-0">
              <Headphones className="w-6 h-6" />
            </div>
            <div>
              <h4 className="font-semibold text-white text-sm">Hỗ Trợ 24/7</h4>
              <p className="text-xs text-slate-400">Hotline và tư vấn AI trực tuyến</p>
            </div>
          </div>
        </div>
      </div>

      {/* Main footer contents */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8 lg:gap-12">
          {/* Brand Info */}
          <div className="space-y-4">
            <div className="flex items-center gap-2">
              <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-cyan-600 to-cyan-400 flex items-center justify-center shadow-md shadow-cyan-500/30">
                <span className="text-white font-extrabold text-xl">C</span>
              </div>
              <span className="font-extrabold text-2xl tracking-tight bg-gradient-to-r from-cyan-400 via-cyan-200 to-white bg-clip-text text-transparent">
                CyanShop
              </span>
            </div>
            <p className="text-sm text-slate-400 leading-relaxed">
              Hệ thống bán lẻ thương mại điện tử hiện đại tích hợp gợi ý sản phẩm thông minh bằng AI, bảo vệ quyền riêng tư người dùng.
            </p>
            <div className="pt-2 text-xs text-cyan-400 flex items-center gap-2">
              <span className="inline-block w-2 h-2 rounded-full bg-cyan-400 animate-pulse" />
              <span>Hệ thống Backend Spring Boot & React kết nối ổn định</span>
            </div>
          </div>

          {/* Quick links */}
          <div>
            <h3 className="text-sm font-semibold text-white uppercase tracking-wider mb-4 border-l-2 border-cyan-500 pl-2">
              Khám Phá
            </h3>
            <ul className="space-y-2.5 text-sm">
              <li>
                <a href="#products-section" className="hover:text-cyan-400 transition-colors">Tất cả sản phẩm</a>
              </li>
              <li>
                <a href="#ai-section" className="hover:text-cyan-400 transition-colors flex items-center gap-1.5">
                  Gợi ý AI cho bạn
                  <span className="text-[10px] bg-cyan-500/20 text-cyan-300 px-1.5 py-0.5 rounded font-mono">NEW</span>
                </a>
              </li>
              <li>
                <a href="#categories-section" className="hover:text-cyan-400 transition-colors">Danh mục mua sắm</a>
              </li>
              <li>
                <button onClick={onOpenAuth} className="hover:text-cyan-400 transition-colors text-left">
                  Đăng nhập / Tài khoản
                </button>
              </li>
            </ul>
          </div>

          {/* Contact info */}
          <div>
            <h3 className="text-sm font-semibold text-white uppercase tracking-wider mb-4 border-l-2 border-cyan-500 pl-2">
              Liên Hệ
            </h3>
            <ul className="space-y-3 text-sm text-slate-400">
              <li className="flex items-start gap-2.5">
                <MapPin className="w-4 h-4 text-cyan-400 shrink-0 mt-0.5" />
                <span>Hà Nội & TP. Hồ Chí Minh, Việt Nam</span>
              </li>
              <li className="flex items-center gap-2.5">
                <Phone className="w-4 h-4 text-cyan-400 shrink-0" />
                <span>1900 6868 (8:00 - 22:00)</span>
              </li>
              <li className="flex items-center gap-2.5">
                <Mail className="w-4 h-4 text-cyan-400 shrink-0" />
                <span>support@cyanshop.vn</span>
              </li>
            </ul>
          </div>

          {/* Newsletter subscription */}
          <div>
            <h3 className="text-sm font-semibold text-white uppercase tracking-wider mb-4 border-l-2 border-cyan-500 pl-2">
              Nhận Ưu Đãi
            </h3>
            <p className="text-sm text-slate-400 mb-4 leading-relaxed">
              Đăng ký email để nhận mã giảm giá 15% độc quyền cho đơn hàng đầu tiên.
            </p>
            <form onSubmit={(e) => { e.preventDefault(); alert('Cảm ơn bạn đã đăng ký nhận tin ưu đãi từ CyanShop!'); }} className="space-y-2.5">
              <input
                type="email"
                required
                placeholder="Nhập email của bạn..."
                className="w-full px-3.5 py-2.5 rounded-lg bg-slate-800 border border-slate-700 text-sm text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-cyan-500 focus:border-cyan-500"
              />
              <button
                type="submit"
                className="w-full py-2.5 px-4 rounded-lg bg-cyan-600 hover:bg-cyan-500 text-white font-medium text-sm transition-colors duration-200 shadow-md shadow-cyan-600/30"
              >
                Đăng Ký Nhận Tin
              </button>
            </form>
          </div>
        </div>

        {/* Bottom bar */}
        <div className="mt-12 pt-8 border-t border-slate-800 flex flex-col sm:flex-row items-center justify-between gap-4 text-xs text-slate-500">
          <p>© {new Date().getFullYear()} CyanShop. Bảo lưu mọi quyền.</p>
          <div className="flex items-center gap-6">
            <span className="hover:text-slate-400 cursor-pointer">Chính sách bảo mật</span>
            <span className="hover:text-slate-400 cursor-pointer">Điều khoản dịch vụ</span>
            <span className="hover:text-slate-400 cursor-pointer">Quy chế hoạt động</span>
          </div>
        </div>
      </div>
    </footer>
  );
}
