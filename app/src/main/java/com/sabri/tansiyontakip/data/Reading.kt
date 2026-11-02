package com.sabri.tansiyontakip.data

import com.sabri.tansiyontakip.util.BpCategory
import com.sabri.tansiyontakip.util.categoryFor

/**
 * Tek bir tansiyon ölçümü.
 *
 * @param id Benzersiz kimlik (şu an için zaman damgası kullanılıyor).
 * @param timestamp Ölçümün yapıldığı an (epoch millis).
 * @param systolic Büyük tansiyon (sistolik).
 * @param diastolic Küçük tansiyon (diyastolik).
 */
data class Reading(
    val id: Long,
    val timestamp: Long,
    val systolic: Int,
    val diastolic: Int
) {
    val category: BpCategory
        get() = categoryFor(systolic, diastolic)
}
