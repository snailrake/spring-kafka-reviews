package com.snailrake.dataservice.dto;

import java.util.List;

public record TopRestaurantAvgRatingResponse(
        List<TopRestaurantAvgRatingItem> items
) {
}

