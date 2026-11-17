package com.shopping.dto.response;

import com.shopping.enums.OrderStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusHistoryResponse {

    private Long historyId;
    private OrderStatus oldStatus;
    private OrderStatus newStatus;
    private String changedBy;
    private String note;
    private LocalDateTime changedAt;
}
