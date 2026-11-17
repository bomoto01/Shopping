package com.shopping;

import com.shopping.dto.request.inventory.InventoryAdjustmentRequest;
import com.shopping.dto.response.inventory.InventoryTransactionResponse;
import com.shopping.entity.InventoryTransaction;
import com.shopping.entity.Product;
import com.shopping.entity.ProductVariant;
import com.shopping.enums.InventoryReferenceType;
import com.shopping.exception.BadRequestException;
import com.shopping.exception.ResourceNotFoundException;
import com.shopping.mapper.InventoryTransactionMapper;
import com.shopping.repository.InventoryTransactionRepository;
import com.shopping.repository.ProductVariantRepository;
import com.shopping.repository.UserRepository;
import com.shopping.service.impl.InventoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTests {

    @Mock
    private ProductVariantRepository variantRepository;
    @Mock
    private InventoryTransactionRepository transactionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private InventoryTransactionMapper transactionMapper;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private Product product;
    private ProductVariant variant;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .productId(100L)
                .name("Áo Polo Nam")
                .build();

        variant = ProductVariant.builder()
                .variantId(10L)
                .product(product)
                .sku("POLO-WHT-M")
                .size("M")
                .color("Trắng")
                .price(new BigDecimal("250000"))
                .stockQuantity(20)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Điều chỉnh tăng tồn kho (nhập hàng) và ghi nhật ký giao dịch kho")
    void testAdjustStockIncreaseSuccess() {
        InventoryAdjustmentRequest request = InventoryAdjustmentRequest.builder()
                .quantityChange(15)
                .reason("Nhập lô hàng mới từ xưởng")
                .referenceType(InventoryReferenceType.PURCHASE_ORDER)
                .referenceId(1001L)
                .build();

        when(variantRepository.findById(10L)).thenReturn(Optional.of(variant));
        when(variantRepository.save(any(ProductVariant.class))).thenReturn(variant);

        InventoryTransaction savedTrans = InventoryTransaction.builder()
                .transactionId(1L)
                .variant(variant)
                .quantityChange(15)
                .reason("Nhập lô hàng mới từ xưởng")
                .referenceType(InventoryReferenceType.PURCHASE_ORDER)
                .referenceId(1001L)
                .build();

        when(transactionRepository.save(any(InventoryTransaction.class))).thenReturn(savedTrans);
        when(transactionMapper.toResponse(savedTrans)).thenReturn(
                InventoryTransactionResponse.builder()
                        .transactionId(1L)
                        .variantId(10L)
                        .sku("POLO-WHT-M")
                        .quantityChange(15)
                        .build()
        );

        InventoryTransactionResponse response = inventoryService.adjustStock(10L, request);

        assertNotNull(response);
        assertEquals(35, variant.getStockQuantity());
        assertEquals(20, response.getPreviousStock());
        assertEquals(35, response.getCurrentStock());
        verify(variantRepository, times(1)).save(variant);
        verify(transactionRepository, times(1)).save(any(InventoryTransaction.class));
    }

    @Test
    @DisplayName("Chặn điều chỉnh tồn kho khi số lượng giảm làm tồn kho bị âm")
    void testAdjustStockNegativeExceedingThrowsBadRequest() {
        InventoryAdjustmentRequest request = InventoryAdjustmentRequest.builder()
                .quantityChange(-25) // tồn kho hiện tại là 20, giảm 25 sẽ thành -5
                .reason("Xuất hàng kiểm kê")
                .referenceType(InventoryReferenceType.MANUAL_ADJUSTMENT)
                .build();

        when(variantRepository.findById(10L)).thenReturn(Optional.of(variant));

        assertThrows(BadRequestException.class, () -> inventoryService.adjustStock(10L, request));
        verify(variantRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Chặn điều chỉnh tồn kho khi số lượng thay đổi bằng 0")
    void testAdjustStockZeroQuantityThrowsBadRequest() {
        InventoryAdjustmentRequest request = InventoryAdjustmentRequest.builder()
                .quantityChange(0)
                .reason("Không thay đổi")
                .referenceType(InventoryReferenceType.MANUAL_ADJUSTMENT)
                .build();

        assertThrows(BadRequestException.class, () -> inventoryService.adjustStock(10L, request));
        verify(variantRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Ngoại lệ khi điều chỉnh tồn kho cho biến thể không tồn tại")
    void testAdjustStockVariantNotFound() {
        InventoryAdjustmentRequest request = InventoryAdjustmentRequest.builder()
                .quantityChange(10)
                .reason("Nhập hàng")
                .referenceType(InventoryReferenceType.PURCHASE_ORDER)
                .build();

        when(variantRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> inventoryService.adjustStock(999L, request));
    }
}
