package com.liner.findme.network.models

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import java.util.Date

@JsonClass(generateAdapter = true)
@Parcelize
data class PhotoCard(
    val photo: PhotoData,
    val geoLocationData: GeoLocationData,
    val caption: String = "",
    val username: String = "",
    val nickname: String = "",
    val scores: List<LocationScore> = emptyList(),
    val placesAround: List<String> = emptyList(),
    @Json(name = "user_profile_image") val userProfileImage: PhotoData = PhotoData(byteArrayOf()),
    @Json(name = "_id") val id: String = "",
    @Json(name = "voted_num") val votedNum: Int = 0,
    @Json(name = "voted_distance_mean") val votedDistanceMean: Float = 0.0f,
    val createdAt: String = ""
) : Parcelable
