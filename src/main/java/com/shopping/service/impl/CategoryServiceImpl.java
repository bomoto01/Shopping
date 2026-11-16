package com.shopping.service.impl;

import com.shopping.dto.request.product.CategoryRequest;
import com.shopping.dto.response.product.CategoryResponse;
import com.shopping.entity.Category;
import com.shopping.exception.BadRequestException;
import com.shopping.exception.DuplicateResourceException;
import com.shopping.exception.ResourceNotFoundException;
import com.shopping.mapper.CategoryMapper;
import com.shopping.repository.CategoryRepository;
import com.shopping.repository.ProductRepository;
import com.shopping.service.interfaces.CategoryService;
import com.shopping.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getActiveCategoriesTree() {
        List<Category> rootCategories = categoryRepository.findByParentIsNullAndIsActiveTrue();
        return categoryMapper.toCategoryResponseList(rootCategories);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllActiveCategories() {
        List<Category> categories = categoryRepository.findByIsActiveTrue();
        return categoryMapper.toCategoryResponseList(categories);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", "id", id));
        return categoryMapper.toCategoryResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        String baseSlug = SlugUtil.toSlug(request.getName());
        String slug = generateUniqueSlug(baseSlug);

        Category parent = null;
        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Danh mục cha", "id", request.getParentId()));
        }

        Category category = Category.builder()
                .name(request.getName().trim())
                .slug(slug)
                .parent(parent)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        Category savedCategory = categoryRepository.save(category);
        log.info("Tạo danh mục mới thành công: categoryId={}, slug={}", savedCategory.getCategoryId(), slug);
        return categoryMapper.toCategoryResponse(savedCategory);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", "id", id));

        if (!category.getName().equalsIgnoreCase(request.getName().trim())) {
            String baseSlug = SlugUtil.toSlug(request.getName());
            category.setSlug(generateUniqueSlug(baseSlug, id));
            category.setName(request.getName().trim());
        }

        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new BadRequestException("Danh mục không thể làm danh mục cha của chính nó");
            }
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Danh mục cha", "id", request.getParentId()));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        }

        Category updated = categoryRepository.save(category);
        log.info("Cập nhật danh mục thành công: categoryId={}", id);
        return categoryMapper.toCategoryResponse(updated);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategoryStatus(Long id, boolean isActive) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", "id", id));
        category.setIsActive(isActive);
        Category saved = categoryRepository.save(category);
        log.info("Cập nhật trạng thái danh mục: categoryId={}, isActive={}", id, isActive);
        return categoryMapper.toCategoryResponse(saved);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", "id", id));

        if (productRepository.existsByCategory_CategoryId(id)) {
            log.warn("Danh mục đang có sản phẩm liên kết, chuyển trạng thái sang không hoạt động thay vì xóa cứng: categoryId={}", id);
            category.setIsActive(false);
            categoryRepository.save(category);
            return;
        }

        if (categoryRepository.existsByParent_CategoryId(id)) {
            throw new BadRequestException("Không thể xóa danh mục đang có danh mục con");
        }

        categoryRepository.delete(category);
        log.info("Xóa danh mục thành công: categoryId={}", id);
    }

    private String generateUniqueSlug(String baseSlug) {
        return generateUniqueSlug(baseSlug, null);
    }

    private String generateUniqueSlug(String baseSlug, Long excludeId) {
        String slug = baseSlug;
        int counter = 1;
        while (true) {
            var existing = categoryRepository.findBySlug(slug);
            if (existing.isEmpty() || (excludeId != null && existing.get().getCategoryId().equals(excludeId))) {
                return slug;
            }
            slug = baseSlug + "-" + counter++;
        }
    }
}
