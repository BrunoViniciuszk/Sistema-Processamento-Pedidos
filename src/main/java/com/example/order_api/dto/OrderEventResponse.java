package com.example.order_api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderEventResponse(
        UUID id,
        String eventType,
        String payload,
        LocalDateTime eventTime
) {}