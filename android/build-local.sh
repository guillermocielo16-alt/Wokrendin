#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
: "${ANDROID_SDK_ROOT:?Set ANDROID_SDK_ROOT to an SDK with platform 35 and build-tools 35.0.0}"
: "${KOTLIN_COMPILER_LIB:?Set KOTLIN_COMPILER_LIB to directory containing Kotlin compiler jars}"
SDK="$ANDROID_SDK_ROOT"; BT="$SDK/build-tools/35.0.0"; KLIB="$KOTLIN_COMPILER_LIB"; BUILD="$PWD/build"
mkdir -p "$BUILD"
WORK="$(mktemp -d "$BUILD/local.XXXXXX")"
trap 'rm -r -- "$WORK"' EXIT
mkdir -p "$WORK/classes" "$WORK/dex"
VERSION_CODE="$(awk '/^ *versionCode = / { print $3; exit }' app/build.gradle.kts)"
VERSION_NAME="$(awk -F '"' '/^ *versionName = / { print $2; exit }' app/build.gradle.kts)"
: "${VERSION_CODE:?Missing versionCode}"
: "${VERSION_NAME:?Missing versionName}"
"$BT/aapt2" compile --dir app/src/main/res -o "$WORK/resources.zip"
sed 's/<manifest /<manifest package="app.miturno" /' app/src/main/AndroidManifest.xml > "$WORK/AndroidManifest.xml"
"$BT/aapt2" link -o "$WORK/base.apk" -I "$SDK/platforms/android-35/android.jar" --manifest "$WORK/AndroidManifest.xml" --min-sdk-version 26 --target-sdk-version 35 --version-code "$VERSION_CODE" --version-name "$VERSION_NAME" --output-text-symbols "$WORK/R.txt" -A app/src/main/assets "$WORK/resources.zip"
# Generate the drawable IDs used by Kotlin from the linked APK's resource table.
awk 'BEGIN { print "package app.miturno\nobject R { object drawable {" }
     $1 == "int" && $2 == "drawable" { print "    const val " $3 " = " $4 }
     END { print "} }" }' "$WORK/R.txt" > "$WORK/R.kt"
java -cp "$KLIB/*" org.jetbrains.kotlin.cli.jvm.K2JVMCompiler -no-stdlib -no-reflect -jvm-target 17 -classpath "$SDK/platforms/android-35/android.jar:$KLIB/stdlib.jar:$KLIB/annotations.jar" -d "$WORK/classes" app/src/main/java/app/miturno/*.kt "$WORK/R.kt"
(cd "$WORK/classes"; zip -qr "$WORK/classes.jar" .)
"$BT/d8" --lib "$SDK/platforms/android-35/android.jar" --min-api 26 --output "$WORK/dex" "$WORK/classes.jar" "$KLIB/stdlib.jar" "$KLIB/annotations.jar"
cp "$WORK/base.apk" "$WORK/unsigned.apk"
(cd "$WORK/dex"; zip -q "$WORK/unsigned.apk" classes*.dex)
"$BT/zipalign" -f -p 4 "$WORK/unsigned.apk" "$WORK/aligned.apk"
# Reuse the original key for updates. Never generate a replacement silently.
: "${MI_TURNO_KEYSTORE:?Set the local original keystore path}"
: "${MI_TURNO_STORE_PASSWORD:?Set keystore password locally}"
: "${MI_TURNO_KEY_PASSWORD:?Set key password locally}"
KEY_ALIAS="${MI_TURNO_KEY_ALIAS:-miturno}"
"$BT/apksigner" sign --ks "$MI_TURNO_KEYSTORE" --ks-pass env:MI_TURNO_STORE_PASSWORD --key-pass env:MI_TURNO_KEY_PASSWORD --ks-key-alias "$KEY_ALIAS" --out "$BUILD/Mi-Turno-Android-$VERSION_NAME.apk" "$WORK/aligned.apk"
"$BT/apksigner" verify --verbose "$BUILD/Mi-Turno-Android-$VERSION_NAME.apk"
cp "$WORK/classes.jar" "$BUILD/classes.jar"
