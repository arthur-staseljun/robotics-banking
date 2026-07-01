#!/usr/bin/env bash

case "${1}" in
  run)
    echo "==> Building backend..."
    (cd backend && mvn -q clean package)

    echo "==> Starting Postgres + backend..."
    exec docker compose up --build -d
    ;;
  stop)
    echo "==> Stopping Postgres..."
    docker compose down
    ;;
  *)
    echo "Usage: $0 [run|stop]" >&2
    exit 1
    ;;
esac
