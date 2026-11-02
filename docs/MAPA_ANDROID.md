# Mapa del proyecto Android

| Archivo | Responsabilidad |
| --- | --- |
| `android/app/src/main/java/app/miturno/CalendarRules.kt` | Patrones, anclas, festivos, turnos y suma de horas extra. Regla original a preservar. |
| `android/app/src/main/java/app/miturno/Store.kt` | SharedPreferences, guardias, vacaciones, fracciones y reportes filtrados. |
| `android/app/src/main/java/app/miturno/MainActivity.kt` | Interfaz nativa, calendarios, selección de fecha/equipo, ajustes, informes y copias. |
| `android/app/src/main/java/app/miturno/Reminders.kt` | Aviso diario del próximo turno, canal de notificaciones y restauración de alarmas. |
| `android/app/src/main/AndroidManifest.xml` | Activity y receptores; permisos POST_NOTIFICATIONS y RECEIVE_BOOT_COMPLETED. No READ_CALENDAR en la base. |
| `android/app/src/main/assets/fonts/` | Sora, Inter, JetBrains Mono offline. |
| `android/licenses/` y assets/licenses | Licencias de las fuentes y Kotlin. |
| `android/tests/RulesTest.kt` y compare-original.js | Comparación de reglas contra la implementación web original. |
| `android/tests/host/` | Pruebas JVM de Store con preferencias simuladas; no son Android real. |
| `android/tests/device/` | Instrumentación Android preparada; ejecución pendiente. |
| `proposals/android-calendar-readonly/` | Módulo histórico original de referencia. La copia integrada está en `app/`. |
| `android/app/src/main/java/app/miturno/AndroidCalendarReader.kt` | Lectura de calendarios y ocurrencias de eventos Android, solo lectura. |
| `android/app/src/main/java/app/miturno/ReminderModels.kt` | Índice por día y modelo puro de navegación compartida. |
| `android/tests/calendar/` | 15 pruebas JVM del modelo de eventos. |

## Reglas laborales de referencia

- Ciclo 28 días: ancla 16 diciembre 2025, A/B/C/D, asignaciones literales del original.
- Rotación 2-2-3: ancla 5 enero 2026, patrón AB `[1,1,0,0,1,1,1,0,0,1,1,0,0,0]`; CD complementario.
- Cambio: delta de días múltiplo de 56, incluso antes del ancla con el mismo comportamiento original.
- Horarios: día 08:00–20:00; noche 20:00–08:00. Guardia cubierta suma 12 h; cancelada/pendiente no suma.
- Fracción: horas positivas, una por fecha. Nueva entrada de igual fecha reemplaza la anterior.
- Festivos exactos: 2026-02-02, 03-16, 05-01, 05-05, 09-16, 11-02, 11-16, 12-25. Son marcas; no reemplazan el cálculo de turno.
- Guardias y vacaciones: claves por fecha/equipo. Reportes: límites inclusivos.
- Perfil y turno del segundo calendario son metadatos; no modifican el patrón 2-2-3.

## Estado y limitaciones

No hay sincronización web/Android. Los datos se guardan localmente; las copias Android no importan automáticamente el localStorage web. La notificación del turno siguiente usa alarma inexacta y puede retrasarse. La lectura de eventos del calendario del teléfono está preparada pero no integrada. La reproducción visual debe validarse en teléfono; no se considera certificada como idéntica píxel a píxel.



## 2026-11-02 — Integración de lectura Android Calendar y notificaciones (fuentes, pendiente APK)

Se implementó en los fuentes de `android/kotlin` una sincronización **manual y de solo lectura** con `CalendarContract` (permiso `READ_CALENDAR`, sin `WRITE_CALENDAR`, sin OAuth ni servicios de pago). Se permite escoger calendarios visibles en un diálogo; solo sus identificadores se conservan en las preferencias privadas separadas `mi_turno_calendar_sources`. Los títulos, descripciones y ubicaciones importados permanecen exclusivamente en memoria, fuera de las copias JSON, informes y logs. La consulta del proveedor se ejecuta en un hilo de I/O; cada sincronización exitosa reemplaza los eventos previos. Ante fallo se conservan los resultados anteriores y se presenta un error; al revocar el permiso desaparecen los datos importados. Se leen las ocurrencias repetitivas y se marcan eventos que cruzan fechas.

El foquito vectorial se presenta en la leyenda, los días mensuales y la tira horizontal, que sigue siendo seleccionable; la selección mueve encabezado, tarjeta de estado laboral original y detalle completo de todos los eventos de la fecha (sin convertirlos en HTML). El botón «Sincronizar calendario» permite elegir fuentes. La consulta se hace para mes visible, fecha seleccionada y tira de 30 días; para consultar otro mes, pulsar de nuevo el botón. Las notificaciones existentes del turno de mañana continúan siendo **independientes**; no se programan duplicados de avisos externos. Usan canal de Android (importancia por defecto), categoría REMINDER, privacidad en pantalla bloqueada y botón para abrir la configuración de sonido/vibración del sistema. Pulsar la notificación abre la fecha laboral correspondiente; un canal previamente creado conserva las opciones elegidas por el usuario.

**Verificaciones actuales:** 15 pruebas JVM del modelo/indexación y fecha seleccionada pasaron en Kotlin local. Hash Git de `CalendarRules.kt` antes y después **idéntico** (`75a84b35096aad361116c92de145c7bf5bcb40e6`). No hay workflow de GitHub Actions, cambio en `main` ni despliegue de Netlify.

**Pendientes obligatorios:** no se dispone de Android SDK/`android.jar` en el entorno actual, por lo que la compilación completa de la APK y el comportamiento real de permisos, sincronización del proveedor, selección táctil, representación del foquito y alertas en el teléfono **NO están verificados**. No hay APK nueva ni firma disponible en este respaldo público. Antes de instalar como actualización se requiere compilación local con la clave original privada, y confirmar que conserva datos y horarios.
