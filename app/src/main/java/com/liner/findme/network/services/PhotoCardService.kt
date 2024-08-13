package com.liner.findme.network.services

import com.liner.findme.di.NetworkModule
import com.liner.findme.network.models.GuessLocationData
import com.liner.findme.network.models.LocationScore
import com.liner.findme.network.models.PhotoCard
import com.liner.findme.network.models.User
import retrofit2.Response
import retrofit2.http.*

interface PhotoCardService {
    @GET("main/get_photo_cards/{filter}")
    suspend fun getPhotoCards(@Header(NetworkModule.HEADER_AUTH) authToken: String?, @Path("filter") filter: String = "explore"): Response<List<PhotoCard>>

    @POST("main/post_photo_card")
    suspend fun createPhotoCard(@Header(NetworkModule.HEADER_AUTH) authToken: String?, @Body photoCard: PhotoCard): Response<Void>

    @POST("main/guess_location")
    suspend fun guessLocation(@Header(NetworkModule.HEADER_AUTH) authToken: String?, @Body guessLocationData: GuessLocationData): Response<Void>

    @GET("main/global_scores/")
    suspend fun getGlobalScores(@Header(NetworkModule.HEADER_AUTH) authToken: String?): Response<List<User>>
}