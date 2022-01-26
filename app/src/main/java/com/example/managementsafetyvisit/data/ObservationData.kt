package com.example.managementsafetyvisit.data

data class ObservationData(
    val perception: String?,
    val type: String?,
    val response: String?,
    val measure: String?,
    val now: Boolean,
    val corrector: String?,
    val date: String?,
    val id: Int
)
