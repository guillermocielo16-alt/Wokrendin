#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
: "${KOTLIN_COMPILER_LIB:?Set Kotlin compiler jar directory}"
mkdir -p build
java -cp "$KOTLIN_COMPILER_LIB/*" org.jetbrains.kotlin.cli.jvm.K2JVMCompiler -no-stdlib -no-reflect \
  -classpath "$KOTLIN_COMPILER_LIB/stdlib.jar" -d build/calendar-model-tests.jar \
  app/src/main/java/app/miturno/ReminderModels.kt tests/calendar/ReminderModelsTest.kt
java -cp "build/calendar-model-tests.jar:$KOTLIN_COMPILER_LIB/stdlib.jar" app.miturno.ReminderModelsTestKt
