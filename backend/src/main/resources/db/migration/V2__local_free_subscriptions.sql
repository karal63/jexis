ALTER TABLE plans ALTER COLUMN stripe_plan_id DROP NOT NULL;
ALTER TABLE prices ALTER COLUMN stripe_price_id DROP NOT NULL;
ALTER TABLE subscriptions ALTER COLUMN stripe_subscription_id DROP NOT NULL;
ALTER TABLE subscriptions ALTER COLUMN current_period_end DROP NOT NULL;

-- Preserve a pre-existing free plan and its configured price/entitlements.
INSERT INTO plans (id, code, name, description, active, status, created_at)
VALUES ('84f3d9c6-321c-4d22-b70d-08dcb3730df1', 'free', 'Free',
        'Default local plan for verified account owners', true, 'PUBLISHED', CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

INSERT INTO prices (id, plan_id, currency, unit_amount, interval, interval_count, active, created_at)
SELECT '84f3d9c6-321c-4d22-b70d-08dcb3730df2', id, 'usd', 0, 'month', 1, true, CURRENT_TIMESTAMP
FROM plans WHERE code = 'free' AND default_price_id IS NULL;

UPDATE plans SET default_price_id = '84f3d9c6-321c-4d22-b70d-08dcb3730df2'
WHERE code = 'free' AND default_price_id IS NULL;

CREATE UNIQUE INDEX subscriptions_one_active_local_per_account
    ON subscriptions (account_id)
    WHERE stripe_subscription_id IS NULL AND status = 'ACTIVE';
