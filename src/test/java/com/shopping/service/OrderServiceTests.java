package com.shopping.service;

import com.shopping.dto.request.CheckoutRequest;
import com.shopping.dto.request.UpdateOrderStatusRequest;
import com.shopping.dto.response.OrderDetailResponse;
import com.shopping.entity.*;
import com.shopping.enums.*;
import com.shopping.exception.BadRequestException;
import com.shopping.mapper.OrderMapper;
import com.shopping.repository.*;
import com.shopping.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private InventoryTransactionRepository inventoryTransactionRepository;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User customerUser;
    private Address address;
    private Product product;
    private ProductVariant variant;
    private Cart cart;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        customerUser = User.builder()
                .userId(1L)
                .email("customer@shopping.vn")
                .fullName("Nguyễn Khách Hàng")
                .status(UserStatus.ACTIVE)
                .build();

        address = Address.builder()
                .addressId(1L)
                .user(customerUser)
                .recipientName("Nguyễn Khách Hàng")
                .phone("0912345678")
                .addressLine("123 Lê Lợi")
                .ward("Bến Nghé")
                .district("Quận 1")
                .province("TP. Hồ Chí Minh")
                .build();

        product = Product.builder()
                .productId(10L)
                .name("Áo Thun Cotton Basic")
                .slug("ao-thun-cotton-basic")
                .build();

        variant = ProductVariant.builder()
                .variantId(100L)
                .product(product)
                .sku("TSHIRT-BLK-M")
                .size("M")
                .color("Đen")
                .price(new BigDecimal("200000.00"))
                .stockQuantity(10)
                .build();

        cart = Cart.builder()
                .cartId(1L)
                .user(customerUser)
                .items(new ArrayList<>())
                .build();

        cartItem = CartItem.builder()
                .cartItemId(5L)
                .cart(cart)
                .variant(variant)
                .quantity(2)
                .build();

        cart.getItems().add(cartItem);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(customerUser.getEmail(), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @DisplayName("Đặt hàng thành công: tồn kho đủ -> trừ tồn kho, lưu order, xóa giỏ hàng")
    void placeOrder_success() {
        CheckoutRequest request = CheckoutRequest.builder()
                .addressId(1L)
                .paymentMethod(PaymentMethod.COD)
                .note("Giao buổi sáng")
                .build();

        when(userRepository.findByEmail(customerUser.getEmail())).thenReturn(Optional.of(customerUser));
        when(cartRepository.findByUser_UserId(customerUser.getUserId())).thenReturn(Optional.of(cart));
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

        Order savedOrder = Order.builder()
                .orderId(500L)
                .orderCode("ORD-20261117-ABCDEF")
                .user(customerUser)
                .subtotal(new BigDecimal("400000.00"))
                .shippingFee(new BigDecimal("30000.00"))
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("430000.00"))
                .orderStatus(OrderStatus.PENDING)
                .items(new ArrayList<>())
                .build();

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderStatusHistoryRepository.save(any(OrderStatusHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderItemRepository.findByOrder_OrderId(500L)).thenReturn(Collections.emptyList());
        when(orderStatusHistoryRepository.findByOrder_OrderIdOrderByChangedAtAsc(500L)).thenReturn(Collections.emptyList());

        OrderDetailResponse detailResponse = OrderDetailResponse.builder()
                .orderId(500L)
                .orderCode("ORD-20261117-ABCDEF")
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("430000.00"))
                .build();
        when(orderMapper.toDetailResponse(savedOrder)).thenReturn(detailResponse);

        OrderDetailResponse response = orderService.placeOrder(request);

        assertNotNull(response);
        assertEquals(500L, response.getOrderId());
        assertEquals(OrderStatus.PENDING, response.getStatus());

        // Kiểm tra tồn kho đã giảm từ 10 xuống 8
        assertEquals(8, variant.getStockQuantity());
        verify(productVariantRepository, times(1)).save(variant);
        verify(inventoryTransactionRepository, times(1)).save(any(InventoryTransaction.class));
        verify(cartItemRepository, times(1)).deleteAll(anyList());
    }

    @Test
    @DisplayName("TC-05: Đặt hàng thất bại khi tồn kho không đủ -> ném BadRequestException")
    void placeOrder_insufficientStock_throwsBadRequestException() {
        variant.setStockQuantity(1); // Tồn kho chỉ còn 1 nhưng giỏ hàng đòi mua 2

        CheckoutRequest request = CheckoutRequest.builder()
                .addressId(1L)
                .paymentMethod(PaymentMethod.COD)
                .build();

        when(userRepository.findByEmail(customerUser.getEmail())).thenReturn(Optional.of(customerUser));
        when(cartRepository.findByUser_UserId(customerUser.getUserId())).thenReturn(Optional.of(cart));
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> orderService.placeOrder(request));
        assertTrue(ex.getMessage().contains("không đủ tồn kho"));

        // Xác nhận không lưu đơn hàng
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Đặt hàng thất bại khi giỏ hàng rỗng -> ném BadRequestException")
    void placeOrder_emptyCart_throwsBadRequestException() {
        cart.getItems().clear();

        CheckoutRequest request = CheckoutRequest.builder()
                .addressId(1L)
                .paymentMethod(PaymentMethod.COD)
                .build();

        when(userRepository.findByEmail(customerUser.getEmail())).thenReturn(Optional.of(customerUser));
        when(cartRepository.findByUser_UserId(customerUser.getUserId())).thenReturn(Optional.of(cart));

        assertThrows(BadRequestException.class, () -> orderService.placeOrder(request));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Hủy đơn hàng thành công khi đơn ở trạng thái PENDING -> hoàn lại tồn kho và ghi nhật ký")
    void cancelOrder_pending_success() {
        Order order = Order.builder()
                .orderId(500L)
                .orderCode("ORD-20261117-ABCDEF")
                .user(customerUser)
                .orderStatus(OrderStatus.PENDING)
                .build();

        OrderItem item = OrderItem.builder()
                .orderItemId(1L)
                .order(order)
                .variant(variant)
                .quantity(2)
                .build();

        when(userRepository.findByEmail(customerUser.getEmail())).thenReturn(Optional.of(customerUser));
        when(orderRepository.findById(500L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrder_OrderId(500L)).thenReturn(List.of(item));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderDetailResponse detailResponse = OrderDetailResponse.builder()
                .orderId(500L)
                .status(OrderStatus.CANCELLED)
                .build();
        when(orderMapper.toDetailResponse(order)).thenReturn(detailResponse);

        int stockBefore = variant.getStockQuantity(); // 10
        orderService.cancelOrder(500L, "Đổi ý không mua nữa");

        assertEquals(OrderStatus.CANCELLED, order.getOrderStatus());
        assertEquals(stockBefore + 2, variant.getStockQuantity());
        verify(productVariantRepository, times(1)).save(variant);
        verify(inventoryTransactionRepository, times(1)).save(any(InventoryTransaction.class));
    }

    @Test
    @DisplayName("Hủy đơn hàng thất bại khi đơn đang giao (SHIPPING) -> ném BadRequestException")
    void cancelOrder_shipping_throwsBadRequestException() {
        Order order = Order.builder()
                .orderId(500L)
                .orderCode("ORD-20261117-ABCDEF")
                .user(customerUser)
                .orderStatus(OrderStatus.SHIPPING)
                .build();

        when(userRepository.findByEmail(customerUser.getEmail())).thenReturn(Optional.of(customerUser));
        when(orderRepository.findById(500L)).thenReturn(Optional.of(order));

        assertThrows(BadRequestException.class, () -> orderService.cancelOrder(500L, "Muốn hủy"));
        assertEquals(OrderStatus.SHIPPING, order.getOrderStatus());
        verify(productVariantRepository, never()).save(any());
    }

    @Test
    @DisplayName("Admin cập nhật trạng thái đơn hàng hợp lệ: PENDING -> CONFIRMED")
    void updateOrderStatus_valid_success() {
        Order order = Order.builder()
                .orderId(500L)
                .orderStatus(OrderStatus.PENDING)
                .build();

        UpdateOrderStatusRequest request = UpdateOrderStatusRequest.builder()
                .status(OrderStatus.CONFIRMED)
                .note("Đã gọi điện xác nhận với khách")
                .build();

        when(orderRepository.findById(500L)).thenReturn(Optional.of(order));
        when(userRepository.findByEmail(customerUser.getEmail())).thenReturn(Optional.of(customerUser));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderDetailResponse detailResponse = OrderDetailResponse.builder()
                .orderId(500L)
                .status(OrderStatus.CONFIRMED)
                .build();
        when(orderMapper.toDetailResponse(order)).thenReturn(detailResponse);

        OrderDetailResponse response = orderService.updateOrderStatus(500L, request);

        assertEquals(OrderStatus.CONFIRMED, order.getOrderStatus());
        verify(orderStatusHistoryRepository, times(1)).save(any(OrderStatusHistory.class));
    }

    @Test
    @DisplayName("Admin cập nhật trạng thái đơn hàng không hợp lệ: PENDING -> DELIVERED -> ném BadRequestException")
    void updateOrderStatus_invalid_throwsBadRequestException() {
        Order order = Order.builder()
                .orderId(500L)
                .orderStatus(OrderStatus.PENDING)
                .build();

        UpdateOrderStatusRequest request = UpdateOrderStatusRequest.builder()
                .status(OrderStatus.DELIVERED)
                .build();

        when(orderRepository.findById(500L)).thenReturn(Optional.of(order));

        assertThrows(BadRequestException.class, () -> orderService.updateOrderStatus(500L, request));
    }
}
