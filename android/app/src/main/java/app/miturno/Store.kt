package app.miturno

import android.content.Context
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import org.json.JSONObject

class Store(context: Context) {
    val prefs = context.getSharedPreferences("mi_turno", Context.MODE_PRIVATE)

    fun text(key: String, default: String = "") = prefs.getString(key, default) ?: default

    fun put(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    fun data(key: String) = JSONObject(text(key, "{}"))

    fun value(kind: String, key: String) = data(kind).optString(key, "")

    fun update(kind: String, key: String, value: String) {
        val obj = data(kind)
        if (value.isEmpty()) obj.remove(key) else obj.put(key, value)
        put(kind, obj.toString())
    }

    fun vacation(date: LocalDate, team: String) = value("vacations", "${date}_$team") == "si"

    fun guard(date: LocalDate, team: String) = value("guards", "${date}_$team")

    fun total(): Double {
        val g = data("guards")
        val f = data("fractions")
        return CalendarRules.extraTotal(
            g.keys().asSequence().associateWith { g.getString(it) },
            f.keys().asSequence().associateWith { f.getJSONObject(it).getDouble("hours") },
        )
    }

    fun report(start: String = "", end: String = ""): String {
        fun range(key: String): Boolean {
            val d = key.substringBefore('_')
            return (start.isEmpty() || d >= start) && (end.isEmpty() || d <= end)
        }
        fun formatted(key: String) =
            LocalDate.parse(key.substringBefore('_'))
                .format(
                    DateTimeFormatter.ofPattern(
                        "EEEE d 'de' MMMM 'de' yyyy",
                        Locale.forLanguageTag("es-MX"),
                    )
                )
        fun name(team: String): String {
            val n = text("member1$team").trim().split(Regex("[\\n, ]")).firstOrNull().orEmpty()
            return if (n.isEmpty()) "" else " ($n)"
        }
        val rangeLabel =
            when {
                start.isNotEmpty() && end.isNotEmpty() -> "$start al $end"
                start.isNotEmpty() -> "Desde $start"
                end.isNotEmpty() -> "Hasta $end"
                else -> "Todo el historial"
            }
        val b =
            StringBuilder(
                "REPORTE DE GUARDIAS Y VACACIONES\nGenerado el: ${LocalDateTime.now()}\nRango: ${rangeLabel}\n"
            )
        b.append("\n--- GUARDIAS ---\n")
        val g = data("guards")
        val keys =
            g.keys()
                .asSequence()
                .filter { range(it) && g.getString(it) != "pendiente" }
                .sorted()
                .toList()
        if (keys.isEmpty()) b.append("(Sin registros en este rango)\n")
        keys.forEach { k ->
            val t = k.substringAfter('_')
            val name = text("member1$t").trim().split(Regex("[\\n, ]")).firstOrNull().orEmpty()
            val label =
                when (g.getString(k)) {
                    "trabaja_dia" -> "Cubrió turno DÍA (08:00 - 20:00)"
                    "trabaja_noche" -> "Cubrió turno NOCHE (20:00 - 08:00)"
                    else -> "No trabajó (guardia cancelada)"
                }
            b.append(
                "Equipo $t${if(name.isEmpty()) "" else " ($name)"} - ${formatted(k)}: $label\n"
            )
        }
        b.append("\n--- TIEMPO EXTRA / FRACCIONES ---\n")
        val f = data("fractions")
        val fs = f.keys().asSequence().filter { range(it) }.sorted().toList()
        if (fs.isEmpty()) b.append("(Sin registros de fracciones en este rango)\n")
        fs.forEach {
            val item = f.getJSONObject(it)
            b.append(
                "[${formatted(it)}] ${item.getDouble("hours")} hrs extra - ${item.getString("note")}\n"
            )
        }
        b.append("\n--- VACACIONES ---\n")
        val v = data("vacations")
        val vs =
            v.keys().asSequence().filter { range(it) && v.getString(it) == "si" }.sorted().toList()
        if (vs.isEmpty()) b.append("(Sin registros en este rango)\n")
        vs.forEach {
            b.append(
                "Equipo ${it.substringAfter('_')}${name(it.substringAfter('_'))} - ${formatted(it)}: Día de vacaciones\n"
            )
        }
        return b.toString().replace("\n", "\r\n")
    }
}
