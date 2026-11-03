# Pruebas de regresión — Menú lateral nativo 1.1.2

Estado: **fuentes modificadas; pendiente compilación y pruebas físicas**. Aplicación: `app.miturno`. Versión prevista: 1.1.2 (versionCode 4).

## Pruebas de interfaz y gestos en Android

- [ ] Abrir Calendario 1 (ciclo 28 días), pulsar ☰: aparece panel izquierdo de hasta 300 dp, colores y acento verde originales; el resto del calendario sigue visible y atenuado.
- [ ] En Android 12+ (API 31+), verificar que detrás del panel se vea el desenfoque leve. En Android 8–11, comprobar al menos el oscurecimiento, sin errores.
- [ ] Abrir Cal 2 (2-2-3), pulsar ☰: icono principal y detalles del panel en malva/rosa, no verde; panel con fondo morado oscuro.
- [ ] Menú: Calendarios, Horas Extra, Ajustes Cal 1/2, Días festivos, Recordatorios; ninguna opción debe abrir un cuadro gris de lista estándar.
- [ ] Horas Extra abre su contenido dentro del panel sin ocultar el fondo; guardias, fracciones, filtros, informe y exportación siguen funcionando.
- [ ] Ajustes Cal 2 muestra primero Mi Perfil, Turno, resaltados, áreas y después equipos; guardar no cambia las rotaciones.
- [ ] Recordatorios entra directamente al apartado de notificaciones dentro del panel; editar la hora o activar/desactivar avisos persiste y no cierra el panel inesperadamente.
- [ ] Cerrar con ×; cerrar al tocar fuera; cerrar al deslizar hacia la izquierda sobre el panel.
- [ ] Atrás Android por gesto o botón: una subpantalla del panel vuelve al índice; un nuevo Atrás cierra el panel; no sale de la app ni vuelve a pantalla principal en ese recorrido.
- [ ] Abrir diálogos de hora, fechas y selección de equipos dentro del panel; Atrás cierra primero el diálogo.
- [ ] Transición de apertura y cierre fluida (~290/260 ms), sin saltos ni parpadeos del fondo.
- [ ] Con el teclado abierto, guardar nombres, áreas y notas no impide navegar y cerrar el panel.
- [ ] Pantalla vertical y horizontal, y equipos largos; el panel desplaza su contenido vertical sin cortar controles.
- [ ] Cambiar claro/oscuro en ambos calendarios; colores y texto legibles.
- [ ] Con fondo neón animado, comprobar consumo razonable y que el resplandor no atraviesa el texto del panel.
- [ ] Después de cerrar el panel, tocar fechas y deslizar la tira horizontal normal, sin bloqueos táctiles.
- [ ] Verificar que una notificación del turno abre el calendario y no deja superpuesto un panel antiguo.

## Invariantes

- `CalendarRules.kt` blob SHA Git esperado: `75a84b35096aad361116c92de145c7bf5bcb40e6`.
- `Store.kt` blob SHA Git esperado: `adfe8a20c7a30def35c7bf7e5c1eebc92176985a`.
- No se cambian horarios 08:00–20:00/20:00–08:00, anclas de los ciclos, festivos ni cálculos de días.
- No se modifica la rama web `main`, ni se usan GitHub Actions o Netlify.
- APK 1.1.1 y firma originales deben mantenerse disponibles como referencia; actualizar sin borrar datos mediante la misma clave privada.

## Limitaciones de la revisión actual

La comprobación del código fuente y los diffs **no sustituye una compilación con Android SDK** ni valida gestos/desenfoque en el teléfono. Confirmar todas las casillas después de compilar localmente y probar la APK 1.1.2 en Android.
