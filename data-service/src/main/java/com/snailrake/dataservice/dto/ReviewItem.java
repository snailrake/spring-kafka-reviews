package com.snailrake.dataservice.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record ReviewItem(
        long id,
        String restaurant,
        String city,
        String author,
        int rating,
        String comment,
        LocalDate visitedOn,
        OffsetDateTime createdAt
) {
}

