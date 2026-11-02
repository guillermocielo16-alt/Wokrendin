# Mi Turno Android — módulo de recordatorios de solo lectura

**Estado: MÓDULO PREPARADO, NO INTEGRADO EN LA APK.**

Este paquete está destinado **exclusivamente a la versión nativa Kotlin** de Mi Turno / Calendario de trabajo. El repositorio público `Wokrendin` contiene la **versión web**, no el proyecto Kotlin que generó la APK. No se modificaron ni el repositorio, ni Netlify, ni la app instalada. Para integrar el módulo en el proyecto nativo es indispensable obtener los fuentes de la última APK aprobada, no reconstruirlos basándose en la web.

## Objetivo funcional acordado

Sincronización **manual y unidireccional**: **calendario de Android → Mi Turno**. Solo se leen eventos existentes en calendarios configurados en el dispositivo, sin API de Google ni cuentas adicionales. Los eventos creados por voz o desde correo deben aparecer previamente en el proveedor de calendarios del teléfono para poder ser importados.

1. Dentro de la leyenda bajo el mes, agregar un icono de **foquito** con la etiqueta **«Recordatorios»**; usar exactamente la escala de los otros iconos (el recurso se entrega en `src/main/res/drawable/ic_reminder_bulb.xml`, 16 dp de referencia, ajustable a la escala existente).
2. Cada casilla mensual con al menos un recordatorio lleva ese mismo foquito **pequeño**, sin reemplazar símbolos existentes de turno, descanso, guardia o festivo.
3. La tira horizontal de días será desplazable y seleccionable; cada día con un recordatorio mostrará el foquito. El usuario podrá tocar cualquier día visible para seleccionarlo.
4. **Una única `selectedDate`** gobernará: a) encabezado superior de la fecha consultada, b) casilla mensual resaltada, c) día activo de la tira horizontal, d) área del resultado laboral original, e) detalle de los recordatorios. Al abrir se inicia en la fecha de hoy; al elegir otra fecha el encabezado debe pasar a esa fecha, sin fingir que cambió la fecha real del sistema.
5. Al tocar una fecha con eventos, **debajo** del resultado laboral original aparecerá «Tienes un recordatorio» y, si hay varios, una tarjeta por evento. Mostrar título, fecha/hora de inicio y fin o «Todo el día», descripción completa, ubicación y calendario origen, si esas propiedades están disponibles. Textos largos deben poder expandirse/recorrerse, nunca recortarse definitivamente. No confundir «recordatorio» con «turno de trabajo» ni cambiar el cálculo laboral.
6. Botón **«Sincronizar calendario»**, manual y visible. Primera vez: solicitar permiso `READ_CALENDAR` y permitir elegir qué calendarios leer. Importar solo los calendarios seleccionados. No otorgar `WRITE_CALENDAR`.
7. Los eventos importados son datos personales: **solo en memoria de la aplicación durante la sesión**, no exportarlos a un repositorio, registros de diagnóstico, copias de seguridad, telemetría o Netlify. Sin permiso: no mostrar eventos externos. No mostrar información de calendarios no seleccionados.
8. Cada sincronización sustituye el índice temporal anterior y quita recordatorios que ya no existan. Mostrar éxito/error sin fingir que una consulta fallida significa que el usuario no tiene citas.

## Prohibiciones absolutas

- **No cambiar** los horarios, patrones de turnos, descansos, guardias, vacaciones, fechas de referencia ni ninguna función que los calcula. Su lógica original debe ser tratada como una caja negra de solo lectura.
- No instalar o contratar API de Google Calendar, no solicitar OAuth, no usar servicios pagados.
- No publicar una nueva web, no vincular GitHub con Netlify, no ejecutar GitHub Actions, no crear un sitio nuevo.
- No sobrescribir el nombre del paquete, las claves de firma, ni desinstalar la APK existente: la actualización debe conservar los datos del teléfono.

## Archivos incluidos

- `ReminderModels.kt`: estructuras de eventos, índice por día y navegación única; **probado localmente en JVM**.
- `AndroidCalendarReader.kt`: lector nativo con `CalendarContract.Instances` y `READ_CALENDAR`; requiere **compilar y probar contra Android SDK y el proyecto real**.
- `ic_reminder_bulb.xml`: icono vectorial pequeño para superponer en las superficies existentes.
- `ReminderModelsTest.kt`: pruebas puras Kotlin sobre fechas, navegación, eventos de varios días, repetidos y medianoche.
- `src/main/AndroidManifest-permission-snippet.xml.txt`: permiso a incorporar (sin escribir una nueva declaración `application`).

## Integración en la APK real (pendiente, no realizada)

1. Obtener y respaldar exactamente el proyecto Kotlin original que compiló la APK actualmente instalada.
2. Crear una rama de trabajo local o un commit separado para la integración, preservando un hash de la versión base y los archivos de cálculo laboral.
3. Insertar `READ_CALENDAR` en el **manifest** del proyecto Android. Registrar en la `Activity` existente la solicitud de permiso en tiempo de ejecución, iniciada al pulsar «Sincronizar calendario». Denegación = calendario laboral sigue funcionando normalmente.
4. Crear selector de calendarios de origen a partir de `AndroidCalendarReader.availableCalendars()`. Guardar únicamente IDs escogidos, no contenidos de eventos. Primera sincronización solo de calendarios seleccionados.
5. En un hilo de I/O, consultar `AndroidCalendarReader.readVisibleEvents()` para el intervalo del mes visible + días necesarios para la tira horizontal. Procesar las ocurrencias repetidas con `ReminderDayIndex.build()`. **Nunca ejecutar consultas al proveedor en el hilo principal**.
6. Adaptar **las vistas nativas ya existentes** para colocar el vector `ic_reminder_bulb` en la leyenda, en cada celda con evento y en cada día horizontal. Respetar tamaño/estilo ya establecidos; la estructura exacta de las vistas (Compose, XML u otras) debe inspeccionarse en la fuente real antes de parchearla.
7. Unificar los callbacks de toque del mes y de la tira horizontal con `ReminderNavigationModel.onMonthDayPressed(date)` y `onHorizontalDayPressed(date)`. Renderizar todas las superficies desde el `ReminderNavigationSnapshot` resultante. Calcular estado laboral utilizando **la función original sin modificar**.
8. Mostrar panel de notas bajo el resultado laboral existente, con tantas tarjetas como eventos, con textos completos y scroll. Renderizar siempre como `TextView`/`Text` plano, no como HTML ejecutable.
9. Registrar pruebas de calendario mensual, tira horizontal, permisos, repetición, horarios locales, límite de día, sin Internet, eventos eliminados y revocación de permisos. Comparar resultados originales de turnos fecha por fecha antes y después.
10. Compilar **localmente**, sin GitHub Actions ni Netlify, usando el package name y firma originales, e instalar como actualización sin borrar los datos. Conservar el APK anterior como punto de recuperación.

## Estado de pruebas

**Completadas:** pruebas JVM para el índice de eventos por fecha y la selección compartida; ninguna prueba modifica horarios de trabajo. `javac/kotlinc` disponible, SDK Android NO disponible en el entorno de revisión.

**Pendientes por falta de fuentes y SDK nativo:** integración con Activity, lectura real de `CalendarContract`, representación visual con diseño idéntico, comportamiento de la vista horizontal real, prueba en dispositivo, actualización firmada de la APK.

## Referencias técnicas

- https://developer.android.com/reference/android/provider/CalendarContract.Instances
- https://developer.android.com/reference/android/Manifest.permission#READ_CALENDAR
- https://developer.android.com/training/permissions/requesting
