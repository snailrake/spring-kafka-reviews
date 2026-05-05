package com.snailrake.apiservice.controller;

import com.snailrake.apiservice.client.DataServiceClient;
import com.snailrake.apiservice.dto.MostReviewedRestaurantResponse;
import com.snailrake.apiservice.dto.ReviewEvent;
import com.snailrake.apiservice.dto.ReviewIn;
import com.snailrake.apiservice.dto.ReviewQueuedResponse;
import com.snailrake.apiservice.dto.ReviewsPerDayResponse;
import com.snailrake.apiservice.dto.SearchResponse;
import com.snailrake.apiservice.dto.TopRestaurantAvgRatingResponse;
import com.snailrake.apiservice.kafka.ReviewEventPublisher;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final DataServiceClient dataServiceClient;
    private final ReviewEventPublisher reviewEventPublisher;

    public ApiController(
            DataServiceClient dataServiceClient,
            ReviewEventPublisher reviewEventPublisher
    ) {
        this.dataServiceClient = dataServiceClient;
        this.reviewEventPublisher = reviewEventPublisher;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    @PostMapping("/reviews")
    public ResponseEntity<ReviewQueuedResponse> addReview(@Valid @RequestBody ReviewIn request) {
        var restaurantName = request.restaurantName().trim();
        var city = request.city().trim();
        var author = request.author().trim();
        var comment = Objects.nonNull(request.comment()) ? request.comment().trim() : null;

        var event = new ReviewEvent(
                UUID.randomUUID(),
                Instant.now(),
                restaurantName,
                city,
                author,
                request.rating(),
                comment,
                request.visitedOn()
        );

        reviewEventPublisher.publish(event);

        return ResponseEntity.accepted().body(new ReviewQueuedResponse("queued", event.eventId().toString()));
    }

    @GetMapping("/reviews/search")
    public ResponseEntity<SearchResponse> searchReviews(
            @RequestParam(required = false) String restaurant,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String author,
            @RequestParam(name = "min_rating", required = false) Integer minRating,
            @RequestParam(name = "max_rating", required = false) Integer maxRating,
            @RequestParam(name = "from_date", required = false) LocalDate fromDate,
            @RequestParam(name = "to_date", required = false) LocalDate toDate,
            @RequestParam(name = "sort_by", required = false) String sortBy,
            @RequestParam(required = false) String direction,
            @RequestParam(required = false) Integer limit
    ) {
        return ResponseEntity.ok(dataServiceClient.search(
                restaurant,
                city,
                author,
                minRating,
                maxRating,
                fromDate,
                toDate,
                sortBy,
                direction,
                limit
        ));
    }

    @GetMapping("/reports/reviews-per-day")
    public ResponseEntity<ReviewsPerDayResponse> reportReviewsPerDay(@RequestParam(defaultValue = "14") int days) {
        return ResponseEntity.ok(dataServiceClient.reviewsPerDay(days));
    }

    @GetMapping("/reports/top-restaurants-by-avg-rating")
    public ResponseEntity<TopRestaurantAvgRatingResponse> reportTopAvg(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "2", name = "min_reviews") int minReviews
    ) {
        return ResponseEntity.ok(dataServiceClient.topRestaurantsByAvgRating(limit, minReviews));
    }

    @GetMapping("/reports/most-reviewed-restaurants")
    public ResponseEntity<MostReviewedRestaurantResponse> reportMostReviewed(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) Integer days
    ) {
        return ResponseEntity.ok(dataServiceClient.mostReviewedRestaurants(limit, days));
    }
}