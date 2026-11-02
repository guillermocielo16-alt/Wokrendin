#!/bin/bash
set -euo pipefail
cd "$(dirname "$0")/.."
: "${KOTLIN_COMPILER_LIB:?Set compiler jar directory}"
: "${JSON_JAR:?Set org.json JSON-java 20240303 jar path}"
mkdir -p build
java -cp "$KOTLIN_COMPILER_LIB/*" org.jetbrains.kotlin.cli.jvm.K2JVMCompiler -no-stdlib -no-reflect -classpath "$KOTLIN_COMPILER_LIB/stdlib.jar:$JSON_JAR" -d build/storetests.jar app/src/main/java/app/miturno/CalendarRules.kt app/src/main/java/app/miturno/Store.kt tests/host/android/content/Context.kt tests/host/StoreTest.kt
java -cp "build/storetests.jar:$KOTLIN_COMPILER_LIB/stdlib.jar:$JSON_JAR" StoreTestKt
