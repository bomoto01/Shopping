package com.shopping.controller;

import com.shopping.apiResponse.ApiResponse;
import com.shopping.dto.response.DashboardSummaryResponse;
import com.shopping.dto.response.TopSellingProductResponse;
import com.shopping.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/reports")
@RequiredArgsConstructor
@Tag(name = "Report Admin", description = "APIs báo cáo & thống kê doanh thu cho Quản trị viên")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
public class AdminReportController {

    private final ReportService reportService;

    @GetMapping("/dashboard")
    @Operation(summary = "Xem tổng quan Dashboard (Tổng doanh thu, tổng đơn hàng, user, top sản phẩm bán chạy)")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getDashboardSummary() {
        DashboardSummaryResponse response = reportService.getDashboardSummary();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Lấy dữ liệu Dashboard thành công", response));
    }

    @GetMapping("/top-selling")
    @Operation(summary = "Xem top sản phẩm bán chạy nhất hệ thống")
    public ResponseEntity<ApiResponse<List<TopSellingProductResponse>>> getTopSellingProducts(
            @RequestParam(defaultValue = "5") int limit) {
        List<TopSellingProductResponse> response = reportService.getTopSellingProducts(limit);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Lấy top sản phẩm bán chạy thành công", response));
    }

    @GetMapping("/revenue")
    @Operation(summary = "Thống kê doanh thu theo khoảng thời gian (ISO-8601 Instant)")
    public ResponseEntity<ApiResponse<BigDecimal>> getRevenueReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate) {
        BigDecimal revenue = reportService.getRevenueReport(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Lấy báo cáo doanh thu thành công", revenue));
    }
}
