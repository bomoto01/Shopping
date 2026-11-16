package com.shopping.service.impl;

import com.shopping.dto.request.product.ProductImageRequest;
import com.shopping.dto.response.product.ProductImageResponse;
import com.shopping.entity.Product;
import com.shopping.entity.ProductImage;
import com.shopping.exception.BadRequestException;
import com.shopping.exception.ResourceNotFoundException;
import com.shopping.mapper.ProductImageMapper;
import com.shopping.repository.ProductImageRepository;
import com.shopping.repository.ProductRepository;
import com.shopping.service.interfaces.ProductImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductRepository productRepository;
    private final ProductImageRepository imageRepository;
    private final ProductImageMapper imageMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ProductImageResponse> getImagesByProductId(Long productId) {
        List<ProductImage> images = imageRepository.findByProduct_ProductIdOrderBySortOrderAscImageIdAsc(productId);
        return imageMapper.toProductImageResponseList(images);
    }

    @Override
    @Transactional
    public ProductImageResponse addImage(Long productId, ProductImageRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm", "id", productId));

        List<ProductImage> existing = imageRepository.findByProduct_ProductIdOrderBySortOrderAscImageIdAsc(productId);
        boolean shouldBePrimary = Boolean.TRUE.equals(request.getIsPrimary()) || existing.isEmpty();

        if (shouldBePrimary && !existing.isEmpty()) {
            imageRepository.resetPrimaryForProduct(productId);
        }

        ProductImage image = imageMapper.toProductImage(request);
        image.setProduct(product);
        image.setIsPrimary(shouldBePrimary);

        ProductImage saved = imageRepository.save(image);
        log.info("Thêm ảnh cho sản phẩm thành công: imageId={}, productId={}", saved.getImageId(), productId);
        return imageMapper.toProductImageResponse(saved);
    }

    @Override
    @Transactional
    public ProductImageResponse setPrimaryImage(Long productId, Long imageId) {
        ProductImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Ảnh sản phẩm", "id", imageId));

        if (!image.getProduct().getProductId().equals(productId)) {
            throw new BadRequestException("Ảnh không thuộc về sản phẩm có id: " + productId);
        }

        imageRepository.resetPrimaryForProduct(productId);
        image.setIsPrimary(true);
        ProductImage saved = imageRepository.save(image);

        log.info("Đặt ảnh chính thành công: imageId={}, productId={}", imageId, productId);
        return imageMapper.toProductImageResponse(saved);
    }

    @Override
    @Transactional
    public void deleteImage(Long productId, Long imageId) {
        ProductImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Ảnh sản phẩm", "id", imageId));

        if (!image.getProduct().getProductId().equals(productId)) {
            throw new BadRequestException("Ảnh không thuộc về sản phẩm có id: " + productId);
        }

        boolean wasPrimary = Boolean.TRUE.equals(image.getIsPrimary());
        imageRepository.delete(image);
        log.info("Xóa ảnh sản phẩm thành công: imageId={}", imageId);

        if (wasPrimary) {
            List<ProductImage> remaining = imageRepository.findByProduct_ProductIdOrderBySortOrderAscImageIdAsc(productId);
            if (!remaining.isEmpty()) {
                ProductImage nextPrimary = remaining.get(0);
                nextPrimary.setIsPrimary(true);
                imageRepository.save(nextPrimary);
                log.info("Tự động gán ảnh chính mới: imageId={}", nextPrimary.getImageId());
            }
        }
    }
}
