package com.example.managementsafetyvisit.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ObservationData(
    var perception: String?,
    var type: String?,
    var response: String?,
    var measure: String?,
    var now: Boolean,
    var corrector: String?,
    var date: String?,
    var id: String
) : Parcelable
