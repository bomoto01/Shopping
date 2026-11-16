package com.shopping.service.interfaces;

import com.shopping.dto.request.product.CategoryRequest;
import com.shopping.dto.response.product.CategoryResponse;

import java.util.List;

public interface CategoryService {

    List<CategoryResponse> getActiveCategoriesTree();

    List<CategoryResponse> getAllActiveCategories();

    CategoryResponse getCategoryById(Long id);

    CategoryResponse createCategory(CategoryRequest request);

    CategoryResponse updateCategory(Long id, CategoryRequest request);

    CategoryResponse updateCategoryStatus(Long id, boolean isActive);

    void deleteCategory(Long id);
}
