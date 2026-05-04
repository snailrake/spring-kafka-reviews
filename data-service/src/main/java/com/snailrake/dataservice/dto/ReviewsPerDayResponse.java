package com.snailrake.dataservice.dto;

import java.util.List;

public record ReviewsPerDayResponse(
        int days,
        List<ReviewsPerDayItem> items
) {
}

