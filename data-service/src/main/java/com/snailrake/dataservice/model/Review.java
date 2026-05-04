package com.snailrake.dataservice.model;

import java.time.LocalDate;

public record Review(
        long restaurantId,
        String author,
        int rating,
        String comment,
        LocalDate visitedOn
) {
}