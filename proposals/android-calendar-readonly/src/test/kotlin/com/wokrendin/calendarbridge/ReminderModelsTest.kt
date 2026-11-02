package com.wokrendin.calendarbridge

import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

private fun checkTest(assertion: Boolean, description: String) {
    check(assertion) { "FAIL: $description" }
    println("PASS: $description")
}

fun main() {
    val tz = ZoneId.of("America/Mexico_City")
    val day = LocalDate.of(2026, 11, 12)
    fun millis(date: LocalDate, hour: Int = 0, zone: ZoneId = tz): Long =
        date.atTime(hour, 0).atZone(zone).toInstant().toEpochMilli()
    val appointment = DeviceCalendarEvent(
        eventId = 7, startMillis = millis(day, 11), endMillisExclusive = millis(day, 12),
        title = "Cita", description = "Documentos", location = "Oficina", calendarName = "Personal")
    val index = ReminderDayIndex.build(listOf(appointment), day.minusDays(1), day.plusDays(2), tz)
    checkTest(index.keys.toList() == listOf(day), "Timed event marks its precise day")
    checkTest(index[day]!!.first().description == "Documentos", "Full event details retained")
    val model = ReminderNavigationModel(day.minusDays(1))
    val state = model.updateVisibleEvents(index)
    checkTest(day in state.markedDates, "Bulb appears on date with event")
    checkTest(state.selectedDay.events.isEmpty(), "No event note for other day")
    val selected = model.onHorizontalDayPressed(day)
    checkTest(selected.headerDate == day && selected.monthlySelectedDate == day &&
        selected.horizontalSelectedDate == day, "Horizontal selection updates all three date surfaces")
    checkTest(selected.selectedDay.reminderCount == 1, "Selected-day detail updated from shared state")
    val fromMonth = model.onMonthDayPressed(day.plusDays(1))
    checkTest(fromMonth.selectedDay.events.isEmpty() && fromMonth.headerDate == day.plusDays(1),
        "Selecting month day clears unrelated event and updates top header")
    val restored = model.onMonthDayPressed(day)
    checkTest(restored.selectedDay.hasReminder, "Monthly selection re-opens the event")
    val allDay = DeviceCalendarEvent(8, millis(day, zone = ZoneOffset.UTC),
        millis(day.plusDays(2), zone = ZoneOffset.UTC), "Viaje", allDay = true)
    val allDayIndex = ReminderDayIndex.build(listOf(allDay), day.minusDays(1), day.plusDays(3), tz)
    checkTest(allDayIndex.keys == setOf(day, day.plusDays(1)),
        "All-day multi-day event uses UTC dates, respects exclusive end")
    val midnight = DeviceCalendarEvent(9, millis(day, 22), millis(day.plusDays(1)), "Termina a medianoche")
    val midnightIndex = ReminderDayIndex.build(listOf(midnight), day, day.plusDays(1), tz)
    checkTest(midnightIndex.keys == setOf(day), "End at midnight does not mark next day")
    val overnight = DeviceCalendarEvent(10, millis(day, 22), millis(day.plusDays(1), 2), "Cruza medianoche")
    val overnightIndex = ReminderDayIndex.build(listOf(overnight), day, day.plusDays(1), tz)
    checkTest(overnightIndex.keys == setOf(day, day.plusDays(1)), "Overnight event marks both days")
    val repeated = DeviceCalendarEvent(7, millis(day.plusDays(7), 11), millis(day.plusDays(7), 12), "Cita")
    val repeatIndex = ReminderDayIndex.build(listOf(appointment, repeated), day, day.plusDays(8), tz)
    checkTest(repeatIndex.keys == setOf(day, day.plusDays(7)), "Recurring instances remain on separate dates")
    val duplicates = ReminderDayIndex.build(listOf(appointment, appointment), day, day, tz)
    checkTest(duplicates[day]?.size == 1, "Duplicate instances are not displayed twice")
    val revoked = model.clearWhenPermissionRevoked()
    checkTest(revoked.markedDates.isEmpty(), "Revoked calendar permission hides imported information")
    checkTest(revoked.headerDate == day, "Permission change preserves currently selected day")
    println("All 15 domain/navigation checks passed")
}
