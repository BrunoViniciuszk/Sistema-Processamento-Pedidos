package com.example.order_api.worker;

import com.example.order_api.service.OrderService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderConsumer {

    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "order.events")
    public void handleEvent(String payload) {
        try {
            JsonNode node = objectMapper.readTree(payload);
            String status = node.get("status").asText();
            UUID id = UUID.fromString(node.get("id").asText());

            log.info("Processando evento de logística para pedido: {}", id);
            Thread.sleep(3000);

            switch (status) {
                case "ORDER_CREATED" -> orderService.updateStatus(id, "IN_TRANSPORT");
                case "IN_TRANSPORT" -> orderService.updateStatus(id, "DELIVERED");
            }

        } catch (Exception e) {
            log.error("Erro no processamento: ", e);
        }
    }
}