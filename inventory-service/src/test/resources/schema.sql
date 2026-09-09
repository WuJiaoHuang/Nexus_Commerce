CREATE TABLE IF NOT EXISTS inventory (
    product_id VARCHAR(64) PRIMARY KEY,
    available_quantity INT
);

CREATE TABLE IF NOT EXISTS inventory_reservation (
    order_id VARCHAR(64) PRIMARY KEY,
    product_id VARCHAR(64),
    quantity INT
);

CREATE TABLE IF NOT EXISTS processed_event (
    event_id VARCHAR(64) PRIMARY KEY,
    event_type VARCHAR(255),
    aggregate_id VARCHAR(64),
    processed_at TIMESTAMP
);
