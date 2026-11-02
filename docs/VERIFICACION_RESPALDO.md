# Verificación del respaldo — 2026-11-02

## Ejecutado ahora

- Recuperación del ZIP fuente exacto; SHA-256 registrado en ANDROID_BASELINE.json.
- Comparación byte por byte de los 13 archivos de app/src/main contra la entrega 1.0.0: idénticos.
- bash -n para build-local.sh, build-device.sh, run-rules.sh y run-store.sh: sin errores de sintaxis.
- Generación de expectativas de 2,557 fechas con compare-original.js y el index.html web v10 de main, fijando TZ=America/Mexico_City: coincide byte por byte con tests/original-expected.txt.
- Revisión del contenido publicado: sin APK, almacenes de firma, cachés ni workflows; fuentes, assets, licencias, pruebas y documentación.
- Configuración de firma pública: variables locales; el script no genera una clave alternativa silenciosamente. La app conserva el mismo package y código de ejecución.

## Históricas; no ejecutadas de nuevo en este respaldo

Compilación APK, firma, 23,013 comprobaciones de reglas, dos de suma y 25 de Store/reportes: consultar android/tests/VALIDATION.txt y el informe histórico. El entorno actual no incluye compilador Kotlin/Gradle/SDK para repetir esa compilación.

## Pendientes

Instalación/actualización real con firma original, fidelidad visual, notificaciones, reinicio, SharedPreferences real y ejecución de instrumentación. La integración CalendarContract del módulo propuesto también sigue pendiente. Este respaldo no se presenta como una nueva APK probada.

## Separación de ramas

La rama Android se crea sobre main 1fe8cdb5ff5a14ad8479ab217842e50e529c1055 y añade android/, proposals/, docs/, README.md y AGENTS.md. Los ocho archivos web se heredan del árbol f71a2631619a1755f068f1aa41bc5706df932c77 sin reemplazarlos. El commit utiliza [skip ci] y [skip netlify]; no se añade CI ni se solicita despliegue.
