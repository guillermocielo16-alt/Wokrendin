package app.miturno

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Literal translation of the supplied web calendar. Holidays are NOT extrapolated. */
object CalendarRules {
    val holidays =
        setOf(
            "2026-02-02",
            "2026-03-16",
            "2026-05-01",
            "2026-05-05",
            "2026-09-16",
            "2026-11-02",
            "2026-11-16",
            "2026-12-25",
        )
    private val day = "CCBBDDDAACCBBBDDAACCCBBDDAAA"
    private val night = "DDAACCCBBDDAAACCBBDDDAACCBBB"
    private val guard = "AACCBBBDDAACCCBBDDAAACCBBDDD"
    private val pattern = intArrayOf(1, 1, 0, 0, 1, 1, 1, 0, 0, 1, 1, 0, 0, 0)

    fun status(date: LocalDate, team: String): String {
        val i = Math.floorMod(ChronoUnit.DAYS.between(LocalDate.of(2025, 12, 16), date), 28).toInt()
        return when (team.first()) {
            day[i] -> "dia"
            night[i] -> "noche"
            guard[i] -> "guardia"
            else -> "descanso"
        }
    }

    fun delta223(date: LocalDate) = ChronoUnit.DAYS.between(LocalDate.of(2026, 1, 5), date)

    fun restCD(date: LocalDate) = pattern[Math.floorMod(delta223(date), 14).toInt()] == 1

    fun work223(date: LocalDate, group: String) = if (group == "AB") restCD(date) else !restCD(date)

    fun change223(date: LocalDate) = delta223(date) % 56L == 0L

    fun holiday(date: LocalDate) = date.toString() in holidays

    fun label(type: String) =
        when (type) {
            "dia" -> "Turno día"
            "noche" -> "Turno noche"
            "guardia" -> "Guardia"
            "vacaciones" -> "Vacaciones"
            "trabajo" -> "Trabajo"
            else -> "Descanso"
        }

    fun hours(type: String) =
        when (type) {
            "dia" -> "08:00 – 20:00"
            "noche" -> "20:00 – 08:00"
            "guardia" -> "Disponible todo el día"
            "vacaciones" -> "Día no laborable"
            else -> "Día libre"
        }

    fun extraTotal(guards: Map<String, String>, fractions: Map<String, Double>) =
        guards.values.count { it == "trabaja_dia" || it == "trabaja_noche" } * 12.0 +
            fractions.values.sum()
}
