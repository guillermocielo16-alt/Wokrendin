package com.wokrendin.calendarbridge

import android.Manifest
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * Reads native Android calendar provider. No network, Google API, login, or WRITE_CALENDAR.
 * Call only from a background dispatcher/thread: provider queries are blocking.
 * Permission requests must be initiated by the host Activity on user action.
 */
class AndroidCalendarReader(private val context: Context) {
    data class CalendarChoice(val id: Long, val displayName: String)
    private val resolver: ContentResolver = context.contentResolver

    fun hasPermission(): Boolean = context.checkSelfPermission(
        Manifest.permission.READ_CALENDAR
    ) == PackageManager.PERMISSION_GRANTED

    fun availableCalendars(): List<CalendarChoice> {
        checkPermission()
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
        )
        val calendars = mutableListOf<CalendarChoice>()
        resolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            "${CalendarContract.Calendars.VISIBLE} = 1",
            null,
            "${CalendarContract.Calendars.CALENDAR_DISPLAY_NAME} COLLATE NOCASE ASC",
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID)
            val nameCol = cursor.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val name = cursor.getString(nameCol).orEmpty().ifBlank { "Calendario $id" }
                calendars.add(CalendarChoice(id, name))
            }
        }
        return calendars
    }

    /**
     * Only read calendars explicitly selected by the user; an empty set reads nothing.
     * No private calendars are silently included.
     * Queries ±2 days around requested window, then caller indexes exact dates,
     * covering all-day events stored at UTC midnight across time zones.
     */
    fun readVisibleEvents(
        fromInclusive: LocalDate,
        throughInclusive: LocalDate,
        calendarIds: Set<Long>,
        zone: ZoneId = ZoneId.systemDefault(),
    ): List<DeviceCalendarEvent> {
        checkPermission()
        require(!throughInclusive.isBefore(fromInclusive))
        require(ChronoUnit.DAYS.between(fromInclusive, throughInclusive) <= 370) {
            "Read calendar events in bounded date windows"
        }
        if (calendarIds.isEmpty()) return emptyList()
        val available = availableCalendars()
        val chosen = available.filter { it.id in calendarIds }
        if (chosen.isEmpty()) return emptyList()
        val names = chosen.associate { it.id to it.displayName }
        val windowStart = fromInclusive.minusDays(2).atStartOfDay(zone).toInstant().toEpochMilli()
        val windowEnd = throughInclusive.plusDays(3).atStartOfDay(zone).toInstant().toEpochMilli()
        val uri = CalendarContract.Instances.CONTENT_URI.buildUpon().also {
            ContentUris.appendId(it, windowStart)
            ContentUris.appendId(it, windowEnd)
        }.build()
        val projection = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.CALENDAR_ID,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.DESCRIPTION,
            CalendarContract.Instances.EVENT_LOCATION,
            CalendarContract.Instances.ALL_DAY,
            CalendarContract.Instances.STATUS,
        )
        val placeholders = List(chosen.size) { "?" }.joinToString(",")
        val selection = "${CalendarContract.Instances.CALENDAR_ID} IN ($placeholders)"
        val args = chosen.map { it.id.toString() }.toTypedArray()
        val results = mutableListOf<DeviceCalendarEvent>()
        resolver.query(uri, projection, selection, args, "${CalendarContract.Instances.BEGIN} ASC")?.use { cursor ->
            fun col(name: String) = cursor.getColumnIndexOrThrow(name)
            val eventIdCol = col(CalendarContract.Instances.EVENT_ID)
            val calendarIdCol = col(CalendarContract.Instances.CALENDAR_ID)
            val beginCol = col(CalendarContract.Instances.BEGIN)
            val endCol = col(CalendarContract.Instances.END)
            val titleCol = col(CalendarContract.Instances.TITLE)
            val descriptionCol = col(CalendarContract.Instances.DESCRIPTION)
            val locationCol = col(CalendarContract.Instances.EVENT_LOCATION)
            val allDayCol = col(CalendarContract.Instances.ALL_DAY)
            val statusCol = col(CalendarContract.Instances.STATUS)
            while (cursor.moveToNext()) {
                if (!cursor.isNull(statusCol) &&
                    cursor.getInt(statusCol) == CalendarContract.Events.STATUS_CANCELED) continue
                val calendarId = cursor.getLong(calendarIdCol)
                results.add(DeviceCalendarEvent(
                    eventId = cursor.getLong(eventIdCol),
                    startMillis = cursor.getLong(beginCol),
                    endMillisExclusive = cursor.getLong(endCol),
                    title = cursor.getString(titleCol).orEmpty().ifBlank { "Sin título" },
                    description = cursor.getString(descriptionCol),
                    location = cursor.getString(locationCol),
                    calendarName = names[calendarId],
                    allDay = cursor.getInt(allDayCol) != 0,
                ))
            }
        }
        return results.distinctBy { it.occurrenceKey }
    }

    private fun checkPermission() {
        if (!hasPermission()) throw SecurityException("READ_CALENDAR not granted")
    }
}
