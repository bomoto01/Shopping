package com.shopping.service.impl;

import com.shopping.apiResponse.PageResponse;
import com.shopping.dto.request.inventory.InventoryAdjustmentRequest;
import com.shopping.dto.response.inventory.InventoryTransactionResponse;
import com.shopping.dto.response.inventory.StockSummaryResponse;
import com.shopping.entity.InventoryTransaction;
import com.shopping.entity.ProductVariant;
import com.shopping.entity.User;
import com.shopping.enums.InventoryReferenceType;
import com.shopping.exception.BadRequestException;
import com.shopping.exception.ResourceNotFoundException;
import com.shopping.mapper.InventoryTransactionMapper;
import com.shopping.repository.InventoryTransactionRepository;
import com.shopping.repository.ProductVariantRepository;
import com.shopping.repository.UserRepository;
import com.shopping.service.interfaces.InventoryService;
import com.shopping.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final ProductVariantRepository variantRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final InventoryTransactionMapper transactionMapper;

    @Override
    @Transactional
    public InventoryTransactionResponse adjustStock(Long variantId, InventoryAdjustmentRequest request) {
        if (request.getQuantityChange() == null || request.getQuantityChange() == 0) {
            throw new BadRequestException("Số lượng thay đổi tồn kho phải khác 0");
        }

        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Biến thể sản phẩm", "id", variantId));

        int prevStock = variant.getStockQuantity() != null ? variant.getStockQuantity() : 0;
        int newStock = prevStock + request.getQuantityChange();

        if (newStock < 0) {
            throw new BadRequestException(String.format(
                    "Số lượng tồn kho không thể âm. Hiện tại còn %d sản phẩm, không thể điều chỉnh giảm %d",
                    prevStock, Math.abs(request.getQuantityChange())
            ));
        }

        variant.setStockQuantity(newStock);
        variantRepository.save(variant);

        User currentUser = null;
        var currentUserIdOpt = SecurityUtil.getCurrentUserPrincipal();
        if (currentUserIdOpt.isPresent()) {
            currentUser = userRepository.findById(currentUserIdOpt.get().getUserId()).orElse(null);
        }

        InventoryTransaction transaction = InventoryTransaction.builder()
                .variant(variant)
                .quantityChange(request.getQuantityChange())
                .reason(request.getReason().trim())
                .referenceType(request.getReferenceType())
                .referenceId(request.getReferenceId())
                .createdBy(currentUser)
                .build();

        InventoryTransaction savedTransaction = transactionRepository.save(transaction);
        log.info("Điều chỉnh tồn kho thành công: variantId={}, sku={}, thay đổi={}, tồn cũ={}, tồn mới={}",
                variantId, variant.getSku(), request.getQuantityChange(), prevStock, newStock);

        InventoryTransactionResponse response = transactionMapper.toResponse(savedTransaction);
        response.setPreviousStock(prevStock);
        response.setCurrentStock(newStock);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InventoryTransactionResponse> getTransactions(
            Long variantId, InventoryReferenceType referenceType, Pageable pageable
    ) {
        Page<InventoryTransaction> page = transactionRepository.findFilteredTransactions(variantId, referenceType, pageable);
        return PageResponse.from(page.map(transactionMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StockSummaryResponse> getStockReport(
            String keyword, Boolean lowStockOnly, int lowStockThreshold, Pageable pageable
    ) {
        boolean filterLowStock = Boolean.TRUE.equals(lowStockOnly);
        Page<ProductVariant> page = variantRepository.findStockReport(keyword, filterLowStock, lowStockThreshold, pageable);
        return PageResponse.from(page.map(transactionMapper::toStockSummaryResponse));
    }
}
