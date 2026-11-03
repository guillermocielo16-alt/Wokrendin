# Mi Turno Android — estado y próxima versión

## 2026-11-02 — Revisión visual neón / WebView (fuentes 1.1.1, SIN APK)

Comparación con las capturas del usuario y CSS web v10: resplandor cálido/violeta/verde bajo tarjetas, franja horizontal compacta, botón principal verde con degradado y flechas pequeñas de navegación mensual. La web tiene radiales y sombras fijas; en Kotlin se añadieron radiales **animados lentamente** con `NeonAmbientDrawable.kt` como fondo de la ventana, sin mover contenido ni animar textos. Ciclo de 26 s, repintado limitado aproximadamente a 12 FPS, se pausa en `onPause` y se reanuda en `onResume`. Respeta las animaciones desactivadas por Android y permite apagarlo desde Ajustes.

Se recuperaron proporciones del carrusel web: ancho adaptable 52–67 dp, altura 72 dp, números 16 sp, etiquetas 9–10 sp, seis días visibles aprox. según pantalla; el foquito de recordatorio es una superposición de 11 dp que no cambia el tamaño de la tarjeta. Los botones de navegación mensual son 34 dp (antes 45 dp con alto mínimo 48); borde de selección de tira ámbar; gradientes con los mismos colores del calendario original. El botón «Alternar Tipo de Calendario» tiene transición cromática propia para ambos tipos y el botón de tema conserva control individual.

- Archivos cambiados: `MainActivity.kt`, nuevo `NeonAmbientDrawable.kt`, `build.gradle.kts` y documentación.
- Versión reservada **1.1.1 (versionCode 3)**; la APK 1.1.0 entregada previamente **NO** incluye estos ajustes.
- Verificación estática: carga de archivos GitHub y presencia de métodos/recursos; sin instalación ni captura de la nueva versión.
- `CalendarRules.kt` conserva SHA Git `75a84b35096aad361116c92de145c7bf5bcb40e6`; `Store.kt` conserva `adfe8a20c7a30def35c7bf7e5c1eebc92176985a`. No cambian anclas, horarios, festivos, guardias ni rotaciones.
- Pendiente: compilación local con Android SDK y firma original privada; validar iluminación nocturna/diurna, consumo/batería, scroll horizontal, barra mensual, tema claro/oscuro, permisos y actualización sin borrar datos. No ejecutar GitHub Actions o Netlify.


### Última APK entregada: versión 1.1.0

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
