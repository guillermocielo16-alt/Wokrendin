# Mi Turno Android nativo

Base 1.0.0 del calendario de trabajo. Kotlin con vistas Android nativas, paquete `app.miturno`. Android 8.0+ (API 26), SDK 35, Java 17, Kotlin 2.0.21, Android Gradle Plugin 8.7.3 y Gradle 8.9.

Esta carpeta es el proyecto que se abre en Android Studio. Rama autorizada: `android/kotlin`. Antes de editar, leer [ESTADO_ACTUAL.md](ESTADO_ACTUAL.md), [mapa del código](../docs/MAPA_ANDROID.md) e [instrucciones de compilación](../docs/COMPILACION_ANDROID.md).

Las reglas laborales y los fuentes de la aplicación son los de la APK 1.0.0 entregada. La configuración de firma se adaptó al respaldo público para usar variables de entorno. Sin clave local, Android Studio puede producir un debug con su firma de desarrollo, que NO sirve para actualizar la APK original. Release queda sin firma hasta configurar la clave original.

No se incluyen APK, SDK, cachés ni claves. La firma original permanece en el paquete privado de fuentes entregado. No generar otra firma ni desinstalar la app para hacer una actualización con datos existentes.

`../proposals/android-calendar-readonly/` es un módulo posterior preparado para lectura manual de eventos del calendario del teléfono: está ahora integrado en los fuentes actuales, pero aún no compilado en APK. Es distinto del aviso diario del turno siguiente ya incluido en la base.

Las pruebas históricas están en `tests/VALIDATION.txt`; la validación real en teléfono y la equivalencia visual siguen pendientes. No ejecutar GitHub Actions ni desplegar esta rama en Netlify.



## 2026-11-02 — Integración de lectura Android Calendar y notificaciones (fuentes, pendiente APK)

Se implementó en los fuentes de `android/kotlin` una sincronización **manual y de solo lectura** con `CalendarContract` (permiso `READ_CALENDAR`, sin `WRITE_CALENDAR`, sin OAuth ni servicios de pago). Se permite escoger calendarios visibles en un diálogo; solo sus identificadores se conservan en las preferencias privadas separadas `mi_turno_calendar_sources`. Los títulos, descripciones y ubicaciones importados permanecen exclusivamente en memoria, fuera de las copias JSON, informes y logs. La consulta del proveedor se ejecuta en un hilo de I/O; cada sincronización exitosa reemplaza los eventos previos. Ante fallo se conservan los resultados anteriores y se presenta un error; al revocar el permiso desaparecen los datos importados. Se leen las ocurrencias repetitivas y se marcan eventos que cruzan fechas.

El foquito vectorial se presenta en la leyenda, los días mensuales y la tira horizontal, que sigue siendo seleccionable; la selección mueve encabezado, tarjeta de estado laboral original y detalle completo de todos los eventos de la fecha (sin convertirlos en HTML). El botón «Sincronizar calendario» permite elegir fuentes. La consulta se hace para mes visible, fecha seleccionada y tira de 30 días; para consultar otro mes, pulsar de nuevo el botón. Las notificaciones existentes del turno de mañana continúan siendo **independientes**; no se programan duplicados de avisos externos. Usan canal de Android (importancia por defecto), categoría REMINDER, privacidad en pantalla bloqueada y botón para abrir la configuración de sonido/vibración del sistema. Pulsar la notificación abre la fecha laboral correspondiente; un canal previamente creado conserva las opciones elegidas por el usuario.

**Verificaciones actuales:** 15 pruebas JVM del modelo/indexación y fecha seleccionada pasaron en Kotlin local. Hash Git de `CalendarRules.kt` antes y después **idéntico** (`75a84b35096aad361116c92de145c7bf5bcb40e6`). No hay workflow de GitHub Actions, cambio en `main` ni despliegue de Netlify.

**Pendientes obligatorios:** no se dispone de Android SDK/`android.jar` en el entorno actual, por lo que la compilación completa de la APK y el comportamiento real de permisos, sincronización del proveedor, selección táctil, representación del foquito y alertas en el teléfono **NO están verificados**. No hay APK nueva ni firma disponible en este respaldo público. Antes de instalar como actualización se requiere compilación local con la clave original privada, y confirmar que conserva datos y horarios.
