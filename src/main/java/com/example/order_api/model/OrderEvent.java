package com.example.order_api.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "order_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false)
    private String eventType;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false)
    private LocalDateTime eventTime;

    @Column(nullable = false)
    private boolean sent = false;
}