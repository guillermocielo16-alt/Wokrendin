# Historial Android

## 2026-11-03 — Corrección de parpadeo y menú WebView original (fuentes 1.1.3; SIN APK)

**Solicitud del usuario:** al tocar una fecha o cambiar de mes se observaban destellos del fondo y transiciones poco fluidas. El menú lateral 1.1.2 añadía una pantalla inventada «Calendarios» que solamente devolvía al calendario, ocultando el contenido de «Horas Extra» y «Ajustes Cal 2» tras un segundo toque.

**Diagnóstico confirmado en el código:** `selectDate()` invocaba `render()`, que construía una nueva ventana completa con `setContentView`. Además, `render(animate=true)` colocaba el contenido en `alpha=0` durante el fundido, exponiendo el fondo. En el HTML original, el icono ☰ del Calendario 1 abría directamente el panel **Horas Extra** y el del Calendario 2 el panel **Ajustes Cal 2**; no existía ningún índice de navegación «Calendarios».

**Cambios de interfaz:** la ventana completa se construye fuera de vista y se intercambia una vez completa. La selección de fechas utiliza `replaceCalendarSection()`: actualiza únicamente encabezado, resultado, sección de guardia/vacaciones, resumen, mes y tira horizontal; conserva el fondo animado y no reinicia la Activity. La misma fecha del mes repinta el indicador con `invalidate`; cambiar de mes reemplaza solo la tarjeta mensual; la barra conserva el desplazamiento cuando la fecha ya está en su rango. Se eliminó el fundido a transparencia total; los cambios mayores solo usan un pequeño desplazamiento sin ocultar contenidos. La primera selección del día actual también actualiza el borde.

**Menú:** se eliminó `drawerMenuContents()` y la fila «Calendarios» redundante. El botón ☰ abre directamente `extras()` para el ciclo 28 días y `settings()` para la rotación 2-2-3. Atrás, ×, toque fuera o gesto de cierre salen del panel, sin página intermedia. Se conserva el ancho/blur/animación/colores diferenciados del panel 1.1.2. Horas Extra vuelve al orden WebView **total → registrar fracción → guardias/fracciones → reportes → integrantes**. El total se muestra en grande (42 sp, «hrs»), las herramientas nativas ya existentes de iluminación, notificaciones y copia permanecen accesibles al final del mismo panel, sin introducir nuevas páginas. Ajustes Cal 2 mantiene perfil, turno, resaltados, áreas e integrantes.

**Cambios controlados:** se editaron la interfaz `MainActivity.kt`, la versión Gradle y documentación/pruebas. Próxima versión `1.1.3` (`versionCode 5`), base APK 1.1.2. `CalendarRules.kt` debe seguir con blob `75a84b35096aad361116c92de145c7bf5bcb40e6`, y `Store.kt` con `adfe8a20c7a30def35c7bf7e5c1eebc92176985a`. No se modificaron fechas ancla, horario de día/noche, festivos ni operaciones de guardia/horas. No se cambió `main`.

**Pruebas:** `android/tests/check-fluidez-menu.js` valida estáticamente estos invariantes y `android/tests/PRUEBAS_FLUIDEZ_MENU_1.1.3.md` indica pruebas físicas. **Pendientes:** compilar con Android SDK local y clave original privada, validar velocidad/fluidez y datos reales en Android, comparar pantalla con referencia. No se ha generado, firmado, instalado ni probado la APK 1.1.3 en este turno. No ejecutar GitHub Actions ni Netlify.



## 2026-11-03 — APK Android 1.1.2: compilación del panel lateral

Compilada desde `android/kotlin` (commit fuente `6ce6872ce79213cbc2d1d8b43388887ea4b087b2`): `Mi-Turno-Android-1.1.2.apk`, versionCode 4, paquete `app.miturno`, minSdk 26. SHA-256 `80e1adddd81b09320459e29ef960554909f7e466aabcbcb0cb67102a79261d04`. Firma v2/v3 y alineación APK verificadas. Certificado SHA-256 `41f530f81876173802f2156e29e105b9714c2ba9ef635a2195166b570800d8bc`, igual a la APK 1.1.1.

Pasaron 23,013 comparaciones de reglas + 2 comprobaciones de horas extra, 15 verificaciones de eventos y selección, y 25 aserciones de almacenamiento/reportes. `CalendarRules.kt` y `Store.kt` conservan sus blobs esperados. Pruebas en teléfono pendientes para validar menú, blur, gestos y navegación. El APK no está guardado en GitHub. Sin Actions ni Netlify.

## 2026-11-03 — Menú lateral restaurado (detalle de fuentes 1.1.2)

Se compararon cuatro capturas de referencia (panel WebView Horas Extra y Ajustes Cal 2, frente al cuadro gris de selección nativa) con el código Kotlin. La causa fue que el botón ☰ invocaba `AlertDialog.Builder(...).setItems(...)` y sustituía el calendario por páginas completas.

**Implementación en `MainActivity.kt`:** el botón ahora abre un panel lateral nativo desde la izquierda (ancho máximo 300 dp o 84% del teléfono) sobre el calendario, sin reconstruir ninguna regla laboral. Superpone un oscurecimiento y desenfoque de 3 dp con `RenderEffect` en Android 12+; en Android 8–11 el oscurecimiento sigue funcionando sin desenfoque. Animación de entrada de 290 ms y salida de 260 ms. Panel desplazable, fondo oscuro compatible con el tema, y acento verde para el ciclo de 28 días o malva/rosa para el calendario 2-2-3. No se utiliza el diálogo genérico para el índice de navegación.

**Contenido:** reutiliza las funciones Kotlin existentes `extras()`, `settings()`, `holidays()`, la configuración de recordatorios, y sus acciones, datos y reportes; se muestra dentro del panel. Ajustes Cal 2 coloca perfil, turno, opciones de resaltado y áreas antes de los integrantes. Los controles de resaltado reutilizan las claves `2stars` y `2guardsVisible` existentes, sin duplicar datos. No se altera el contenido ni los cálculos de horas extra. El botón «Recordatorios» posiciona la vista en su apartado.

**Gestos:** cerrar con ×, tocar fuera o deslizar el panel hacia la izquierda; pulsar/gesto Atrás regresa de una sección al índice y del índice cierra. Se registra un callback de `OnBackInvokedDispatcher` solo mientras el panel está abierto en API 33+, y se mantiene `onBackPressed` para versiones anteriores. No se exige AndroidX ni servicio remoto. El menú lateral se reconstruye tras cambios de ajustes conservando la posición de desplazamiento y sin reemplazar el calendario de fondo.

**Integridad:** `CalendarRules.kt` Git SHA `75a84b35096aad361116c92de145c7bf5bcb40e6` y `Store.kt` `adfe8a20c7a30def35c7bf7e5c1eebc92176985a` permanecen exactamente iguales al punto seguro. Paquete `app.miturno`, lectura de calendario Android, notificaciones y firmas sin cambios. Se reservó `versionCode 4` / `versionName 1.1.2`.

**Estado actualizado:** la APK 1.1.2 se compiló localmente el 3 de noviembre de 2026. Ver la entrada superior para hash, firma y suites JVM. La inspección física del panel, blur y gestos en el teléfono continúa pendiente. La lista detallada de pruebas manuales está en `android/tests/VERIFICACION_MENU_LATERAL_1.1.2.md`. Sin GitHub Actions ni Netlify.



## 2026-11-03 — APK 1.1.1 compilada: ajustes visuales neón

Compilada localmente la rama `android/kotlin`, commit `4ed366438a5cf77562bbed6614ad361c41c4537b`, con paridad visual y fondo neón ambiental lento. APK `Mi-Turno-Android-1.1.1.apk`, versionCode 3, paquete `app.miturno`, SHA-256 `3ec3b55bb046c9e25929d7a6828b6ce315ce589b1c91c4b81fa2eb2cc1e66d49`. Certificado SHA-256 coincide con la 1.1.0 (`41f530f81876173802f2156e29e105b9714c2ba9ef635a2195166b570800d8bc`); firma v2/v3 y alineación verificadas.

Pasaron 23,013 comparaciones de reglas + 2 verificaciones de horas extra, 15 verificaciones de eventos/selección y 25 aserciones de almacenamiento/reportes. Los blobs Git de `CalendarRules.kt` y `Store.kt` permanecen sin cambios. Pruebas en dispositivo pendientes. APK y clave no se guardan en el repositorio. Sin GitHub Actions ni Netlify.

## 2026-11-02 — Revisión visual neón / WebView (fuentes antes de compilar)

Comparación con las capturas del usuario y CSS web v10: resplandor cálido/violeta/verde bajo tarjetas, franja horizontal compacta, botón principal verde con degradado y flechas pequeñas de navegación mensual. La web tiene radiales y sombras fijas; en Kotlin se añadieron radiales **animados lentamente** con `NeonAmbientDrawable.kt` como fondo de la ventana, sin mover contenido ni animar textos. Ciclo de 26 s, repintado limitado aproximadamente a 12 FPS, se pausa en `onPause` y se reanuda en `onResume`. Respeta las animaciones desactivadas por Android y permite apagarlo desde Ajustes.

Se recuperaron proporciones del carrusel web: ancho adaptable 52–67 dp, altura 72 dp, números 16 sp, etiquetas 9–10 sp, seis días visibles aprox. según pantalla; el foquito de recordatorio es una superposición de 11 dp que no cambia el tamaño de la tarjeta. Los botones de navegación mensual son 34 dp (antes 45 dp con alto mínimo 48); borde de selección de tira ámbar; gradientes con los mismos colores del calendario original. El botón «Alternar Tipo de Calendario» tiene transición cromática propia para ambos tipos y el botón de tema conserva control individual.

- Archivos cambiados: `MainActivity.kt`, nuevo `NeonAmbientDrawable.kt`, `build.gradle.kts` y documentación.
- Versión reservada **1.1.1 (versionCode 3)**; la APK 1.1.0 entregada previamente **NO** incluye estos ajustes.
- Verificación estática: carga de archivos GitHub y presencia de métodos/recursos; sin instalación ni captura de la nueva versión.
- `CalendarRules.kt` conserva SHA Git `75a84b35096aad361116c92de145c7bf5bcb40e6`; `Store.kt` conserva `adfe8a20c7a30def35c7bf7e5c1eebc92176985a`. No cambian anclas, horarios, festivos, guardias ni rotaciones.
- La compilación se registró el 3 de noviembre de 2026 como APK 1.1.1. Pendiente validación visual y funcional en teléfono; no afirmar verificación física hasta realizarla. No ejecutar GitHub Actions o Netlify.



## 2026-11-02 — APK Android 1.1.0

Compilación local de la integración Android Calendar y notificaciones basada en `b8ba18034e5fc83c9a02c862e48fabee671d8f74`. Se generaron IDs de recursos antes de compilar Kotlin, se adaptó el contraste del foquito y se corrigieron el manejo de errores del proveedor, la selección de fuentes y la separación de avisos de prueba. APK con firma original y código de versión 2. Las pruebas locales y el hash de entrega figuran en [ESTADO_ACTUAL.md](../android/ESTADO_ACTUAL.md). Validación física pendiente.

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
