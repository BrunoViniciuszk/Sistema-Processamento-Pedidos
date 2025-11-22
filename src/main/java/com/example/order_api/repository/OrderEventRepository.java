package com.example.order_api.repository;

import com.example.order_api.model.OrderEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface OrderEventRepository extends JpaRepository<OrderEvent, UUID> {
    List<OrderEvent> findAllByOrderIdOrderByEventTimeAsc(UUID orderId);

    List<OrderEvent> findBySentFalse();
}