package com.snailrake.dataservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snailrake.dataservice.dto.ReviewEvent;
import com.snailrake.dataservice.service.ReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ReviewListener {

    private static final Logger log = LoggerFactory.getLogger(ReviewListener.class);

    private final ObjectMapper objectMapper;
    private final ReviewService reviewService;

    public ReviewListener(ObjectMapper objectMapper, ReviewService reviewService) {
        this.objectMapper = objectMapper;
        this.reviewService = reviewService;
    }

    @KafkaListener(topics = "${kafka.topic}")
    public void onMessage(String message) {
        try {
            var event = objectMapper.readValue(message, ReviewEvent.class);
            reviewService.saveReview(event);
        } catch (Exception exception) {
            log.debug("Skipping invalid Kafka message: {}", exception.getMessage());
        }
    }
}