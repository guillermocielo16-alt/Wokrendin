package app.miturno

import android.app.*
import android.content.*
import android.os.Build
import java.time.*

object Reminders {
    fun channel(c: Context) {
        c.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(
                NotificationChannel(
                    "turnos",
                    "Turnos y recordatorios",
                    NotificationManager.IMPORTANCE_DEFAULT,
                )
            )
    }

    fun schedule(c: Context) {
        val s = Store(c)
        val am = c.getSystemService(AlarmManager::class.java)
        val pi =
            PendingIntent.getBroadcast(
                c,
                1,
                Intent(c, ReminderReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        am.cancel(pi)
        if (s.text("remind") != "on") return
        val hour = s.text("remindHour", "19").toIntOrNull() ?: 19
        val minute = s.text("remindMinute", "0").toIntOrNull() ?: 0
        val now = ZonedDateTime.now()
        var next = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        if (!next.isAfter(now)) next = next.plusDays(1)
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.toInstant().toEpochMilli(), pi)
    }

    fun notify(c: Context, preview: Boolean = false) {
        channel(c)
        val s = Store(c)
        val d = LocalDate.now().plusDays(if (preview) 0 else 1)
        val second = s.text("calendar", "1") == "2"
        val t = s.text("team", "A")
        val g = s.text("group", "CD")
        var type =
            if (second) {
                if (CalendarRules.work223(d, g)) "trabajo" else "descanso"
            } else CalendarRules.status(d, t)
        if (!second && s.vacation(d, t)) type = "vacaciones"
        var detail = CalendarRules.hours(type)
        if (!second && type == "guardia")
            detail =
                when (s.guard(d, t)) {
                    "trabaja_dia" -> "08:00 – 20:00"
                    "trabaja_noche" -> "20:00 – 08:00"
                    "no_trabaja" -> "Guardia cancelada · Día libre"
                    else -> detail
                }
        if (second && type == "trabajo") detail = CalendarRules.hours(s.text("shift", "dia"))
        val pending =
            PendingIntent.getActivity(
                c,
                0,
                Intent(c, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
        val body =
            "$d · ${if(second) "Grupo $g" else "Equipo $t"} · $detail${if(CalendarRules.holiday(d)) " · Festivo" else ""}"
        val n =
            Notification.Builder(c, "turnos")
                .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
                .setContentTitle("${if(preview) "Hoy" else "Mañana"}: ${CalendarRules.label(type)}")
                .setContentText(body)
                .setStyle(Notification.BigTextStyle().bigText(body))
                .setContentIntent(pending)
                .setAutoCancel(true)
                .build()
        if (
            Build.VERSION.SDK_INT < 33 ||
                c.checkSelfPermission("android.permission.POST_NOTIFICATIONS") ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
        )
            c.getSystemService(NotificationManager::class.java).notify(1, n)
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        Reminders.notify(c)
        Reminders.schedule(c)
    }
}

class RestoreReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        Reminders.schedule(c)
    }
}
