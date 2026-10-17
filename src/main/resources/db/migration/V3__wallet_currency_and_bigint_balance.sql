-- Existing wallets were created exclusively in USD. Preserve minor-unit amounts.
ALTER TABLE wallets ALTER COLUMN available_balance TYPE bigint;
ALTER TABLE wallets ADD COLUMN currency varchar(3);
UPDATE wallets SET currency = 'USD';
ALTER TABLE wallets ALTER COLUMN currency SET NOT NULL;
ALTER TABLE wallets ADD CONSTRAINT wallets_currency_supported CHECK (currency = 'USD');
