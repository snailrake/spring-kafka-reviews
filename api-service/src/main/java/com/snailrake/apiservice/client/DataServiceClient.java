package com.snailrake.apiservice.client;

import com.snailrake.apiservice.dto.MostReviewedRestaurantResponse;
import com.snailrake.apiservice.dto.ReviewsPerDayResponse;
import com.snailrake.apiservice.dto.SearchResponse;
import com.snailrake.apiservice.dto.TopRestaurantAvgRatingResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

@Component
public class DataServiceClient {

    private final RestClient restClient;

    public DataServiceClient(
            RestClient.Builder builder,
            @Value("${data-service.base-url}") String baseUrl
    ) {
        this.restClient = builder
                .baseUrl(baseUrl)
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public SearchResponse search(
            String restaurant,
            String city,
            String author,
            Integer minRating,
            Integer maxRating,
            LocalDate fromDate,
            LocalDate toDate,
            String sortBy,
            String direction,
            Integer limit
    ) {
        return Objects.requireNonNull(restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/reviews/search")
                        .queryParamIfPresent("restaurant", present(restaurant))
                        .queryParamIfPresent("city", present(city))
                        .queryParamIfPresent("author", present(author))
                        .queryParamIfPresent("min_rating", present(minRating))
                        .queryParamIfPresent("max_rating", present(maxRating))
                        .queryParamIfPresent("from_date", present(fromDate))
                        .queryParamIfPresent("to_date", present(toDate))
                        .queryParamIfPresent("sort_by", present(sortBy))
                        .queryParamIfPresent("direction", present(direction))
                        .queryParamIfPresent("limit", present(limit))
                        .build())
                .retrieve()
                .body(SearchResponse.class));
    }

    public ReviewsPerDayResponse reviewsPerDay(int days) {
        return Objects.requireNonNull(restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/reports/reviews-per-day")
                        .queryParam("days", days)
                        .build())
                .retrieve()
                .body(ReviewsPerDayResponse.class));
    }

    public TopRestaurantAvgRatingResponse topRestaurantsByAvgRating(int limit, int minReviews) {
        return Objects.requireNonNull(restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/reports/top-restaurants-by-avg-rating")
                        .queryParam("limit", limit)
                        .queryParam("min_reviews", minReviews)
                        .build())
                .retrieve()
                .body(TopRestaurantAvgRatingResponse.class));
    }

    public MostReviewedRestaurantResponse mostReviewedRestaurants(int limit, Integer days) {
        return Objects.requireNonNull(restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/reports/most-reviewed-restaurants")
                        .queryParam("limit", limit)
                        .queryParamIfPresent("days", present(days))
                        .build())
                .retrieve()
                .body(MostReviewedRestaurantResponse.class));
    }

    private static Optional<String> present(String value) {
        if (Objects.isNull(value)) {
            return Optional.empty();
        }

        var trimmed = value.trim();
        if (trimmed.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(trimmed);
    }

    private static <T> Optional<T> present(T value) {
        if (Objects.isNull(value)) {
            return Optional.empty();
        }

        return Optional.of(value);
    }
}