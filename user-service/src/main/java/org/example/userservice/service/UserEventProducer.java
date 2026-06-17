package org.example.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.userservice.model.dto.UserCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserEventProducer {
    private final KafkaTemplate<Object, Object> kafkaTemplate;

    private static final String TOPIC = "user-creation-events";

    public void sendUserCreatedEvent(String email, String username, String role) {
        UserCreatedEvent event = new UserCreatedEvent(email, username, role);

        log.info("Sending Kafka event for registered user: {}", email);

        kafkaTemplate.send(TOPIC, email, event);
    }
}
