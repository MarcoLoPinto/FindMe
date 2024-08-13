package com.liner.findme.repositories

import com.liner.findme.network.models.PhotoCard
import com.liner.findme.network.models.User
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

interface PhotoCardRepository {

    suspend fun getPhotoCards(filter: String = "explore"): List<PhotoCard>

    suspend fun createPhotoCard(photoCard: PhotoCard): Response<Void>?

    fun getGlobalScores(): Flow<List<User>>

}