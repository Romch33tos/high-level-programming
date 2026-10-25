#!/usr/bin/env bash
set -e

cd "$(dirname "$0")/.."

LIB_DIR="lib"
OUT_DIR="out_test"

JUNIT_STANDALONE=$(ls "$LIB_DIR"/junit-platform-console-standalone-*.jar | head -n1)
KOTLINX_CLI_JAR="${LIB_DIR}/kotlinx-cli-jvm-0.3.5.jar"

if [ ! -d "$OUT_DIR" ]; then
  echo "Сначала выполните ./scripts/build_tests.sh"
  exit 1
fi

echo "Запуск модульных тестов JUnit..."
java -jar "$JUNIT_STANDALONE" \
  --class-path "$OUT_DIR:$KOTLINX_CLI_JAR" \
  --scan-class-path
