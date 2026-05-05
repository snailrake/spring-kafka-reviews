package com.snailrake.apiservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.snailrake.apiservice.dto.ReviewEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ReviewEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topic;

    public ReviewEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${kafka.topic}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    public void publish(ReviewEvent event) {
        try {
            var payload = objectMapper.writeValueAsString(event);
            var key = event.city().trim() + "|" + event.restaurantName().trim();
            kafkaTemplate.send(topic, key, payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize review event", exception);
        }
    }
}