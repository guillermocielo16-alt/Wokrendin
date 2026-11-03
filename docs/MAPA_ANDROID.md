# Mapa del proyecto Android

| Archivo | Responsabilidad |
| --- | --- |
| `android/app/src/main/java/app/miturno/CalendarRules.kt` | Patrones, anclas, festivos, turnos y suma de horas extra. Regla original a preservar. |
| `android/app/src/main/java/app/miturno/Store.kt` | SharedPreferences, guardias, vacaciones, fracciones y reportes filtrados. |
| `android/app/src/main/java/app/miturno/MainActivity.kt` | Interfaz nativa, calendarios, menú lateral deslizante con desenfoque, Atrás del sistema, selección de fecha/equipo, ajustes, informes y copias. |
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

## 2026-11-03 — Menú lateral restaurado (fuentes para APK 1.1.2, SIN COMPILAR)

Se compararon cuatro capturas de referencia (panel WebView Horas Extra y Ajustes Cal 2, frente al cuadro gris de selección nativa) con el código Kotlin. La causa fue que el botón ☰ invocaba `AlertDialog.Builder(...).setItems(...)` y sustituía el calendario por páginas completas.

**Implementación en `MainActivity.kt`:** el botón ahora abre un panel lateral nativo desde la izquierda (ancho máximo 300 dp o 84% del teléfono) sobre el calendario, sin reconstruir ninguna regla laboral. Superpone un oscurecimiento y desenfoque de 3 dp con `RenderEffect` en Android 12+; en Android 8–11 el oscurecimiento sigue funcionando sin desenfoque. Animación de entrada de 290 ms y salida de 260 ms. Panel desplazable, fondo oscuro compatible con el tema, y acento verde para el ciclo de 28 días o malva/rosa para el calendario 2-2-3. No se utiliza el diálogo genérico para el índice de navegación.

**Contenido:** reutiliza las funciones Kotlin existentes `extras()`, `settings()`, `holidays()`, la configuración de recordatorios, y sus acciones, datos y reportes; se muestra dentro del panel. Ajustes Cal 2 coloca perfil, turno, opciones de resaltado y áreas antes de los integrantes. Los controles de resaltado reutilizan las claves `2stars` y `2guardsVisible` existentes, sin duplicar datos. No se altera el contenido ni los cálculos de horas extra. El botón «Recordatorios» posiciona la vista en su apartado.

**Gestos:** cerrar con ×, tocar fuera o deslizar el panel hacia la izquierda; pulsar/gesto Atrás regresa de una sección al índice y del índice cierra. Se registra un callback de `OnBackInvokedDispatcher` solo mientras el panel está abierto en API 33+, y se mantiene `onBackPressed` para versiones anteriores. No se exige AndroidX ni servicio remoto. El menú lateral se reconstruye tras cambios de ajustes conservando la posición de desplazamiento y sin reemplazar el calendario de fondo.

**Integridad:** `CalendarRules.kt` Git SHA `75a84b35096aad361116c92de145c7bf5bcb40e6` y `Store.kt` `adfe8a20c7a30def35c7bf7e5c1eebc92176985a` permanecen exactamente iguales al punto seguro. Paquete `app.miturno`, lectura de calendario Android, notificaciones y firmas sin cambios. Se reservó `versionCode 4` / `versionName 1.1.2`.

**Verificación realizada:** inspección de cambios, presencia de panel en lugar de `AlertDialog.setItems`, revisión estructural del Kotlin, comparación de hashes de reglas y datos. **No se ha compilado ni instalado la 1.1.2**, ni se ha probado el desenfoque y gesto en dispositivo. El SDK Android y la clave privada no están disponibles en este entorno. Para probar y compilar localmente utilizar `android/tests/VERIFICACION_MENU_LATERAL_1.1.2.md`; no usar GitHub Actions ni Netlify.


## 2026-11-03 — Corrección de parpadeo y menú WebView original (fuentes 1.1.3; SIN APK)

**Solicitud del usuario:** al tocar una fecha o cambiar de mes se observaban destellos del fondo y transiciones poco fluidas. El menú lateral 1.1.2 añadía una pantalla inventada «Calendarios» que solamente devolvía al calendario, ocultando el contenido de «Horas Extra» y «Ajustes Cal 2» tras un segundo toque.

**Diagnóstico confirmado en el código:** `selectDate()` invocaba `render()`, que construía una nueva ventana completa con `setContentView`. Además, `render(animate=true)` colocaba el contenido en `alpha=0` durante el fundido, exponiendo el fondo. En el HTML original, el icono ☰ del Calendario 1 abría directamente el panel **Horas Extra** y el del Calendario 2 el panel **Ajustes Cal 2**; no existía ningún índice de navegación «Calendarios».

**Cambios de interfaz:** la ventana completa se construye fuera de vista y se intercambia una vez completa. La selección de fechas utiliza `replaceCalendarSection()`: actualiza únicamente encabezado, resultado, sección de guardia/vacaciones, resumen, mes y tira horizontal; conserva el fondo animado y no reinicia la Activity. La misma fecha del mes repinta el indicador con `invalidate`; cambiar de mes reemplaza solo la tarjeta mensual; la barra conserva el desplazamiento cuando la fecha ya está en su rango. Se eliminó el fundido a transparencia total; los cambios mayores solo usan un pequeño desplazamiento sin ocultar contenidos. La primera selección del día actual también actualiza el borde.

**Menú:** se eliminó `drawerMenuContents()` y la fila «Calendarios» redundante. El botón ☰ abre directamente `extras()` para el ciclo 28 días y `settings()` para la rotación 2-2-3. Atrás, ×, toque fuera o gesto de cierre salen del panel, sin página intermedia. Se conserva el ancho/blur/animación/colores diferenciados del panel 1.1.2. Horas Extra vuelve al orden WebView **total → registrar fracción → guardias/fracciones → reportes → integrantes**. El total se muestra en grande (42 sp, «hrs»), las herramientas nativas ya existentes de iluminación, notificaciones y copia permanecen accesibles al final del mismo panel, sin introducir nuevas páginas. Ajustes Cal 2 mantiene perfil, turno, resaltados, áreas e integrantes.

**Cambios controlados:** se editaron la interfaz `MainActivity.kt`, la versión Gradle y documentación/pruebas. Próxima versión `1.1.3` (`versionCode 5`), base APK 1.1.2. `CalendarRules.kt` debe seguir con blob `75a84b35096aad361116c92de145c7bf5bcb40e6`, y `Store.kt` con `adfe8a20c7a30def35c7bf7e5c1eebc92176985a`. No se modificaron fechas ancla, horario de día/noche, festivos ni operaciones de guardia/horas. No se cambió `main`.

**Pruebas:** `android/tests/check-fluidez-menu.js` valida estáticamente estos invariantes y `android/tests/PRUEBAS_FLUIDEZ_MENU_1.1.3.md` indica pruebas físicas. **Pendientes:** compilar con Android SDK local y clave original privada, validar velocidad/fluidez y datos reales en Android, comparar pantalla con referencia. No se ha generado, firmado, instalado ni probado la APK 1.1.3 en este turno. No ejecutar GitHub Actions ni Netlify.

