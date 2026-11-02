import app.miturno.CalendarRules as R
import java.io.File
import java.time.LocalDate

fun main(args: Array<String>) {
    var checks = 0
    File(args[0]).forEachLine { line ->
        val v = line.split('|')
        val d = LocalDate.parse(v[0])
        listOf("A", "B", "C", "D").forEachIndexed { i, t ->
            check(R.status(d, t) == v[i + 1]) { "$d $t: ${R.status(d,t)} != ${v[i+1]}" }
        }
        check(R.restCD(d).toString() == v[5])
        check(R.work223(d, "AB").toString() == v[5])
        check(R.work223(d, "CD") != v[5].toBoolean())
        check(R.change223(d).toString() == v[6])
        check(R.holiday(d).toString() == v[7])
        checks += 9
    }
    check(
        R.extraTotal(
            mapOf("a" to "trabaja_dia", "b" to "trabaja_noche", "c" to "no_trabaja"),
            mapOf("d" to 2.5, "e" to 1.25),
        ) == 27.75
    )
    check(R.extraTotal(emptyMap(), emptyMap()) == 0.0)
    println("PASS: $checks differential checks against original JS + 2 overtime checks")
}
