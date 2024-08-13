package com.liner.findme.network.services

import com.liner.findme.di.NetworkModule
import com.liner.findme.network.models.PhotoCard
import com.liner.findme.network.models.User
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthenticationService {
    @POST("auth/authentication")
    suspend fun signIn(@Header(NetworkModule.HEADER_AUTH) authToken: String?, @Body usernameAndNickname: Map<String, String>): Response<Unit>

    @GET("auth/me")
    suspend fun userDetails(@Header(NetworkModule.HEADER_AUTH) authToken: String?): Response<User>
}