CREATE TABLE orders (
    id BIGINT IDENTITY(1,1) NOT NULL,
    customer_reference VARCHAR(100) NOT NULL,
    status VARCHAR(30) NOT NULL,
    total_amount DECIMAL(19,2) NOT NULL,
    created_at DATETIMEOFFSET NOT NULL,
    CONSTRAINT pk_orders PRIMARY KEY (id),
    CONSTRAINT ck_orders_total_amount_positive CHECK (total_amount > 0)
);

CREATE INDEX ix_orders_customer_reference ON orders (customer_reference);
CREATE INDEX ix_orders_status ON orders (status);
