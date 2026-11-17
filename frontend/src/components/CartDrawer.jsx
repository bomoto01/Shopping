import React, { useState } from 'react';
import { X, ShoppingBag, Plus, Minus, ArrowRight } from 'lucide-react';

export default function CartDrawer({
  isOpen,
  onClose,
  cart,
  onUpdateQty,
  onUpdateQuantity,
  onRemoveItem,
  onApplyCoupon,
  onCheckout
}) {
  const [couponCode, setCouponCode] = useState('');

  if (!isOpen) return null;

  const handleQtyChange = (cartItemId, newQty) => {
    if (newQty <= 0) {
      if (onRemoveItem) {
        onRemoveItem(cartItemId);
        return;
      }
    }
    const updateFn = onUpdateQuantity || onUpdateQty;
    if (updateFn) updateFn(cartItemId, newQty);
  };

  const formatPrice = (val) => new Intl.NumberFormat('vi-VN').format(val || 0) + 'đ';

  const items = cart?.items || [];
  const subtotal = cart?.totalAmount || 0;
  const shipping = subtotal >= 500000 || subtotal === 0 ? 0 : 30000;
  const discount = cart?.discountAmount || 0;
  const total = Math.max(0, subtotal + shipping - discount);

  return (
    <div className="fixed inset-0 z-50 overflow-hidden">
      {/* Backdrop */}
      <div 
        className="absolute inset-0 bg-slate-900/40 backdrop-blur-sm transition-opacity"
        onClick={onClose}
      ></div>

      <div className="fixed inset-y-0 right-0 max-w-full flex pl-10">
        <div className="w-screen max-w-md bg-white shadow-2xl flex flex-col animate-in slide-in-from-right duration-300">
          
          {/* Header */}
          <div className="p-4 sm:p-6 border-b border-slate-100 flex items-center justify-between bg-cyan-50/40">
            <div className="flex items-center gap-2.5">
              <div className="w-8 h-8 rounded-xl bg-cyan-600 text-white flex items-center justify-center shadow-sm">
                <ShoppingBag className="w-4 h-4" />
              </div>
              <h2 className="text-base sm:text-lg font-bold text-slate-900">
                Giỏ Hàng Của Bạn
              </h2>
            </div>
            <button
              onClick={onClose}
              className="w-8 h-8 rounded-full hover:bg-slate-200 text-slate-400 hover:text-slate-600 flex items-center justify-center transition-colors"
            >
              <X className="w-4 h-4" />
            </button>
          </div>

          {/* Cart Items List */}
          <div className="flex-1 overflow-y-auto p-4 sm:p-6 space-y-3.5">
            {items.length === 0 ? (
              <div className="text-center py-16 text-slate-400">
                <ShoppingBag className="w-10 h-10 mx-auto mb-2 text-slate-300 stroke-1" />
                <p className="text-xs sm:text-sm font-medium">Giỏ hàng của bạn đang trống</p>
                <p className="text-[11px] text-slate-400 mt-1">Hãy chọn sản phẩm bạn yêu thích</p>
              </div>
            ) : (
              items.map((item) => (
                <div
                  key={item.cartItemId}
                  className="flex items-center justify-between gap-3 p-3.5 rounded-2xl bg-slate-50 border border-slate-100"
                >
                  <div className="flex-1 min-w-0">
                    <h4 className="text-xs font-bold text-slate-900 truncate">
                      {item.productName}
                    </h4>
                    <div className="text-[11px] text-slate-500 mt-0.5">
                      {item.size || ''} {item.color ? `• ${item.color}` : ''}
                    </div>
                    <div className="text-xs font-black text-cyan-700 mt-1">
                      {formatPrice(item.unitPrice)}
                    </div>
                  </div>

                  {/* Quantity Stepper */}
                  <div className="flex items-center gap-1.5 bg-white border border-slate-200 rounded-xl p-1 shadow-sm">
                    <button
                      onClick={() => handleQtyChange(item.cartItemId, item.quantity - 1)}
                      className="w-6 h-6 rounded-lg flex items-center justify-center text-slate-600 hover:bg-slate-100 text-xs active:scale-95"
                      title="Giảm số lượng"
                    >
                      <Minus className="w-3 h-3" />
                    </button>
                    <span className="text-xs font-bold px-1.5 text-slate-800 min-w-[20px] text-center">
                      {item.quantity}
                    </span>
                    <button
                      onClick={() => handleQtyChange(item.cartItemId, item.quantity + 1)}
                      className="w-6 h-6 rounded-lg flex items-center justify-center text-slate-600 hover:bg-slate-100 text-xs active:scale-95"
                      title="Tăng số lượng"
                    >
                      <Plus className="w-3 h-3" />
                    </button>
                  </div>
                </div>
              ))
            )}
          </div>

          {/* Footer (Coupon & Checkout) */}
          {items.length > 0 && (
            <div className="p-4 sm:p-6 border-t border-slate-100 bg-slate-50 space-y-3.5">
              
              {/* Coupon Form */}
              <div className="flex gap-2">
                <input
                  type="text"
                  value={couponCode}
                  onChange={(e) => setCouponCode(e.target.value.toUpperCase())}
                  placeholder="Mã giảm giá (ví dụ: SUMMER2026)"
                  className="flex-1 px-3.5 py-2 text-xs rounded-xl border border-slate-200 focus:outline-none focus:border-cyan-500 font-bold uppercase tracking-wider bg-white"
                />
                <button
                  onClick={() => onApplyCoupon(couponCode)}
                  className="px-4 py-2 bg-slate-800 hover:bg-slate-900 text-white text-xs font-bold rounded-xl transition-colors active:scale-95"
                >
                  Áp dụng
                </button>
              </div>

              {/* Price Breakdown */}
              <div className="space-y-1.5 text-xs text-slate-600 pt-2 border-t border-slate-200/80">
                <div className="flex justify-between">
                  <span>Tạm tính:</span>
                  <span className="font-semibold text-slate-900">{formatPrice(subtotal)}</span>
                </div>
                {discount > 0 && (
                  <div className="flex justify-between text-cyan-600 font-semibold">
                    <span>Mã giảm giá:</span>
                    <span>-{formatPrice(discount)}</span>
                  </div>
                )}
                <div className="flex justify-between">
                  <span>Phí vận chuyển:</span>
                  <span className="font-semibold text-slate-900">
                    {shipping === 0 ? 'Miễn phí' : formatPrice(shipping)}
                  </span>
                </div>
                <div className="flex justify-between text-sm font-black text-slate-900 pt-1.5 border-t border-slate-200">
                  <span>Tổng thanh toán:</span>
                  <span className="text-cyan-700 text-base font-extrabold">{formatPrice(total)}</span>
                </div>
              </div>

              {/* Checkout Button */}
              <button
                onClick={onCheckout}
                className="w-full btn-cyan py-3.5 text-sm font-bold shadow-md shadow-cyan-600/25"
              >
                <span>Tiến hành đặt hàng</span>
                <ArrowRight className="w-4 h-4 ml-1" />
              </button>

            </div>
          )}

        </div>
      </div>
    </div>
  );
}
