-- Store the cart-item amount required by the CartItem entity.
ALTER TABLE cart_items
    ADD COLUMN IF NOT EXISTS subtotal NUMERIC(10, 2);

UPDATE cart_items
SET subtotal = unit_price * quantity
WHERE subtotal IS NULL;

ALTER TABLE cart_items
    ALTER COLUMN subtotal SET NOT NULL;
