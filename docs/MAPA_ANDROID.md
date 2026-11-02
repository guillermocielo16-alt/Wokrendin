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
| `proposals/android-calendar-readonly/` | Módulo preparado de eventos externos, fuera de la compilación de app. |

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
