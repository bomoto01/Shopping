package com.shopping.service.impl;

import com.shopping.dto.response.DashboardSummaryResponse;
import com.shopping.dto.response.OrderStatusCountResponse;
import com.shopping.dto.response.TopSellingProductResponse;
import com.shopping.enums.OrderStatus;
import com.shopping.repository.OrderItemRepository;
import com.shopping.repository.OrderRepository;
import com.shopping.repository.ProductRepository;
import com.shopping.repository.UserRepository;
import com.shopping.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary() {
        BigDecimal totalRevenue = orderRepository.getTotalRevenue();
        long totalOrders = orderRepository.count();
        long totalCustomers = userRepository.count();
        long totalProducts = productRepository.count();

        // Thống kê theo trạng thái
        List<Object[]> statusCountsRaw = orderRepository.countOrdersGroupByStatus();
        Map<OrderStatus, Long> countMap = new HashMap<>();
        for (Object[] row : statusCountsRaw) {
            OrderStatus status = (OrderStatus) row[0];
            Long count = (Long) row[1];
            countMap.put(status, count);
        }

        List<OrderStatusCountResponse> statusCounts = new ArrayList<>();
        for (OrderStatus status : OrderStatus.values()) {
            statusCounts.add(OrderStatusCountResponse.builder()
                    .status(status)
                    .count(countMap.getOrDefault(status, 0L))
                    .build());
        }

        List<TopSellingProductResponse> topProducts = getTopSellingProducts(5);

        return DashboardSummaryResponse.builder()
                .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .totalOrders(totalOrders)
                .totalCustomers(totalCustomers)
                .totalProducts(totalProducts)
                .orderStatusCounts(statusCounts)
                .topSellingProducts(topProducts)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopSellingProductResponse> getTopSellingProducts(int limit) {
        int safeLimit = (limit <= 0 || limit > 100) ? 5 : limit;
        List<Object[]> rawList = orderItemRepository.findTopSellingProducts(PageRequest.of(0, safeLimit));

        List<TopSellingProductResponse> responses = new ArrayList<>();
        for (Object[] row : rawList) {
            Long productId = (Long) row[0];
            String productName = (String) row[1];
            Long totalQty = ((Number) row[2]).longValue();
            BigDecimal totalRev = (BigDecimal) row[3];

            responses.add(TopSellingProductResponse.builder()
                    .productId(productId)
                    .productName(productName)
                    .totalQuantitySold(totalQty)
                    .totalRevenue(totalRev)
                    .build());
        }
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getRevenueReport(Instant startDate, Instant endDate) {
        BigDecimal revenue = orderRepository.getRevenueBetween(startDate, endDate);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }
}
