package com.shopping;

import com.shopping.dto.request.product.ProductVariantRequest;
import com.shopping.dto.response.product.ProductVariantResponse;
import com.shopping.entity.Product;
import com.shopping.entity.ProductVariant;
import com.shopping.exception.DuplicateResourceException;
import com.shopping.mapper.ProductVariantMapper;
import com.shopping.repository.ProductRepository;
import com.shopping.repository.ProductVariantRepository;
import com.shopping.service.impl.ProductVariantServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductVariantServiceTests {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductVariantRepository variantRepository;
    @Mock
    private ProductVariantMapper variantMapper;

    @InjectMocks
    private ProductVariantServiceImpl variantService;

    private Product product;
    private ProductVariant variant;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .productId(100L)
                .name("Áo Thun")
                .build();

        variant = ProductVariant.builder()
                .variantId(1L)
                .product(product)
                .sku("TSHIRT-BLK-L")
                .size("L")
                .color("Đen")
                .price(new BigDecimal("199000"))
                .stockQuantity(50)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Thêm biến thể thất bại khi mã SKU đã tồn tại")
    void testCreateVariantDuplicateSku() {
        ProductVariantRequest request = ProductVariantRequest.builder()
                .sku("TSHIRT-BLK-L")
                .size("L")
                .color("Đen")
                .price(new BigDecimal("199000"))
                .stockQuantity(50)
                .build();

        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(variantRepository.existsBySku("TSHIRT-BLK-L")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> variantService.createVariant(100L, request));
        verify(variantRepository, never()).save(any());
    }

    @Test
    @DisplayName("Thêm biến thể SKU thành công")
    void testCreateVariantSuccess() {
        ProductVariantRequest request = ProductVariantRequest.builder()
                .sku("TSHIRT-BLK-L")
                .size("L")
                .color("Đen")
                .price(new BigDecimal("199000"))
                .stockQuantity(50)
                .build();

        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(variantRepository.existsBySku("TSHIRT-BLK-L")).thenReturn(false);
        when(variantMapper.toProductVariant(request)).thenReturn(variant);
        when(variantRepository.save(any(ProductVariant.class))).thenReturn(variant);
        when(variantMapper.toProductVariantResponse(variant)).thenReturn(
                ProductVariantResponse.builder().variantId(1L).sku("TSHIRT-BLK-L").build()
        );

        ProductVariantResponse response = variantService.createVariant(100L, request);

        assertNotNull(response);
        assertEquals("TSHIRT-BLK-L", response.getSku());
        verify(variantRepository, times(1)).save(any(ProductVariant.class));
    }
}
