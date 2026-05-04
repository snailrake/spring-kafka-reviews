package com.snailrake.dataservice.model;

import java.time.LocalDate;

public record NewReview(
        String author,
        int rating,
        String comment,
        LocalDate visitedOn
) {
}