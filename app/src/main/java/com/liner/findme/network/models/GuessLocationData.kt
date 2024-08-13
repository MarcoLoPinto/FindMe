package com.liner.findme.network.models

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@JsonClass(generateAdapter = true)
@Parcelize
data class GuessLocationData(
    val geoLocationData: GeoLocationData,
    @Json(name = "photo_card_id") val photoCardId: String
) : Parcelable
