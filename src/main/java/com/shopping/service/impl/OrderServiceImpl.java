package com.shopping.service.impl;

import com.shopping.apiResponse.PageResponse;
import com.shopping.dto.request.CheckoutRequest;
import com.shopping.dto.request.UpdateOrderStatusRequest;
import com.shopping.dto.response.OrderDetailResponse;
import com.shopping.dto.response.OrderSummaryResponse;
import com.shopping.entity.*;
import com.shopping.enums.*;
import com.shopping.exception.BadRequestException;
import com.shopping.exception.ResourceNotFoundException;
import com.shopping.exception.UnauthorizedException;
import com.shopping.mapper.OrderMapper;
import com.shopping.repository.*;
import com.shopping.service.OrderService;
import com.shopping.util.OrderCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final CouponRepository couponRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final OrderMapper orderMapper;

    private static final BigDecimal DEFAULT_SHIPPING_FEE = new BigDecimal("30000.00");
    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("500000.00");

    @Override
    @Transactional
    public OrderDetailResponse placeOrder(CheckoutRequest request) {
        User user = getCurrentAuthenticatedUser();

        Cart cart = cartRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new BadRequestException("Giỏ hàng của bạn đang trống"));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BadRequestException("Giỏ hàng của bạn đang trống");
        }

        Address shippingAddress = addressRepository.findById(request.getAddressId())
                .filter(a -> a.getUser().getUserId().equals(user.getUserId()))
                .orElseThrow(() -> new ResourceNotFoundException("Địa chỉ giao hàng không hợp lệ hoặc không thuộc về bạn"));

        String orderCode = OrderCodeGenerator.generateOrderCode();

        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        // TC-05 Kiểm tra và trừ tồn kho từng sản phẩm trong giỏ hàng
        for (CartItem cartItem : cart.getItems()) {
            ProductVariant variant = cartItem.getVariant();
            int requestedQty = cartItem.getQuantity();

            if (variant.getStockQuantity() < requestedQty) {
                throw new BadRequestException(String.format(
                        "Sản phẩm '%s' (SKU: %s) không đủ tồn kho. Tồn kho khả dụng: %d, yêu cầu: %d",
                        variant.getProduct().getName(), variant.getSku(), variant.getStockQuantity(), requestedQty
                ));
            }

            // Trừ tồn kho
            int previousStock = variant.getStockQuantity();
            int newStock = previousStock - requestedQty;
            variant.setStockQuantity(newStock);
            productVariantRepository.save(variant);

            BigDecimal lineTotal = variant.getPrice().multiply(BigDecimal.valueOf(requestedQty));
            subtotal = subtotal.add(lineTotal);

            OrderItem orderItem = OrderItem.builder()
                    .variant(variant)
                    .productNameSnapshot(variant.getProduct().getName())
                    .skuSnapshot(variant.getSku())
                    .sizeSnapshot(variant.getSize())
                    .colorSnapshot(variant.getColor())
                    .unitPrice(variant.getPrice())
                    .quantity(requestedQty)
                    .lineTotal(lineTotal)
                    .build();

            orderItems.add(orderItem);
        }

        // Tính phí vận chuyển (miễn phí từ 500k)
        BigDecimal shippingFee = subtotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0 ? BigDecimal.ZERO : DEFAULT_SHIPPING_FEE;

        // Xử lý mã giảm giá nếu có
        BigDecimal discountAmount = BigDecimal.ZERO;
        Coupon coupon = null;
        if (request.getCouponCode() != null && !request.getCouponCode().trim().isEmpty()) {
            coupon = couponRepository.findByCodeIgnoreCase(request.getCouponCode().trim())
                    .orElseThrow(() -> new BadRequestException("Mã giảm giá không tồn tại: " + request.getCouponCode()));

            validateCoupon(coupon, subtotal);

            if (coupon.getDiscountType() == DiscountType.PERCENT) {
                discountAmount = subtotal.multiply(coupon.getDiscountValue())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            } else if (coupon.getDiscountType() == DiscountType.FIXED_AMOUNT) {
                discountAmount = coupon.getDiscountValue().min(subtotal);
            }

            coupon.setUsedCount(coupon.getUsedCount() + 1);
            couponRepository.save(coupon);
        }

        BigDecimal totalAmount = subtotal.add(shippingFee).subtract(discountAmount);
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalAmount = BigDecimal.ZERO;
        }

        String addressSnapshot = String.format("%s | %s | %s, %s, %s, %s",
                shippingAddress.getRecipientName(),
                shippingAddress.getPhone(),
                shippingAddress.getAddressLine(),
                shippingAddress.getWard(),
                shippingAddress.getDistrict(),
                shippingAddress.getProvince());

        Order order = Order.builder()
                .orderCode(orderCode)
                .user(user)
                .subtotal(subtotal)
                .shippingFee(shippingFee)
                .discountAmount(discountAmount)
                .totalAmount(totalAmount)
                .orderStatus(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.UNPAID)
                .paymentMethod(request.getPaymentMethod())
                .addressSnapshot(addressSnapshot)
                .coupon(coupon)
                .couponCodeSnapshot(coupon != null ? coupon.getCode() : null)
                .note(request.getNote())
                .items(new ArrayList<>())
                .build();

        Order savedOrder = orderRepository.save(order);

        for (OrderItem item : orderItems) {
            item.setOrder(savedOrder);
            savedOrder.getItems().add(orderItemRepository.save(item));

            // Ghi nhật ký tồn kho
            InventoryTransaction invTx = InventoryTransaction.builder()
                    .variant(item.getVariant())
                    .quantityChange(-item.getQuantity())
                    .reason("Đặt hàng: " + orderCode)
                    .referenceType(InventoryReferenceType.ORDER_RESERVATION)
                    .referenceId(savedOrder.getOrderId())
                    .createdBy(user)
                    .build();
            inventoryTransactionRepository.save(invTx);
        }

        // Ghi lịch sử trạng thái
        OrderStatusHistory initialHistory = OrderStatusHistory.builder()
                .order(savedOrder)
                .oldStatus(null)
                .newStatus(OrderStatus.PENDING)
                .changedBy(user.getEmail())
                .note("Khách hàng đặt hàng thành công")
                .build();
        orderStatusHistoryRepository.save(initialHistory);

        // Xóa giỏ hàng sau khi đặt thành công
        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();

        return buildOrderDetailResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderSummaryResponse> getMyOrders(Pageable pageable) {
        User user = getCurrentAuthenticatedUser();
        Page<Order> orderPage = orderRepository.findByUser_UserIdOrderByCreatedAtDesc(user.getUserId(), pageable);

        return PageResponse.<OrderSummaryResponse>builder()
                .content(orderMapper.toSummaryResponseList(orderPage.getContent()))
                .pageNumber(orderPage.getNumber())
                .pageSize(orderPage.getSize())
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .last(orderPage.isLast())
                .first(orderPage.isFirst())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailResponse getMyOrderDetail(Long orderId) {
        User user = getCurrentAuthenticatedUser();
        Order order = orderRepository.findById(orderId)
                .filter(o -> o.getUser().getUserId().equals(user.getUserId()))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng hoặc đơn hàng không thuộc về bạn"));

        return buildOrderDetailResponse(order);
    }

    @Override
    @Transactional
    public OrderDetailResponse cancelOrder(Long orderId, String reason) {
        User user = getCurrentAuthenticatedUser();
        Order order = orderRepository.findById(orderId)
                .filter(o -> o.getUser().getUserId().equals(user.getUserId()))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng hoặc đơn hàng không thuộc về bạn"));

        if (order.getOrderStatus() != OrderStatus.PENDING && order.getOrderStatus() != OrderStatus.CONFIRMED) {
            throw new BadRequestException("Không thể hủy đơn hàng ở trạng thái: " + order.getOrderStatus() +
                    ". Đơn hàng chỉ có thể hủy khi đang ở trạng thái CHỜ XỬ LÝ (PENDING) hoặc ĐÃ XÁC NHẬN (CONFIRMED).");
        }

        OrderStatus oldStatus = order.getOrderStatus();
        order.setOrderStatus(OrderStatus.CANCELLED);

        // Hoàn lại tồn kho cho từng sản phẩm
        refundInventory(order, "Khách hàng hủy đơn: " + (reason != null ? reason : ""), user);

        // Hoàn lại lượt sử dụng mã giảm giá nếu có
        if (order.getCoupon() != null) {
            Coupon coupon = order.getCoupon();
            if (coupon.getUsedCount() > 0) {
                coupon.setUsedCount(coupon.getUsedCount() - 1);
                couponRepository.save(coupon);
            }
        }

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .oldStatus(oldStatus)
                .newStatus(OrderStatus.CANCELLED)
                .changedBy(user.getEmail())
                .note("Khách hàng hủy đơn. Lý do: " + (reason != null ? reason : "Không có"))
                .build();
        orderStatusHistoryRepository.save(history);

        Order saved = orderRepository.save(order);
        return buildOrderDetailResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderSummaryResponse> getAdminOrders(String keyword, OrderStatus status, Pageable pageable) {
        Page<Order> orderPage = orderRepository.findAdminOrders(keyword, status, pageable);

        return PageResponse.<OrderSummaryResponse>builder()
                .content(orderMapper.toSummaryResponseList(orderPage.getContent()))
                .pageNumber(orderPage.getNumber())
                .pageSize(orderPage.getSize())
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .last(orderPage.isLast())
                .first(orderPage.isFirst())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailResponse getAdminOrderDetail(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));

        return buildOrderDetailResponse(order);
    }

    @Override
    @Transactional
    public OrderDetailResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));

        OrderStatus currentStatus = order.getOrderStatus();
        OrderStatus targetStatus = request.getStatus();

        if (currentStatus == targetStatus) {
            throw new BadRequestException("Đơn hàng hiện tại đã ở trạng thái: " + targetStatus);
        }

        validateStatusTransition(currentStatus, targetStatus);

        order.setOrderStatus(targetStatus);

        User operator = getCurrentAuthenticatedUser();

        // Nếu admin/staff chuyển sang CANCELLED -> hoàn tồn kho và hoàn coupon
        if (targetStatus == OrderStatus.CANCELLED) {
            refundInventory(order, "Đơn hàng bị hủy: " + (request.getNote() != null ? request.getNote() : ""), operator);
            if (order.getCoupon() != null) {
                Coupon coupon = order.getCoupon();
                if (coupon.getUsedCount() > 0) {
                    coupon.setUsedCount(coupon.getUsedCount() - 1);
                    couponRepository.save(coupon);
                }
            }
        }

        // Nếu giao hàng thành công (DELIVERED) và đang là COD -> chuyển paymentStatus thành PAID
        if (targetStatus == OrderStatus.DELIVERED) {
            order.setPaymentStatus(PaymentStatus.PAID);
        }

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .oldStatus(currentStatus)
                .newStatus(targetStatus)
                .changedBy(operator.getEmail())
                .note(request.getNote() != null ? request.getNote() : "Cập nhật trạng thái đơn hàng")
                .build();
        orderStatusHistoryRepository.save(history);

        Order updated = orderRepository.save(order);
        return buildOrderDetailResponse(updated);
    }

    private void refundInventory(Order order, String reason, User operator) {
        List<OrderItem> items = orderItemRepository.findByOrder_OrderId(order.getOrderId());
        for (OrderItem item : items) {
            ProductVariant variant = item.getVariant();
            int newStock = variant.getStockQuantity() + item.getQuantity();
            variant.setStockQuantity(newStock);
            productVariantRepository.save(variant);

            InventoryTransaction invTx = InventoryTransaction.builder()
                    .variant(variant)
                    .quantityChange(item.getQuantity())
                    .reason(reason)
                    .referenceType(InventoryReferenceType.ORDER_CANCELLATION)
                    .referenceId(order.getOrderId())
                    .createdBy(operator)
                    .build();
            inventoryTransactionRepository.save(invTx);
        }
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus target) {
        if (current == OrderStatus.CANCELLED || current == OrderStatus.DELIVERED) {
            throw new BadRequestException("Không thể chuyển đổi trạng thái khi đơn hàng đã kết thúc (" + current + ")");
        }

        boolean valid = switch (current) {
            case PENDING -> target == OrderStatus.CONFIRMED || target == OrderStatus.CANCELLED;
            case CONFIRMED -> target == OrderStatus.PACKING || target == OrderStatus.CANCELLED;
            case PACKING -> target == OrderStatus.SHIPPING || target == OrderStatus.CANCELLED;
            case SHIPPING -> target == OrderStatus.DELIVERED || target == OrderStatus.CANCELLED;
            default -> false;
        };

        if (!valid) {
            throw new BadRequestException(String.format("Không thể chuyển trạng thái từ '%s' sang '%s'", current, target));
        }
    }

    private void validateCoupon(Coupon coupon, BigDecimal orderAmount) {
        if (!Boolean.TRUE.equals(coupon.getIsActive())) {
            throw new BadRequestException("Mã giảm giá đã bị vô hiệu hóa");
        }
        if (coupon.getStartAt() != null && Instant.now().isBefore(coupon.getStartAt())) {
            throw new BadRequestException("Mã giảm giá chưa đến thời gian áp dụng");
        }
        if (coupon.isExpired()) {
            throw new BadRequestException("Mã giảm giá đã hết hạn sử dụng");
        }
        if (coupon.getMinOrderValue() != null && orderAmount.compareTo(coupon.getMinOrderValue()) < 0) {
            throw new BadRequestException(String.format(
                    "Đơn hàng chưa đạt giá trị tối thiểu để áp dụng mã giảm giá. Yêu cầu: %s, hiện tại: %s",
                    coupon.getMinOrderValue(), orderAmount));
        }
        if (coupon.isLimitReached()) {
            throw new BadRequestException("Mã giảm giá đã hết lượt sử dụng");
        }
    }

    private OrderDetailResponse buildOrderDetailResponse(Order order) {
        OrderDetailResponse response = orderMapper.toDetailResponse(order);

        List<OrderItem> items = orderItemRepository.findByOrder_OrderId(order.getOrderId());
        response.setItems(orderMapper.toItemResponseList(items));

        List<OrderStatusHistory> histories = orderStatusHistoryRepository.findByOrder_OrderIdOrderByChangedAtAsc(order.getOrderId());
        response.setStatusHistories(orderMapper.toHistoryResponseList(histories));

        return response;
    }

    private User getCurrentAuthenticatedUser() {
        String email = getCurrentAuthenticatedUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Vui lòng đăng nhập để tiếp tục"));
    }

    private String getCurrentAuthenticatedUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new UnauthorizedException("Vui lòng đăng nhập để tiếp tục");
        }
        return auth.getName();
    }
}
