package com.snailrake.dataservice.service;

import com.snailrake.dataservice.dto.MostReviewedRestaurantResponse;
import com.snailrake.dataservice.dto.ReviewEvent;
import com.snailrake.dataservice.dto.ReviewsPerDayResponse;
import com.snailrake.dataservice.dto.SearchResponse;
import com.snailrake.dataservice.dto.TopRestaurantAvgRatingResponse;
import com.snailrake.dataservice.mapper.ReviewEventMapper;
import com.snailrake.dataservice.model.Review;
import com.snailrake.dataservice.repo.ReportRepository;
import com.snailrake.dataservice.repo.RestaurantRepository;
import com.snailrake.dataservice.repo.ReviewRepository;
import com.snailrake.dataservice.validation.ReviewEventValidator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ReviewService {

    private final RestaurantRepository restaurantRepository;
    private final ReviewRepository reviewRepository;
    private final ReportRepository reportRepository;
    private final ReviewEventValidator reviewEventValidator;
    private final ReviewEventMapper reviewEventMapper;

    public ReviewService(
            RestaurantRepository restaurantRepository,
            ReviewRepository reviewRepository,
            ReportRepository reportRepository,
            ReviewEventValidator reviewEventValidator,
            ReviewEventMapper reviewEventMapper
    ) {
        this.restaurantRepository = restaurantRepository;
        this.reviewRepository = reviewRepository;
        this.reportRepository = reportRepository;
        this.reviewEventValidator = reviewEventValidator;
        this.reviewEventMapper = reviewEventMapper;
    }

    public void saveReview(ReviewEvent event) {
        var normalized = reviewEventValidator.validate(event);

        var restaurant = reviewEventMapper.toRestaurant(normalized);
        var newReview = reviewEventMapper.toNewReview(normalized);

        var restaurantId = restaurantRepository.save(restaurant);
        var review = new Review(
                restaurantId,
                newReview.author(),
                newReview.rating(),
                newReview.comment(),
                newReview.visitedOn()
        );

        reviewRepository.save(review);
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
            int limit
    ) {
        var items = reportRepository.search(
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
        );

        return new SearchResponse(items.size(), items);
    }

    public ReviewsPerDayResponse reviewsPerDay(int days) {
        var items = reportRepository.reviewsPerDay(days);
        return new ReviewsPerDayResponse(days, items);
    }

    public TopRestaurantAvgRatingResponse topRestaurantsByAvgRating(int limit, int minReviews) {
        var items = reportRepository.topRestaurantsByAvgRating(limit, minReviews);
        return new TopRestaurantAvgRatingResponse(items);
    }

    public MostReviewedRestaurantResponse mostReviewedRestaurants(int limit, Integer days) {
        var items = reportRepository.mostReviewedRestaurants(limit, days);
        return new MostReviewedRestaurantResponse(days, items);
    }
}