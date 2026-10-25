#!/usr/bin/env bash
set -e

cd "$(dirname "$0")/.."

SRC_DIR="src"
TEST_DIR="test"
LIB_DIR="lib"
OUT_DIR="out_test"

JUNIT_STANDALONE=$(ls "$LIB_DIR"/junit-platform-console-standalone-*.jar | head -n1)
KOTLINX_CLI_JAR="${LIB_DIR}/kotlinx-cli-jvm-0.3.5.jar"

echo "Компиляция основного кода и тестов..."
rm -rf "$OUT_DIR"
mkdir -p "$OUT_DIR"

kotlinc \
  -cp "$JUNIT_STANDALONE:$KOTLINX_CLI_JAR" \
  -include-runtime \
  -d "$OUT_DIR" \
  $(find "$SRC_DIR" "$TEST_DIR" -name '*.kt')

echo "Компиляция тестов завершена. Классы в $OUT_DIR"
