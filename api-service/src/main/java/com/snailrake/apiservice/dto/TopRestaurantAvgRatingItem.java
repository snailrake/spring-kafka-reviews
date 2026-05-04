package com.snailrake.apiservice.dto;

public record TopRestaurantAvgRatingItem(
        String restaurant,
        String city,
        double avgRating,
        long reviewsCount
) {
}

