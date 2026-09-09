CREATE TABLE IF NOT EXISTS orders (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64),
    product_id VARCHAR(64),
    quantity INT,
    status VARCHAR(255),
    created_at TIMESTAMP
);
