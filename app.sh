#!/usr/bin/env bash

case "${1}" in
  run)
    echo "==> Building backend..."
    (cd backend && mvn -q clean package)

    echo "==> Building images and starting Postgres + backend + frontend..."
    docker compose up --build -d

    echo
    echo "Frontend: http://localhost:8081"
    echo "Backend:  http://localhost:8080/api"
    ;;
  stop)
    echo "==> Stopping the stack..."
    docker compose down
    ;;
  *)
    echo "Usage: $0 [run|stop]" >&2
    exit 1
    ;;
esac
