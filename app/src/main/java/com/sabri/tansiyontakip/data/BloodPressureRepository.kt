package com.sabri.tansiyontakip.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

/**
 * Ölçümleri cihazda (SharedPreferences + JSON) saklar.
 * Harici bir izin veya internet bağlantısı gerektirmez.
 */
class BloodPressureRepository(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _readings = MutableStateFlow(loadReadings())
    val readings: StateFlow<List<Reading>> = _readings.asStateFlow()

    fun addReading(systolic: Int, diastolic: Int, timestamp: Long) {
        val reading = Reading(
            id = timestamp,
            timestamp = timestamp,
            systolic = systolic,
            diastolic = diastolic
        )
        val updated = (_readings.value + reading).sortedBy { it.timestamp }
        persist(updated)
        _readings.value = updated
    }

    fun deleteReading(id: Long) {
        val updated = _readings.value.filterNot { it.id == id }
        persist(updated)
        _readings.value = updated
    }

    private fun loadReadings(): List<Reading> {
        val raw = prefs.getString(KEY_READINGS, null)
        if (raw != null) {
            val parsed = runCatching { parse(raw) }.getOrNull()
            if (parsed != null && parsed.isNotEmpty()) {
                return parsed.sortedBy { it.timestamp }
            }
        }

        val seeded = SampleData.create()
        persist(seeded)
        return seeded
    }

    private fun persist(readings: List<Reading>) {
        prefs.edit().putString(KEY_READINGS, toJson(readings)).apply()
    }

    private fun toJson(readings: List<Reading>): String {
        val array = JSONArray()
        readings.forEach { reading ->
            array.put(
                JSONObject()
                    .put("id", reading.id)
                    .put("timestamp", reading.timestamp)
                    .put("systolic", reading.systolic)
                    .put("diastolic", reading.diastolic)
            )
        }
        return array.toString()
    }

    private fun parse(raw: String): List<Reading> {
        val array = JSONArray(raw)
        val result = mutableListOf<Reading>()
        for (index in 0 until array.length()) {
            val item = array.getJSONObject(index)
            result += Reading(
                id = item.getLong("id"),
                timestamp = item.getLong("timestamp"),
                systolic = item.getInt("systolic"),
                diastolic = item.getInt("diastolic")
            )
        }
        return result
    }

    private companion object {
        const val PREFS_NAME = "tansiyon_takip"
        const val KEY_READINGS = "readings"
    }
}
