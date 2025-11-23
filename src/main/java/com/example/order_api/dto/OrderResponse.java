package com.example.order_api.dto;

import com.example.order_api.enums.OrderStatus;
import com.example.order_api.model.Order;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String customerId,
        OrderStatus status,
        LocalDateTime createdAt
) {
    public static OrderResponse from(Order o) {
        return new OrderResponse(
                o.getId(),
                o.getCustomerId(),
                o.getCurrentStatus(),
                o.getCreatedAt()
        );
    }
}

