package com.snailrake.apiservice.dto;

import java.util.List;

public record TopRestaurantAvgRatingResponse(
        List<TopRestaurantAvgRatingItem> items
) {
}

