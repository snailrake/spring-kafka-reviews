package com.snailrake.dataservice.dto;

import java.util.List;

public record MostReviewedRestaurantResponse(
        Integer days,
        List<MostReviewedRestaurantItem> items
) {
}

