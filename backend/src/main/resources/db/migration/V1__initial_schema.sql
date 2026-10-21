-- Initial PostgreSQL schema from the current JPA mappings.
-- Includes the card replacement constraints from docs/sql/account-resource-limits.sql.

create table accounts (
    is_deleted boolean not null,
    created_at timestamp(6) not null,
    deleted_at timestamp(6),
    updated_at timestamp(6),
    id uuid not null,
    owner_id uuid not null,
    account_link varchar(255) not null unique,
    city varchar(255),
    connect_account_id varchar(255) not null unique,
    country varchar(255),
    email varchar(255),
    first_name varchar(255),
    last_name varchar(255),
    line1 varchar(255),
    line2 varchar(255),
    phone varchar(255),
    postal_code varchar(255),
    state varchar(255),
    primary key (id)
);

create table authorizations (
    approved boolean,
    amount bigint not null,
    created_at timestamp(6),
    updated_at timestamp(6),
    card_id uuid not null,
    id uuid not null,
    wallet_id uuid not null,
    currency varchar(255) not null,
    merchant_category varchar(255),
    merchant_city varchar(255),
    merchant_country varchar(255),
    merchant_name varchar(255),
    status varchar(255) not null check ((status in ('PENDING','CLOSED','REVERSED'))),
    stripe_authorization_id varchar(255) not null,
    primary key (id)
);

create table card_holders (
    is_deleted boolean not null,
    created_at timestamp(6) not null,
    deleted_at timestamp(6),
    updated_at timestamp(6),
    account_id uuid not null,
    id uuid not null,
    user_id uuid not null,
    billing_address_line1 varchar(255) not null,
    billing_address_line2 varchar(255),
    billing_city varchar(255) not null,
    billing_country varchar(255) not null,
    billing_postal_code varchar(255) not null,
    billing_state varchar(255) not null,
    name varchar(255) not null,
    status varchar(255) not null check ((status in ('active','inactive'))),
    stripe_card_holder_id varchar(255) not null,
    spending_limits jsonb,
    primary key (id)
);

create table cards (
    is_deleted boolean not null,
    replacement_pending boolean default false not null,
    created_at timestamp(6) not null,
    deleted_at timestamp(6),
    exp_year bigint not null,
    updated_at timestamp(6),
    card_holder_id uuid not null,
    id uuid not null,
    replacement_card_id uuid unique,
    stripe_financial_account_id uuid not null,
    user_id uuid not null,
    brand varchar(255) not null,
    currency varchar(255) not null,
    last4 varchar(255) not null,
    replacement_reason varchar(255) check ((replacement_reason in ('damaged','expired','lost','stolen'))),
    status varchar(255) not null check ((status in ('active','canceled','inactive'))),
    stripe_card_id varchar(255) not null,
    type varchar(255) not null,
    spending_limits jsonb,
    primary key (id)
);

create table disputes (
    amount bigint not null,
    created_at timestamp(6),
    resolved_at timestamp(6),
    id uuid not null,
    transaction_id uuid not null unique,
    wallet_id uuid not null,
    currency varchar(255) not null,
    reason varchar(255) check ((reason in ('canceled','duplicate','fraudulent','merchandise_not_as_described','no_valid_authorization','not_received','other','service_not_as_described'))),
    status varchar(255) not null check ((status in ('NEEDS_RESPONSE','WARNING_NEEDS_RESPONSE','UNDER_REVIEW','WON','LOST'))),
    stripe_dispute_id varchar(255) not null unique,
    primary key (id)
);

create table entitlements (
    created_at timestamp(6) not null,
    id uuid not null,
    description varchar(255),
    key varchar(255) not null unique,
    type varchar(255) not null,
    primary key (id)
);

create table external_accounts (
    is_default boolean not null,
    is_deleted boolean not null,
    created_at timestamp(6) not null,
    deleted_at timestamp(6),
    account_id uuid not null,
    id uuid not null,
    bank_name varchar(255) not null,
    country varchar(255) not null,
    currency varchar(255) not null,
    last4 varchar(255) not null,
    stripe_external_account_id varchar(255) not null unique,
    primary key (id)
);

create table invoices (
    amount_paid bigint not null,
    created_at timestamp(6) not null,
    id uuid not null,
    subscription_id uuid not null,
    currency varchar(255) not null,
    hosted_invoice_url varchar(255),
    invoice_pdf varchar(255),
    status varchar(255) not null check ((status in ('PAID','OPEN'))),
    stripe_invoice_id varchar(255) not null unique,
    primary key (id)
);

create table members (
    account_id uuid not null,
    id uuid not null,
    user_id uuid not null,
    role varchar(255) not null check ((role in ('OWNER','ADMIN','MEMBER'))),
    primary key (id)
);

create table password_reset_tokens (
    created_at timestamp(6) not null,
    expires_at timestamp(6) not null,
    used_at timestamp(6),
    id uuid not null,
    user_id uuid not null,
    token_hash varchar(255) not null unique,
    primary key (id)
);

create table plan_entitlements (
    created_at timestamp(6) not null,
    entitlement_id uuid not null,
    id uuid not null,
    plan_id uuid not null,
    value varchar(255) not null,
    primary key (id)
);

create table plans (
    active boolean not null,
    created_at timestamp(6) not null,
    default_price_id uuid,
    id uuid not null,
    code varchar(255) not null unique,
    description varchar(255),
    name varchar(255) not null,
    status varchar(255) check ((status in ('DRAFT','PUBLISHED'))),
    stripe_plan_id varchar(255) not null unique,
    primary key (id)
);

create table prices (
    active boolean not null,
    interval_count integer not null,
    created_at timestamp(6) not null,
    unit_amount bigint not null,
    id uuid not null,
    plan_id uuid not null,
    currency varchar(255) not null,
    interval varchar(255) not null,
    stripe_price_id varchar(255) not null unique,
    primary key (id)
);

create table subscription_entitlements (
    created_at timestamp(6) not null,
    entitlement_id uuid not null,
    id uuid not null,
    subscription_id uuid not null,
    value varchar(255) not null,
    primary key (id)
);

create table subscriptions (
    cancel_at_period_end boolean not null,
    canceled_at timestamp(6),
    created_at timestamp(6) not null,
    current_period_end timestamp(6) not null,
    current_period_start timestamp(6) not null,
    updated_at timestamp(6),
    account_id uuid not null,
    id uuid not null,
    plan_id uuid not null,
    price_id uuid not null,
    scheduled_plan_id uuid,
    user_id uuid not null,
    status varchar(255) not null check ((status in ('INCOMPLETE','INCOMPLETE_EXPIRED','TRIALING','ACTIVE','PAST_DUE','CANCELED','UNPAID','PAUSED'))),
    stripe_payment_method_id varchar(255),
    stripe_schedule_id varchar(255),
    stripe_subscription_id varchar(255) not null unique,
    primary key (id)
);

create table transactions (
    amount bigint not null,
    created_at timestamp(6),
    expected_arrival_date timestamp(6),
    authorization_id uuid,
    card_id uuid,
    id uuid not null,
    wallet_id uuid not null,
    bank_account_last4 varchar(255),
    bank_name varchar(255),
    currency varchar(255) not null,
    direction varchar(255) not null check ((direction in ('DEBIT','CREDIT'))),
    merchant_category varchar(255),
    merchant_city varchar(255),
    merchant_country varchar(255),
    merchant_name varchar(255),
    payment_method varchar(255) check ((payment_method in ('ACH','DOMESTIC_WIRE','CARD','INTERNAL'))),
    routing_number varchar(255),
    status varchar(255) not null check ((status in ('COMPLETED','FAILED','CANCELED','PROCESSING'))),
    stripe_object_id varchar(255) unique,
    stripe_transaction_id varchar(255),
    type varchar(255) not null check ((type in ('INBOUND_TRANSFER','OUTBOUND_TRANSFER','CARD_AUTHORIZATION','CARD_TRANSACTION','CARD_REFUND','CARD_REVERSAL','FEE','ADJUSTMENT'))),
    primary key (id)
);

create table user_roles (
    user_id uuid not null,
    role varchar(255) check ((role in ('ADMIN','MODERATOR','USER')))
);

create table users (
    is_activated boolean not null,
    is_deleted boolean not null,
    created_at timestamp(6) not null,
    deleted_at timestamp(6),
    updated_at timestamp(6),
    id uuid not null,
    activation_token_hash varchar(255) unique,
    email varchar(255) not null unique,
    first_name varchar(255) not null,
    last_name varchar(255) not null,
    password varchar(255) not null,
    phone_number varchar(255) not null unique,
    stripe_customer_id varchar(255) unique,
    primary key (id)
);

create table wallets (
    available_balance integer not null,
    is_deleted boolean not null,
    created_at timestamp(6) not null,
    deleted_at timestamp(6),
    updated_at timestamp(6),
    account_id uuid not null,
    id uuid not null,
    name varchar(255) not null,
    stripe_financial_account_id varchar(255) not null,
    primary key (id)
);

alter table if exists accounts 
   add constraint FKjln86358moqf5k5pw89oiq8ur 
   foreign key (owner_id) 
   references users;

alter table if exists authorizations 
   add constraint FK94e0cbja7x0m1laa2iqibpmky 
   foreign key (card_id) 
   references cards;

alter table if exists authorizations 
   add constraint FKst6hosmyksshauv3vbg1mu14v 
   foreign key (wallet_id) 
   references wallets;

alter table if exists card_holders 
   add constraint FKoo07sbyd31vko8h2vyvpsn5y7 
   foreign key (account_id) 
   references accounts;

alter table if exists card_holders 
   add constraint FK4n8cvao3mogfrvhexi8heyssp 
   foreign key (user_id) 
   references users;

alter table if exists cards 
   add constraint FKk0h32i8s6odq7flpjm77i4iir 
   foreign key (card_holder_id) 
   references card_holders;

alter table if exists cards 
   add constraint FK48ld6k63ywtylw7h1dei3xniw 
   foreign key (replacement_card_id) 
   references cards;

alter table if exists cards 
   add constraint FKld1pb5hiaqlp2lo3cf67f6vx5 
   foreign key (stripe_financial_account_id) 
   references wallets;

alter table if exists cards 
   add constraint FKcmanafgwbibfijy2o5isfk3d5 
   foreign key (user_id) 
   references users;

alter table if exists disputes 
   add constraint FKqrmkcxv1ndg8mea82b7h40ejk 
   foreign key (transaction_id) 
   references transactions;

alter table if exists disputes 
   add constraint FKobhfo25hyuhao4r1he9qp2qsl 
   foreign key (wallet_id) 
   references wallets;

alter table if exists external_accounts 
   add constraint FKbe3ify3viyfd2vw9a9825v40 
   foreign key (account_id) 
   references accounts;

alter table if exists invoices 
   add constraint FKekyltlmwbronx3ubn5it0346p 
   foreign key (subscription_id) 
   references subscriptions;

alter table if exists members 
   add constraint FKfyrcxh2hd0bgubuqgh4x5365m 
   foreign key (account_id) 
   references accounts;

alter table if exists members 
   add constraint FKpj3n6wh5muoeakc485whgs3x5 
   foreign key (user_id) 
   references users;

alter table if exists password_reset_tokens 
   add constraint FKk3ndxg5xp6v7wd4gjyusp15gq 
   foreign key (user_id) 
   references users;

alter table if exists plan_entitlements 
   add constraint FKd75gdwsiemv4302r3wlwamek6 
   foreign key (entitlement_id) 
   references entitlements;

alter table if exists plan_entitlements 
   add constraint FKblnefun3vyc44hps0lh0hyymh 
   foreign key (plan_id) 
   references plans;

alter table if exists plans 
   add constraint FK6ykysi8rxbltbmq7yt88j9rjc 
   foreign key (default_price_id) 
   references prices;

alter table if exists prices 
   add constraint FK40ffwicswko580av3x9jbivpp 
   foreign key (plan_id) 
   references plans;

alter table if exists subscription_entitlements 
   add constraint FKiebl5k6drajvfj2yuv8bfg672 
   foreign key (entitlement_id) 
   references entitlements;

alter table if exists subscription_entitlements 
   add constraint FKniclriwo4dcqyivq9sjd3aksb 
   foreign key (subscription_id) 
   references subscriptions;

alter table if exists subscriptions 
   add constraint FK2i5t5e392cxe52a84xmkdl0os 
   foreign key (account_id) 
   references accounts;

alter table if exists subscriptions 
   add constraint FKb1uf5qnxi6uj95se8ykydntl1 
   foreign key (plan_id) 
   references plans;

alter table if exists subscriptions 
   add constraint FK8bgqqpwcr25magbcxvo8j393j 
   foreign key (price_id) 
   references prices;

alter table if exists subscriptions 
   add constraint FKo25xehuekcop28nimk52h2ebb 
   foreign key (scheduled_plan_id) 
   references plans;

alter table if exists subscriptions 
   add constraint FKhro52ohfqfbay9774bev0qinr 
   foreign key (user_id) 
   references users;

alter table if exists transactions 
   add constraint FKascp0ydf5se2g8kyfpduwjbv1 
   foreign key (authorization_id) 
   references authorizations;

alter table if exists transactions 
   add constraint FKjxdscq0bxpy0pl465vvsqc89j 
   foreign key (card_id) 
   references cards;

alter table if exists transactions 
   add constraint FK23bop5lktue0o5q7kr19ti8h 
   foreign key (wallet_id) 
   references wallets;

alter table if exists user_roles 
   add constraint FKhfh9dx7w3ubf1co1vdev94g3f 
   foreign key (user_id) 
   references users;

alter table if exists wallets 
   add constraint FKbfd8hbcxjeqsesps653fg81hk 
   foreign key (account_id) 
   references accounts;
