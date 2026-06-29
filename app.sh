#!/usr/bin/env bash

case "${1}" in
  run)
    echo "==> Starting Postgres..."
    docker compose up -d --wait

    echo "==> Starting application..."
    exec mvn spring-boot:run
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
