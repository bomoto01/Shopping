package com.shopping;

import com.shopping.dto.request.product.ProductCreateRequest;
import com.shopping.dto.response.product.ProductDetailResponse;
import com.shopping.entity.Category;
import com.shopping.entity.Product;
import com.shopping.enums.ProductStatus;
import com.shopping.exception.ResourceNotFoundException;
import com.shopping.mapper.ProductImageMapper;
import com.shopping.mapper.ProductMapper;
import com.shopping.mapper.ProductVariantMapper;
import com.shopping.repository.CategoryRepository;
import com.shopping.repository.ProductRepository;
import com.shopping.service.impl.ProductServiceImpl;
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
class ProductServiceTests {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ProductMapper productMapper;
    @Mock
    private ProductVariantMapper variantMapper;
    @Mock
    private ProductImageMapper imageMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private Category category;
    private Product product;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .categoryId(1L)
                .name("Áo Thun")
                .slug("ao-thun")
                .isActive(true)
                .build();

        product = Product.builder()
                .productId(100L)
                .category(category)
                .name("Áo Thun Cổ Tròn Cotton")
                .slug("ao-thun-co-tron-cotton")
                .brand("Coolmate")
                .basePrice(new BigDecimal("199000"))
                .status(ProductStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("Tạo sản phẩm mới tự động sinh slug hợp lệ")
    void testCreateProductSuccess() {
        ProductCreateRequest request = ProductCreateRequest.builder()
                .categoryId(1L)
                .name("Áo Thun Cổ Tròn Cotton")
                .brand("Coolmate")
                .basePrice(new BigDecimal("199000"))
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.findBySlug("ao-thun-co-tron-cotton")).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toProductDetailResponse(product)).thenReturn(
                ProductDetailResponse.builder().productId(100L).slug("ao-thun-co-tron-cotton").build()
        );

        ProductDetailResponse response = productService.createProduct(request);

        assertNotNull(response);
        assertEquals("ao-thun-co-tron-cotton", response.getSlug());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Lấy chi tiết sản phẩm công khai: ném ngoại lệ khi sản phẩm không tồn tại hoặc ngừng bán")
    void testGetPublicProductNotFound() {
        when(productRepository.findByProductIdAndStatus(999L, ProductStatus.ACTIVE)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getPublicProductById(999L));
    }
}
