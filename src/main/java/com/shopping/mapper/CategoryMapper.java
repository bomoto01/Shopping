package com.shopping.mapper;

import com.shopping.dto.response.product.CategoryResponse;
import com.shopping.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "parentId", source = "parent.categoryId")
    @Mapping(target = "parentName", source = "parent.name")
    @Mapping(target = "subCategories", source = "subCategories")
    CategoryResponse toCategoryResponse(Category category);

    List<CategoryResponse> toCategoryResponseList(List<Category> categories);
}
