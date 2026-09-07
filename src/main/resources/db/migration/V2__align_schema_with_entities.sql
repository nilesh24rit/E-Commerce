-- Align the original production schema with the current UUID-based entities.
-- This migration targets the legacy V1 schema used by the initial deployment.
-- New UUID values are generated for legacy BIGSERIAL identifiers.

ALTER TABLE user_roles DROP CONSTRAINT IF EXISTS user_roles_role_id_fkey;
ALTER TABLE products DROP CONSTRAINT IF EXISTS products_category_id_fkey;
ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_coupon_id_fkey;

ALTER TABLE roles ALTER COLUMN id DROP DEFAULT;
ALTER TABLE roles ALTER COLUMN id TYPE UUID USING gen_random_uuid();
ALTER TABLE categories ALTER COLUMN id DROP DEFAULT;
ALTER TABLE categories ALTER COLUMN id TYPE UUID USING gen_random_uuid();
ALTER TABLE inventory ALTER COLUMN id DROP DEFAULT;
ALTER TABLE inventory ALTER COLUMN id TYPE UUID USING gen_random_uuid();
ALTER TABLE cart_items ALTER COLUMN id DROP DEFAULT;
ALTER TABLE cart_items ALTER COLUMN id TYPE UUID USING gen_random_uuid();
ALTER TABLE coupons ALTER COLUMN id DROP DEFAULT;
ALTER TABLE coupons ALTER COLUMN id TYPE UUID USING gen_random_uuid();
ALTER TABLE order_items ALTER COLUMN id DROP DEFAULT;
ALTER TABLE order_items ALTER COLUMN id TYPE UUID USING gen_random_uuid();
ALTER TABLE refresh_tokens ALTER COLUMN id DROP DEFAULT;
ALTER TABLE refresh_tokens ALTER COLUMN id TYPE UUID USING gen_random_uuid();
ALTER TABLE reviews ALTER COLUMN id DROP DEFAULT;
ALTER TABLE reviews ALTER COLUMN id TYPE UUID USING gen_random_uuid();
ALTER TABLE wishlist_items ALTER COLUMN id DROP DEFAULT;
ALTER TABLE wishlist_items ALTER COLUMN id TYPE UUID USING gen_random_uuid();

ALTER TABLE user_roles ALTER COLUMN role_id TYPE UUID USING gen_random_uuid();
ALTER TABLE products ALTER COLUMN category_id TYPE UUID USING gen_random_uuid();
ALTER TABLE orders ALTER COLUMN coupon_id TYPE UUID USING gen_random_uuid();

ALTER TABLE roles ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE roles ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT now();

ALTER TABLE categories ADD COLUMN IF NOT EXISTS slug VARCHAR(100);
UPDATE categories SET slug = lower(regexp_replace(name, '[^a-zA-Z0-9]+', '-', 'g')) WHERE slug IS NULL;
ALTER TABLE categories ALTER COLUMN slug SET NOT NULL;
ALTER TABLE categories ADD COLUMN IF NOT EXISTS image_url VARCHAR(500);
ALTER TABLE categories ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE inventory RENAME COLUMN quantity TO available_quantity;
ALTER TABLE inventory RENAME COLUMN reserved TO reserved_quantity;
ALTER TABLE inventory ADD COLUMN IF NOT EXISTS sold_quantity INTEGER NOT NULL DEFAULT 0;
ALTER TABLE inventory ADD COLUMN IF NOT EXISTS reorder_level INTEGER NOT NULL DEFAULT 10;
ALTER TABLE inventory ADD COLUMN IF NOT EXISTS warehouse_location VARCHAR(100);
ALTER TABLE inventory ADD COLUMN IF NOT EXISTS last_restocked_at TIMESTAMP;

ALTER TABLE coupons RENAME COLUMN min_order_value TO minimum_order_amount;
ALTER TABLE coupons RENAME COLUMN max_uses TO usage_limit;
ALTER TABLE coupons RENAME COLUMN current_uses TO used_count;
ALTER TABLE coupons RENAME COLUMN expiry_date TO valid_until;
ALTER TABLE coupons ADD COLUMN IF NOT EXISTS description VARCHAR(255);
ALTER TABLE coupons ADD COLUMN IF NOT EXISTS valid_from TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE coupons ALTER COLUMN valid_until SET NOT NULL;

ALTER TABLE orders ADD COLUMN IF NOT EXISTS order_number VARCHAR(255);
UPDATE orders SET order_number = 'ORD-' || id::text WHERE order_number IS NULL;
ALTER TABLE orders ALTER COLUMN order_number SET NOT NULL;
ALTER TABLE orders ADD CONSTRAINT idx_order_number UNIQUE (order_number);
ALTER TABLE orders RENAME COLUMN status TO order_status;
ALTER TABLE orders RENAME COLUMN discount_amount TO discount;
ALTER TABLE orders RENAME COLUMN final_amount TO subtotal;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS shipping_charge NUMERIC(10, 2) NOT NULL DEFAULT 0;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS payment_status VARCHAR(30) NOT NULL DEFAULT 'PENDING';

ALTER TABLE order_items RENAME COLUMN total_price TO subtotal;
ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS revoked BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE refresh_tokens ADD COLUMN IF NOT EXISTS device_identifier VARCHAR(100);
ALTER TABLE reviews ADD COLUMN IF NOT EXISTS title VARCHAR(200);
ALTER TABLE reviews ADD COLUMN IF NOT EXISTS verified_purchase BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE wishlist_items ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE wishlist_items ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT now();

ALTER TABLE user_roles ADD CONSTRAINT user_roles_role_id_fkey FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE;
ALTER TABLE products ADD CONSTRAINT products_category_id_fkey FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE SET NULL;
ALTER TABLE orders ADD CONSTRAINT orders_coupon_id_fkey FOREIGN KEY (coupon_id) REFERENCES coupons (id) ON DELETE SET NULL;

ALTER TABLE products ALTER COLUMN category_id SET NOT NULL;

ALTER TABLE payments RENAME COLUMN status TO payment_status;
ALTER TABLE payments RENAME COLUMN transaction_id TO gateway_transaction_id;
ALTER TABLE payments ADD COLUMN IF NOT EXISTS payment_id VARCHAR(255);
UPDATE payments SET payment_id = 'PAY-' || id::text WHERE payment_id IS NULL;
ALTER TABLE payments ALTER COLUMN payment_id SET NOT NULL;
ALTER TABLE payments ADD COLUMN IF NOT EXISTS gateway_name VARCHAR(255) NOT NULL DEFAULT 'UNKNOWN';
ALTER TABLE payments ADD COLUMN IF NOT EXISTS gateway_response TEXT;
ALTER TABLE payments ALTER COLUMN currency TYPE VARCHAR(3);
ALTER TABLE payments ALTER COLUMN payment_method SET NOT NULL;
ALTER TABLE payments ADD CONSTRAINT idx_payment_payment_id UNIQUE (payment_id);
ALTER TABLE categories ADD CONSTRAINT idx_category_slug UNIQUE (slug);
