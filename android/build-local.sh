#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
: "${ANDROID_SDK_ROOT:?Set ANDROID_SDK_ROOT to an SDK with platform 35 and build-tools 35.0.0}"
: "${KOTLIN_COMPILER_LIB:?Set KOTLIN_COMPILER_LIB to directory containing Kotlin compiler jars}"
SDK="$ANDROID_SDK_ROOT"; BT="$SDK/build-tools/35.0.0"; KLIB="$KOTLIN_COMPILER_LIB"; BUILD="$PWD/build"
mkdir -p "$BUILD/classes" "$BUILD/dex"
java -cp "$KLIB/*" org.jetbrains.kotlin.cli.jvm.K2JVMCompiler -no-stdlib -no-reflect -jvm-target 17 -classpath "$SDK/platforms/android-35/android.jar:$KLIB/stdlib.jar:$KLIB/annotations.jar" -d "$BUILD/classes" app/src/main/java/app/miturno/*.kt
"$BT/aapt2" compile --dir app/src/main/res -o "$BUILD/resources.zip"
sed 's/<manifest /<manifest package="app.miturno" /' app/src/main/AndroidManifest.xml > "$BUILD/AndroidManifest.xml"
"$BT/aapt2" link -o "$BUILD/base.apk" -I "$SDK/platforms/android-35/android.jar" --manifest "$BUILD/AndroidManifest.xml" --min-sdk-version 26 --target-sdk-version 35 --version-code 1 --version-name 1.0.0 -A app/src/main/assets "$BUILD/resources.zip"
(cd "$BUILD/classes"; zip -qr "$BUILD/classes.jar" .)
"$BT/d8" --lib "$SDK/platforms/android-35/android.jar" --min-api 26 --output "$BUILD/dex" "$BUILD/classes.jar" "$KLIB/stdlib.jar" "$KLIB/annotations.jar"
cp "$BUILD/base.apk" "$BUILD/unsigned.apk"
(cd "$BUILD/dex"; zip -q "$BUILD/unsigned.apk" classes*.dex)
"$BT/zipalign" -f -p 4 "$BUILD/unsigned.apk" "$BUILD/aligned.apk"
# Reuse the original key for updates. Never generate a replacement silently.
: "${MI_TURNO_KEYSTORE:?Set the local original keystore path}"
: "${MI_TURNO_STORE_PASSWORD:?Set keystore password locally}"
: "${MI_TURNO_KEY_PASSWORD:?Set key password locally}"
KEY_ALIAS="${MI_TURNO_KEY_ALIAS:-miturno}"
"$BT/apksigner" sign --ks "$MI_TURNO_KEYSTORE" --ks-pass env:MI_TURNO_STORE_PASSWORD --key-pass env:MI_TURNO_KEY_PASSWORD --ks-key-alias "$KEY_ALIAS" --out "$BUILD/Mi-Turno-Android-1.0.0.apk" "$BUILD/aligned.apk"
"$BT/apksigner" verify --verbose "$BUILD/Mi-Turno-Android-1.0.0.apk"
