CREATE TABLE IF NOT EXISTS inventory_inquiry (
    inquiry_id VARCHAR(50) PRIMARY KEY,
    request_code VARCHAR(50) NOT NULL REFERENCES order_request(request_code),
    site_code VARCHAR(50) NOT NULL REFERENCES import_site(site_code),
    status VARCHAR(20) NOT NULL,
    item_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS inventory_inquiry_item (
    id BIGSERIAL PRIMARY KEY,
    inquiry_id VARCHAR(50) NOT NULL REFERENCES inventory_inquiry(inquiry_id) ON DELETE CASCADE,
    merchandise_code VARCHAR(50) NOT NULL REFERENCES merchandise(code),
    requested_qty INT NOT NULL,
    unit VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS internal_warehouse_inventory (
    id BIGSERIAL PRIMARY KEY,
    merchandise_code VARCHAR(50) NOT NULL REFERENCES merchandise(code),
    in_stock_quantity INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (merchandise_code)
);

CREATE INDEX IF NOT EXISTS idx_inventory_inquiry_request_code
    ON inventory_inquiry(request_code);

CREATE INDEX IF NOT EXISTS idx_inventory_inquiry_site_code
    ON inventory_inquiry(site_code);

CREATE INDEX IF NOT EXISTS idx_inventory_inquiry_item_inquiry_id
    ON inventory_inquiry_item(inquiry_id);
