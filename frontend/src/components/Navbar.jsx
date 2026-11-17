import React, { useState } from 'react';
import { Sparkles, ShoppingBag, User as UserIcon, Search, ChevronDown, Receipt, Shield, LogOut } from 'lucide-react';

export default function Navbar({
  user,
  cartCount,
  onSearch,
  searchKeyword,
  onSearchChange,
  onOpenCart,
  onOpenAuth,
  onOpenOrders,
  onToggleAiPrivacy,
  onTogglePrivacy,
  onLogout
}) {
  const [searchTerm, setSearchTerm] = useState(searchKeyword || '');
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [isMobileSearchOpen, setIsMobileSearchOpen] = useState(false);

  const handleSearchChange = (e) => {
    const val = e.target.value;
    setSearchTerm(val);
    if (onSearch) onSearch(val);
    if (onSearchChange) onSearchChange(val);
  };

  const handleToggle = onTogglePrivacy || onToggleAiPrivacy;

  return (
    <header className="sticky top-0 z-40 bg-white/95 backdrop-blur-md border-b border-cyan-100 shadow-sm">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16 sm:h-20">
          
          {/* Logo */}
          <div 
            className="flex items-center gap-3 cursor-pointer select-none"
            onClick={() => window.scrollTo({ top: 0, behavior: 'smooth' })}
          >
            <div className="w-10 h-10 rounded-2xl bg-gradient-to-tr from-cyan-600 to-cyan-400 flex items-center justify-center text-white shadow-md shadow-cyan-500/25">
              <Sparkles className="w-5 h-5 text-white" />
            </div>
            <div>
              <span className="text-xl sm:text-2xl font-black tracking-tight text-slate-900">
                CYAN<span className="text-cyan-600">.AI</span>
              </span>
              <span className="hidden sm:block text-[10px] font-bold tracking-wider uppercase text-cyan-700">
                Thời Trang & AI Trợ Lý
              </span>
            </div>
          </div>

          {/* Search bar (Desktop & Tablet) */}
          <div className="hidden md:flex flex-1 max-w-md mx-6 lg:mx-10">
            <div className="relative w-full">
              <input
                type="text"
                value={searchTerm}
                onChange={handleSearchChange}
                placeholder="Tìm kiếm áo thun, sơ mi, đầm vintage, quần jeans..."
                className="w-full pl-10 pr-4 py-2.5 text-sm rounded-xl border border-slate-200 bg-slate-50/70 focus:bg-white focus:outline-none focus:border-cyan-500 focus:ring-4 focus:ring-cyan-500/10 transition-all placeholder:text-slate-400"
              />
              <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-3.5" />
            </div>
          </div>

          {/* Right Actions */}
          <div className="flex items-center gap-2 sm:gap-3">
            {/* Mobile Search Toggle */}
            <button
              onClick={() => setIsMobileSearchOpen(!isMobileSearchOpen)}
              className="md:hidden p-2.5 text-slate-600 hover:text-cyan-600 rounded-xl hover:bg-cyan-50 transition-colors"
              title="Tìm kiếm"
            >
              <Search className="w-5 h-5" />
            </button>

            {/* User Account / Auth */}
            {user ? (
              <div className="relative">
                <button
                  onClick={() => setIsDropdownOpen(!isDropdownOpen)}
                  className="flex items-center gap-2 py-2 px-3 rounded-xl bg-slate-50 hover:bg-cyan-50 text-slate-800 border border-slate-200 hover:border-cyan-200 transition-all text-xs sm:text-sm font-semibold"
                >
                  <div className="w-6 h-6 rounded-full bg-cyan-600 text-white flex items-center justify-center font-bold text-xs shadow-sm">
                    {user.fullName ? user.fullName.charAt(0).toUpperCase() : 'U'}
                  </div>
                  <span className="max-w-[120px] truncate hidden sm:inline">{user.fullName || user.email}</span>
                  <ChevronDown className="w-3.5 h-3.5 text-slate-400" />
                </button>

                {/* Dropdown Menu */}
                {isDropdownOpen && (
                  <div 
                    className="absolute right-0 mt-2 w-56 bg-white border border-slate-100 rounded-2xl shadow-xl py-2 z-50 animate-in fade-in slide-in-from-top-2 duration-150"
                    onMouseLeave={() => setIsDropdownOpen(false)}
                  >
                    <div className="px-4 py-2.5 border-b border-slate-100">
                      <div className="text-xs font-bold text-slate-900 truncate">{user.fullName}</div>
                      <div className="text-[11px] text-slate-400 truncate">{user.email}</div>
                    </div>
                    
                    <button
                      onClick={() => { setIsDropdownOpen(false); onOpenOrders(); }}
                      className="w-full px-4 py-2 text-left text-xs text-slate-700 hover:bg-cyan-50 hover:text-cyan-700 font-medium flex items-center gap-2.5 transition-colors"
                    >
                      <Receipt className="w-4 h-4 text-cyan-600" />
                      <span>Đơn hàng của tôi</span>
                    </button>

                    <button
                      onClick={() => { setIsDropdownOpen(false); handleToggle && handleToggle(); }}
                      className="w-full px-4 py-2 text-left text-xs text-slate-700 hover:bg-cyan-50 hover:text-cyan-700 font-medium flex items-center gap-2.5 transition-colors"
                    >
                      <Shield className="w-4 h-4 text-cyan-600" />
                      <span>AI Cá nhân hóa: <strong className={user.allowPersonalization ? "text-cyan-600" : "text-slate-400"}>{user.allowPersonalization ? 'BẬT' : 'TẮT'}</strong></span>
                    </button>

                    <div className="border-t border-slate-100 my-1"></div>

                    <button
                      onClick={() => { setIsDropdownOpen(false); onLogout(); }}
                      className="w-full px-4 py-2 text-left text-xs text-rose-600 hover:bg-rose-50 font-semibold flex items-center gap-2.5 transition-colors"
                    >
                      <LogOut className="w-4 h-4" />
                      <span>Đăng xuất</span>
                    </button>
                  </div>
                )}
              </div>
            ) : (
              <button
                onClick={onOpenAuth}
                className="btn-cyan-outline text-xs sm:text-sm px-4 py-2 sm:px-5 sm:py-2.5"
              >
                <UserIcon className="w-4 h-4" />
                <span>Đăng nhập</span>
              </button>
            )}

            {/* Cart Button */}
            <button
              onClick={onOpenCart}
              className="relative btn-cyan bg-cyan-50 hover:bg-cyan-100 text-cyan-800 border border-cyan-200/80 shadow-none px-3.5 py-2 sm:px-4 sm:py-2.5"
            >
              <ShoppingBag className="w-4 h-4 text-cyan-600" />
              <span className="hidden sm:inline font-bold">Giỏ hàng</span>
              <span className="w-5 h-5 bg-cyan-600 text-white text-[11px] font-extrabold rounded-full flex items-center justify-center shadow-sm">
                {cartCount}
              </span>
            </button>
          </div>
        </div>

        {/* Mobile Search Input */}
        {isMobileSearchOpen && (
          <div className="md:hidden pb-3">
            <div className="relative w-full">
              <input
                type="text"
                value={searchTerm}
                onChange={handleSearchChange}
                placeholder="Tìm trang phục theo tên, kiểu dáng..."
                className="w-full pl-10 pr-4 py-2 text-xs rounded-xl border border-slate-200 bg-slate-50 focus:bg-white focus:outline-none focus:border-cyan-500"
              />
              <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
            </div>
          </div>
        )}
      </div>
    </header>
  );
}
