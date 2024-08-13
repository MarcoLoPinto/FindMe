package com.liner.findme.network.models

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@JsonClass(generateAdapter = true)
@Parcelize
data class GeoLocationData(val latitude: Double, val longitude: Double) : Parcelable
