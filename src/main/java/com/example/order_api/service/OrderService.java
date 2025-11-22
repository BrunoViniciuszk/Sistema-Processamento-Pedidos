package com.example.order_api.service;

import com.example.order_api.config.RabbitConfig;
import com.example.order_api.dto.CreateOrderRequest;
import com.example.order_api.dto.OrderEventResponse;
import com.example.order_api.dto.OrderResponse;
import com.example.order_api.model.Order;
import com.example.order_api.model.OrderEvent;
import com.example.order_api.repository.OrderEventRepository;
import com.example.order_api.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventRepository eventRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        UUID orderId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Order order = Order.builder()
                .id(orderId)
                .customerId(request.customerId())
                .currentStatus("ORDER_CREATED")
                .createdAt(now)
                .updatedAt(now)
                .build();
        orderRepository.save(order);


        String jsonPayload = toJson(new OrderResponse(orderId, request.customerId(), "ORDER_CREATED", now));

        OrderEvent event = OrderEvent.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .eventType("ORDER_CREATED")
                .payload(jsonPayload)
                .eventTime(now)
                .sent(false)
                .build();
        eventRepository.save(event);


        trySendToRabbit(event);

        return OrderResponse.from(order);
    }

    @Transactional
    public void updateStatus(UUID orderId, String newStatus) {
        Order order = orderRepository.findById(orderId).orElseThrow();

        if(order.getCurrentStatus().equals(newStatus)) return;

        order.setCurrentStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        String jsonPayload = toJson(new OrderResponse(orderId, order.getCustomerId(), newStatus, order.getUpdatedAt()));

        OrderEvent event = OrderEvent.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .eventType(newStatus)
                .payload(jsonPayload)
                .eventTime(LocalDateTime.now())
                .sent(false)
                .build();
        eventRepository.save(event);


        if (!newStatus.equals("DELIVERED")) {
            trySendToRabbit(event);
        }
    }

    private void trySendToRabbit(OrderEvent event) {
        try {
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY, event.getPayload());
            event.setSent(true);
            eventRepository.save(event);
            log.info("Evento enviado: {}", event.getEventType());
        } catch (Exception e) {
            log.error("Falha no envio imediato (será reprocessado pelo Job): {}", e.getMessage());
        }
    }

    private String toJson(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            throw new RuntimeException("Erro JSON", e);
        }
    }

    public List<OrderEventResponse> getEvents(UUID id) {
        return eventRepository.findAllByOrderIdOrderByEventTimeAsc(id).stream()
                .map(e -> new OrderEventResponse(
                        e.getId(),
                        e.getEventType(),
                        e.getPayload(),
                        e.getEventTime()
                ))
                .toList();
    }

    public OrderResponse getById(UUID id) {
        return OrderResponse.from(orderRepository.findById(id).orElseThrow());
    }
}