package com.snailrake.apiservice.dto;

import java.time.LocalDate;

public record ReviewsPerDayItem(
        LocalDate day,
        long reviewsCount
) {
}

