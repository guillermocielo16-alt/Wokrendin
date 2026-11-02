# Historial Android


## 2026-11-02 — Integración de lectura Android Calendar y notificaciones (fuentes, pendiente APK)

Se implementó en los fuentes de `android/kotlin` una sincronización **manual y de solo lectura** con `CalendarContract` (permiso `READ_CALENDAR`, sin `WRITE_CALENDAR`, sin OAuth ni servicios de pago). Se permite escoger calendarios visibles en un diálogo; solo sus identificadores se conservan en las preferencias privadas separadas `mi_turno_calendar_sources`. Los títulos, descripciones y ubicaciones importados permanecen exclusivamente en memoria, fuera de las copias JSON, informes y logs. La consulta del proveedor se ejecuta en un hilo de I/O; cada sincronización exitosa reemplaza los eventos previos. Ante fallo se conservan los resultados anteriores y se presenta un error; al revocar el permiso desaparecen los datos importados. Se leen las ocurrencias repetitivas y se marcan eventos que cruzan fechas.

El foquito vectorial se presenta en la leyenda, los días mensuales y la tira horizontal, que sigue siendo seleccionable; la selección mueve encabezado, tarjeta de estado laboral original y detalle completo de todos los eventos de la fecha (sin convertirlos en HTML). El botón «Sincronizar calendario» permite elegir fuentes. La consulta se hace para mes visible, fecha seleccionada y tira de 30 días; para consultar otro mes, pulsar de nuevo el botón. Las notificaciones existentes del turno de mañana continúan siendo **independientes**; no se programan duplicados de avisos externos. Usan canal de Android (importancia por defecto), categoría REMINDER, privacidad en pantalla bloqueada y botón para abrir la configuración de sonido/vibración del sistema. Pulsar la notificación abre la fecha laboral correspondiente; un canal previamente creado conserva las opciones elegidas por el usuario.

**Verificaciones actuales:** 15 pruebas JVM del modelo/indexación y fecha seleccionada pasaron en Kotlin local. Hash Git de `CalendarRules.kt` antes y después **idéntico** (`75a84b35096aad361116c92de145c7bf5bcb40e6`). No hay workflow de GitHub Actions, cambio en `main` ni despliegue de Netlify.

**Pendientes obligatorios:** no se dispone de Android SDK/`android.jar` en el entorno actual, por lo que la compilación completa de la APK y el comportamiento real de permisos, sincronización del proveedor, selección táctil, representación del foquito y alertas en el teléfono **NO están verificados**. No hay APK nueva ni firma disponible en este respaldo público. Antes de instalar como actualización se requiere compilación local con la clave original privada, y confirmar que conserva datos y horarios.


## 2026-11-02 — Respaldo inicial en GitHub

Creada la rama `android/kotlin` desde la web v10 `1fe8cdb5ff5a14ad8479ab217842e50e529c1055`. Añadidos fuentes nativos 1.0.0, assets, licencias, pruebas históricas, instrucciones de continuidad y módulo de eventos externos preparado en `proposals/` (sin integrar).

Sin cambios a los fuentes de ejecución `app/src/main/`. Configuración de firma adaptada a variables locales para excluir claves del repositorio público. No se generó un APK nuevo, no se fusionó con main, no se añadieron workflows y no se desplegó en Netlify. Commit con marcadores de omisión CI/despliegue.

## 2026-10-28 — Entrega Android 1.0.0

APK Kotlin nativo, paquete `app.miturno`. Comparación de reglas, cálculos y reportes aprobada según `android/tests/VALIDATION.txt`. Pruebas reales en Android y revisión de fidelidad visual pendientes. Informe histórico conservado en `android/ESTADO_ENTREGA_1.0.0_2026-10-28.md`.
