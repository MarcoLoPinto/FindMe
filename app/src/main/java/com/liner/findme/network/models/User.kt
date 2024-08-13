package com.liner.findme.network.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User(
    val username: String,
    val nickname: String,
    @Json(name = "user_profile_image") val userProfileImage: PhotoData = PhotoData(byteArrayOf()),
    @Json(name = "_id") val id: String = "",
    @Json(name = "voted_num") val votedNum: Int = 0,
    @Json(name = "voted_distance_mean") val votedDistanceMean: Float = 0.0f
)
