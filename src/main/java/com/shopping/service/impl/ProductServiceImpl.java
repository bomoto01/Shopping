package com.shopping.service.impl;

import com.shopping.apiResponse.PageResponse;
import com.shopping.dto.request.product.ProductCreateRequest;
import com.shopping.dto.request.product.ProductUpdateRequest;
import com.shopping.dto.response.product.ProductDetailResponse;
import com.shopping.dto.response.product.ProductResponse;
import com.shopping.entity.Category;
import com.shopping.entity.Product;
import com.shopping.entity.ProductImage;
import com.shopping.entity.ProductVariant;
import com.shopping.enums.ProductStatus;
import com.shopping.exception.DuplicateResourceException;
import com.shopping.exception.ResourceNotFoundException;
import com.shopping.mapper.ProductImageMapper;
import com.shopping.mapper.ProductMapper;
import com.shopping.mapper.ProductVariantMapper;
import com.shopping.repository.CategoryRepository;
import com.shopping.repository.ProductRepository;
import com.shopping.service.interfaces.ProductService;
import com.shopping.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final ProductVariantMapper variantMapper;
    private final ProductImageMapper imageMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getPublicProducts(
            String keyword, Long categoryId, String brand,
            BigDecimal minPrice, BigDecimal maxPrice,
            String size, String color, Pageable pageable
    ) {
        Page<Product> productPage = productRepository.filterProducts(
                keyword, categoryId, brand, minPrice, maxPrice, size, color, ProductStatus.ACTIVE, pageable
        );
        return PageResponse.from(productPage.map(productMapper::toProductResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponse getPublicProductById(Long id) {
        Product product = productRepository.findByProductIdAndStatus(id, ProductStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm", "id", id));
        return productMapper.toProductDetailResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponse getPublicProductBySlug(String slug) {
        Product product = productRepository.findBySlugAndStatus(slug, ProductStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm", "slug", slug));
        return productMapper.toProductDetailResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getRelatedProducts(Long productId, int limit) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm", "id", productId));

        List<Product> related = productRepository.findByCategory_CategoryIdAndProductIdNotAndStatus(
                product.getCategory().getCategoryId(), productId, ProductStatus.ACTIVE, PageRequest.of(0, limit)
        );
        return related.stream().map(productMapper::toProductResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getAdminProducts(
            String keyword, Long categoryId, String brand,
            ProductStatus status, Pageable pageable
    ) {
        Page<Product> productPage = productRepository.filterProducts(
                keyword, categoryId, brand, null, null, null, null, status, pageable
        );
        return PageResponse.from(productPage.map(productMapper::toProductResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponse getAdminProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm", "id", id));
        return productMapper.toProductDetailResponse(product);
    }

    @Override
    @Transactional
    public ProductDetailResponse createProduct(ProductCreateRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", "id", request.getCategoryId()));

        String baseSlug = SlugUtil.toSlug(request.getName());
        String slug = generateUniqueSlug(baseSlug);

        Product product = Product.builder()
                .category(category)
                .name(request.getName().trim())
                .slug(slug)
                .description(request.getDescription())
                .brand(request.getBrand() != null ? request.getBrand().trim() : null)
                .basePrice(request.getBasePrice())
                .status(ProductStatus.ACTIVE)
                .images(new ArrayList<>())
                .variants(new ArrayList<>())
                .build();

        if (request.getVariants() != null) {
            for (var variantReq : request.getVariants()) {
                ProductVariant variant = variantMapper.toProductVariant(variantReq);
                variant.setProduct(product);
                product.getVariants().add(variant);
            }
        }

        if (request.getImages() != null) {
            for (var imageReq : request.getImages()) {
                ProductImage image = imageMapper.toProductImage(imageReq);
                image.setProduct(product);
                product.getImages().add(image);
            }
        }

        Product saved = productRepository.save(product);
        log.info("Tạo sản phẩm mới thành công: productId={}, slug={}", saved.getProductId(), slug);
        return productMapper.toProductDetailResponse(saved);
    }

    @Override
    @Transactional
    public ProductDetailResponse updateProduct(Long id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm", "id", id));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", "id", request.getCategoryId()));

        if (!product.getName().equalsIgnoreCase(request.getName().trim())) {
            String baseSlug = SlugUtil.toSlug(request.getName());
            product.setSlug(generateUniqueSlug(baseSlug, id));
            product.setName(request.getName().trim());
        }

        product.setCategory(category);
        product.setDescription(request.getDescription());
        product.setBrand(request.getBrand() != null ? request.getBrand().trim() : null);
        product.setBasePrice(request.getBasePrice());

        if (request.getStatus() != null) {
            product.setStatus(request.getStatus());
        }

        Product updated = productRepository.save(product);
        log.info("Cập nhật sản phẩm thành công: productId={}", id);
        return productMapper.toProductDetailResponse(updated);
    }

    @Override
    @Transactional
    public ProductDetailResponse updateProductStatus(Long id, ProductStatus status) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm", "id", id));
        product.setStatus(status);
        Product saved = productRepository.save(product);
        log.info("Cập nhật trạng thái sản phẩm: productId={}, status={}", id, status);
        return productMapper.toProductDetailResponse(saved);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm", "id", id));

        product.setStatus(ProductStatus.INACTIVE);
        productRepository.save(product);
        log.info("Đã chuyển trạng thái sản phẩm sang INACTIVE (soft-delete): productId={}", id);
    }

    private String generateUniqueSlug(String baseSlug) {
        return generateUniqueSlug(baseSlug, null);
    }

    private String generateUniqueSlug(String baseSlug, Long excludeId) {
        String slug = baseSlug;
        int counter = 1;
        while (true) {
            var existing = productRepository.findBySlug(slug);
            if (existing.isEmpty() || (excludeId != null && existing.get().getProductId().equals(excludeId))) {
                return slug;
            }
            slug = baseSlug + "-" + counter++;
        }
    }
}
