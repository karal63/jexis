ALTER TABLE users ALTER COLUMN phone_number DROP NOT NULL;
UPDATE users SET phone_number = NULL WHERE phone_number = '';
