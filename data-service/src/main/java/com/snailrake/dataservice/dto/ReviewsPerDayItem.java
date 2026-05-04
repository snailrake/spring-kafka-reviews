package com.snailrake.dataservice.dto;

import java.time.LocalDate;

public record ReviewsPerDayItem(
        LocalDate day,
        long reviewsCount
) {
}

