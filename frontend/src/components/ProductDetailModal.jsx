import React, { useState, useEffect } from 'react';
import { X, ShoppingCart, CheckCircle, Star, Loader2 } from 'lucide-react';
import api from '../api/client';

export default function ProductDetailModal({ productId, product: initialProduct, isOpen, onClose, onAddToCart }) {
  const [product, setProduct] = useState(initialProduct || null);
  const [loading, setLoading] = useState(!initialProduct && !!productId);
  const [selectedVariant, setSelectedVariant] = useState(null);
  const [ratingSummary, setRatingSummary] = useState(null);
  const [quantity, setQuantity] = useState(1);

  useEffect(() => {
    if (productId) {
      setLoading(true);
      api.get(`/products/${productId}`)
        .then((res) => {
          const prod = res.data?.data || res.data;
          setProduct(prod);
          if (prod?.variants && prod.variants.length > 0) {
            setSelectedVariant(prod.variants[0]);
          }
        })
        .catch((err) => {
          console.error('Error fetching product detail', err);
        })
        .finally(() => {
          setLoading(false);
        });

      api.get(`/products/${productId}/reviews/summary`)
        .then((res) => setRatingSummary(res.data?.data || res.data))
        .catch(() => {});
    } else if (initialProduct) {
      setProduct(initialProduct);
      if (initialProduct?.variants && initialProduct.variants.length > 0) {
        setSelectedVariant(initialProduct.variants[0]);
      }
    }
  }, [productId, initialProduct]);

  if (!isOpen && !productId && !initialProduct) return null;

  const formatPrice = (val) => new Intl.NumberFormat('vi-VN').format(val || 0) + 'đ';

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto flex items-center justify-center p-4">
      {/* Backdrop blur */}
      <div 
        className="fixed inset-0 bg-slate-900/60 backdrop-blur-sm transition-opacity"
        onClick={onClose}
      />

      {/* Modal Box */}
      <div className="relative bg-white rounded-3xl max-w-2xl w-full p-6 sm:p-8 shadow-2xl z-10 overflow-hidden animate-in fade-in zoom-in-95 duration-200">
        
        {/* Close Button */}
        <button
          onClick={onClose}
          className="absolute top-4 right-4 w-9 h-9 rounded-full bg-slate-100 hover:bg-slate-200 text-slate-500 hover:text-slate-800 flex items-center justify-center transition-colors z-20"
        >
          <X className="w-5 h-5" />
        </button>

        {loading ? (
          <div className="flex flex-col items-center justify-center py-20 gap-3 text-cyan-600">
            <Loader2 className="w-8 h-8 animate-spin" />
            <span className="text-sm font-semibold text-slate-500">Đang tải thông tin sản phẩm...</span>
          </div>
        ) : product ? (
          <>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-6 sm:gap-8">
              
              {/* Image */}
              <div className="rounded-2xl overflow-hidden bg-slate-100 aspect-square flex items-center justify-center border border-slate-100">
                <img
                  src={product.images?.[0]?.imageUrl || product.primaryImageUrl || 'https://images.unsplash.com/photo-1523381210434-271e8be1f52b?w=600'}
                  alt={product.name}
                  className="w-full h-full object-cover"
                />
              </div>

              {/* Info & Variants */}
              <div className="flex flex-col justify-between">
                <div>
                  <span className="text-xs font-bold text-cyan-600 uppercase tracking-wider block mb-1">
                    {product.category?.name || product.categoryName || 'Thời trang'}
                  </span>
                  <h2 className="text-xl sm:text-2xl font-black text-slate-900 mb-2 leading-snug">
                    {product.name}
                  </h2>

                  {/* Rating stars */}
                  {ratingSummary && (
                    <div className="flex items-center gap-1.5 mb-3 text-xs text-slate-600">
                      <div className="flex text-amber-400">
                        {[1, 2, 3, 4, 5].map((i) => (
                          <Star
                            key={i}
                            className={`w-3.5 h-3.5 ${i <= Math.round(ratingSummary.averageRating || 5) ? 'fill-current' : 'text-slate-200'}`}
                          />
                        ))}
                      </div>
                      <span className="font-bold text-slate-800">{ratingSummary.averageRating || '5.0'}</span>
                      <span className="text-slate-400">({ratingSummary.totalReviews || 0} đánh giá)</span>
                    </div>
                  )}

                  {/* Price */}
                  <div className="text-2xl font-black text-cyan-700 mb-5">
                    {formatPrice(selectedVariant?.price || product.basePrice)}
                  </div>

                  {/* Variant Selector */}
                  <div className="space-y-3 mb-6">
                    <label className="block text-xs font-bold uppercase tracking-wider text-slate-700">
                      Chọn kích cỡ (Size) & Màu sắc:
                    </label>

                    {product.variants && product.variants.length > 0 ? (
                      <div className="flex flex-wrap gap-2">
                        {product.variants.map((v) => {
                          const isSelected = selectedVariant?.variantId === v.variantId;
                          return (
                            <button
                              key={v.variantId}
                              onClick={() => setSelectedVariant(v)}
                              className={`px-3.5 py-1.5 rounded-xl border text-xs font-bold transition-all ${
                                isSelected
                                  ? 'border-cyan-600 bg-cyan-50 text-cyan-800 shadow-sm'
                                  : 'border-slate-200 text-slate-700 hover:border-slate-300'
                              }`}
                            >
                              {v.size} {v.color ? `• ${v.color}` : ''}
                            </button>
                          );
                        })}
                      </div>
                    ) : (
                      <div className="text-xs text-rose-500 font-semibold">Sản phẩm hiện đang hết hàng</div>
                    )}

                    {/* Quantity stepper */}
                    <div className="flex items-center gap-3 pt-2">
                      <span className="text-xs font-bold text-slate-700">Số lượng:</span>
                      <div className="flex items-center border border-slate-200 rounded-xl overflow-hidden">
                        <button
                          onClick={() => setQuantity(Math.max(1, quantity - 1))}
                          className="px-2.5 py-1 text-slate-600 hover:bg-slate-100 font-bold text-sm"
                        >
                          -
                        </button>
                        <span className="px-3 py-1 text-xs font-bold text-slate-800">{quantity}</span>
                        <button
                          onClick={() => setQuantity(quantity + 1)}
                          className="px-2.5 py-1 text-slate-600 hover:bg-slate-100 font-bold text-sm"
                        >
                          +
                        </button>
                      </div>
                    </div>

                    {/* Stock indicator */}
                    <div className="text-xs text-slate-500 flex items-center gap-1.5 pt-1">
                      <CheckCircle className="w-3.5 h-3.5 text-cyan-600" />
                      <span>
                        Tồn kho: <strong className="text-slate-800">{selectedVariant?.stockQuantity ?? 0}</strong> sản phẩm
                      </span>
                    </div>
                  </div>
                </div>

                {/* Action Button Cân xứng */}
                <div>
                  <button
                    onClick={() => onAddToCart(product, selectedVariant?.variantId, quantity)}
                    disabled={!selectedVariant || selectedVariant.stockQuantity <= 0}
                    className="w-full btn-cyan py-3.5 text-sm font-bold shadow-md shadow-cyan-600/20"
                  >
                    <ShoppingCart className="w-4 h-4" />
                    <span>Thêm vào giỏ hàng</span>
                  </button>
                </div>

              </div>

            </div>

            {/* Description */}
            <div className="mt-8 pt-6 border-t border-slate-100">
              <h4 className="text-xs font-extrabold uppercase tracking-wider text-slate-800 mb-2">
                Mô tả sản phẩm
              </h4>
              <p className="text-xs sm:text-sm text-slate-600 leading-relaxed">
                {product.description || 'Chất vải cao cấp, mềm mại và thoáng khí. Thiết kế trẻ trung, hiện đại phù hợp đi làm, đi chơi và dạo phố.'}
              </p>
            </div>
          </>
        ) : null}

      </div>
    </div>
  );
}
