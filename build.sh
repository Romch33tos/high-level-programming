#!/usr/bin/env bash
set -e

cd "$(dirname "$0")/.."

SRC_DIR="src"
LIB_DIR="lib"
KOTLINX_CLI_JAR="${LIB_DIR}/kotlinx-cli-jvm-0.3.5.jar"
H2_JAR="${LIB_DIR}/h2-2.2.224.jar"
JAR_NAME="app.jar"
BUILD_DIR="build"

if [ ! -f "${KOTLINX_CLI_JAR}" ] || [ ! -f "${H2_JAR}" ]; then
  echo "Не найдены библиотеки в lib/. Проверьте наличие kotlinx-cli и h2."
  exit 1
fi

rm -rf "${BUILD_DIR}"
mkdir -p "${BUILD_DIR}/classes"
rm -f "${JAR_NAME}"

kotlinc "${SRC_DIR}" \
  -cp "${KOTLINX_CLI_JAR};${H2_JAR}" \
  -include-runtime \
  -d "${BUILD_DIR}/app-with-runtime.jar"

cd "${BUILD_DIR}/classes"
jar xf "../app-with-runtime.jar"
jar xf "../../${KOTLINX_CLI_JAR}"
jar xf "../../${H2_JAR}"
cd - > /dev/null

cat > "${BUILD_DIR}/MANIFEST.MF" <<EOF
Main-Class: MainKt
EOF

jar cfm "${JAR_NAME}" "${BUILD_DIR}/MANIFEST.MF" -C "${BUILD_DIR}/classes" .

rm -rf "${BUILD_DIR}"

echo "Сборка завершена: ${JAR_NAME}"
