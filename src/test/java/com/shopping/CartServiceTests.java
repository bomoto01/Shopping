package com.shopping;

import com.shopping.dto.request.cart.AddToCartRequest;
import com.shopping.dto.request.cart.UpdateCartItemRequest;
import com.shopping.dto.response.cart.CartResponse;
import com.shopping.entity.*;
import com.shopping.enums.ProductStatus;
import com.shopping.enums.RoleType;
import com.shopping.enums.UserStatus;
import com.shopping.exception.BadRequestException;
import com.shopping.exception.ForbiddenException;
import com.shopping.mapper.CartMapper;
import com.shopping.repository.CartItemRepository;
import com.shopping.repository.CartRepository;
import com.shopping.repository.ProductVariantRepository;
import com.shopping.repository.UserRepository;
import com.shopping.security.UserPrincipal;
import com.shopping.service.impl.CartServiceImpl;
import org.junit.jupiter.api.AfterEach;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTests {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private ProductVariantRepository variantRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CartMapper cartMapper;

    @InjectMocks
    private CartServiceImpl cartService;

    private User customerUser;
    private Product activeProduct;
    private ProductVariant inStockVariant;
    private Cart sampleCart;

    @BeforeEach
    void setUp() {
        Role customerRole = Role.builder().roleId(1).roleCode(RoleType.ROLE_CUSTOMER).build();
        customerUser = User.builder()
                .userId(1L)
                .email("customer@shopping.vn")
                .fullName("Khách hàng A")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(customerRole))
                .build();

        activeProduct = Product.builder()
                .productId(100L)
                .name("Áo Thun Cotton")
                .status(ProductStatus.ACTIVE)
                .build();

        inStockVariant = ProductVariant.builder()
                .variantId(10L)
                .product(activeProduct)
                .sku("TSHIRT-BLK-M")
                .size("M")
                .color("Đen")
                .price(new BigDecimal("150000"))
                .stockQuantity(5) // Tồn kho = 5
                .isActive(true)
                .build();

        sampleCart = Cart.builder()
                .cartId(50L)
                .user(customerUser)
                .items(new ArrayList<>())
                .build();

        UserPrincipal principal = UserPrincipal.create(customerUser);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Thêm sản phẩm vào giỏ hàng thành công")
    void testAddToCartSuccess() {
        AddToCartRequest request = AddToCartRequest.builder()
                .variantId(10L)
                .quantity(2) // 2 <= 5
                .build();

        when(cartRepository.findByUser_UserId(1L)).thenReturn(Optional.of(sampleCart));
        when(variantRepository.findById(10L)).thenReturn(Optional.of(inStockVariant));
        when(cartItemRepository.findByCart_CartIdAndVariant_VariantId(50L, 10L)).thenReturn(Optional.empty());
        when(cartMapper.toCartResponse(any())).thenReturn(
                CartResponse.builder().cartId(50L).totalQuantity(2).totalAmount(new BigDecimal("300000")).build()
        );

        CartResponse response = cartService.addToCart(request);

        assertNotNull(response);
        assertEquals(2, response.getTotalQuantity());
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    @DisplayName("TC-04: Thêm vào giỏ thất bại khi số lượng yêu cầu vượt quá tồn kho hiện có")
    void testAddToCartExceedsStockThrowsBadRequest() {
        AddToCartRequest request = AddToCartRequest.builder()
                .variantId(10L)
                .quantity(10) // Tồn kho chỉ có 5, yêu cầu 10
                .build();

        when(cartRepository.findByUser_UserId(1L)).thenReturn(Optional.of(sampleCart));
        when(variantRepository.findById(10L)).thenReturn(Optional.of(inStockVariant));

        assertThrows(BadRequestException.class, () -> cartService.addToCart(request));
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-04: Cập nhật số lượng dòng giỏ hàng vượt quá tồn kho thất bại")
    void testUpdateCartItemExceedsStockThrowsBadRequest() {
        CartItem cartItem = CartItem.builder()
                .cartItemId(101L)
                .cart(sampleCart)
                .variant(inStockVariant)
                .quantity(2)
                .build();

        UpdateCartItemRequest request = UpdateCartItemRequest.builder()
                .quantity(8) // Tồn kho chỉ có 5, yêu cầu 8
                .build();

        when(cartRepository.findByUser_UserId(1L)).thenReturn(Optional.of(sampleCart));
        when(cartItemRepository.findById(101L)).thenReturn(Optional.of(cartItem));

        assertThrows(BadRequestException.class, () -> cartService.updateCartItem(101L, request));
    }

    @Test
    @DisplayName("Cập nhật số lượng <= 0 sẽ tự động xóa dòng hàng khỏi giỏ")
    void testUpdateCartItemZeroQuantityDeletesItem() {
        CartItem cartItem = CartItem.builder()
                .cartItemId(101L)
                .cart(sampleCart)
                .variant(inStockVariant)
                .quantity(2)
                .build();
        sampleCart.getItems().add(cartItem);

        UpdateCartItemRequest request = UpdateCartItemRequest.builder()
                .quantity(0)
                .build();

        when(cartRepository.findByUser_UserId(1L)).thenReturn(Optional.of(sampleCart));
        when(cartItemRepository.findById(101L)).thenReturn(Optional.of(cartItem));
        when(cartMapper.toCartResponse(any())).thenReturn(CartResponse.builder().items(List.of()).build());

        CartResponse response = cartService.updateCartItem(101L, request);

        assertNotNull(response);
        verify(cartItemRepository, times(1)).delete(cartItem);
    }

    @Test
    @DisplayName("Chặn sửa hoặc xóa dòng giỏ hàng của người dùng khác")
    void testCannotModifyOtherUsersCartItem() {
        Cart otherCart = Cart.builder().cartId(999L).build();
        CartItem foreignItem = CartItem.builder()
                .cartItemId(102L)
                .cart(otherCart)
                .variant(inStockVariant)
                .quantity(1)
                .build();

        when(cartRepository.findByUser_UserId(1L)).thenReturn(Optional.of(sampleCart));
        when(cartItemRepository.findById(102L)).thenReturn(Optional.of(foreignItem));

        UpdateCartItemRequest request = UpdateCartItemRequest.builder().quantity(3).build();

        assertThrows(ForbiddenException.class, () -> cartService.updateCartItem(102L, request));
    }
}
