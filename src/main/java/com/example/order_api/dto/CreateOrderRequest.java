package com.example.order_api.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateOrderRequest(
        @NotBlank
        String customerId
) {}

