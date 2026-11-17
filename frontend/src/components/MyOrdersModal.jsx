import React, { useState, useEffect } from 'react';
import { X, Receipt, Clock, Package, AlertCircle, Loader2 } from 'lucide-react';
import api from '../api/client';

export default function MyOrdersModal({ isOpen, onClose, orders: initialOrders, onCancelOrder }) {
  const [orders, setOrders] = useState(initialOrders || []);
  const [loading, setLoading] = useState(false);
  const [cancellingId, setCancellingId] = useState(null);

  const fetchOrders = async () => {
    setLoading(true);
    try {
      const res = await api.get('/orders?page=0&size=20');
      const data = res.data?.data;
      setOrders(data?.content || []);
    } catch (err) {
      console.warn('Failed to fetch orders', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (isOpen) {
      if (!initialOrders) {
        fetchOrders();
      } else {
        setOrders(initialOrders);
      }
    }
  }, [isOpen, initialOrders]);

  if (!isOpen) return null;

  const formatPrice = (val) => new Intl.NumberFormat('vi-VN').format(val || 0) + 'đ';

  const getStatusBadge = (status) => {
    switch (status) {
      case 'PENDING':
        return 'bg-amber-100 text-amber-800 border-amber-200';
      case 'CONFIRMED':
        return 'bg-blue-100 text-blue-800 border-blue-200';
      case 'PACKING':
        return 'bg-purple-100 text-purple-800 border-purple-200';
      case 'SHIPPING':
        return 'bg-cyan-100 text-cyan-800 border-cyan-200';
      case 'DELIVERED':
        return 'bg-emerald-100 text-emerald-800 border-emerald-200';
      case 'CANCELLED':
        return 'bg-rose-100 text-rose-800 border-rose-200';
      default:
        return 'bg-slate-100 text-slate-800 border-slate-200';
    }
  };

  const handleCancel = async (orderId) => {
    if (!window.confirm('Bạn có chắc chắn muốn hủy đơn hàng này?')) return;
    setCancellingId(orderId);
    try {
      if (onCancelOrder) {
        await onCancelOrder(orderId);
      } else {
        await api.put(`/orders/${orderId}/cancel?reason=Khách hàng yêu cầu hủy qua ứng dụng`);
      }
      await fetchOrders();
    } catch (err) {
      const msg = err.response?.data?.message || 'Không thể hủy đơn hàng';
      alert(msg);
    } finally {
      setCancellingId(null);
    }
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto flex items-center justify-center p-4">
      <div 
        className="fixed inset-0 bg-slate-900/60 backdrop-blur-sm transition-opacity"
        onClick={onClose}
      />

      <div className="relative bg-white rounded-3xl max-w-2xl w-full p-6 sm:p-8 shadow-2xl z-10 animate-in fade-in zoom-in-95 duration-200 max-h-[90vh] flex flex-col">
        
        <button
          onClick={onClose}
          className="absolute top-4 right-4 w-9 h-9 rounded-full bg-slate-100 hover:bg-slate-200 text-slate-500 hover:text-slate-800 flex items-center justify-center transition-colors"
        >
          <X className="w-5 h-5" />
        </button>

        <div className="flex items-center gap-3 mb-6">
          <div className="w-10 h-10 rounded-2xl bg-cyan-100 text-cyan-700 flex items-center justify-center shadow-sm">
            <Receipt className="w-5 h-5" />
          </div>
          <div>
            <h3 className="text-xl font-black text-slate-900">Lịch Sử Đơn Hàng</h3>
            <p className="text-xs text-slate-500">Theo dõi tiến trình vận chuyển và trạng thái đơn hàng của bạn</p>
          </div>
        </div>

        {/* Order List */}
        <div className="flex-1 overflow-y-auto space-y-3.5 pr-1">
          {loading ? (
            <div className="flex flex-col items-center justify-center py-16 text-cyan-600 gap-2">
              <Loader2 className="w-6 h-6 animate-spin" />
              <span className="text-xs text-slate-500 font-medium">Đang tải lịch sử đơn hàng...</span>
            </div>
          ) : orders && orders.length > 0 ? (
            orders.map((o) => (
              <div
                key={o.orderId}
                className="p-4 sm:p-5 rounded-2xl border border-slate-200 bg-white shadow-sm space-y-3"
              >
                <div className="flex items-center justify-between text-xs">
                  <div className="flex items-center gap-2">
                    <Package className="w-4 h-4 text-cyan-600" />
                    <span className="font-extrabold text-slate-900">Đơn #{o.orderCode || o.orderId}</span>
                  </div>
                  <span className={`px-2.5 py-0.5 rounded-full text-[11px] font-bold border ${getStatusBadge(o.status)}`}>
                    {o.status}
                  </span>
                </div>

                <div className="text-xs text-slate-500 space-y-1">
                  <div className="flex justify-between">
                    <span>Tổng thanh toán:</span>
                    <span className="font-extrabold text-cyan-700 text-sm">{formatPrice(o.totalAmount)}</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Phương thức:</span>
                    <span className="font-semibold text-slate-700">{o.paymentMethod} ({o.paymentStatus})</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Số lượng món:</span>
                    <span className="font-semibold text-slate-700">{o.totalItems || o.items?.length || 1} sản phẩm</span>
                  </div>
                  {o.createdAt && (
                    <div className="flex justify-between text-[11px] text-slate-400">
                      <span>Thời gian:</span>
                      <span>{new Date(o.createdAt).toLocaleString('vi-VN')}</span>
                    </div>
                  )}
                </div>

                {(o.status === 'PENDING' || o.status === 'CONFIRMED') && (
                  <div className="pt-2 border-t border-slate-100 flex justify-end">
                    <button
                      onClick={() => handleCancel(o.orderId)}
                      disabled={cancellingId === o.orderId}
                      className="px-3.5 py-1.5 rounded-xl bg-rose-50 hover:bg-rose-100 text-rose-600 font-bold text-xs transition-colors flex items-center gap-1.5 disabled:opacity-50"
                    >
                      {cancellingId === o.orderId ? (
                        <>
                          <Loader2 className="w-3 h-3 animate-spin" />
                          <span>Đang hủy...</span>
                        </>
                      ) : (
                        <span>Hủy đơn hàng</span>
                      )}
                    </button>
                  </div>
                )}
              </div>
            ))
          ) : (
            <div className="text-center py-16 text-slate-400">
              <Clock className="w-10 h-10 mx-auto mb-2 text-slate-300 stroke-1" />
              <p className="text-xs font-semibold text-slate-500">Bạn chưa có đơn hàng nào.</p>
              <p className="text-[11px] text-slate-400 mt-1">Khi bạn đặt hàng, danh sách sẽ hiển thị tại đây.</p>
            </div>
          )}
        </div>

      </div>
    </div>
  );
}
