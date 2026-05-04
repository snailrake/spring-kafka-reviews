package com.snailrake.apiservice.dto;

public record MostReviewedRestaurantItem(
        String restaurant,
        String city,
        long reviewsCount
) {
}

