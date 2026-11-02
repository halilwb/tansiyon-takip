package com.sabri.tansiyontakip.util

import java.util.Calendar
import java.util.Locale

const val DAY_MILLIS: Long = 24L * 60L * 60L * 1000L

private val MONTHS = arrayOf(
    "Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran",
    "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"
)

private val WEEKDAYS = arrayOf(
    "Pazar", "Pazartesi", "Salı", "Çarşamba", "Perşembe", "Cuma", "Cumartesi"
)

fun startOfDay(millis: Long = System.currentTimeMillis()): Long =
    Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

fun formatTime(millis: Long): String {
    val calendar = Calendar.getInstance().apply { timeInMillis = millis }
    return String.format(
        Locale.US,
        "%02d:%02d",
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE)
    )
}

fun formatDate(millis: Long): String {
    val calendar = Calendar.getInstance().apply { timeInMillis = millis }
    return "${calendar.get(Calendar.DAY_OF_MONTH)} " +
        "${MONTHS[calendar.get(Calendar.MONTH)]} " +
        "${calendar.get(Calendar.YEAR)}"
}

fun formatDateShort(millis: Long): String {
    val calendar = Calendar.getInstance().apply { timeInMillis = millis }
    return "${calendar.get(Calendar.DAY_OF_MONTH)} ${MONTHS[calendar.get(Calendar.MONTH)].take(3)}"
}

fun formatFullDate(millis: Long): String {
    val calendar = Calendar.getInstance().apply { timeInMillis = millis }
    return "${formatDate(millis)}, ${WEEKDAYS[calendar.get(Calendar.DAY_OF_WEEK) - 1]}"
}

fun dayLabel(millis: Long): String {
    val today = startOfDay()
    val day = startOfDay(millis)
    return when (day) {
        today -> "Bugün"
        today - DAY_MILLIS -> "Dün"
        else -> formatFullDate(millis)
    }
}
