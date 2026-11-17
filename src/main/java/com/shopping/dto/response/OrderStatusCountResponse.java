package com.shopping.dto.response;

import com.shopping.enums.OrderStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusCountResponse {

    private OrderStatus status;
    private Long count;
}
