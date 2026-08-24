package com.example.localization

object AppLanguage {

    enum class Language(val code: String, val displayName: String) {
        ENGLISH("en", "English"),
        HINDI("hi", "हिंदी")
    }

    private val translations = mapOf(
        "app_title" to mapOf(
            "en" to "PranaHealth AI",
            "hi" to "प्राणहेल्थ AI"
        ),
        "app_subtitle" to mapOf(
            "en" to "Sensor-Only Health & Disaster Companion",
            "hi" to "सेंसर-आधारित स्वास्थ्य एवं आपदा साथी"
        ),
        "start_ppg" to mapOf(
            "en" to "Measure Vitals (Camera PPG)",
            "hi" to "स्वास्थ्य मापें (कैमरा PPG)"
        ),
        "heart_rate" to mapOf(
            "en" to "Heart Rate",
            "hi" to "हृदय गति"
        ),
        "hrv" to mapOf(
            "en" to "Heart Rate Variability",
            "hi" to "एचआरवी (HRV)"
        ),
        "spo2" to mapOf(
            "en" to "Blood Oxygen (SpO2)",
            "hi" to "ऑक्सीजन स्तर (SpO2)"
        ),
        "respiration" to mapOf(
            "en" to "Respiration Rate",
            "hi" to "श्वसन दर"
        ),
        "heat_stress" to mapOf(
            "en" to "Heat Stress Index",
            "hi" to "ताप तनाव सूचकांक"
        ),
        "vitality_score" to mapOf(
            "en" to "Overall Vitality Score",
            "hi" to "समग्र जीवन शक्ति स्कोर"
        ),
        "disaster_alerts" to mapOf(
            "en" to "Disaster Resilience & Alerts",
            "hi" to "आपदा चेतावनी एवं सुरक्षा"
        ),
        "fall_detection" to mapOf(
            "en" to "Motion & Fall Sentinel",
            "hi" to "गति एवं गिरावट पहचान"
        ),
        "audio_check" to mapOf(
            "en" to "Acoustic Respiratory Check",
            "hi" to "ध्वनि श्वसन जांच"
        ),
        "emergency_sos" to mapOf(
            "en" to "Emergency SOS (Offline SMS)",
            "hi" to "आपातकालीन SOS (ऑफ़लाइन SMS)"
        ),
        "disclaimer" to mapOf(
            "en" to "Medical Wellness Disclaimer: This app provides early-warning guidance and wellness monitoring using on-device phone sensors. It is not a certified medical diagnostic device. If you feel unwell, consult a healthcare professional immediately.",
            "hi" to "चिकित्सा अस्वीकरण: यह ऐप केवल प्राथमिक चेतावनी और स्वास्थ्य निगरानी के लिए है। यह प्रमाणित चिकित्सा उपकरण नहीं है। अस्वस्थ महसूस होने पर तुरंत डॉक्टर से परामर्श लें।"
        )
    )

    fun getString(key: String, lang: String = "en"): String {
        return translations[key]?.get(lang) ?: translations[key]?.get("en") ?: key
    }
}
