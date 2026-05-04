package com.snailrake.dataservice.repo;

import com.snailrake.dataservice.dto.MostReviewedRestaurantItem;
import com.snailrake.dataservice.dto.ReviewItem;
import com.snailrake.dataservice.dto.ReviewsPerDayItem;
import com.snailrake.dataservice.dto.TopRestaurantAvgRatingItem;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ReportRepository {

    private final NamedParameterJdbcTemplate namedJdbc;

    private static final RowMapper<ReviewItem> REVIEW_ITEM_MAPPER = new ReviewItemRowMapper();
    private static final RowMapper<ReviewsPerDayItem> REVIEWS_PER_DAY_MAPPER = new ReviewsPerDayRowMapper();
    private static final RowMapper<TopRestaurantAvgRatingItem> TOP_AVG_MAPPER = new TopAvgRowMapper();
    private static final RowMapper<MostReviewedRestaurantItem> MOST_REVIEWED_MAPPER = new MostReviewedRowMapper();

    public ReportRepository(NamedParameterJdbcTemplate namedJdbc) {
        this.namedJdbc = namedJdbc;
    }

    public List<ReviewItem> search(
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
        var resolvedSortBy = switch (sortBy) {
            case "created_at" -> "rv.created_at";
            case "visited_on" -> "rv.visited_on";
            case "rating" -> "rv.rating";
            case "restaurant" -> "r.name";
            case "city" -> "r.city";
            default -> throw new IllegalArgumentException("Unsupported sort_by: " + sortBy);
        };

        var dir = "desc".equalsIgnoreCase(direction) ? "DESC" : "ASC";

        var sql = new StringBuilder();
        sql.append("""
                SELECT
                    rv.id,
                    r.name AS restaurant,
                    r.city,
                    rv.author,
                    rv.rating,
                    rv.comment,
                    rv.visited_on,
                    rv.created_at
                FROM reviews rv
                JOIN restaurants r ON r.id = rv.restaurant_id
                """);

        var filter = new WhereBuilder()
                .ilikeContains("r.name", "restaurant", restaurant)
                .ilikeContains("r.city", "city", city)
                .ilikeContains("rv.author", "author", author)
                .gte("rv.rating", "minRating", minRating)
                .lte("rv.rating", "maxRating", maxRating)
                .fromDateInclusiveUtc("rv.created_at", "fromTs", fromDate)
                .toDateExclusiveUtc("rv.created_at", "toTs", toDate);

        filter.applyTo(sql);

        sql.append(" ORDER BY ").append(resolvedSortBy).append(" ").append(dir);
        sql.append(" LIMIT :limit");

        var params = filter.params();
        params.addValue("limit", limit);

        return namedJdbc.query(sql.toString(), params, REVIEW_ITEM_MAPPER);
    }

    public List<ReviewsPerDayItem> reviewsPerDay(int days) {
        var since = OffsetDateTime.now(ZoneOffset.UTC).minusDays(days);
        var params = new MapSqlParameterSource().addValue("since", Timestamp.from(since.toInstant()));

        return namedJdbc.query(
                """
                SELECT DATE(rv.created_at) AS day, COUNT(*) AS reviews_count
                FROM reviews rv
                WHERE rv.created_at >= :since
                GROUP BY day
                ORDER BY day
                """,
                params,
                REVIEWS_PER_DAY_MAPPER
        );
    }

    public List<TopRestaurantAvgRatingItem> topRestaurantsByAvgRating(int limit, int minReviews) {
        return namedJdbc.query(
                """
                SELECT
                    r.name AS restaurant,
                    r.city,
                    ROUND(AVG(rv.rating)::numeric, 2) AS avg_rating,
                    COUNT(*) AS reviews_count
                FROM reviews rv
                JOIN restaurants r ON r.id = rv.restaurant_id
                GROUP BY r.id
                HAVING COUNT(*) >= :minReviews
                ORDER BY avg_rating DESC, reviews_count DESC
                LIMIT :limit
                """,
                new MapSqlParameterSource()
                        .addValue("minReviews", minReviews)
                        .addValue("limit", limit),
                TOP_AVG_MAPPER
        );
    }

    public List<MostReviewedRestaurantItem> mostReviewedRestaurants(int limit, Integer days) {
        var sql = new StringBuilder();
        sql.append("""
                SELECT
                    r.name AS restaurant,
                    r.city,
                    COUNT(*) AS reviews_count
                FROM reviews rv
                JOIN restaurants r ON r.id = rv.restaurant_id
                """);

        var params = new MapSqlParameterSource();

        if (Objects.nonNull(days)) {
            var since = OffsetDateTime.now(ZoneOffset.UTC).minusDays(days);
            sql.append(" WHERE rv.created_at >= :since");
            params.addValue("since", Timestamp.from(since.toInstant()));
        }

        sql.append("""
                GROUP BY r.id
                ORDER BY reviews_count DESC
                LIMIT :limit
                """);

        params.addValue("limit", limit);

        return namedJdbc.query(sql.toString(), params, MOST_REVIEWED_MAPPER);
    }

    private static final class WhereBuilder {

        private final List<String> clauses = new ArrayList<>();
        private final MapSqlParameterSource params = new MapSqlParameterSource();

        public WhereBuilder ilikeContains(String column, String paramName, String value) {
            if (StringUtils.hasText(value)) {
                clauses.add(column + " ILIKE :" + paramName);
                params.addValue(paramName, "%" + value.trim() + "%");
            }

            return this;
        }

        public WhereBuilder gte(String column, String paramName, Integer value) {
            if (Objects.nonNull(value)) {
                clauses.add(column + " >= :" + paramName);
                params.addValue(paramName, value);
            }

            return this;
        }

        public WhereBuilder lte(String column, String paramName, Integer value) {
            if (Objects.nonNull(value)) {
                clauses.add(column + " <= :" + paramName);
                params.addValue(paramName, value);
            }

            return this;
        }

        public WhereBuilder fromDateInclusiveUtc(String column, String paramName, LocalDate fromDate) {
            if (Objects.nonNull(fromDate)) {
                var fromTs = Timestamp.from(fromDate.atStartOfDay().atOffset(ZoneOffset.UTC).toInstant());
                clauses.add(column + " >= :" + paramName);
                params.addValue(paramName, fromTs);
            }

            return this;
        }

        public WhereBuilder toDateExclusiveUtc(String column, String paramName, LocalDate toDate) {
            if (Objects.nonNull(toDate)) {
                var toTs = Timestamp.from(toDate.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC).toInstant());
                clauses.add(column + " < :" + paramName);
                params.addValue(paramName, toTs);
            }

            return this;
        }

        public void applyTo(StringBuilder sql) {
            if (!clauses.isEmpty()) {
                sql.append(" WHERE ").append(String.join(" AND ", clauses));
            }
        }

        public MapSqlParameterSource params() {
            return params;
        }
    }

    private static final class ReviewItemRowMapper implements RowMapper<ReviewItem> {

        @Override
        public ReviewItem mapRow(ResultSet rs, int rowNum) throws SQLException {
            var visitedOnDate = rs.getDate("visited_on");
            var visitedOn = Objects.nonNull(visitedOnDate) ? visitedOnDate.toLocalDate() : null;

            var createdAtTs = rs.getTimestamp("created_at");
            var createdAt = Objects.nonNull(createdAtTs) ? createdAtTs.toInstant().atOffset(ZoneOffset.UTC) : null;

            return new ReviewItem(
                    rs.getLong("id"),
                    rs.getString("restaurant"),
                    rs.getString("city"),
                    rs.getString("author"),
                    rs.getInt("rating"),
                    rs.getString("comment"),
                    visitedOn,
                    createdAt
            );
        }
    }

    private static final class ReviewsPerDayRowMapper implements RowMapper<ReviewsPerDayItem> {

        @Override
        public ReviewsPerDayItem mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new ReviewsPerDayItem(
                    rs.getDate("day").toLocalDate(),
                    rs.getLong("reviews_count")
            );
        }
    }

    private static final class TopAvgRowMapper implements RowMapper<TopRestaurantAvgRatingItem> {

        @Override
        public TopRestaurantAvgRatingItem mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new TopRestaurantAvgRatingItem(
                    rs.getString("restaurant"),
                    rs.getString("city"),
                    rs.getBigDecimal("avg_rating").doubleValue(),
                    rs.getLong("reviews_count")
            );
        }
    }

    private static final class MostReviewedRowMapper implements RowMapper<MostReviewedRestaurantItem> {

        @Override
        public MostReviewedRestaurantItem mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new MostReviewedRestaurantItem(
                    rs.getString("restaurant"),
                    rs.getString("city"),
                    rs.getLong("reviews_count")
            );
        }
    }
}