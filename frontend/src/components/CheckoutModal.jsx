import React, { useState, useEffect } from 'react';
import { X, Truck, Check, Plus, MapPin, AlertCircle, Loader2 } from 'lucide-react';
import api from '../api/client';

export default function CheckoutModal({ isOpen, onClose, cart, onOrderSuccess }) {
  const [addresses, setAddresses] = useState([]);
  const [selectedAddressId, setSelectedAddressId] = useState(null);
  const [useNewAddress, setUseNewAddress] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');

  const [formData, setFormData] = useState({
    recipientName: '',
    phone: '',
    province: 'Hà Nội',
    district: 'Quận Cầu Giấy',
    ward: 'Phường Dịch Vọng',
    addressLine: '',
    paymentMethod: 'COD',
    couponCode: '',
    note: 'Giao hàng giờ hành chính'
  });

  useEffect(() => {
    if (isOpen) {
      setErrorMsg('');
      api.get('/addresses')
        .then((res) => {
          const list = res.data?.data || [];
          setAddresses(list);
          if (list.length > 0) {
            const def = list.find(a => a.isDefault) || list[0];
            setSelectedAddressId(def.addressId);
            setUseNewAddress(false);
          } else {
            setUseNewAddress(true);
          }
        })
        .catch(() => {
          setUseNewAddress(true);
        });
    }
  }, [isOpen]);

  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);
    setErrorMsg('');

    try {
      let finalAddressId = selectedAddressId;

      if (useNewAddress || !finalAddressId) {
        // Create new address first
        const addrPayload = {
          recipientName: formData.recipientName.trim(),
          phone: formData.phone.trim(),
          province: formData.province.trim(),
          district: formData.district.trim(),
          ward: formData.ward.trim(),
          addressLine: formData.addressLine.trim(),
          isDefault: addresses.length === 0
        };
        const addrRes = await api.post('/addresses', addrPayload);
        const createdAddr = addrRes.data?.data;
        finalAddressId = createdAddr?.addressId;
      }

      if (!finalAddressId) {
        setErrorMsg('Vui lòng chọn hoặc nhập địa chỉ giao hàng hợp lệ');
        setIsSubmitting(false);
        return;
      }

      // Submit checkout
      const checkoutPayload = {
        addressId: finalAddressId,
        paymentMethod: formData.paymentMethod,
        couponCode: formData.couponCode.trim() || null,
        note: formData.note.trim() || null
      };

      const orderRes = await api.post('/orders/checkout', checkoutPayload);
      const placedOrder = orderRes.data?.data;

      if (onOrderSuccess) {
        onOrderSuccess(placedOrder);
      }
      onClose();
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Đặt hàng không thành công. Vui lòng thử lại!';
      setErrorMsg(msg);
    } finally {
      setIsSubmitting(false);
    }
  };

  const formatPrice = (val) => new Intl.NumberFormat('vi-VN').format(val || 0) + 'đ';

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto flex items-center justify-center p-4">
      <div 
        className="fixed inset-0 bg-slate-900/60 backdrop-blur-sm transition-opacity"
        onClick={onClose}
      />

      <div className="relative bg-white rounded-3xl max-w-lg w-full p-6 sm:p-8 shadow-2xl z-10 animate-in fade-in zoom-in-95 duration-200">
        
        <button
          onClick={onClose}
          className="absolute top-4 right-4 w-9 h-9 rounded-full bg-slate-100 hover:bg-slate-200 text-slate-500 hover:text-slate-800 flex items-center justify-center transition-colors"
        >
          <X className="w-5 h-5" />
        </button>

        <div className="flex items-center gap-3 mb-6">
          <div className="w-10 h-10 rounded-2xl bg-cyan-100 text-cyan-700 flex items-center justify-center shadow-sm">
            <Truck className="w-5 h-5" />
          </div>
          <div>
            <h3 className="text-xl font-black text-slate-900">Xác Nhận Đặt Hàng</h3>
            <p className="text-xs text-slate-500">Hoàn tất địa chỉ và chọn phương thức thanh toán</p>
          </div>
        </div>

        {errorMsg && (
          <div className="mb-4 p-3 rounded-xl bg-rose-50 border border-rose-200 text-rose-700 text-xs flex items-center gap-2">
            <AlertCircle className="w-4 h-4 shrink-0" />
            <span>{errorMsg}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          
          {/* Saved Addresses Selector (if any) */}
          {addresses.length > 0 && (
            <div className="space-y-2">
              <label className="block text-xs font-bold text-slate-700">Chọn sổ địa chỉ của bạn:</label>
              <div className="space-y-2 max-h-40 overflow-y-auto pr-1">
                {addresses.map((a) => (
                  <div
                    key={a.addressId}
                    onClick={() => {
                      setSelectedAddressId(a.addressId);
                      setUseNewAddress(false);
                    }}
                    className={`p-3 rounded-xl border cursor-pointer text-xs transition-all ${
                      !useNewAddress && selectedAddressId === a.addressId
                        ? 'border-cyan-500 bg-cyan-50/50 shadow-sm'
                        : 'border-slate-200 hover:border-slate-300 bg-white'
                    }`}
                  >
                    <div className="flex items-center justify-between font-bold text-slate-800">
                      <span>{a.recipientName} - {a.phone}</span>
                      {a.isDefault && (
                        <span className="text-[10px] bg-cyan-500 text-white px-2 py-0.5 rounded-full">
                          Mặc định
                        </span>
                      )}
                    </div>
                    <p className="text-[11px] text-slate-500 mt-0.5">
                      {a.addressLine}, {a.ward}, {a.district}, {a.province}
                    </p>
                  </div>
                ))}
              </div>

              <button
                type="button"
                onClick={() => setUseNewAddress(!useNewAddress)}
                className="text-xs text-cyan-600 hover:text-cyan-700 font-bold flex items-center gap-1 mt-1"
              >
                <Plus className="w-3.5 h-3.5" />
                <span>{useNewAddress ? 'Chọn địa chỉ có sẵn' : 'Nhập địa chỉ mới'}</span>
              </button>
            </div>
          )}

          {/* New Address Inputs */}
          {useNewAddress && (
            <div className="space-y-3 pt-2 border-t border-slate-100">
              <h4 className="text-xs font-bold text-slate-800 flex items-center gap-1.5">
                <MapPin className="w-3.5 h-3.5 text-cyan-600" />
                <span>Địa chỉ nhận hàng mới</span>
              </h4>

              <div className="grid grid-cols-2 gap-2">
                <div>
                  <label className="block text-[11px] font-semibold text-slate-600 mb-1">Người nhận *</label>
                  <input
                    type="text"
                    required={useNewAddress}
                    value={formData.recipientName}
                    onChange={(e) => setFormData({ ...formData, recipientName: e.target.value })}
                    placeholder="Nguyễn Văn A"
                    className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 bg-slate-50 focus:bg-white focus:outline-none focus:border-cyan-500"
                  />
                </div>
                <div>
                  <label className="block text-[11px] font-semibold text-slate-600 mb-1">Số điện thoại *</label>
                  <input
                    type="tel"
                    required={useNewAddress}
                    value={formData.phone}
                    onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                    placeholder="0912345678"
                    className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 bg-slate-50 focus:bg-white focus:outline-none focus:border-cyan-500"
                  />
                </div>
              </div>

              <div className="grid grid-cols-3 gap-2">
                <div>
                  <label className="block text-[11px] font-semibold text-slate-600 mb-1">Tỉnh/TP *</label>
                  <input
                    type="text"
                    required={useNewAddress}
                    value={formData.province}
                    onChange={(e) => setFormData({ ...formData, province: e.target.value })}
                    className="w-full px-2.5 py-2 text-xs rounded-xl border border-slate-200 bg-slate-50 focus:bg-white focus:outline-none focus:border-cyan-500"
                  />
                </div>
                <div>
                  <label className="block text-[11px] font-semibold text-slate-600 mb-1">Quận/Huyện *</label>
                  <input
                    type="text"
                    required={useNewAddress}
                    value={formData.district}
                    onChange={(e) => setFormData({ ...formData, district: e.target.value })}
                    className="w-full px-2.5 py-2 text-xs rounded-xl border border-slate-200 bg-slate-50 focus:bg-white focus:outline-none focus:border-cyan-500"
                  />
                </div>
                <div>
                  <label className="block text-[11px] font-semibold text-slate-600 mb-1">Phường/Xã *</label>
                  <input
                    type="text"
                    required={useNewAddress}
                    value={formData.ward}
                    onChange={(e) => setFormData({ ...formData, ward: e.target.value })}
                    className="w-full px-2.5 py-2 text-xs rounded-xl border border-slate-200 bg-slate-50 focus:bg-white focus:outline-none focus:border-cyan-500"
                  />
                </div>
              </div>

              <div>
                <label className="block text-[11px] font-semibold text-slate-600 mb-1">Địa chỉ chi tiết (Số nhà, đường) *</label>
                <input
                  type="text"
                  required={useNewAddress}
                  value={formData.addressLine}
                  onChange={(e) => setFormData({ ...formData, addressLine: e.target.value })}
                  placeholder="Số 45 ngõ 12 Đường Cầu Giấy"
                  className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 bg-slate-50 focus:bg-white focus:outline-none focus:border-cyan-500"
                />
              </div>
            </div>
          )}

          {/* Payment Method */}
          <div>
            <label className="block text-xs font-bold text-slate-700 mb-1.5">Phương thức thanh toán *</label>
            <div className="grid grid-cols-2 gap-2">
              <label className={`p-2.5 rounded-xl border flex items-center gap-2 cursor-pointer text-xs font-semibold ${
                formData.paymentMethod === 'COD' ? 'border-cyan-500 bg-cyan-50 text-cyan-800' : 'border-slate-200 text-slate-700'
              }`}>
                <input
                  type="radio"
                  name="paymentMethod"
                  value="COD"
                  checked={formData.paymentMethod === 'COD'}
                  onChange={() => setFormData({ ...formData, paymentMethod: 'COD' })}
                  className="accent-cyan-600"
                />
                <span>COD (Tiền mặt)</span>
              </label>

              <label className={`p-2.5 rounded-xl border flex items-center gap-2 cursor-pointer text-xs font-semibold ${
                formData.paymentMethod === 'BANK_TRANSFER' ? 'border-cyan-500 bg-cyan-50 text-cyan-800' : 'border-slate-200 text-slate-700'
              }`}>
                <input
                  type="radio"
                  name="paymentMethod"
                  value="BANK_TRANSFER"
                  checked={formData.paymentMethod === 'BANK_TRANSFER'}
                  onChange={() => setFormData({ ...formData, paymentMethod: 'BANK_TRANSFER' })}
                  className="accent-cyan-600"
                />
                <span>Chuyển khoản</span>
              </label>
            </div>
          </div>

          {/* Note */}
          <div>
            <label className="block text-xs font-bold text-slate-700 mb-1">Ghi chú giao hàng</label>
            <input
              type="text"
              value={formData.note}
              onChange={(e) => setFormData({ ...formData, note: e.target.value })}
              placeholder="Giao hàng buổi chiều, gọi trước khi đến..."
              className="w-full px-3 py-2 text-xs rounded-xl border border-slate-200 bg-slate-50 focus:bg-white focus:outline-none focus:border-cyan-500"
            />
          </div>

          {/* Cart Summary */}
          {cart && (
            <div className="p-3 rounded-xl bg-cyan-50/50 border border-cyan-100 flex items-center justify-between text-xs font-bold text-slate-800">
              <span>Tổng số tiền thanh toán:</span>
              <span className="text-sm font-extrabold text-cyan-700">
                {formatPrice(cart.totalAmount || 0)}
              </span>
            </div>
          )}

          {/* Submit Button */}
          <button
            type="submit"
            disabled={isSubmitting}
            className="w-full btn-cyan py-3.5 text-sm font-bold shadow-lg shadow-cyan-600/25 mt-2"
          >
            {isSubmitting ? (
              <span className="flex items-center gap-2">
                <Loader2 className="w-4 h-4 animate-spin" />
                Đang tiến hành đặt hàng...
              </span>
            ) : (
              <span className="flex items-center gap-2">
                <Check className="w-4 h-4" />
                Xác nhận đặt hàng ngay
              </span>
            )}
          </button>
        </form>

      </div>
    </div>
  );
}
