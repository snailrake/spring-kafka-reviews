package com.snailrake.dataservice.controller;

import com.snailrake.dataservice.dto.MostReviewedRestaurantResponse;
import com.snailrake.dataservice.dto.ReviewsPerDayResponse;
import com.snailrake.dataservice.dto.SearchResponse;
import com.snailrake.dataservice.dto.TopRestaurantAvgRatingResponse;
import com.snailrake.dataservice.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping
public class DataController {

    private final ReviewService reviewService;

    public DataController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    @GetMapping("/reviews/search")
    public ResponseEntity<SearchResponse> search(
            @RequestParam(required = false) String restaurant,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) Integer min_rating,
            @RequestParam(required = false) Integer max_rating,
            @RequestParam(required = false) LocalDate from_date,
            @RequestParam(required = false) LocalDate to_date,
            @RequestParam(defaultValue = "created_at") String sort_by,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(defaultValue = "50") int limit
    ) {
        return ResponseEntity.ok(reviewService.search(
                restaurant,
                city,
                author,
                min_rating,
                max_rating,
                from_date,
                to_date,
                sort_by,
                direction,
                limit
        ));
    }

    @GetMapping("/reports/reviews-per-day")
    public ResponseEntity<ReviewsPerDayResponse> reviewsPerDay(@RequestParam(defaultValue = "14") int days) {
        return ResponseEntity.ok(reviewService.reviewsPerDay(days));
    }

    @GetMapping("/reports/top-restaurants-by-avg-rating")
    public ResponseEntity<TopRestaurantAvgRatingResponse> topByAvg(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "2") int min_reviews
    ) {
        return ResponseEntity.ok(reviewService.topRestaurantsByAvgRating(limit, min_reviews));
    }

    @GetMapping("/reports/most-reviewed-restaurants")
    public ResponseEntity<MostReviewedRestaurantResponse> mostReviewed(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) Integer days
    ) {
        return ResponseEntity.ok(reviewService.mostReviewedRestaurants(limit, days));
    }
}

