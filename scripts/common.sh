#!/usr/bin/env bash
set -euo pipefail
ROOT=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
cd "$ROOT"
fail() { printf '%s\n' "$*" >&2; exit 1; }

load_environment() {
    MODE=${1:-}
    case "$MODE" in dev|prod) ;; *) fail 'Usage: select dev or prod' ;; esac
    ENV_FILE="$ROOT/.env.$MODE"
    [[ -f "$ENV_FILE" ]] || fail "Create .env.$MODE from .env.$MODE.example first."
    # A literal KEY=value format, never shell source/eval. Values are not expanded.
    LOADED_KEYS=" "
    local line key value line_number=0
    while IFS= read -r line || [[ -n "$line" ]]; do
        line_number=$((line_number + 1))
        line=${line%$'\r'}
        [[ -z "$line" || "$line" == \#* ]] && continue
        [[ "$line" == *=* ]] || fail "Invalid environment line $line_number (expected KEY=value)."
        key=${line%%=*}; value=${line#*=}
        [[ "$key" =~ ^[A-Z][A-Z0-9_]*$ ]] || fail "Invalid environment key on line $line_number."
        case "$key" in
            DB_*|RABBITMQ_*|REDIS_URI|APP_PORT|APP_ORIGIN|PUBLIC_API_URL|JWT_*|STRIPE_*|MAIL_*|CADDY_ADDRESS) ;;
            *) fail "Unsupported environment key: $key" ;;
        esac
        [[ "$value" != \"* && "$value" != \'* && "$value" != *'${'* ]] || fail "Use an unquoted literal value for $key; variable references are unsupported."
        [[ "$LOADED_KEYS" != *" $key "* ]] || fail "Duplicate environment key: $key"
        LOADED_KEYS="$LOADED_KEYS$key "
        [[ "$value" != [[:space:]]* && "$value" != *[[:space:]] ]] || fail "Remove leading/trailing whitespace from $key."
        export "$key=$value"
    done < "$ENV_FILE"
    # Do not inherit a profile or Spring config location from the calling shell.
    unset SPRING_PROFILES_ACTIVE SPRING_PROFILES_DEFAULT SPRING_CONFIG_IMPORT SPRING_CONFIG_LOCATION SPRING_CONFIG_ADDITIONAL_LOCATION
    export SPRING_PROFILES_ACTIVE="$MODE"
}

require_config() {
    local key
    for key in DB_NAME DB_URL DB_USERNAME DB_PASSWORD RABBITMQ_HOST RABBITMQ_PORT RABBITMQ_USERNAME RABBITMQ_PASSWORD APP_PORT APP_ORIGIN PUBLIC_API_URL JWT_ACCESS_SECRET JWT_REFRESH_SECRET STRIPE_API_KEY STRIPE_ACCOUNT_WEBHOOK STRIPE_WALLET_WEBHOOK STRIPE_CARD_WEBHOOK STRIPE_AUTHORIZATION_WEBHOOK STRIPE_DISPUTE_WEBHOOK STRIPE_SUBSCRIPTION_WEBHOOK STRIPE_INVOICE_WEBHOOK MAIL_HOST MAIL_PORT MAIL_USERNAME MAIL_PASSWORD; do
        [[ "$LOADED_KEYS" == *" $key "* && -n "${!key:-}" && "${!key}" != CHANGE_ME* ]] || fail "Set $key in .env.$MODE."
    done
    if [[ "$MODE" == prod ]]; then
        [[ -n "${CADDY_ADDRESS:-}" ]] || fail 'Set CADDY_ADDRESS in .env.prod.'
        [[ "$APP_PORT" == 3000 && "$RABBITMQ_HOST" == rabbitmq && "$RABBITMQ_PORT" == 5672 && "$DB_URL" == "jdbc:postgresql://postgres:5432/$DB_NAME" ]] || fail 'PROD must use internal port 3000 and the postgres/rabbitmq Docker services; see .env.prod.example.'
    fi
}

require_docker() {
    command -v docker >/dev/null || fail 'Install Docker with Compose v2 first.'
    docker compose version >/dev/null || fail 'Install Docker Compose v2.'
    docker info >/dev/null 2>&1 || fail 'Docker is not running. Start Docker first.'
}

compose() {
    # Loaded values take precedence over Compose interpolation. Container env_file
    # uses raw format (Compose >=2.30), preserving dollar signs, hashes and slashes.
    docker compose --project-name "jexis-$MODE" --env-file "$ENV_FILE" -f "$ROOT/compose.$MODE.yml" "$@"
}
