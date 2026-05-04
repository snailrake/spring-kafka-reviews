package com.snailrake.apiservice.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ReviewEvent(
        UUID eventId,
        Instant eventTime,
        String restaurantName,
        String city,
        String author,
        int rating,
        String comment,
        LocalDate visitedOn
) {
}
