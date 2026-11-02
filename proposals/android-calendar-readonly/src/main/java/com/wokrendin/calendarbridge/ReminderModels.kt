package com.wokrendin.calendarbridge

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

/** Events from Android Calendar, read-only. These never mutate the shift calendar. */
data class DeviceCalendarEvent(
    val eventId: Long,
    val startMillis: Long,
    val endMillisExclusive: Long,
    val title: String,
    val description: String? = null,
    val location: String? = null,
    val calendarName: String? = null,
    val allDay: Boolean = false,
) {
    val occurrenceKey: String get() = "$eventId:$startMillis"
}

data class ReminderDayState(
    val date: LocalDate,
    val events: List<DeviceCalendarEvent>,
) {
    val hasReminder: Boolean get() = events.isNotEmpty()
    val reminderCount: Int get() = events.size
}

/**
 * Only decorates existing calendar dates with events; it NEVER calculates or changes shifts.
 * For all-day CalendarContract events, BEGIN/END represent UTC-midnight dates.
 * For timed events, the date is the user's local wall-clock date.
 * END is exclusive, so an event ending at midnight does not decorate the next day.
 */
object ReminderDayIndex {
    fun build(
        events: List<DeviceCalendarEvent>,
        fromInclusive: LocalDate,
        throughInclusive: LocalDate,
        zone: ZoneId = ZoneId.systemDefault(),
    ): Map<LocalDate, List<DeviceCalendarEvent>> {
        require(!throughInclusive.isBefore(fromInclusive))
        val buckets = linkedMapOf<LocalDate, MutableList<DeviceCalendarEvent>>()
        for (event in events) {
            val eventZone = if (event.allDay) ZoneOffset.UTC else zone
            val startDay = Instant.ofEpochMilli(event.startMillis).atZone(eventZone).toLocalDate()
            val lastMillis = maxOf(event.startMillis, event.endMillisExclusive - 1)
            val lastDay = Instant.ofEpochMilli(lastMillis).atZone(eventZone).toLocalDate()
            var day = maxOf(startDay, fromInclusive)
            val lastVisible = minOf(lastDay, throughInclusive)
            while (!day.isAfter(lastVisible)) {
                buckets.getOrPut(day) { mutableListOf() }.add(event)
                day = day.plusDays(1)
            }
        }
        return buckets.mapValues { (_, dayEvents) ->
            dayEvents.distinctBy { it.occurrenceKey }.sortedWith(
                compareBy<DeviceCalendarEvent> { it.startMillis }.thenBy { it.title }
            )
        }
    }

    fun forDate(index: Map<LocalDate, List<DeviceCalendarEvent>>, date: LocalDate): ReminderDayState =
        ReminderDayState(date, index[date].orEmpty())
}

/**
 * ONE selectedDate drives the monthly highlight, horizontal strip, day header,
 * original work-status area, and the note with full event details.
 * The work-status calculator remains untouched and is called by the existing app.
 */
class ReminderNavigationModel(initialDate: LocalDate) {
    var selectedDate: LocalDate = initialDate
        private set

    private var eventIndex: Map<LocalDate, List<DeviceCalendarEvent>> = emptyMap()

    fun onMonthDayPressed(date: LocalDate): ReminderNavigationSnapshot = select(date)
    fun onHorizontalDayPressed(date: LocalDate): ReminderNavigationSnapshot = select(date)
    fun onTodayPressed(today: LocalDate): ReminderNavigationSnapshot = select(today)

    fun updateVisibleEvents(index: Map<LocalDate, List<DeviceCalendarEvent>>): ReminderNavigationSnapshot {
        eventIndex = index
        return snapshot()
    }

    fun clearWhenPermissionRevoked(): ReminderNavigationSnapshot {
        eventIndex = emptyMap()
        return snapshot()
    }

    fun snapshot(): ReminderNavigationSnapshot = ReminderNavigationSnapshot(
        headerDate = selectedDate,
        monthlySelectedDate = selectedDate,
        horizontalSelectedDate = selectedDate,
        selectedDay = ReminderDayIndex.forDate(eventIndex, selectedDate),
        markedDates = eventIndex.filterValues { it.isNotEmpty() }.keys,
    )

    private fun select(date: LocalDate): ReminderNavigationSnapshot {
        selectedDate = date
        return snapshot()
    }
}

data class ReminderNavigationSnapshot(
    val headerDate: LocalDate,
    val monthlySelectedDate: LocalDate,
    val horizontalSelectedDate: LocalDate,
    val selectedDay: ReminderDayState,
    val markedDates: Set<LocalDate>,
)
