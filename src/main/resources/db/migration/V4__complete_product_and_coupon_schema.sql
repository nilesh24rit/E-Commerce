-- Add fields required by the current Product and Coupon entities.
ALTER TABLE products
    ADD COLUMN IF NOT EXISTS slug VARCHAR(200),
    ADD COLUMN IF NOT EXISTS short_description VARCHAR(500),
    ADD COLUMN IF NOT EXISTS brand VARCHAR(100),
    ADD COLUMN IF NOT EXISTS discount_price NUMERIC(10, 2),
    ADD COLUMN IF NOT EXISTS quantity INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS status VARCHAR(50) NOT NULL DEFAULT 'IN_STOCK',
    ADD COLUMN IF NOT EXISTS average_rating DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    ADD COLUMN IF NOT EXISTS total_reviews BIGINT NOT NULL DEFAULT 0;

UPDATE products
SET slug = LEFT(
        COALESCE(NULLIF(regexp_replace(lower(name), '[^a-z0-9]+', '-', 'g'), ''), 'product')
        || '-' || LEFT(id::text, 8),
        200
    )
WHERE slug IS NULL;

ALTER TABLE products
    ALTER COLUMN slug SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS idx_product_slug ON products (slug);

ALTER TABLE coupons
    ADD COLUMN IF NOT EXISTS maximum_discount_amount NUMERIC(10, 2);