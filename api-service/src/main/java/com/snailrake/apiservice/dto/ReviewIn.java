package com.snailrake.apiservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ReviewIn(
        @NotBlank @Size(max = 120) String restaurantName,
        @NotBlank @Size(max = 120) String city,
        @NotBlank @Size(max = 80) String author,
        @Min(1) @Max(5) int rating,
        @Size(max = 1000) String comment,
        LocalDate visitedOn
) {
}
