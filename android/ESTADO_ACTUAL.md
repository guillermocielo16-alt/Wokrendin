# Mi Turno Android — estado actual

Fecha de respaldo: 2 de noviembre de 2026 (México).

## Referencia para continuar

- Repositorio: `guillermocielo16-alt/Wokrendin`.
- Rama exclusiva Android: `android/kotlin`.
- Proyecto Android Studio: carpeta `android/`.
- Base funcional: Mi Turno Android nativo 1.0.0, paquete `app.miturno`, versionCode 1.
- La rama web `main` permanece en `1fe8cdb5ff5a14ad8479ab217842e50e529c1055` al crear este respaldo. Los ocho archivos web se heredan sin cambios. No fusionar esta rama a main para publicar Android.
- Esta operación respalda y documenta; no modifica reglas de negocio, no genera un APK nuevo y no integra funcionalidades nuevas.

## Fuentes y autenticidad

Se recuperó el paquete exacto `Mi-Turno-Android-1.0.0-Fuentes.zip`, SHA-256 `8ea72f1d14a1e1da4fb0501ffeb0bd4cdfa5fca3d77a07af631470f81a226e14`. El APK entregado corresponde al SHA-256 `0af7e233008bd56f0fd8e3ddcb685439d5526df13dd7c0bd2e96d9e7117ebe95`. No se suben APK, cachés, SDK ni herramientas de compilación.

Todos los archivos de `app/src/main/` son idénticos a ese ZIP. El manifiesto `docs/ANDROID_BASELINE.json` permite comprobarlos por SHA-256. Se adaptó únicamente la configuración de firma y sus instrucciones para que el repositorio público no contenga claves. La firma original sigue en el paquete privado de fuentes entregado; debe recuperarse localmente para actualizar la instalación existente sin desinstalarla.

## Funciones existentes

Dos calendarios nativos: ciclo de 28 días A/B/C/D y 2-2-3 AB/CD. Consultas de fecha, navegación mensual y tira de 30 días. Coincidencias de descansos y guardias, ocho festivos de 2026, personal por equipo, guardias, vacaciones, fracciones de horas extra, total de horas, reportes TXT con filtros, copiar y compartir. Perfil/turno/cuatro áreas del calendario 2. Temas y fuentes originales, animaciones nativas, copias JSON.

La notificación actual es un aviso diario configurable del turno de mañana. No lee eventos del calendario Android.

## Reglas que deben permanecer intactas

`CalendarRules.kt`: ancla 28 días 16-12-2025; ancla 2-2-3 05-01-2026; patrón AB de 14 días; cambio cada 56 días. Día 08–20, noche 20–08; 12 h por guardia trabajada. Un registro de fracción por fecha, reemplazable. Las vacaciones y guardias se guardan por fecha/equipo. Los festivos son ocho fechas explícitas de 2026: no extrapolarlos ni desplazar los ciclos. Usar las funciones existentes como caja negra para futuros cambios de interfaz o recordatorios.

## Pruebas documentadas de la entrega

- Compilación y firma APK v2/v3 aprobadas en la entrega original.
- 23,013 comparaciones de reglas en 2,557 fechas (2024–2030) y dos comprobaciones extra de suma.
- 25 comprobaciones JVM de registros/reportes con preferencias simuladas.
- Registro histórico: `tests/VALIDATION.txt`. No equivale a pruebas en teléfono.
- Emulador y pruebas de instrumentación: no completadas. Revisión visual y notificaciones reales pendientes.

Durante este respaldo se verificaron integridad de los 13 archivos de ejecución, ausencia de claves y sintaxis de scripts. Se regeneraron con el HTML web v10 las expectativas de 2,557 fechas en TZ=America/Mexico_City y coincidieron con las guardadas. No se repitieron la compilación Kotlin, la suite JVM ni las pruebas en dispositivo.

## Pendiente ya preparado: lectura del calendario del teléfono

`proposals/android-calendar-readonly/` conserva el paquete `mi-turno-android-recordatorios-modulo-kotlin.zip` sin integrarlo en `app`. Su documento de integración es la especificación funcional y su resultado registra 15 pruebas JVM históricas del modelo. El lector CalendarContract aún requiere integración y validación con Android.

Objetivo acordado: sincronización manual y unidireccional Android → Mi Turno, permiso READ_CALENDAR, selección de calendarios, foquito en leyenda/mes/tira, fecha seleccionada única, detalles completos debajo del resultado laboral. Eventos externos solo en memoria de la sesión, sin exportarlos a copias, logs o repositorio. Sin WRITE_CALENDAR, OAuth ni servicios pagados. No confundir esos eventos con la notificación diaria ya existente.

El texto del paquete que indica que aún no se disponía de los fuentes es histórico; con este respaldo ya están en `android/`. El módulo sigue NO INTEGRADO y no forma parte de la APK 1.0.0.

## Siguiente trabajo

1. Partir de esta rama y registrar el commit base antes de editar.
2. Aplicar los ajustes que indique el usuario, preservando paquete, firma, datos y reglas.
3. Si se integra el módulo, seguir `proposals/android-calendar-readonly/INTEGRACION_Y_RESTRICCIONES.md` y verificar permisos, eventos repetidos, medianoche, selección compartida y revocación.
4. Ejecutar pruebas y compilar localmente; no GitHub Actions ni despliegues Netlify.
5. Validar actualización en teléfono con la firma original. Actualizar este estado y el historial antes de entregar el APK.
