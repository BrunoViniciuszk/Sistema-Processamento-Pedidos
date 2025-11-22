package com.example.order_api.worker;

import com.example.order_api.config.RabbitConfig;
import com.example.order_api.model.OrderEvent;
import com.example.order_api.repository.OrderEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventPublisherJob {

    private final OrderEventRepository eventRepository;
    private final RabbitTemplate rabbitTemplate;

    @Scheduled(fixedDelayString = "${app.job.interval:10000}")
    public void reprocessFailedEvents() {
        List<OrderEvent> pendingEvents = eventRepository.findBySentFalse();

        if (pendingEvents.isEmpty()) return;

        log.info("Job de recuperação: encontrado {} eventos não enviados.", pendingEvents.size());

        for (OrderEvent event : pendingEvents) {
            try {
                rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY, event.getPayload());
                event.setSent(true);
                eventRepository.save(event);
                log.info("Evento {} recuperado e enviado com sucesso.", event.getId());
            } catch (Exception e) {
                log.error("Ainda não foi possível enviar o evento {}.", event.getId());
            }
        }
    }
}