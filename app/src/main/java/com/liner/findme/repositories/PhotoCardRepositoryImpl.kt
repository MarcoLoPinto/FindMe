package com.liner.findme.repositories

import com.google.firebase.auth.FirebaseAuth
import com.liner.findme.extensions.getToken
import com.liner.findme.network.models.PhotoCard
import com.liner.findme.network.models.User
import com.liner.findme.network.services.PhotoCardService
import com.squareup.moshi.JsonDataException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import java.net.ConnectException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoCardRepositoryImpl @Inject constructor(
    private val photoCardApi: PhotoCardService,
    private val auth: FirebaseAuth
) : PhotoCardRepository {
    override suspend fun getPhotoCards(filter: String): List<PhotoCard> = withContext(Dispatchers.IO) {
        try {
            val photoCards = photoCardApi.getPhotoCards(auth.getToken(), filter).body() ?: emptyList()
            return@withContext photoCards
        } catch (ex: HttpException){
            ex.printStackTrace()
            return@withContext emptyList()
        } catch (ex: JsonDataException){
            ex.printStackTrace()
            return@withContext emptyList()
        } catch (ex: UnknownHostException) {
            ex.printStackTrace()
            return@withContext emptyList()
        } catch (ex: ConnectException) {
            ex.printStackTrace()
            return@withContext emptyList()
        }

    }

    override suspend fun createPhotoCard(photoCard: PhotoCard): Response<Void>? = withContext(Dispatchers.IO) {
        var response: Response<Void>? = null

        try {

            response = photoCardApi.createPhotoCard(auth.getToken(), photoCard)
            if(response.isSuccessful.not()){
                throw HttpException(response)
            }

        } catch (ex: HttpException){
            ex.printStackTrace()
        } catch (ex: JsonDataException){
            ex.printStackTrace()
        } catch (ex: UnknownHostException) {
            ex.printStackTrace()
        } catch (ex: ConnectException) {
            ex.printStackTrace()
        }

        return@withContext response
    }

    override fun getGlobalScores() = flow<List<User>> {

        try {
            val userDetails = photoCardApi.getGlobalScores(auth.getToken()).body() ?: emptyList()
            emit(userDetails)
        } catch (ex: HttpException){
            ex.printStackTrace()
            emit(emptyList())
        } catch (ex: JsonDataException){
            ex.printStackTrace()
            emit(emptyList())
        } catch (ex: UnknownHostException) {
            ex.printStackTrace()
        } catch (ex: ConnectException) {
            ex.printStackTrace()
        }

    }.flowOn(Dispatchers.IO)

}