#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
: "${KOTLIN_COMPILER_LIB:?Set Kotlin compiler jar directory}"
: "${ORIGINAL_HTML:?Set path to original index.html}"
TZ=America/Mexico_City node tests/compare-original.js "$ORIGINAL_HTML" tests/original-expected.txt
mkdir -p build
java -cp "$KOTLIN_COMPILER_LIB/*" org.jetbrains.kotlin.cli.jvm.K2JVMCompiler -no-stdlib -no-reflect -classpath "$KOTLIN_COMPILER_LIB/stdlib.jar" -d build/rules-test.jar app/src/main/java/app/miturno/CalendarRules.kt tests/RulesTest.kt
java -cp "build/rules-test.jar:$KOTLIN_COMPILER_LIB/stdlib.jar" RulesTestKt tests/original-expected.txt
