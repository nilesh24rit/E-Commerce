-- ============================================================
-- CommerceX V1 Initial Schema Migration
-- ============================================================
-- NOTE: This migration is only applied in PROD (profile=prod)
-- where flyway.enabled=true and ddl-auto=validate.
-- In DEV, Hibernate ddl-auto=update handles schema creation.
-- This migration documents the production schema baseline.
-- Run baseline-on-migrate=true on first deployment to mark
-- the existing schema as migrated without re-running DDL.
-- ============================================================

-- Roles table
CREATE TABLE IF NOT EXISTS roles (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id         UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    email      VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name  VARCHAR(100) NOT NULL,
    phone      VARCHAR(20),
    enabled    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at TIMESTAMP    NOT NULL DEFAULT now()
);

-- Users <-> Roles join table
CREATE TABLE IF NOT EXISTS user_roles (
    user_id UUID   NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- Refresh tokens
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id         BIGSERIAL    PRIMARY KEY,
    token      VARCHAR(512) NOT NULL UNIQUE,
    user_id    UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    expiry_date TIMESTAMP   NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT now(),
    ip_address VARCHAR(50),
    user_agent VARCHAR(255)
);

-- Categories
CREATE TABLE IF NOT EXISTS categories (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(150) NOT NULL UNIQUE,
    description TEXT,
    created_at  TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT now()
);

-- Products
CREATE TABLE IF NOT EXISTS products (
    id          UUID         NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    price       NUMERIC(19, 2) NOT NULL,
    sku         VARCHAR(100) NOT NULL UNIQUE,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    category_id BIGINT       REFERENCES categories (id) ON DELETE SET NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT now()
);

-- Inventory
CREATE TABLE IF NOT EXISTS inventory (
    id          BIGSERIAL    PRIMARY KEY,
    product_id  UUID         NOT NULL UNIQUE REFERENCES products (id) ON DELETE CASCADE,
    quantity    INTEGER      NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    reserved    INTEGER      NOT NULL DEFAULT 0 CHECK (reserved >= 0),
    created_at  TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT now()
);

-- Carts
CREATE TABLE IF NOT EXISTS carts (
    id         UUID      NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id    UUID      NOT NULL UNIQUE REFERENCES users (id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Cart Items
CREATE TABLE IF NOT EXISTS cart_items (
    id         BIGSERIAL      PRIMARY KEY,
    cart_id    UUID           NOT NULL REFERENCES carts (id) ON DELETE CASCADE,
    product_id UUID           NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    quantity   INTEGER        NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(19, 2) NOT NULL,
    created_at TIMESTAMP      NOT NULL DEFAULT now(),
    updated_at TIMESTAMP      NOT NULL DEFAULT now(),
    UNIQUE (cart_id, product_id)
);

-- Coupons
CREATE TABLE IF NOT EXISTS coupons (
    id              BIGSERIAL      PRIMARY KEY,
    code            VARCHAR(50)    NOT NULL UNIQUE,
    discount_type   VARCHAR(20)    NOT NULL,
    discount_value  NUMERIC(19, 2) NOT NULL,
    min_order_value NUMERIC(19, 2),
    max_uses        INTEGER,
    current_uses    INTEGER        NOT NULL DEFAULT 0,
    active          BOOLEAN        NOT NULL DEFAULT TRUE,
    expiry_date     TIMESTAMP,
    created_at      TIMESTAMP      NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP      NOT NULL DEFAULT now()
);

-- Orders
CREATE TABLE IF NOT EXISTS orders (
    id              UUID           NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id         UUID           NOT NULL REFERENCES users (id),
    status          VARCHAR(30)    NOT NULL,
    total_amount    NUMERIC(19, 2) NOT NULL,
    discount_amount NUMERIC(19, 2) NOT NULL DEFAULT 0,
    final_amount    NUMERIC(19, 2) NOT NULL,
    coupon_id       BIGINT         REFERENCES coupons (id) ON DELETE SET NULL,
    shipping_address TEXT,
    created_at      TIMESTAMP      NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP      NOT NULL DEFAULT now()
);

-- Order Items
CREATE TABLE IF NOT EXISTS order_items (
    id          BIGSERIAL      PRIMARY KEY,
    order_id    UUID           NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    product_id  UUID           NOT NULL REFERENCES products (id),
    quantity    INTEGER        NOT NULL CHECK (quantity > 0),
    unit_price  NUMERIC(19, 2) NOT NULL,
    total_price NUMERIC(19, 2) NOT NULL,
    created_at  TIMESTAMP      NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP      NOT NULL DEFAULT now()
);

-- Payments
CREATE TABLE IF NOT EXISTS payments (
    id               UUID           NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    order_id         UUID           NOT NULL UNIQUE REFERENCES orders (id) ON DELETE CASCADE,
    amount           NUMERIC(19, 2) NOT NULL,
    currency         VARCHAR(10)    NOT NULL DEFAULT 'USD',
    status           VARCHAR(30)    NOT NULL,
    payment_method   VARCHAR(50),
    transaction_id   VARCHAR(255),
    failure_reason   VARCHAR(512),
    created_at       TIMESTAMP      NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP      NOT NULL DEFAULT now()
);

-- Wishlists
CREATE TABLE IF NOT EXISTS wishlists (
    id         UUID      NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id    UUID      NOT NULL UNIQUE REFERENCES users (id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Wishlist Items
CREATE TABLE IF NOT EXISTS wishlist_items (
    id          BIGSERIAL PRIMARY KEY,
    wishlist_id UUID      NOT NULL REFERENCES wishlists (id) ON DELETE CASCADE,
    product_id  UUID      NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    added_at    TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (wishlist_id, product_id)
);

-- Reviews
CREATE TABLE IF NOT EXISTS reviews (
    id         BIGSERIAL PRIMARY KEY,
    user_id    UUID      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    product_id UUID      NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    rating     INTEGER   NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment    TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (user_id, product_id)
);

-- ============================================================
-- Indexes for Performance
-- ============================================================

-- Products
CREATE INDEX IF NOT EXISTS idx_products_category ON products (category_id);
CREATE INDEX IF NOT EXISTS idx_products_active    ON products (active);
CREATE INDEX IF NOT EXISTS idx_products_sku       ON products (sku);
CREATE INDEX IF NOT EXISTS idx_products_price     ON products (price);

-- Orders
CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders (user_id);
CREATE INDEX IF NOT EXISTS idx_orders_status  ON orders (status);
CREATE INDEX IF NOT EXISTS idx_orders_created ON orders (created_at DESC);

-- Order Items
CREATE INDEX IF NOT EXISTS idx_order_items_order   ON order_items (order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_product ON order_items (product_id);

-- Payments
CREATE INDEX IF NOT EXISTS idx_payments_order  ON payments (order_id);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments (status);

-- Refresh Tokens
CREATE INDEX IF NOT EXISTS idx_refresh_token       ON refresh_tokens (token);
CREATE INDEX IF NOT EXISTS idx_refresh_token_user  ON refresh_tokens (user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_token_expiry ON refresh_tokens (expiry_date);

-- Reviews
CREATE INDEX IF NOT EXISTS idx_reviews_product ON reviews (product_id);
CREATE INDEX IF NOT EXISTS idx_reviews_user    ON reviews (user_id);

-- Cart Items
CREATE INDEX IF NOT EXISTS idx_cart_items_cart    ON cart_items (cart_id);
CREATE INDEX IF NOT EXISTS idx_cart_items_product ON cart_items (product_id);

-- Wishlist Items
CREATE INDEX IF NOT EXISTS idx_wishlist_items_wishlist ON wishlist_items (wishlist_id);

-- ============================================================
-- Seed default roles
-- ============================================================
INSERT INTO roles (name) VALUES ('ROLE_CUSTOMER') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('ROLE_ADMIN')    ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('ROLE_SELLER')   ON CONFLICT (name) DO NOTHING;
