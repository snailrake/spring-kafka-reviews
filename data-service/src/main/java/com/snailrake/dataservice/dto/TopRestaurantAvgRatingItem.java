package com.snailrake.dataservice.dto;

public record TopRestaurantAvgRatingItem(
        String restaurant,
        String city,
        double avgRating,
        long reviewsCount
) {
}

