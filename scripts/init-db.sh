#!/usr/bin/env bash
set -e

cd "$(dirname "$0")/.."

LIB_DIR="lib"
SQL_DIR="scripts/sql"
DATA_DIR="data"

H2_JAR=$(ls "$LIB_DIR"/h2-*.jar | head -n1)

if [ -z "$H2_JAR" ]; then
  echo "ОШИБКА: Не найден h2-*.jar в папке lib/"
  echo "Скачайте H2 Database с Maven Central и положите в lib/"
  exit 1
fi

mkdir -p "$DATA_DIR"
