package com.sabri.tansiyontakip.data

import java.util.Calendar
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * İlk açılışta gösterilmek üzere gerçekçi örnek veriler üretir.
 * Son 30 gün, sabah ve akşam ölçümleri; zamanla hafifçe iyileşen bir trend.
 */
object SampleData {

    fun create(days: Int = 30): List<Reading> {
        val random = Random(2026)
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        val readings = mutableListOf<Reading>()
        val span = (days - 1).coerceAtLeast(1)

        for (dayOffset in days - 1 downTo 0) {
            val progress = (days - 1 - dayOffset).toFloat() / span
            val baseSystolic = 142f - progress * 14f
            val baseDiastolic = 91f - progress * 10f

            listOf(8 to 10, 20 to 40).forEachIndexed { index, (hour, minute) ->
                calendar.timeInMillis = now
                calendar.add(Calendar.DAY_OF_YEAR, -dayOffset)
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                if (calendar.timeInMillis > now) return@forEachIndexed

                val systolic = (baseSystolic + random.nextInt(-6, 7) + if (index == 0) 3 else 0)
                    .roundToInt()
                    .coerceIn(108, 168)
                val diastolic = (baseDiastolic + random.nextInt(-4, 5) + if (index == 0) 1 else 0)
                    .roundToInt()
                    .coerceIn(66, 102)

                readings += Reading(
                    id = calendar.timeInMillis,
                    timestamp = calendar.timeInMillis,
                    systolic = maxOf(systolic, diastolic + 25),
                    diastolic = diastolic
                )
            }
        }

        return readings.sortedBy { it.timestamp }
    }
}
