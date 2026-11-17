package com.shopping.service;

import com.shopping.dto.response.DashboardSummaryResponse;
import com.shopping.dto.response.TopSellingProductResponse;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface ReportService {

    DashboardSummaryResponse getDashboardSummary();

    List<TopSellingProductResponse> getTopSellingProducts(int limit);

    BigDecimal getRevenueReport(Instant startDate, Instant endDate);
}
