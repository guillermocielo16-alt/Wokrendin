# Calendario de trabajo — rama Android

Esta es la rama **`android/kotlin`** de `guillermocielo16-alt/Wokrendin`.

| Componente | Ubicación | Estado |
| --- | --- | --- |
| Web | Rama `main` | Independiente; archivos heredados sin cambios |
| Android nativo Kotlin | [`android/`](android/) | Base 1.0.0 respaldada |
| Estado y próximos ajustes | [`android/ESTADO_ACTUAL.md`](android/ESTADO_ACTUAL.md) | Punto de partida para continuar |
| Compilación y firma | [`docs/COMPILACION_ANDROID.md`](docs/COMPILACION_ANDROID.md) | Solo entorno local |
| Mapa y reglas | [`docs/MAPA_ANDROID.md`](docs/MAPA_ANDROID.md) | Guía de mantenimiento |
| Módulo de eventos del teléfono | [`proposals/android-calendar-readonly/`](proposals/android-calendar-readonly/) | Preparado, NO integrado |

Los archivos web de la raíz se heredan del commit `1fe8cdb5ff5a14ad8479ab217842e50e529c1055`. La rama `main` no se mueve ni se fusiona durante este respaldo. No desplegar esta rama como una actualización de la web.

No hay workflows nuevos, APK ni claves de firma en esta rama. Todos los futuros cambios de Android se realizan aquí y se documentan antes de entregar una compilación.
