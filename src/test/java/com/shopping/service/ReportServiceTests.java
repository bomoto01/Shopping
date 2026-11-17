package com.shopping.service;

import com.shopping.dto.response.DashboardSummaryResponse;
import com.shopping.dto.response.TopSellingProductResponse;
import com.shopping.enums.OrderStatus;
import com.shopping.repository.OrderItemRepository;
import com.shopping.repository.OrderRepository;
import com.shopping.repository.ProductRepository;
import com.shopping.repository.UserRepository;
import com.shopping.service.impl.ReportServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTests {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    @Test
    @DisplayName("Lấy dữ liệu Dashboard Summary thành công")
    void getDashboardSummary_success() {
        when(orderRepository.getTotalRevenue()).thenReturn(new BigDecimal("15000000.00"));
        when(orderRepository.count()).thenReturn(50L);
        when(userRepository.count()).thenReturn(120L);
        when(productRepository.count()).thenReturn(35L);

        List<Object[]> rawStatusCounts = List.of(
                new Object[]{OrderStatus.DELIVERED, 30L},
                new Object[]{OrderStatus.PENDING, 10L},
                new Object[]{OrderStatus.CANCELLED, 5L}
        );
        when(orderRepository.countOrdersGroupByStatus()).thenReturn(rawStatusCounts);
        when(orderItemRepository.findTopSellingProducts(any(PageRequest.class))).thenReturn(Collections.emptyList());

        DashboardSummaryResponse summary = reportService.getDashboardSummary();

        assertNotNull(summary);
        assertEquals(new BigDecimal("15000000.00"), summary.getTotalRevenue());
        assertEquals(50L, summary.getTotalOrders());
        assertEquals(120L, summary.getTotalCustomers());
        assertEquals(35L, summary.getTotalProducts());
        assertEquals(OrderStatus.values().length, summary.getOrderStatusCounts().size());
    }

    @Test
    @DisplayName("Lấy danh sách top sản phẩm bán chạy thành công")
    void getTopSellingProducts_success() {
        List<Object[]> rawList = List.of(
                new Object[]{10L, "Áo Thun Cotton", 100L, new BigDecimal("20000000.00")},
                new Object[]{12L, "Quần Kaki", 50L, new BigDecimal("15000000.00")}
        );
        when(orderItemRepository.findTopSellingProducts(any(PageRequest.class))).thenReturn(rawList);

        List<TopSellingProductResponse> topProducts = reportService.getTopSellingProducts(5);

        assertNotNull(topProducts);
        assertEquals(2, topProducts.size());
        assertEquals("Áo Thun Cotton", topProducts.get(0).getProductName());
        assertEquals(100L, topProducts.get(0).getTotalQuantitySold());
        assertEquals(new BigDecimal("20000000.00"), topProducts.get(0).getTotalRevenue());
    }

    @Test
    @DisplayName("Lấy báo cáo doanh thu theo khoảng thời gian thành công")
    void getRevenueReport_success() {
        Instant now = Instant.now();
        Instant past = now.minus(30, ChronoUnit.DAYS);

        when(orderRepository.getRevenueBetween(past, now)).thenReturn(new BigDecimal("8500000.00"));

        BigDecimal revenue = reportService.getRevenueReport(past, now);

        assertNotNull(revenue);
        assertEquals(new BigDecimal("8500000.00"), revenue);
        verify(orderRepository, times(1)).getRevenueBetween(past, now);
    }
}
