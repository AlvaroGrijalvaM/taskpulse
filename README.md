# TaskPulse

Aplicación independiente para relojes con Wear OS: crea tareas con fecha y hora
y recibe un recordatorio —con sonido y vibración— en el momento programado.
Todo funciona en el propio reloj, sin teléfono, sin Internet ni cuenta de usuario.

Desarrollada en Kotlin con Jetpack Compose for Wear OS. Las tareas se guardan
con Room (SQLite local) y las alarmas se programan con `AlarmManager`, de modo
que los recordatorios se disparan aunque la app esté cerrada.

## Funciones

- Lista de tareas pendientes ordenadas por fecha, con las vencidas destacadas.
- Crear, editar, completar y eliminar tareas desde el reloj.
- Selector manual de fecha y hora con ruedas de Wear OS (día, hora y minuto),
  con soporte de la corona rotativa.
- Recordatorio con notificación, sonido y vibración a la hora programada.
- Al tocar la notificación (o "Open on watch" desde un móvil pareado) se abre
  el detalle de la tarea, listo para completarla o editarla.
- Interruptor global en Ajustes: al apagarlo cancela las alarmas pendientes
  y al reactivarlas las vuelve a programar.
- Reprogramación automática de alarmas tras reiniciar el reloj y al mostrar
  la lista: ninguna alarma programada se pierde.
- Ajustes con estado de los permisos que necesita el sistema de avisos.

## Compilar y probar

Requisitos: Android Studio (con SDK 35 y build-tools 35.0.1), JDK 17 y un
reloj o emulador con Wear OS (Android 8.0 / API 26 o superior).

1. Abrir esta carpeta con Android Studio (el proyecto Gradle está en la raíz).
2. Crear un emulador de Wear OS: **Device Manager → Create device → Wear OS**
   (por ejemplo, "Wear OS Large Round", API 35 con imagen Google Play).
3. Instalar y ejecutar: `gradlew.bat :app:installDebug` (Windows) o
   `./gradlew :app:installDebug` (macOS/Linux), o botón *Run* de Android Studio.

También se puede compilar el APK desde la terminal:

```sh
./gradlew.bat :app:assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

Para depurar con el reloj conectado (alarmas recibidas, avisos publicados,
intents de notificación):

```sh
adb logcat -s TaskPulse
```

## Permisos y limitaciones

- `POST_NOTIFICATIONS`: se solicita al abrir la app (Android 13+). Sin él no
  se muestran recordatorios.
- `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM`: permiten alarmas puntuales. Si
  el sistema los deniega, la app recurre a una alarma inexacta (el aviso puede
  llegar algo tarde) y la pantalla de Ajustes lo indica.
- El sonido y la vibración dependen de la configuración del reloj (modo no
  molestar, volúmenes, canal de notificaciones de la app). La app no puede
  forzarlos; la recreación del canal en cada aviso garantiza que sus ajustes
  vigentes se apliquen siempre.
- Tras un reinicio, `BootReceiver` vuelve a programar las alarmas pendientes
  desde la base de datos local. Si el reloj estuvo apagado y un recordatorio
  ya pasó, la tarea aparece como vencida en la lista sin emitir el aviso.
- Borrar o completar una tarea descarta también su notificación pendiente.

## Solución de problemas

- **La build falla con `Unable to delete directory ...`**: borra las carpetas
  `build/` y `app/build` (o ejecuta `attrib -R -H -S "<ruta>\build" /S /D` en
  Windows) y vuelve a compilar. Suele deberse a archivos heredados de otra
  ubicación con atributos especiales.
- **No llega sonido o vibración**: comprueba el canal en el reloj
  (Ajustes → Apps → TaskPulse → Notificaciones) y los volúmenes; con
  `adb logcat -s TaskPulse` se ve si el aviso se publicó y con qué motivo.

## Estructura

```
app/src/main/java/com/taskpulse/
├── data/          # Room: entidad Task, DAO, base de datos y repositorio
├── reminders/     # AlarmManager: programación, receptores, arranque y
│                  # preferencia global de recordatorios
├── notifications/ # Canal y notificación del recordatorio
├── ui/            # ViewModels, navegación (SwipeDismiss) y pantallas Compose
└── util/          # Formato de fecha y hora
```