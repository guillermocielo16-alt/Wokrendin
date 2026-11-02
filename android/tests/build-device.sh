#!/bin/bash
set -e
cd "$(dirname "$0")/.."
: "${ANDROID_SDK_ROOT:?Set Android SDK root}"
: "${KOTLIN_COMPILER_LIB:?Set Kotlin compiler directory}"
SDK="$ANDROID_SDK_ROOT"; BT="$SDK/build-tools/35.0.0"; B="$PWD/build"; K="$KOTLIN_COMPILER_LIB"
mkdir -p "$B/testclasses" "$B/testdex"
java -cp "$K/*" org.jetbrains.kotlin.cli.jvm.K2JVMCompiler -no-stdlib -no-reflect -jvm-target 17 -classpath "$SDK/platforms/android-35/android.jar:$K/stdlib.jar:$B/classes.jar" -d "$B/testclasses" tests/device/ValidationRunner.kt
(cd "$B/testclasses";zip -qr "$B/tests.jar" .)
"$BT/aapt2" link -o "$B/tests-base.apk" -I "$SDK/platforms/android-35/android.jar" --manifest tests/device/AndroidManifest.xml
"$BT/d8" --lib "$SDK/platforms/android-35/android.jar" --classpath "$B/classes.jar" --min-api 26 --output "$B/testdex" "$B/tests.jar"
(cd "$B/testdex";zip -q "$B/tests-base.apk" classes*.dex)
"$BT/zipalign" -f 4 "$B/tests-base.apk" "$B/tests-aligned.apk"
: "${MI_TURNO_KEYSTORE:?Set the same keystore used for the application}"
: "${MI_TURNO_STORE_PASSWORD:?Set keystore password locally}"
: "${MI_TURNO_KEY_PASSWORD:?Set key password locally}"
"$BT/apksigner" sign --ks "$MI_TURNO_KEYSTORE" --ks-pass env:MI_TURNO_STORE_PASSWORD --key-pass env:MI_TURNO_KEY_PASSWORD --ks-key-alias "${MI_TURNO_KEY_ALIAS:-miturno}" --out "$B/tests.apk" "$B/tests-aligned.apk"
