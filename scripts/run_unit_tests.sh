#!/usr/bin/env bash
set -e

cd "$(dirname "$0")/.."

LIB_DIR="lib"
OUT_DIR="out_test"

JUNIT_STANDALONE=$(ls "$LIB_DIR"/junit-platform-console-standalone-*.jar | head -n1)
KOTLINX_CLI_JAR="${LIB_DIR}/kotlinx-cli-jvm-0.3.5.jar"
KOTLIN_STDLIB=$(ls "$LIB_DIR"/kotlin-stdlib-*.jar | head -n1)

if [ -z "$KOTLIN_STDLIB" ]; then
  echo "ОШИБКА: Не найден kotlin-stdlib.jar в папке lib/"
  echo "Скачайте kotlin-stdlib с Maven Central и положите в lib/"
  exit 1
fi

CP="${OUT_DIR};${KOTLINX_CLI_JAR};${KOTLIN_STDLIB}"

if [ ! -d "$OUT_DIR" ]; then
  echo "Сначала выполните ./scripts/build_tests.sh"
  exit 1
fi

echo "Запуск модульных тестов JUnit..."
java -jar "$JUNIT_STANDALONE" \
  --class-path "$CP" \
  --scan-class-path
