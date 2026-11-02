> Documento histórico de la entrega original. Para el respaldo público y los pendientes actuales, consultar ESTADO_ACTUAL.md. La clave mencionada aquí NO se publica en GitHub.

# Mi Turno Android 1.0.0 — Estado de entrega

Fecha: 28 de octubre de 2026 (México).

## Entrega

APK instalable firmado para distribución interna; aplicación nativa Kotlin con vistas Android, sin WebView ni JavaScript. Compatible desde Android 8.0. Funciona sin conexión. Se entrega además el proyecto fuente, sus pruebas, licencias y la firma interna necesaria para actualizar esta instalación conservando los datos.

Fuente usada: `mi-turno-netlify-final.zip`, SHA-256 `a96b2d69d7621600ecb338f0994d0bd5f93970d594eba124a6cde4114d6f37bd`. No se ha modificado ni desplegado el sitio web, ni se han usado GitHub Actions.

## Lógica y funciones

Se tradujeron los patrones originales, sin mover horarios ni fechas de referencia: ciclo de 28 días para A/B/C/D (16-12-2025) y 2-2-3 para AB/CD (05-01-2026, patrón de 14 días, cambio cada 56). Se usa fecha civil para evitar que cambios de hora desplacen días del patrón.

Se conservan los ocho festivos originales de 2026, las coincidencias entre calendarios, consultas por fecha, navegación mensual, próximos 30 días, personal por equipo, guardias pendientes/canceladas/trabajadas, vacaciones, fracciones de horas extra, suma de 12 h por guardia trabajada y reportes filtrados. Las fracciones mantienen un registro por fecha; guardar otra en esa fecha reemplaza la anterior.

Reportes: vista previa, TXT guardado mediante el selector de archivos, copiar y compartir. Integrantes independientes para ambos calendarios; perfil, turno y hasta cuatro áreas para el segundo. Los nombres comienzan vacíos; se retiró el nombre personal precargado en el ZIP.

## Diseño y adaptación

Fuentes Sora, Inter y JetBrains Mono incluidas dentro de la app. Se reproducen las paletas independientes de ambos calendarios y sus temas claro/oscuro, celdas de color, tarjeta de estado, franja lateral, fechas y marcadores. Se añaden animaciones de entrada y pulsación, ripple y respuesta háptica.

Se adapta la gestión lateral a pantallas nativas. No se afirma igualdad píxel a píxel: hace falta confirmar medidas, fuentes y comportamiento visual en un teléfono real.

## Funciones Android añadidas

Recordatorio diario configurable del turno de mañana para el calendario y grupo seleccionados, permiso opcional de notificaciones y reprogramación al reiniciar, cambiar la hora/zona o actualizar. Usa AlarmManager sin privilegio de alarma exacta: Android y el fabricante pueden retrasar el aviso. Forzar detención impide recibir avisos hasta abrir de nuevo la app.

Copia JSON y restauración confirmada de los registros y ajustes locales. No existe sincronización automática con los datos del navegador, cuentas de Google Calendar ni servicio remoto.

## Validación realizada

- Compilación Kotlin completa y generación del APK.
- Firma APK verificada: esquemas v2 y v3.
- 23,013 comprobaciones diferenciales en 2,557 fechas entre 2024 y 2030: turnos A/B/C/D, descansos CD, grupos AB/CD, cambios y festivos coinciden con el JavaScript original.
- Dos comprobaciones adicionales de suma de horas extra.
- 25 comprobaciones JVM de registros y reportes con preferencias simuladas: suma, reemplazo de fracciones, persistencia en el adaptador de prueba, aislamiento de vacaciones por equipo, intervalos inclusivos, cancelaciones y notas como texto. No son una prueba de SharedPreferences real ni sustituyen la ejecución del APK.
- Revisión del manifiesto: permisos de notificaciones y arranque; sin permiso de Internet ni acceso amplio a archivos.

El registro reproducible está incluido en `tests/VALIDATION.txt` dentro de los fuentes.

## Verificación pendiente en Android

El entorno no tiene aceleración KVM. Se preparó un emulador Android 11 por software, pero no se logró completar la sesión de validación por ADB al preparar esta entrega. Se incluyen las pruebas de instrumentación; su ejecución no se presenta como aprobada.

Quedan pendientes instalación y apertura en teléfono, revisión visual de ambos temas/calendarios, selector de guardias, vacaciones, entrada y exportación de datos, permisos/recepción real de notificaciones y restauración tras reinicio. La compilación y la comparación de reglas sí están aprobadas; no se declara la app completamente validada en dispositivo.

## Instalación y continuidad

Instalar `Mi-Turno-Android-1.0.0.apk`. Es una firma interna de prueba, no una publicación en Play Store. El proyecto fuente incluye instrucciones de Android Studio, compilación local, pruebas, licencias y la clave de prueba en `signing/`; conservar esta clave para actualizaciones y no publicarla en un repositorio abierto.

SHA-256 del APK: `0af7e233008bd56f0fd8e3ddcb685439d5526df13dd7c0bd2e96d9e7117ebe95`.
