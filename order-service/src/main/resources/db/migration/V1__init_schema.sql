CREATE TABLE orders (
                        id BIGSERIAL PRIMARY KEY,
                        order_number VARCHAR(255) NOT NULL UNIQUE,
                        customer_id VARCHAR(255) NOT NULL,
                        status VARCHAR(50) NOT NULL,
                        payment_method VARCHAR(50),
                        payment_id VARCHAR(255),
                        currency VARCHAR(3) NOT NULL DEFAULT 'PLN',
                        total_amount DECIMAL(19, 2) NOT NULL DEFAULT 0,
                        version BIGINT NOT NULL DEFAULT 0,
                        created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_items (
                        id BIGSERIAL PRIMARY KEY,
                        order_id BIGINT NOT NULL,
                        product_id BIGINT NOT NULL,
                        sku VARCHAR(255) NOT NULL,
                        price DECIMAL(19, 2) NOT NULL,
                        quantity INTEGER NOT NULL,
                        CONSTRAINT fk_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE
);

CREATE INDEX idx_order_items_order_id ON order_items(order_id);