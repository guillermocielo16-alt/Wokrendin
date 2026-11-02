# Compilación y pruebas locales

## Android Studio / Gradle

1. Clonar el repositorio y seleccionar `android/kotlin`.
2. Abrir `android/` en Android Studio con JDK 17, SDK Platform 35 y build-tools 35.0.0.
3. Usar Gradle 8.9 y sincronizar (AGP 8.7.3, Kotlin 2.0.21). Esta base no contiene wrapper; usar Gradle instalado localmente o configurarlo desde Android Studio.
4. Desde `android/`, ejecutar `gradle assembleDebug`. Sin firma original configurada utiliza la firma debug local; no es actualización de la app instalada.
5. Para una actualización real, configurar las variables locales siguientes antes de abrir Android Studio o ejecutar Gradle, y compilar `gradle assembleRelease`.

| Variable | Uso |
| --- | --- |
| `MI_TURNO_KEYSTORE` | Ruta absoluta a la clave original recuperada del paquete privado de fuentes |
| `MI_TURNO_STORE_PASSWORD` | Contraseña local del almacén |
| `MI_TURNO_KEY_ALIAS` | Alias original; valor predeterminado `miturno` |
| `MI_TURNO_KEY_PASSWORD` | Contraseña local de la clave |

Sin `MI_TURNO_KEYSTORE`, release queda sin firmar. Nunca publicar el almacén ni sus contraseñas. `local.properties`, `signing/`, `.env`, keystores y compilaciones están ignorados. El ZIP privado de la entrega original contiene la firma de prueba necesaria; este repositorio público no la contiene. Usar otra clave impide actualizar la instalación existente. No desinstalarla para eludir el problema, porque puede borrar los registros.

## Alternativa sin Gradle

`build-local.sh` usa Java 17, zip, Android SDK 35/build-tools 35.0.0 y los JAR de Kotlin. Configurar `ANDROID_SDK_ROOT`, `KOTLIN_COMPILER_LIB` y las variables de firma anteriores. No genera claves de sustitución automáticamente.

JAR usados en la compilación de la entrega: kotlin-compiler/stdlib/script-runtime 2.0.21, kotlin-reflect 1.6.10, trove4j 1.0.20200330, annotations 13.0 y kotlinx-coroutines-core-jvm 1.6.4. Colocarlos localmente con los nombres que esperan los scripts (`stdlib.jar`, `annotations.jar` y el resto en el mismo directorio). No subir estas dependencias a git.

```bash
cd android
bash build-local.sh
```

Salida: `android/build/Mi-Turno-Android-1.0.0.apk`. La versión es la de la base; antes de publicar una actualización funcional, incrementar versionCode/versionName tanto en Gradle como en el comando aapt2 del script y documentarlo.

## Reglas y reportes

Desde `android/`, con las dependencias locales:

```bash
ORIGINAL_HTML="$PWD/../index.html" bash tests/run-rules.sh
bash tests/run-store.sh
```

La segunda prueba necesita `JSON_JAR` apuntando a org.json JSON-java 20240303. El HTML de la raíz se conserva de la web v10; el comparador extrae únicamente sus funciones de calendario. `tests/original-expected.txt` guarda las expectativas históricas de 2,557 fechas.

## Instrumentación Android

Después de `build-local.sh`, con el mismo almacén de firma:

```bash
bash tests/build-device.sh
adb install -r build/Mi-Turno-Android-1.0.0.apk
adb install -r build/tests.apk
adb shell am instrument -w app.miturno.tests/app.miturno.tests.ValidationRunner
```

Las pruebas tocan preferencias y las restauran al finalizar. Usar primero un dispositivo/emulador de prueba sin datos importantes. No se presentan como aprobadas en la entrega original.

Validar además temas, celdas, scroll, guardias, vacaciones, exportación TXT/JSON, compartir, permiso de notificaciones y recepción tras reiniciar. Para el módulo propuesto, añadir pruebas de CalendarContract, selección de calendarios y revocación. No ejecutar GitHub Actions ni Netlify para estas tareas.
