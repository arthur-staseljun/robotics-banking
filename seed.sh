#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
TX_COUNT="${1:-100}"

json() { curl -s -X POST "$1" -H 'Content-Type: application/json' -d "$2"; }
extract_id() { sed -n 's/.*"id":[[:space:]]*\([0-9]*\).*/\1/p'; }

echo "Seeding against $BASE_URL"

USER_ID=$(json "$BASE_URL/api/users" \
  "{\"name\":\"Test User\",\"email\":\"test+$RANDOM@example.com\"}" | extract_id)
echo "user          = $USER_ID"

EUR_ID=$(json "$BASE_URL/api/users/$USER_ID/accounts" '{"currency":"EUR"}' | extract_id)
USD_ID=$(json "$BASE_URL/api/users/$USER_ID/accounts" '{"currency":"USD"}' | extract_id)
GBP_ID=$(json "$BASE_URL/api/users/$USER_ID/accounts" '{"currency":"GBP"}' | extract_id)
echo "accounts      = EUR:$EUR_ID USD:$USD_ID GBP:$GBP_ID"

echo "Funding EUR with deposits + debits ($TX_COUNT ops) ..."
for i in $(seq 1 "$TX_COUNT"); do
  if (( i % 6 == 0 )); then
    json "$BASE_URL/api/accounts/$EUR_ID/debit" \
      "{\"amount\":5,\"description\":\"debit $i\"}" >/dev/null
  else
    json "$BASE_URL/api/accounts/$EUR_ID/deposit" \
      "{\"amount\":20,\"description\":\"deposit $i\"}" >/dev/null
  fi
done

echo "Exchanging EUR -> USD -> GBP (creates EXCHANGE_OUT / EXCHANGE_IN) ..."
for i in $(seq 1 5); do
  json "$BASE_URL/api/accounts/exchange" \
    "{\"fromAccountId\":$EUR_ID,\"toAccountId\":$USD_ID,\"amount\":10,\"description\":\"exchange $i\"}" >/dev/null
  json "$BASE_URL/api/accounts/exchange" \
    "{\"fromAccountId\":$USD_ID,\"toAccountId\":$GBP_ID,\"amount\":5,\"description\":\"exchange $i\"}" >/dev/null
done

echo "Done."
echo
echo "Home page:    http://localhost:4200          (lists 3 accounts)"
echo "Account page: /accounts/$EUR_ID              (scroll + all 4 tx types)"
echo "Note: run on a fresh DB (docker compose down -v && docker compose up -d)"
echo "      so the user is id 1 (the Home page is hardcoded to user 1)."
