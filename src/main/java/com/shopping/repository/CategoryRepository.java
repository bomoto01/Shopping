package com.shopping.repository;

import com.shopping.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByIsActiveTrue();

    List<Category> findByParentIsNullAndIsActiveTrue();

    Optional<Category> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsByParent_CategoryId(Long parentId);
}
