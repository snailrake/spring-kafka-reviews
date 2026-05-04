package com.snailrake.dataservice.repo;

import com.snailrake.dataservice.model.Restaurant;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Objects;

@Repository
public class RestaurantRepository {

    private final JdbcTemplate jdbcTemplate;

    public RestaurantRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long save(Restaurant restaurant) {
        var id = jdbcTemplate.queryForObject(
                """
                INSERT INTO restaurants (name, city)
                VALUES (?, ?)
                ON CONFLICT (name, city) DO UPDATE SET name = EXCLUDED.name
                RETURNING id
                """,
                Long.class,
                restaurant.name(),
                restaurant.city()
        );

        if (Objects.isNull(id)) {
            throw new IllegalStateException("Failed to save restaurant");
        }

        return id;
    }
}