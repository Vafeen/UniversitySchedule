package ru.vafeen.universityschedule.utils

import ru.vafeen.universityschedule.noui.lesson_additions.Frequency
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.temporal.ChronoField

operator fun LocalTime.compareTo(localDateTime: LocalDateTime): Int {
    return this.compareTo(LocalTime.of(localDateTime.hour, localDateTime.minute))
}

fun LocalDate.getDateString(): String = "${dayOfMonth}." + if (month.value < 10) "0${month.value}"
else month.value

fun LocalDate.getDateString(ruDaysOfWeek: List<String>): String =
    "${dayOfWeek.ruDayOfWeek(ruDaysOfWeek = ruDaysOfWeek)}, ${dayOfMonth}." + if (month.value < 10) "0${month.value}"
    else month.value


fun LocalTime.getTimeStringAsHMS(): String =
    "${
        if (hour < 10) "0$hour" else "$hour"
    }:${
        if (minute < 10) "0$minute" else "$minute"
    }:${
        if (second < 10) "0$second" else "$second"
    }"

fun LocalDate.getLastSeptemberOfThisAcademicYear(): LocalDate {
    return if (this >= LocalDate.of(year, Month.SEPTEMBER, 1))
        LocalDate.of(year, Month.SEPTEMBER, 1)
    else LocalDate.of(year - 1, Month.SEPTEMBER, 1)
}

fun LocalDate.getFrequencyByLocalDate(): Frequency {
    val lastSeptember = getLastSeptemberOfThisAcademicYear()
    val resultFrequency =
        if (get(ChronoField.ALIGNED_WEEK_OF_YEAR) % 2 == 0) Frequency.Denominator
        else Frequency.Numerator
    return if (lastSeptember.dayOfWeek == DayOfWeek.SUNDAY)
        resultFrequency.getOpposite()
    else resultFrequency
}

