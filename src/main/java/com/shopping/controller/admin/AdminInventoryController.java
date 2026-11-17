package com.shopping.controller.admin;

import com.shopping.apiResponse.ApiResponse;
import com.shopping.apiResponse.PageResponse;
import com.shopping.constants.PaginationConstants;
import com.shopping.dto.request.inventory.InventoryAdjustmentRequest;
import com.shopping.dto.response.inventory.InventoryTransactionResponse;
import com.shopping.dto.response.inventory.StockSummaryResponse;
import com.shopping.enums.InventoryReferenceType;
import com.shopping.service.interfaces.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/inventory")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
@Tag(name = "Admin/Staff - Inventory Management", description = "Các API quản lý tồn kho, nhập/xuất kho và truy vết nhật ký giao dịch kho")
public class AdminInventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/adjust/{variantId}")
    @Operation(summary = "Điều chỉnh số lượng tồn kho của SKU", description = "Tăng/giảm tồn kho kèm lý do, loại chứng từ và tự động lưu vết nhật ký.")
    public ResponseEntity<ApiResponse<InventoryTransactionResponse>> adjustStock(
            @PathVariable("variantId") Long variantId,
            @Valid @RequestBody InventoryAdjustmentRequest request
    ) {
        InventoryTransactionResponse response = inventoryService.adjustStock(variantId, request);
        return ResponseEntity.ok(ApiResponse.success(200, "Điều chỉnh tồn kho thành công", response));
    }

    @GetMapping("/transactions")
    @Operation(summary = "Xem lịch sử nhật ký giao dịch kho", description = "Tra cứu danh sách các giao dịch nhập/xuất/điều chỉnh tồn kho.")
    public ResponseEntity<ApiResponse<PageResponse<InventoryTransactionResponse>>> getTransactions(
            @RequestParam(name = "variantId", required = false) Long variantId,
            @RequestParam(name = "referenceType", required = false) InventoryReferenceType referenceType,
            @RequestParam(name = "page", defaultValue = PaginationConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(name = "size", defaultValue = PaginationConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(name = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(name = "sortDir", defaultValue = PaginationConstants.DEFAULT_SORT_DIRECTION) String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, Math.min(size, PaginationConstants.MAX_PAGE_SIZE), sort);

        PageResponse<InventoryTransactionResponse> response = inventoryService.getTransactions(variantId, referenceType, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/stock")
    @Operation(summary = "Báo cáo tồn kho & Cảnh báo sắp hết hàng", description = "Xem danh sách tồn kho các biến thể SKU, lọc theo từ khóa hoặc cảnh báo dưới ngưỡng.")
    public ResponseEntity<ApiResponse<PageResponse<StockSummaryResponse>>> getStockReport(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "lowStockOnly", defaultValue = "false") Boolean lowStockOnly,
            @RequestParam(name = "threshold", defaultValue = "10") int threshold,
            @RequestParam(name = "page", defaultValue = PaginationConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(name = "size", defaultValue = PaginationConstants.DEFAULT_PAGE_SIZE) int size
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, PaginationConstants.MAX_PAGE_SIZE), Sort.by("stockQuantity").ascending());
        PageResponse<StockSummaryResponse> response = inventoryService.getStockReport(keyword, lowStockOnly, threshold, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
