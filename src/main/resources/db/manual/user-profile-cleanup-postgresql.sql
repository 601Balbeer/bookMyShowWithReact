-- Run once against an existing PostgreSQL database after backing up data.
-- User locations are not needed for booking or theater management; theater
-- locations remain stored on the theaters table.
ALTER TABLE users DROP COLUMN IF EXISTS location_sys_id;

-- Customer tiers and points have no implemented rewards behavior.
ALTER TABLE customers DROP COLUMN IF EXISTS loyalty_points;
ALTER TABLE customers DROP COLUMN IF EXISTS membership_type;
ALTER TABLE customers ADD COLUMN IF NOT EXISTS date_of_birth DATE;

-- Store the owner organization's display name alongside its registration data.
ALTER TABLE "theatre-owners"
    ADD COLUMN IF NOT EXISTS business_name VARCHAR(255);
