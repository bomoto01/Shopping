package com.shopping;

import com.shopping.dto.request.product.CategoryRequest;
import com.shopping.dto.response.product.CategoryResponse;
import com.shopping.entity.Category;
import com.shopping.exception.BadRequestException;
import com.shopping.mapper.CategoryMapper;
import com.shopping.repository.CategoryRepository;
import com.shopping.repository.ProductRepository;
import com.shopping.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTests {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category parentCategory;
    private Category childCategory;

    @BeforeEach
    void setUp() {
        parentCategory = Category.builder()
                .categoryId(1L)
                .name("Thời Trang Nam")
                .slug("thoi-trang-nam")
                .isActive(true)
                .build();

        childCategory = Category.builder()
                .categoryId(2L)
                .parent(parentCategory)
                .name("Áo Thun")
                .slug("ao-thun")
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Tạo mới danh mục tự sinh slug tiếng Việt")
    void testCreateCategoryGeneratesSlug() {
        CategoryRequest request = CategoryRequest.builder()
                .name("Thời Trang Nam")
                .isActive(true)
                .build();

        when(categoryRepository.findBySlug("thoi-trang-nam")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(parentCategory);
        when(categoryMapper.toCategoryResponse(parentCategory)).thenReturn(
                CategoryResponse.builder().categoryId(1L).slug("thoi-trang-nam").name("Thời Trang Nam").build()
        );

        CategoryResponse response = categoryService.createCategory(request);

        assertNotNull(response);
        assertEquals("thoi-trang-nam", response.getSlug());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("Cập nhật danh mục: chặn đặt danh mục làm cha của chính nó")
    void testCannotSetSelfAsParent() {
        CategoryRequest request = CategoryRequest.builder()
                .name("Thời Trang Nam")
                .parentId(1L)
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parentCategory));

        assertThrows(BadRequestException.class, () -> categoryService.updateCategory(1L, request));
    }

    @Test
    @DisplayName("Xóa danh mục có chứa sản phẩm chuyển sang vô hiệu hóa thay vì xóa cứng")
    void testDeleteCategoryWithProductsSoftDeactivates() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parentCategory));
        when(productRepository.existsByCategory_CategoryId(1L)).thenReturn(true);

        categoryService.deleteCategory(1L);

        assertFalse(parentCategory.getIsActive());
        verify(categoryRepository, times(1)).save(parentCategory);
        verify(categoryRepository, never()).delete(any());
    }
}
