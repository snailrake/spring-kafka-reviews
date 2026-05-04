package com.snailrake.dataservice.repo;

import com.snailrake.dataservice.model.Review;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ReviewRepository {

    private final JdbcTemplate jdbcTemplate;

    public ReviewRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(Review review) {
        jdbcTemplate.update(
                """
                INSERT INTO reviews (restaurant_id, author, rating, comment, visited_on)
                VALUES (?, ?, ?, ?, ?)
                """,
                review.restaurantId(),
                review.author(),
                review.rating(),
                review.comment(),
                review.visitedOn()
        );
    }
}