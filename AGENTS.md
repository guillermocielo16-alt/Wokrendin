# Instrucciones para trabajar en este repositorio

## Alcance

Proyecto exclusivo Calendario del trabajo / Mi Turno. No mezclar con otros proyectos o repositorios.

- Android: rama `android/kotlin`, proyecto `android/`.
- Web: rama `main`; no modificarla ni fusionar Android en ella como parte de tareas Android.
- Leer primero `android/ESTADO_ACTUAL.md` y `docs/MAPA_ANDROID.md`.
- No crear ramas adicionales salvo instrucción del usuario. Los commits de Android van en `android/kotlin`.
- No GitHub Actions, CI remoto, despliegues Netlify ni enlaces nuevos entre GitHub y Netlify. Compilar y probar localmente.
- Antes de editar, registrar el commit base y comprobar archivos actuales. No reconstruir la app desde la web si los fuentes Kotlin ya están aquí.

## Invariantes

Preservar horarios, patrones, anclas, festivos y cálculo laboral. No cambiar `CalendarRules.kt` por ajustes visuales, navegación o recordatorios. No extrapolar festivos ni alterar ciclos por DST.

Preservar package `app.miturno`, firma y datos. Para actualizaciones, recuperar la clave original localmente; no subir claves, contraseñas, datos personales, APK ni cachés. No desinstalar la app para resolver incompatibilidad de firma sin autorización explícita.

## Módulo preparado

`proposals/android-calendar-readonly/` conserva una propuesta histórica. La integración real de solo lectura está en `android/app/src/main/java/app/miturno/`. Leer su especificación antes de usarlo. Solo READ_CALENDAR; nunca WRITE_CALENDAR, OAuth ni servicios pagados. Eventos externos en memoria de sesión, excluidos de backups, logs y repositorios. La integración debe mantener intactos los cálculos laborales y el diseño.

## Entrega de ajustes

Actualizar `android/ESTADO_ACTUAL.md` y `docs/HISTORIAL_ANDROID.md`. Distinguir pruebas históricas, pruebas ejecutadas ahora y pendientes. No afirmar validación en teléfono, permisos o fidelidad visual si no se ejecutó. Registrar versión, hash del APK y commit fuente al compilar.
