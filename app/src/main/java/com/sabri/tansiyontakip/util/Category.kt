package com.sabri.tansiyontakip.util

/**
 * Tansiyon değerlerinin genel sınıflandırması.
 * Etiketler bilgilendirme amaçlıdır, tıbbi tanı yerine geçmez.
 */
enum class BpCategory(val label: String, val shortLabel: String) {
    LOW("Düşük Tansiyon", "Düşük"),
    NORMAL("Normal", "Normal"),
    ELEVATED("Yükselmiş", "Yüksek"),
    STAGE1("Hipertansiyon Evre 1", "Evre 1"),
    STAGE2("Hipertansiyon Evre 2", "Evre 2"),
    CRISIS("Hipertansif Kriz", "Kriz")
}

fun categoryFor(systolic: Int, diastolic: Int): BpCategory = when {
    systolic >= 180 || diastolic >= 120 -> BpCategory.CRISIS
    systolic >= 140 || diastolic >= 90 -> BpCategory.STAGE2
    systolic >= 130 || diastolic >= 80 -> BpCategory.STAGE1
    systolic >= 120 -> BpCategory.ELEVATED
    systolic < 90 || diastolic < 60 -> BpCategory.LOW
    else -> BpCategory.NORMAL
}

fun BpCategory.isSevere(): Boolean =
    this == BpCategory.STAGE2 || this == BpCategory.CRISIS
