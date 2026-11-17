import React, { useState, useEffect, useCallback } from 'react';
import Navbar from './components/Navbar';
import HeroBanner from './components/HeroBanner';
import CategoryFilter from './components/CategoryFilter';
import AiRecommendations from './components/AiRecommendations';
import ProductCard from './components/ProductCard';
import ProductDetailModal from './components/ProductDetailModal';
import CartDrawer from './components/CartDrawer';
import CheckoutModal from './components/CheckoutModal';
import AuthModal from './components/AuthModal';
import MyOrdersModal from './components/MyOrdersModal';
import Footer from './components/Footer';
import api from './api/client';
import { Sparkles, ShoppingBag, AlertCircle, CheckCircle, Info, RefreshCw } from 'lucide-react';

export default function App() {
  // Auth & Profile
  const [user, setUser] = useState(null);
  const [isAuthOpen, setIsAuthOpen] = useState(false);
  const [isOrdersOpen, setIsOrdersOpen] = useState(false);

  // Data
  const [categories, setCategories] = useState([]);
  const [selectedCategoryId, setSelectedCategoryId] = useState(null);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [products, setProducts] = useState([]);
  const [loadingProducts, setLoadingProducts] = useState(false);
  const [pageInfo, setPageInfo] = useState({ page: 0, totalPages: 1, totalElements: 0 });

  // AI Recommendations
  const [recommendations, setRecommendations] = useState(null);
  const [loadingAi, setLoadingAi] = useState(false);

  // Cart
  const [cart, setCart] = useState(null);
  const [isCartOpen, setIsCartOpen] = useState(false);
  const [isCheckoutOpen, setIsCheckoutOpen] = useState(false);

  // Selected Product Detail
  const [selectedProductId, setSelectedProductId] = useState(null);

  // Toast notification
  const [toast, setToast] = useState(null);

  const showToast = (message, type = 'info') => {
    setToast({ message, type });
    setTimeout(() => {
      setToast(null);
    }, 3500);
  };

  // Interaction logger (TC-11 Anti-Spoofing: only VIEW / CLICK)
  const recordInteraction = useCallback(async (productId, type) => {
    try {
      if (!productId) return;
      await api.post('/interactions', {
        productId,
        interactionType: type
      });
    } catch {
      // Ignore background analytics errors
    }
  }, []);

  // Fetch current user
  const fetchCurrentUser = useCallback(async () => {
    const token = localStorage.getItem('cyan_token');
    if (!token) {
      setUser(null);
      return;
    }
    try {
      const res = await api.get('/auth/me');
      if (res.data?.data) {
        setUser(res.data.data);
      }
    } catch {
      localStorage.removeItem('cyan_token');
      localStorage.removeItem('cyan_user');
      setUser(null);
    }
  }, []);

  // Fetch Cart
  const fetchCart = useCallback(async () => {
    const token = localStorage.getItem('cyan_token');
    if (!token) {
      // Guest cart from localStorage
      const guestCart = JSON.parse(localStorage.getItem('cyan_guest_cart') || '{"items":[],"totalAmount":0}');
      setCart(guestCart);
      return;
    }
    try {
      const res = await api.get('/cart');
      if (res.data?.data) {
        setCart(res.data.data);
      }
    } catch (err) {
      console.warn('Failed to load cart', err);
    }
  }, []);

  // Fetch Categories
  const fetchCategories = useCallback(async () => {
    try {
      const res = await api.get('/categories');
      if (res.data?.data) {
        setCategories(res.data.data);
      }
    } catch (err) {
      console.warn('Failed to load categories', err);
    }
  }, []);

  // Fetch Products
  const fetchProducts = useCallback(async (catId, keyword, page = 0) => {
    setLoadingProducts(true);
    try {
      const params = {
        page,
        size: 12,
        sortBy: 'createdAt',
        sortDir: 'desc'
      };
      if (catId) params.categoryId = catId;
      if (keyword && keyword.trim()) params.keyword = keyword.trim();

      const res = await api.get('/products', { params });
      const data = res.data?.data;
      if (data) {
        setProducts(data.content || []);
        setPageInfo({
          page: data.pageNumber || 0,
          totalPages: data.totalPages || 1,
          totalElements: data.totalElements || 0
        });
      }
    } catch (err) {
      console.warn('Failed to load products', err);
    } finally {
      setLoadingProducts(false);
    }
  }, []);

  // Fetch AI Recommendations
  const fetchAiRecommendations = useCallback(async () => {
    setLoadingAi(true);
    try {
      const res = await api.get('/recommendations?limit=6');
      if (res.data?.data) {
        setRecommendations(res.data.data);
      }
    } catch (err) {
      console.warn('AI recommendation error, fallback will trigger', err);
    } finally {
      setLoadingAi(false);
    }
  }, []);

  // Initial load
  useEffect(() => {
    fetchCurrentUser();
    fetchCategories();
    fetchAiRecommendations();
  }, [fetchCurrentUser, fetchCategories, fetchAiRecommendations]);

  // Load products when category or keyword changes
  useEffect(() => {
    fetchProducts(selectedCategoryId, searchKeyword, 0);
  }, [selectedCategoryId, searchKeyword, fetchProducts]);

  // Load cart when user changes
  useEffect(() => {
    fetchCart();
  }, [user, fetchCart]);

  // Handle Add to Cart
  const handleAddToCart = async (product, variantId = null, quantity = 1) => {
    recordInteraction(product.productId, 'CLICK');

    if (!user) {
      showToast('Vui lòng đăng nhập để lưu giỏ hàng của bạn', 'info');
      setIsAuthOpen(true);
      return;
    }

    try {
      let targetVariantId = variantId || product.variants?.[0]?.variantId;
      if (!targetVariantId) {
        // Fetch product detail to get first active variant
        const detailRes = await api.get(`/products/${product.productId}`);
        const variants = detailRes.data?.data?.variants || [];
        const activeVariant = variants.find(v => v.stockQuantity > 0) || variants[0];
        targetVariantId = activeVariant?.variantId;
      }

      if (!targetVariantId) {
        showToast('Sản phẩm tạm thời hết hàng', 'error');
        return;
      }

      await api.post('/cart/items', {
        variantId: targetVariantId,
        quantity
      });
      await fetchCart();
      showToast(`Đã thêm "${product.name}" vào giỏ hàng!`, 'success');
      setIsCartOpen(true);
    } catch (err) {
      const msg = err.response?.data?.message || 'Không thể thêm sản phẩm vào giỏ hàng';
      showToast(msg, 'error');
    }
  };

  // Handle Cart item update
  const handleUpdateCartQuantity = async (cartItemId, newQty) => {
    try {
      await api.patch(`/cart/items/${cartItemId}`, { quantity: newQty });
      await fetchCart();
    } catch (err) {
      const msg = err.response?.data?.message || 'Không thể cập nhật số lượng';
      showToast(msg, 'error');
    }
  };

  // Handle Cart item remove
  const handleRemoveCartItem = async (cartItemId) => {
    try {
      await api.delete(`/cart/items/${cartItemId}`);
      await fetchCart();
      showToast('Đã xóa sản phẩm khỏi giỏ hàng', 'info');
    } catch (err) {
      const msg = err.response?.data?.message || 'Không thể xóa sản phẩm';
      showToast(msg, 'error');
    }
  };

  // Toggle AI Personalization Privacy
  const handleTogglePrivacy = async () => {
    if (!user) return;
    const nextState = !user.allowPersonalization;
    try {
      await api.patch('/users/me/personalization', { allowPersonalization: nextState });
      setUser(prev => ({ ...prev, allowPersonalization: nextState }));
      showToast(
        nextState
          ? 'Đã BẬT gợi ý cá nhân hóa AI'
          : 'Đã TẮT cá nhân hóa AI (Quyền riêng tư được bảo vệ)',
        'success'
      );
      // Refresh AI recommendations
      fetchAiRecommendations();
    } catch {
      showToast('Không thể cập nhật cài đặt riêng tư', 'error');
    }
  };

  // Logout
  const handleLogout = () => {
    localStorage.removeItem('cyan_token');
    localStorage.removeItem('cyan_user');
    setUser(null);
    setCart(null);
    showToast('Đã đăng xuất tài khoản', 'info');
    fetchAiRecommendations();
  };

  // Calculate cart count
  const cartCount = cart?.items?.reduce((acc, item) => acc + (item.quantity || 0), 0) || 0;

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900 flex flex-col font-sans selection:bg-cyan-500 selection:text-white">
      {/* Toast Notification */}
      {toast && (
        <div className="fixed top-5 right-5 z-[9999] max-w-sm w-full animate-bounce-subtle">
          <div className={`flex items-center gap-3 p-4 rounded-xl shadow-xl border ${
            toast.type === 'success' ? 'bg-cyan-900/90 text-cyan-100 border-cyan-400' :
            toast.type === 'error' ? 'bg-rose-900/90 text-rose-100 border-rose-400' :
            'bg-slate-900/90 text-slate-100 border-cyan-500/50'
          } backdrop-blur-md`}>
            {toast.type === 'success' && <CheckCircle className="w-5 h-5 text-cyan-400 shrink-0" />}
            {toast.type === 'error' && <AlertCircle className="w-5 h-5 text-rose-400 shrink-0" />}
            {toast.type === 'info' && <Info className="w-5 h-5 text-cyan-400 shrink-0" />}
            <p className="text-sm font-medium flex-1">{toast.message}</p>
          </div>
        </div>
      )}

      {/* Responsive Navbar */}
      <Navbar
        user={user}
        cartCount={cartCount}
        onOpenCart={() => setIsCartOpen(true)}
        onOpenAuth={() => setIsAuthOpen(true)}
        onOpenOrders={() => setIsOrdersOpen(true)}
        onLogout={handleLogout}
        onTogglePrivacy={handleTogglePrivacy}
        searchKeyword={searchKeyword}
        onSearchChange={setSearchKeyword}
      />

      <main className="flex-1">
        {/* Hero Banner */}
        <HeroBanner
          onExploreClick={() => {
            const el = document.getElementById('products-section');
            el?.scrollIntoView({ behavior: 'smooth' });
          }}
          onAiClick={() => {
            const el = document.getElementById('ai-section');
            el?.scrollIntoView({ behavior: 'smooth' });
          }}
          aiFeaturedProduct={recommendations?.products?.[0]}
        />

        {/* AI Recommendations Section */}
        <div id="ai-section" className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 sm:py-12">
          <AiRecommendations
            recommendationData={recommendations}
            onProductClick={(prod) => {
              recordInteraction(prod.productId, 'VIEW');
              setSelectedProductId(prod.productId);
            }}
            onAddToCart={(prod) => handleAddToCart(prod)}
            onRefresh={fetchAiRecommendations}
          />
        </div>

        {/* Categories Navigation Bar */}
        <div id="categories-section" className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-4 pb-2">
          <CategoryFilter
            categories={categories}
            selectedCategoryId={selectedCategoryId}
            onSelectCategory={(id) => {
              setSelectedCategoryId(id);
              fetchProducts(id, searchKeyword, 0);
            }}
          />
        </div>

        {/* Main Products Grid Section */}
        <section id="products-section" className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-6 mb-6 border-b border-cyan-100 gap-4">
            <div>
              <div className="flex items-center gap-2">
                <ShoppingBag className="w-6 h-6 text-cyan-600" />
                <h2 className="text-2xl font-bold text-slate-800">
                  {selectedCategoryId ? 'Sản Phẩm Trong Danh Mục' : 'Tất Cả Sản Phẩm'}
                </h2>
              </div>
              <p className="text-sm text-slate-500 mt-1">
                {searchKeyword ? `Kết quả tìm kiếm cho "${searchKeyword}": ` : ''}
                Hiện có {pageInfo.totalElements} sản phẩm sẵn sàng phục vụ bạn
              </p>
            </div>

            {/* Quick Actions / Reset Filter */}
            {(selectedCategoryId || searchKeyword) && (
              <button
                onClick={() => {
                  setSelectedCategoryId(null);
                  setSearchKeyword('');
                  fetchProducts(null, '', 0);
                }}
                className="btn-cyan-outline text-xs py-2 px-3 self-start sm:self-auto flex items-center gap-1.5"
              >
                <RefreshCw className="w-3.5 h-3.5" />
                Xóa bộ lọc
              </button>
            )}
          </div>

          {/* Product Cards Grid */}
          {loadingProducts ? (
            <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4 sm:gap-6">
              {[...Array(8)].map((_, i) => (
                <div key={i} className="bg-white rounded-2xl p-4 shadow-sm border border-slate-100 animate-pulse space-y-3">
                  <div className="w-full aspect-square bg-slate-200 rounded-xl" />
                  <div className="h-4 bg-slate-200 rounded w-2/3" />
                  <div className="h-4 bg-slate-200 rounded w-1/3" />
                  <div className="h-9 bg-slate-200 rounded-xl w-full" />
                </div>
              ))}
            </div>
          ) : products.length > 0 ? (
            <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4 sm:gap-6">
              {products.map((product) => (
                <ProductCard
                  key={product.productId}
                  product={product}
                  onClick={() => {
                    recordInteraction(product.productId, 'VIEW');
                    setSelectedProductId(product.productId);
                  }}
                  onAddToCart={(p) => handleAddToCart(p)}
                />
              ))}
            </div>
          ) : (
            <div className="text-center py-16 bg-white rounded-2xl border border-dashed border-cyan-200 p-8 max-w-lg mx-auto">
              <div className="w-16 h-16 rounded-full bg-cyan-50 flex items-center justify-center mx-auto mb-4 text-cyan-600">
                <ShoppingBag className="w-8 h-8" />
              </div>
              <h3 className="text-lg font-bold text-slate-800 mb-1">Không tìm thấy sản phẩm nào</h3>
              <p className="text-sm text-slate-500 mb-6">
                Hãy thử tìm kiếm với từ khóa khác hoặc chuyển sang danh mục sản phẩm khác.
              </p>
              <button
                onClick={() => {
                  setSelectedCategoryId(null);
                  setSearchKeyword('');
                  fetchProducts(null, '', 0);
                }}
                className="btn-cyan text-sm py-2 px-5 inline-flex"
              >
                Xem tất cả sản phẩm
              </button>
            </div>
          )}

          {/* Pagination */}
          {pageInfo.totalPages > 1 && (
            <div className="flex items-center justify-center gap-2 mt-12">
              <button
                disabled={pageInfo.page === 0}
                onClick={() => fetchProducts(selectedCategoryId, searchKeyword, pageInfo.page - 1)}
                className="px-4 py-2 text-sm font-medium rounded-xl border border-cyan-200 bg-white text-slate-700 hover:bg-cyan-50 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
              >
                Trang trước
              </button>
              <span className="text-sm font-medium text-slate-600 px-3">
                {pageInfo.page + 1} / {pageInfo.totalPages}
              </span>
              <button
                disabled={pageInfo.page >= pageInfo.totalPages - 1}
                onClick={() => fetchProducts(selectedCategoryId, searchKeyword, pageInfo.page + 1)}
                className="px-4 py-2 text-sm font-medium rounded-xl border border-cyan-200 bg-white text-slate-700 hover:bg-cyan-50 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
              >
                Trang sau
              </button>
            </div>
          )}
        </section>
      </main>

      {/* Footer */}
      <Footer onOpenAuth={() => setIsAuthOpen(true)} />

      {/* Modals & Drawers */}
      {selectedProductId && (
        <ProductDetailModal
          productId={selectedProductId}
          isOpen={!!selectedProductId}
          onClose={() => setSelectedProductId(null)}
          onAddToCart={(product, skuId, qty) => handleAddToCart(product, skuId, qty)}
        />
      )}

      <CartDrawer
        isOpen={isCartOpen}
        onClose={() => setIsCartOpen(false)}
        cart={cart}
        onUpdateQuantity={handleUpdateCartQuantity}
        onRemoveItem={handleRemoveCartItem}
        onCheckout={() => {
          setIsCartOpen(false);
          setIsCheckoutOpen(true);
        }}
      />

      <CheckoutModal
        isOpen={isCheckoutOpen}
        onClose={() => setIsCheckoutOpen(false)}
        cart={cart}
        onOrderSuccess={(order) => {
          fetchCart();
          showToast(`Đặt hàng #${order.orderCode || order.orderId} thành công!`, 'success');
          setIsOrdersOpen(true);
        }}
      />

      <AuthModal
        isOpen={isAuthOpen}
        onClose={() => setIsAuthOpen(false)}
        onLoginSuccess={(userData) => {
          setUser(userData);
          fetchCart();
          fetchAiRecommendations();
          showToast(`Xin chào, ${userData.fullName || 'bạn'}!`, 'success');
        }}
      />

      <MyOrdersModal
        isOpen={isOrdersOpen}
        onClose={() => setIsOrdersOpen(false)}
      />
    </div>
  );
}
