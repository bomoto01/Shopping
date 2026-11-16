package com.shopping.dto.response.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

    private Long categoryId;
    private Long parentId;
    private String parentName;
    private String name;
    private String slug;
    private Boolean isActive;
    private List<CategoryResponse> subCategories;
}
