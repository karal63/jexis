-- Do not infer merchant values from wallet-side amounts for historical transactions.
-- Bank transactions and card transactions without provider data leave these null.
ALTER TABLE transactions ADD COLUMN merchant_amount bigint;
ALTER TABLE transactions ADD COLUMN merchant_currency varchar(3);
