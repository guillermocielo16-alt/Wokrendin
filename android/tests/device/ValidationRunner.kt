package app.miturno.tests

import android.app.Instrumentation
import android.os.Bundle
import app.miturno.*
import java.time.LocalDate
import org.json.JSONObject

class ValidationRunner : Instrumentation() {
    override fun onCreate(arguments: Bundle?) {
        super.onCreate(arguments)
        start()
    }

    override fun onStart() {
        val s = Store(targetContext)
        val prior = s.prefs.all
        var count = 0
        fun expect(v: Boolean) {
            check(v) { "Failed device check ${count+1}" }
            count++
        }
        try {
            s.prefs.edit().clear().commit()
            expect(s.total() == 0.0)
            s.update("guards", "2026-10-01_A", "trabaja_dia")
            expect(s.total() == 12.0)
            s.update("guards", "2026-10-02_B", "trabaja_noche")
            expect(s.total() == 24.0)
            s.update("guards", "2026-10-03_C", "no_trabaja")
            expect(s.total() == 24.0)
            s.put(
                "fractions",
                JSONObject()
                    .put(
                        "2026-10-04",
                        JSONObject()
                            .put("hours", 2.5)
                            .put("note", "<script>alert('x')</script> & prueba"),
                    )
                    .toString(),
            )
            expect(s.total() == 26.5)
            val f = s.data("fractions")
            f.getJSONObject("2026-10-04").put("hours", 3.25)
            s.put("fractions", f.toString())
            expect(s.total() == 27.25)
            expect(Store(targetContext).total() == 27.25)
            s.put("member1A", "Ana García")
            s.put("member1B", "Luis Pérez")
            s.update("vacations", "2026-10-05_A", "si")
            expect(s.vacation(LocalDate.of(2026, 10, 5), "A"))
            expect(!s.vacation(LocalDate.of(2026, 10, 5), "B"))
            val report = s.report()
            expect(report.contains("(Ana)"))
            expect(report.contains("(Luis)"))
            expect(report.contains("08:00 - 20:00"))
            expect(report.contains("20:00 - 08:00"))
            expect(report.contains("guardia cancelada"))
            expect(report.contains("3.25 hrs extra"))
            expect(report.contains("<script>alert('x')</script> & prueba"))
            expect(report.contains("Día de vacaciones"))
            val range = s.report("2026-10-02", "2026-10-04")
            expect(!range.contains("(Ana)"))
            expect(range.contains("(Luis)"))
            expect(range.contains("3.25 hrs extra"))
            expect(!range.contains("Día de vacaciones"))
            val empty = s.report("2027-01-01", "2027-12-31")
            expect(empty.contains("Sin registros"))
            expect(!empty.contains("3.25 hrs extra"))
            s.update("guards", "2026-10-01_A", "")
            expect(s.total() == 15.25)
            s.update("vacations", "2026-10-05_A", "")
            expect(!s.vacation(LocalDate.of(2026, 10, 5), "A"))
            val b = Bundle()
            b.putString(
                "stream",
                "PASS: $count device assertions (storage, overtime, reports, range filters, plain-text notes, vacation isolation)\n",
            )
            finish(RESULT_OK, b)
        } catch (e: Throwable) {
            val b = Bundle()
            b.putString("stream", "FAIL: ${e.stackTraceToString()}")
            finish(0, b)
        } finally {
            val edit = s.prefs.edit().clear()
            prior.forEach { (k, v) -> edit.putString(k, v.toString()) }
            edit.commit()
        }
    }

    companion object {
        const val RESULT_OK = -1
    }
}
