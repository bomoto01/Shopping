package com.shopping;

import com.shopping.dto.response.wishlist.WishlistResponse;
import com.shopping.entity.Product;
import com.shopping.entity.Role;
import com.shopping.entity.User;
import com.shopping.entity.Wishlist;
import com.shopping.enums.ProductStatus;
import com.shopping.enums.RoleType;
import com.shopping.enums.UserStatus;
import com.shopping.mapper.WishlistMapper;
import com.shopping.repository.ProductRepository;
import com.shopping.repository.UserRepository;
import com.shopping.repository.WishlistRepository;
import com.shopping.security.UserPrincipal;
import com.shopping.service.impl.WishlistServiceImpl;
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
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTests {

    @Mock
    private WishlistRepository wishlistRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private WishlistMapper wishlistMapper;

    @InjectMocks
    private WishlistServiceImpl wishlistService;

    private User currentUser;
    private Product activeProduct;
    private Wishlist wishlist;

    @BeforeEach
    void setUp() {
        Role customerRole = Role.builder().roleId(1).roleCode(RoleType.ROLE_CUSTOMER).build();
        currentUser = User.builder()
                .userId(1L)
                .email("customer@shopping.vn")
                .fullName("Khách hàng A")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(customerRole))
                .build();

        activeProduct = Product.builder()
                .productId(100L)
                .name("Áo Sơ Mi Lụa")
                .slug("ao-so-mi-lua")
                .basePrice(new BigDecimal("350000"))
                .status(ProductStatus.ACTIVE)
                .build();

        wishlist = Wishlist.builder()
                .wishlistId(1L)
                .user(currentUser)
                .product(activeProduct)
                .build();

        UserPrincipal principal = UserPrincipal.create(currentUser);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Thêm sản phẩm vào danh sách yêu thích thành công")
    void testAddToWishlistSuccess() {
        when(productRepository.findById(100L)).thenReturn(Optional.of(activeProduct));
        when(wishlistRepository.findByUser_UserIdAndProduct_ProductId(1L, 100L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));
        when(wishlistRepository.save(any(Wishlist.class))).thenReturn(wishlist);
        when(wishlistMapper.toResponse(wishlist)).thenReturn(
                WishlistResponse.builder().wishlistId(1L).productId(100L).productName("Áo Sơ Mi Lụa").build()
        );

        WishlistResponse response = wishlistService.addToWishlist(100L);

        assertNotNull(response);
        assertEquals(100L, response.getProductId());
        verify(wishlistRepository, times(1)).save(any(Wishlist.class));
    }

    @Test
    @DisplayName("Thêm sản phẩm đã có trong yêu thích trả về bản ghi hiện tại không tạo trùng")
    void testAddToWishlistAlreadyExists() {
        when(productRepository.findById(100L)).thenReturn(Optional.of(activeProduct));
        when(wishlistRepository.findByUser_UserIdAndProduct_ProductId(1L, 100L)).thenReturn(Optional.of(wishlist));
        when(wishlistMapper.toResponse(wishlist)).thenReturn(
                WishlistResponse.builder().wishlistId(1L).productId(100L).build()
        );

        WishlistResponse response = wishlistService.addToWishlist(100L);

        assertNotNull(response);
        verify(wishlistRepository, never()).save(any());
    }

    @Test
    @DisplayName("Kiểm tra sản phẩm có trong yêu thích hay không")
    void testIsWishlisted() {
        when(wishlistRepository.existsByUser_UserIdAndProduct_ProductId(1L, 100L)).thenReturn(true);
        when(wishlistRepository.existsByUser_UserIdAndProduct_ProductId(1L, 200L)).thenReturn(false);

        assertTrue(wishlistService.isWishlisted(100L));
        assertFalse(wishlistService.isWishlisted(200L));
    }
}
