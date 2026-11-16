package com.shopping.service.impl;

import com.shopping.dto.request.product.ProductVariantRequest;
import com.shopping.dto.response.product.ProductVariantResponse;
import com.shopping.entity.Product;
import com.shopping.entity.ProductVariant;
import com.shopping.exception.BadRequestException;
import com.shopping.exception.DuplicateResourceException;
import com.shopping.exception.ResourceNotFoundException;
import com.shopping.mapper.ProductVariantMapper;
import com.shopping.repository.ProductRepository;
import com.shopping.repository.ProductVariantRepository;
import com.shopping.service.interfaces.ProductVariantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductVariantServiceImpl implements ProductVariantService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductVariantMapper variantMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ProductVariantResponse> getVariantsByProductId(Long productId) {
        List<ProductVariant> variants = variantRepository.findByProduct_ProductId(productId);
        return variantMapper.toProductVariantResponseList(variants);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductVariantResponse getVariantById(Long variantId) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Biến thể sản phẩm", "id", variantId));
        return variantMapper.toProductVariantResponse(variant);
    }

    @Override
    @Transactional
    public ProductVariantResponse createVariant(Long productId, ProductVariantRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm", "id", productId));

        String cleanSku = request.getSku().trim().toUpperCase();
        if (variantRepository.existsBySku(cleanSku)) {
            throw new DuplicateResourceException("Mã SKU '" + cleanSku + "' đã tồn tại trong hệ thống");
        }

        ProductVariant variant = variantMapper.toProductVariant(request);
        variant.setProduct(product);
        variant.setSku(cleanSku);

        ProductVariant saved = variantRepository.save(variant);
        log.info("Tạo biến thể mới thành công: variantId={}, sku={}, productId={}", saved.getVariantId(), cleanSku, productId);
        return variantMapper.toProductVariantResponse(saved);
    }

    @Override
    @Transactional
    public ProductVariantResponse updateVariant(Long productId, Long variantId, ProductVariantRequest request) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Biến thể sản phẩm", "id", variantId));

        if (!variant.getProduct().getProductId().equals(productId)) {
            throw new BadRequestException("Biến thể không thuộc về sản phẩm có id: " + productId);
        }

        String cleanSku = request.getSku().trim().toUpperCase();
        if (!cleanSku.equalsIgnoreCase(variant.getSku()) && variantRepository.existsBySku(cleanSku)) {
            throw new DuplicateResourceException("Mã SKU '" + cleanSku + "' đã tồn tại trong hệ thống");
        }

        variantMapper.updateVariantFromRequest(request, variant);
        variant.setSku(cleanSku);

        ProductVariant updated = variantRepository.save(variant);
        log.info("Cập nhật biến thể thành công: variantId={}, sku={}", variantId, cleanSku);
        return variantMapper.toProductVariantResponse(updated);
    }

    @Override
    @Transactional
    public ProductVariantResponse updateVariantStatus(Long productId, Long variantId, boolean isActive) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Biến thể sản phẩm", "id", variantId));

        if (!variant.getProduct().getProductId().equals(productId)) {
            throw new BadRequestException("Biến thể không thuộc về sản phẩm có id: " + productId);
        }

        variant.setIsActive(isActive);
        ProductVariant saved = variantRepository.save(variant);
        log.info("Cập nhật trạng thái biến thể: variantId={}, isActive={}", variantId, isActive);
        return variantMapper.toProductVariantResponse(saved);
    }

    @Override
    @Transactional
    public void deleteVariant(Long productId, Long variantId) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Biến thể sản phẩm", "id", variantId));

        if (!variant.getProduct().getProductId().equals(productId)) {
            throw new BadRequestException("Biến thể không thuộc về sản phẩm có id: " + productId);
        }

        variant.setIsActive(false);
        variantRepository.save(variant);
        log.info("Đã chuyển trạng thái ngừng hoạt động cho biến thể: variantId={}", variantId);
    }
}
