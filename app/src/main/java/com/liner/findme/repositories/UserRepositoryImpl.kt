package com.liner.findme.repositories

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.google.firebase.auth.FirebaseAuth
import com.liner.findme.extensions.getToken
import com.liner.findme.extensions.toByteArray
import com.liner.findme.network.models.PhotoData
import com.liner.findme.network.models.User
import com.liner.findme.network.services.UserService
import com.squareup.moshi.JsonDataException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.net.ConnectException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userApi: UserService,
    private val context: Context,
    private val auth: FirebaseAuth
) : UserRepository {
    override fun updateUserProfile(uri: Uri): Flow<User?> = flow<User?> {

        var user: User? = null

        try {

            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.applicationContext.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(context.applicationContext.contentResolver, uri)
            }

            val isLandscape = bitmap.width >= bitmap.height
            val croppedBitmap = Bitmap.createBitmap(
                bitmap,
                if(isLandscape) (bitmap.width - bitmap.height)/2 else 0,
                if(isLandscape) 0 else (bitmap.height - bitmap.width)/2,
                if(isLandscape) bitmap.height else bitmap.width,
                if(isLandscape) bitmap.height else bitmap.width
            )

            val scaledBitmap = Bitmap.createScaledBitmap(croppedBitmap, 320, 320, true)

            val photoData = PhotoData(scaledBitmap.toByteArray())

            user = userApi.updateUserProfile(auth.getToken(), photoData).body()

        } catch (ex: HttpException) {
            ex.printStackTrace()
        } catch (ex: JsonDataException) {
            ex.printStackTrace()
        } catch (ex: UnknownHostException) {
            ex.printStackTrace()
        } catch (ex: ConnectException) {
            ex.printStackTrace()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        emit(user)

    }.flowOn(Dispatchers.IO)

}
