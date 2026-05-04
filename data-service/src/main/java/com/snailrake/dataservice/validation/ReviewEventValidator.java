package com.snailrake.dataservice.validation;

import com.snailrake.dataservice.dto.ReviewEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Component
public class ReviewEventValidator {

    public ReviewEvent validate(ReviewEvent event) {
        if (Objects.isNull(event)) {
            throw new IllegalArgumentException("Event is null");
        }

        var restaurantName = normalizeRequired(event.restaurantName(), "restaurantName");
        var city = normalizeRequired(event.city(), "city");
        var author = normalizeRequired(event.author(), "author");

        var rating = event.rating();
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("rating must be between 1 and 5");
        }

        var comment = normalizeOptional(event.comment());

        return new ReviewEvent(
                event.eventId(),
                event.eventTime(),
                restaurantName,
                city,
                author,
                rating,
                comment,
                event.visitedOn()
        );
    }

    private String normalizeRequired(String value, String fieldName) {
        var normalized = normalizeOptional(value);
        if (Objects.isNull(normalized)) {
            throw new IllegalArgumentException(fieldName + " is blank");
        }
        return normalized;
    }

    private String normalizeOptional(String value) {
        var trimmed = StringUtils.hasText(value) ? value.trim() : null;
        var result = (Objects.nonNull(trimmed) && !trimmed.isBlank()) ? trimmed : null;
        return result;
    }
}