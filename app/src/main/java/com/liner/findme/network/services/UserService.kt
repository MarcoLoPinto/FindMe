package com.liner.findme.network.services

import com.liner.findme.di.NetworkModule
import com.liner.findme.network.models.PhotoData
import com.liner.findme.network.models.User
import com.squareup.moshi.Json
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface UserService {
    @POST("main/update_user_profile_image")
    suspend fun updateUserProfile(@Header(NetworkModule.HEADER_AUTH) authToken: String?, @Body userProfileImage: PhotoData): Response<User>
}