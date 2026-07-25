package com.rodzina.wyjazdy.data.model

/** Wartości `value` odpowiadają 1:1 stringom z sekcji 3 specyfikacji - to jest to, co ląduje w Firestore. */
enum class TransportType(val value: String, val label: String) {
    CAR("car", "Samochód"),
    TRAIN("train", "Pociąg"),
    BUS("bus", "Autobus"),
    PLANE("plane", "Samolot"),
    OTHER("other", "Inny");

    companion object {
        fun fromValue(value: String?): TransportType =
            entries.firstOrNull { it.value == value } ?: OTHER
    }
}

enum class EventType(val value: String, val label: String) {
    SZKOLENIE("szkolenie", "Szkolenie"),
    WYDARZENIE("wydarzenie", "Wydarzenie"),
    SPOTKANIE("spotkanie", "Spotkanie"),
    INNE("inne", "Inne");

    companion object {
        fun fromValue(value: String?): EventType =
            entries.firstOrNull { it.value == value } ?: INNE
    }
}

enum class TripStatus(val value: String, val label: String) {
    PLANOWANY("planowany", "Planowany"),
    W_DRODZE("w_drodze", "W drodze"),
    DOJECHALA("dojechala", "Dojechał(a)"),
    WRACA("wraca", "Wraca"),
    ZAKONCZONY("zakonczony", "Zakończony");

    companion object {
        fun fromValue(value: String?): TripStatus =
            entries.firstOrNull { it.value == value } ?: PLANOWANY
    }
}
