package com.chubb.claims.service;

import com.chubb.claims.domain.OutboxEvent;
import com.chubb.claims.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String TOPIC = "claims.events";

    @Scheduled(fixedRate = 2000)
    @Transactional
    public void publishUnsentEvents() {
        List<OutboxEvent> unsentEvents = outboxEventRepository.findUnsentEvents();
        if (unsentEvents.isEmpty()) {
            return;
        }

        for (OutboxEvent event : unsentEvents) {
            try {
                kafkaTemplate.send(TOPIC, event.getAggregateId().toString(), event.getPayload())
                        .get(); // ensure sent successfully
                event.setSentAt(OffsetDateTime.now());
                outboxEventRepository.save(event);
                log.info("Published outbox event ID {} of type {} to Kafka topic {}", event.getId(), event.getType(), TOPIC);
            } catch (Exception e) {
                log.error("Failed to publish outbox event ID {} to Kafka", event.getId(), e);
                // break or continue to retry next tick
                break;
            }
        }
    }
}
