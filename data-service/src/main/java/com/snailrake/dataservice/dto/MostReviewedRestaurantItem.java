package com.snailrake.dataservice.dto;

public record MostReviewedRestaurantItem(
        String restaurant,
        String city,
        long reviewsCount
) {
}

