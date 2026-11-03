# Comprobación local — Fluidez y menú original — Android 1.1.3

**Estado:** APK 1.1.3 compilada y firmada; validación física pendiente. SHA-256 `20dad64f1cb198b5f97be3c3a8a67e85c15c0041ffca2523620939ca838224ea`. Certificado `41f530f81876173802f2156e29e105b9714c2ba9ef635a2195166b570800d8bc`, igual a 1.1.2. La compilación y pruebas de código no certifican FPS ni ausencia visual de parpadeos en un dispositivo.

## Pruebas estáticas locales (sin CI)

Ejecutar en la raíz del repositorio:

```sh
node android/tests/check-fluidez-menu.js
```

Comprueba que no cambiaron los blobs de `CalendarRules.kt` y `Store.kt`, que el menú va directamente al apartado original, que no hay pantalla intermedia «Calendarios», que la actualización de fechas es localizada y que la pantalla completa se prepara antes del reemplazo. En esta revisión pasaron 12 aserciones.

## Pruebas manuales necesarias en la APK 1.1.3

- [ ] En Calendario 1 tocar una fecha en el mes: el fondo no queda expuesto por un instante; se actualizan fecha, resultado, guardia/vacaciones y selección del día.
- [ ] Tocar rápidamente 20 fechas distintas en el mismo mes: sin destellos ni reinicio del neón; la aplicación no se cierra.
- [ ] Tocar el día de hoy por primera vez: aparece el borde de selección.
- [ ] Tocar una fecha de otro mes mediante «Consultar una fecha» y volver a hoy: el mes y la barra horizontal se actualizan de forma coherente.
- [ ] Avanzar y retroceder varios meses con las flechas: cambiar solamente el calendario mensual sin parpadear el resto de la pantalla.
- [ ] Arrastrar horizontalmente la tira de días y seleccionar varias fechas consecutivas: conservar la posición de desplazamiento; no reconstruir toda la ventana.
- [ ] Repetir selección de fechas en Calendario 2 (2-2-3), incluidos cambios de mes y días con foquito de recordatorios.
- [ ] Alternar Calendarios 1 y 2, seleccionar equipos, cambiar tema e importar citas: verificar que no aparece pantalla transparente ni fondo descubierto.
- [ ] Abrir menú del **Calendario 1**: mostrar directamente «⏱ Horas Extra», con total acumulado grande, registrar fracción, guardias y fracciones, reportes e integrantes en orden; sin pantalla «Calendarios».
- [ ] Abrir menú del **Calendario 2**: mostrar directamente «⚙ Ajustes Cal 2», perfil, equipo, turno, resaltados, áreas e integrantes; sin panel intermedio.
- [ ] Pulsar Atrás del sistema y deslizar desde el borde: el menú se cierra correctamente y el calendario permanece en la misma fecha; tocar fuera y × también lo cierran.
- [ ] Verificar que conservaron su acceso las opciones ya existentes de iluminación, notificaciones y copias, ubicadas al final del panel.
- [ ] Comprobar encabezado, horas y detalle de guardias con varias fechas de la versión 1.1.2: resultados idénticos.
- [ ] Guardar una fracción de horas extra y volver a abrir el panel: el total y el historial se actualizan, sin borrar otros datos.
- [ ] Con tema claro/oscuro y ahorro de batería: comprobar legibilidad y continuidad; monitorear ANR y uso de CPU.
- [ ] Verificar actualización con mismo certificado y `versionCode=5`, conservando datos de la instalación 1.1.2.

## Restricciones

Queda prohibido cambiar anclas, horarios, festivos, guardias, rotaciones o sumas. No modificar la rama `main`, ni usar GitHub Actions o Netlify. Publicar APK solo tras compilación local y firma privada original; no subir la firma ni datos del teléfono.
