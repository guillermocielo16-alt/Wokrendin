# Mi Turno Android nativo

Base 1.0.0 del calendario de trabajo. Kotlin con vistas Android nativas, paquete `app.miturno`. Android 8.0+ (API 26), SDK 35, Java 17, Kotlin 2.0.21, Android Gradle Plugin 8.7.3 y Gradle 8.9.

Esta carpeta es el proyecto que se abre en Android Studio. Rama autorizada: `android/kotlin`. Antes de editar, leer [ESTADO_ACTUAL.md](ESTADO_ACTUAL.md), [mapa del código](../docs/MAPA_ANDROID.md) e [instrucciones de compilación](../docs/COMPILACION_ANDROID.md).

Las reglas laborales y los fuentes de la aplicación son los de la APK 1.0.0 entregada. La configuración de firma se adaptó al respaldo público para usar variables de entorno. Sin clave local, Android Studio puede producir un debug con su firma de desarrollo, que NO sirve para actualizar la APK original. Release queda sin firma hasta configurar la clave original.

No se incluyen APK, SDK, cachés ni claves. La firma original permanece en el paquete privado de fuentes entregado. No generar otra firma ni desinstalar la app para hacer una actualización con datos existentes.

`../proposals/android-calendar-readonly/` es un módulo posterior preparado para lectura manual de eventos del calendario del teléfono: sigue sin integrar. Es distinto del aviso diario del turno siguiente ya incluido en la base.

Las pruebas históricas están en `tests/VALIDATION.txt`; la validación real en teléfono y la equivalencia visual siguen pendientes. No ejecutar GitHub Actions ni desplegar esta rama en Netlify.
