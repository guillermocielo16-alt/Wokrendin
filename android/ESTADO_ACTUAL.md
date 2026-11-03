# Mi Turno Android — estado y próxima versión

## 2026-11-03 — APK 1.1.3 compilada: fluidez y menú original

Compilada localmente desde `android/kotlin`, commit fuente `03495bfb717449f1ec2db67ce7648803ae062d30`. Incluye la actualización localizada al seleccionar fecha y cambiar de mes, sin reconstruir la ventana completa ni mostrar el fondo a través de un fundido; el menú abre directamente Horas Extra en Calendario 1 y Ajustes Cal 2 en Calendario 2.

- APK: `Mi-Turno-Android-1.1.3.apk`; `app.miturno`, versionCode 5, minSdk 26.
- SHA-256: `20dad64f1cb198b5f97be3c3a8a67e85c15c0041ffca2523620939ca838224ea`.
- Certificado SHA-256: `41f530f81876173802f2156e29e105b9714c2ba9ef635a2195166b570800d8bc`, igual a las APK 1.1.2 y 1.1.1; firma v2/v3 y alineación verificadas.
- Pasaron 12 aserciones estáticas en `check-fluidez-menu.js`; 23,013 comparaciones de reglas en 2,557 fechas + 2 cálculos de horas extra; 15 comprobaciones de eventos/selección; y 25 de almacenamiento/reportes.
- `CalendarRules.kt` permanece en blob Git `75a84b35096aad361116c92de145c7bf5bcb40e6`; `Store.kt` en `adfe8a20c7a30def35c7bf7e5c1eebc92176985a`.
- Pendiente validación física en el teléfono de parpadeo, selección rápida, navegación mensual, menú, blur y gestos. Las pruebas de código no demuestran fluidez visual real. APK y clave no se suben al repositorio; no se usaron Actions ni Netlify.

---

## 2026-11-03 — Corrección de parpadeo y menú WebView original (implementación 1.1.3; APK compilada)

**Solicitud del usuario:** al tocar una fecha o cambiar de mes se observaban destellos del fondo y transiciones poco fluidas. El menú lateral 1.1.2 añadía una pantalla inventada «Calendarios» que solamente devolvía al calendario, ocultando el contenido de «Horas Extra» y «Ajustes Cal 2» tras un segundo toque.

**Diagnóstico confirmado en el código:** `selectDate()` invocaba `render()`, que construía una nueva ventana completa con `setContentView`. Además, `render(animate=true)` colocaba el contenido en `alpha=0` durante el fundido, exponiendo el fondo. En el HTML original, el icono ☰ del Calendario 1 abría directamente el panel **Horas Extra** y el del Calendario 2 el panel **Ajustes Cal 2**; no existía ningún índice de navegación «Calendarios».

**Cambios de interfaz:** la ventana completa se construye fuera de vista y se intercambia una vez completa. La selección de fechas utiliza `replaceCalendarSection()`: actualiza únicamente encabezado, resultado, sección de guardia/vacaciones, resumen, mes y tira horizontal; conserva el fondo animado y no reinicia la Activity. La misma fecha del mes repinta el indicador con `invalidate`; cambiar de mes reemplaza solo la tarjeta mensual; la barra conserva el desplazamiento cuando la fecha ya está en su rango. Se eliminó el fundido a transparencia total; los cambios mayores solo usan un pequeño desplazamiento sin ocultar contenidos. La primera selección del día actual también actualiza el borde.

**Menú:** se eliminó `drawerMenuContents()` y la fila «Calendarios» redundante. El botón ☰ abre directamente `extras()` para el ciclo 28 días y `settings()` para la rotación 2-2-3. Atrás, ×, toque fuera o gesto de cierre salen del panel, sin página intermedia. Se conserva el ancho/blur/animación/colores diferenciados del panel 1.1.2. Horas Extra vuelve al orden WebView **total → registrar fracción → guardias/fracciones → reportes → integrantes**. El total se muestra en grande (42 sp, «hrs»), las herramientas nativas ya existentes de iluminación, notificaciones y copia permanecen accesibles al final del mismo panel, sin introducir nuevas páginas. Ajustes Cal 2 mantiene perfil, turno, resaltados, áreas e integrantes.

**Cambios controlados:** se editaron la interfaz `MainActivity.kt`, la versión Gradle y documentación/pruebas. Próxima versión `1.1.3` (`versionCode 5`), base APK 1.1.2. `CalendarRules.kt` debe seguir con blob `75a84b35096aad361116c92de145c7bf5bcb40e6`, y `Store.kt` con `adfe8a20c7a30def35c7bf7e5c1eebc92176985a`. No se modificaron fechas ancla, horario de día/noche, festivos ni operaciones de guardia/horas. No se cambió `main`.

**Pruebas:** el script `android/tests/check-fluidez-menu.js` pasó sus 12 aserciones estáticas ejecutables; 23,013 comparaciones de reglas + 2 de horas extra, 15 comprobaciones de eventos/selección y 25 aserciones de almacenamiento/reportes también pasaron. La APK 1.1.3 fue compilada y firmada localmente (detalle superior). **Pendiente:** probar en el teléfono la ausencia de parpadeo, los gestos y el diseño. No se afirma validación visual física. No ejecutar GitHub Actions ni Netlify.



## 2026-11-03 — APK 1.1.2 compilada: panel lateral restaurado

Compilación local desde la rama `android/kotlin`, commit fuente `6ce6872ce79213cbc2d1d8b43388887ea4b087b2`. Esta versión incorpora el panel lateral nativo sobre el calendario, navegación interna, contenido de Horas Extra y ajustes, gestos Atrás/cierre, colores por calendario, oscurecimiento y blur de fondo en Android 12+. En Android 8–11 conserva el oscurecimiento sin blur.

- APK: `Mi-Turno-Android-1.1.2.apk`, paquete `app.miturno`, versionCode 4, minSdk 26 (Android 8).
- SHA-256 APK: `80e1adddd81b09320459e29ef960554909f7e466aabcbcb0cb67102a79261d04`.
- Certificado SHA-256: `41f530f81876173802f2156e29e105b9714c2ba9ef635a2195166b570800d8bc`, igual a 1.1.1; firma v2/v3 y alineación verificadas.
- Pruebas locales aprobadas: 23,013 comparaciones de reglas (2,557 fechas) + 2 de horas extra; 15 de eventos/selección; 25 de almacenamiento, horas extra y reportes.
- `CalendarRules.kt` mantiene blob Git `75a84b35096aad361116c92de145c7bf5bcb40e6`; `Store.kt` mantiene `adfe8a20c7a30def35c7bf7e5c1eebc92176985a`.
- Pendiente prueba en teléfono de blur, navegación Atrás, gestos, accesibilidad táctil, temas, uso de batería y actualización conservando datos. La compilación no equivale a validación física. El APK no se subió al repositorio público. Sin GitHub Actions ni Netlify.

---

## 2026-11-03 — Menú lateral restaurado (implementación 1.1.2; compilación registrada arriba)

Se compararon cuatro capturas de referencia (panel WebView Horas Extra y Ajustes Cal 2, frente al cuadro gris de selección nativa) con el código Kotlin. La causa fue que el botón ☰ invocaba `AlertDialog.Builder(...).setItems(...)` y sustituía el calendario por páginas completas.

**Implementación en `MainActivity.kt`:** el botón ahora abre un panel lateral nativo desde la izquierda (ancho máximo 300 dp o 84% del teléfono) sobre el calendario, sin reconstruir ninguna regla laboral. Superpone un oscurecimiento y desenfoque de 3 dp con `RenderEffect` en Android 12+; en Android 8–11 el oscurecimiento sigue funcionando sin desenfoque. Animación de entrada de 290 ms y salida de 260 ms. Panel desplazable, fondo oscuro compatible con el tema, y acento verde para el ciclo de 28 días o malva/rosa para el calendario 2-2-3. No se utiliza el diálogo genérico para el índice de navegación.

**Contenido:** reutiliza las funciones Kotlin existentes `extras()`, `settings()`, `holidays()`, la configuración de recordatorios, y sus acciones, datos y reportes; se muestra dentro del panel. Ajustes Cal 2 coloca perfil, turno, opciones de resaltado y áreas antes de los integrantes. Los controles de resaltado reutilizan las claves `2stars` y `2guardsVisible` existentes, sin duplicar datos. No se altera el contenido ni los cálculos de horas extra. El botón «Recordatorios» posiciona la vista en su apartado.

**Gestos:** cerrar con ×, tocar fuera o deslizar el panel hacia la izquierda; pulsar/gesto Atrás regresa de una sección al índice y del índice cierra. Se registra un callback de `OnBackInvokedDispatcher` solo mientras el panel está abierto en API 33+, y se mantiene `onBackPressed` para versiones anteriores. No se exige AndroidX ni servicio remoto. El menú lateral se reconstruye tras cambios de ajustes conservando la posición de desplazamiento y sin reemplazar el calendario de fondo.

**Integridad:** `CalendarRules.kt` Git SHA `75a84b35096aad361116c92de145c7bf5bcb40e6` y `Store.kt` `adfe8a20c7a30def35c7bf7e5c1eebc92176985a` permanecen exactamente iguales al punto seguro. Paquete `app.miturno`, lectura de calendario Android, notificaciones y firmas sin cambios. Se reservó `versionCode 4` / `versionName 1.1.2`.

**Verificación de compilación:** APK 1.1.2 construida con Kotlin 2.0.21, Android SDK 35 y firma de prueba original. Alineación y firmas v2/v3 verificadas; coincide el certificado con la APK 1.1.1. Pasaron las 23,013 comparaciones de reglas y 2 de horas extra, las 15 comprobaciones de eventos/selección, y las 25 aserciones de almacenamiento/reportes. Los blobs de `CalendarRules.kt` y `Store.kt` siguen iguales. La inspección física del panel, blur y gestos en el teléfono aún está pendiente. La lista detallada está en `android/tests/VERIFICACION_MENU_LATERAL_1.1.2.md`. Sin GitHub Actions ni Netlify.



## 2026-11-03 — APK 1.1.1 compilada: paridad visual neón / WebView

Se compiló la rama `android/kotlin` desde el commit `4ed366438a5cf77562bbed6614ad361c41c4537b`. Incluye el resplandor neón ambiental animado lentamente como fondo, los colores y gradientes de ambas variantes, la tira horizontal compacta, indicadores superpuestos, borde ámbar en la fecha activa y controles mensuales compactos documentados abajo. El efecto se pausa cuando la app va a segundo plano, respeta las animaciones desactivadas en Android y se puede apagar desde Ajustes.

- APK: `Mi-Turno-Android-1.1.1.apk`; paquete `app.miturno`, versionCode 3, Android 8 o posterior.
- SHA-256 APK: `3ec3b55bb046c9e25929d7a6828b6ce315ce589b1c91c4b81fa2eb2cc1e66d49`.
- Certificado SHA-256: `41f530f81876173802f2156e29e105b9714c2ba9ef635a2195166b570800d8bc`, igual a la 1.1.0. Firma v2/v3 verificada y APK alineada; permite actualización sobre la instalación anterior con la misma firma.
- Pruebas locales: 23,013 comparaciones de reglas (2,557 fechas) y 2 comprobaciones de horas extra; 15 verificaciones de eventos/selección; 25 aserciones de almacenamiento, horas extra y reportes; todas aprobadas.
- `CalendarRules.kt` conserva blob Git `75a84b35096aad361116c92de145c7bf5bcb40e6`; `Store.kt` conserva `adfe8a20c7a30def35c7bf7e5c1eebc92176985a`.
- Pendiente: instalar y revisar visualmente en el teléfono, especialmente el efecto de luz, batería, temas, navegación y notificaciones. No se afirma validación física. APK y clave permanecen fuera del repositorio; no se ejecutaron GitHub Actions ni Netlify.

---

## 2026-11-02 — Revisión visual neón / WebView (registro de fuentes previo a la APK)

Comparación con las capturas del usuario y CSS web v10: resplandor cálido/violeta/verde bajo tarjetas, franja horizontal compacta, botón principal verde con degradado y flechas pequeñas de navegación mensual. La web tiene radiales y sombras fijas; en Kotlin se añadieron radiales **animados lentamente** con `NeonAmbientDrawable.kt` como fondo de la ventana, sin mover contenido ni animar textos. Ciclo de 26 s, repintado limitado aproximadamente a 12 FPS, se pausa en `onPause` y se reanuda en `onResume`. Respeta las animaciones desactivadas por Android y permite apagarlo desde Ajustes.

Se recuperaron proporciones del carrusel web: ancho adaptable 52–67 dp, altura 72 dp, números 16 sp, etiquetas 9–10 sp, seis días visibles aprox. según pantalla; el foquito de recordatorio es una superposición de 11 dp que no cambia el tamaño de la tarjeta. Los botones de navegación mensual son 34 dp (antes 45 dp con alto mínimo 48); borde de selección de tira ámbar; gradientes con los mismos colores del calendario original. El botón «Alternar Tipo de Calendario» tiene transición cromática propia para ambos tipos y el botón de tema conserva control individual.

- Archivos cambiados: `MainActivity.kt`, nuevo `NeonAmbientDrawable.kt`, `build.gradle.kts` y documentación.
- Versión reservada **1.1.1 (versionCode 3)**; la APK 1.1.0 entregada previamente **NO** incluye estos ajustes.
- Verificación estática: carga de archivos GitHub y presencia de métodos/recursos; sin instalación ni captura de la nueva versión.
- `CalendarRules.kt` conserva SHA Git `75a84b35096aad361116c92de145c7bf5bcb40e6`; `Store.kt` conserva `adfe8a20c7a30def35c7bf7e5c1eebc92176985a`. No cambian anclas, horarios, festivos, guardias ni rotaciones.
- La compilación de estas fuentes se completó el 3 de noviembre de 2026 como APK 1.1.1 (ver registro superior). Pendiente: validar en teléfono iluminación nocturna/diurna, consumo/batería, scroll horizontal, barra mensual, tema claro/oscuro, permisos y actualización sin borrar datos. No ejecutar GitHub Actions o Netlify.


### APK anterior: versión 1.1.0 (2 de noviembre de 2026)

Entrega del 2 de noviembre de 2026 (México). Rama `android/kotlin` de `guillermocielo16-alt/Wokrendin`. Proyecto Android Studio en `android/`. Versión 1.1.0, código 2, paquete `app.miturno`, Android 8 o posterior.

## Cambios

La integración disponible en el commit `b8ba18034e5fc83c9a02c862e48fabee671d8f74` se compiló localmente. El botón «Sincronizar calendario» explica el permiso y permite elegir fuentes del calendario Android. La consulta se hace en segundo plano para el mes visible, la fecha seleccionada y la tira de 30 días. Cambiar de mes requiere pulsar el botón de nuevo. Cada lectura exitosa reemplaza los resultados y conserva solo los IDs de los calendarios elegidos en preferencias separadas. Los detalles de citas viven en memoria durante la sesión.

Los foquitos aparecen en leyenda, mes y tira. Se usa ámbar oscuro en tema claro y ámbar claro en tema oscuro para distinguir el indicador sobre el fondo. Los colores de los turnos, guardias y descansos se conservan. Elegir fecha desde el mes o la tira actualiza el encabezado, el resultado laboral y los detalles de las citas. La propuesta histórica se conserva en `proposals/android-calendar-readonly/`.

El aviso laboral diario usa el canal de recordatorios de Android y abre la fecha correspondiente al tocarlo. El aviso de prueba tiene su propio identificador. Las citas importadas se muestran para consulta y sus alertas las gestiona el calendario de origen. El aviso laboral usa una alarma inexacta y puede retrasarse según el sistema.

## Compilación y firma

El compilador local enlaza primero los recursos para generar los IDs que usa Kotlin. La versión se toma de Gradle. Se reutilizó la firma de la APK 1.0.0. La comprobación comparó el certificado de ambas APK y verificó esquema v2/v3, alineación y manifiesto. Las claves y APK se mantienen fuera del repositorio público.

- APK `Mi-Turno-Android-1.1.0.apk`.
- SHA-256 `f4b5d094fd7abf6c41ad0b685d88957c27303f33ed00e344c4e6705d0e9e6ef9`.
- Certificado SHA-256 `41f530f81876173802f2156e29e105b9714c2ba9ef635a2195166b570800d8bc`.

## Validación ejecutada

- 15 comprobaciones JVM del modelo de eventos y selección.
- 23,013 comparaciones de reglas en 2,557 fechas contra el HTML web v10 y 2 comprobaciones de suma.
- 25 comprobaciones JVM de registros y reportes.
- `CalendarRules.kt` conserva el blob `75a84b35096aad361116c92de145c7bf5bcb40e6`. El almacenamiento laboral conserva el blob `adfe8a20c7a30def35c7bf7e5c1eebc92176985a`.
- Manifiesto empaquetado con READ_CALENDAR, POST_NOTIFICATIONS y RECEIVE_BOOT_COMPLETED. Sin acceso de escritura al calendario ni permiso de Internet.
- Compilación con Kotlin 2.0.21, Java 17 y Android SDK 35 en el entorno local. Sin GitHub Actions ni Netlify.

Pendiente validar en el teléfono la actualización sobre 1.0.0, permisos, lectura de citas reales, colores, navegación táctil y recepción de avisos. La coincidencia de paquete y certificado comprueba la compatibilidad de firma. La conservación de datos en el teléfono se confirma al actualizarlo.

## Invariantes

Preservar los dos calendarios laborales, anclas, turnos, festivos, guardias, descansos y cálculos. El ciclo de 28 días usa el ancla 16-12-2025. El ciclo 2-2-3 usa el ancla 05-01-2026 y el cambio cada 56 días. Una guardia trabajada suma 12 h. Las fracciones se registran por fecha y se reemplazan al repetirla. Para el origen de la entrega anterior consultar `ESTADO_ENTREGA_1.0.0_2026-10-28.md` y `docs/ANDROID_BASELINE.json`.
