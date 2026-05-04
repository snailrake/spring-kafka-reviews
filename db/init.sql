CREATE TABLE IF NOT EXISTS restaurants (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    city VARCHAR(120) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (name, city)
);

CREATE TABLE IF NOT EXISTS reviews (
    id BIGSERIAL PRIMARY KEY,
    restaurant_id BIGINT NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    author VARCHAR(80) NOT NULL,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    visited_on DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_reviews_restaurant_id ON reviews (restaurant_id);
CREATE INDEX IF NOT EXISTS idx_reviews_created_at ON reviews (created_at);

INSERT INTO restaurants (name, city)
VALUES
    ('Pasta Corner', 'Saratov'),
    ('Burger Yard', 'Saratov')
ON CONFLICT DO NOTHING;

INSERT INTO reviews (restaurant_id, author, rating, comment, visited_on)
SELECT r.id, v.author, v.rating, v.comment, v.visited_on
FROM (VALUES
    ('Pasta Corner', 'Saratov', 'Alex', 5, 'Great pasta and fast service', CURRENT_DATE - INTERVAL '2 days'),
    ('Burger Yard', 'Saratov', 'Nikita', 4, 'Good burgers, a bit noisy', CURRENT_DATE - INTERVAL '1 day')
) AS v(name, city, author, rating, comment, visited_on)
JOIN restaurants r ON r.name = v.name AND r.city = v.city;

