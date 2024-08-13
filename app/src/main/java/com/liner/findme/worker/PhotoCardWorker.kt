package com.liner.findme.worker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toUri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.liner.findme.extensions.toByteArray
import com.liner.findme.network.models.GeoLocationData
import com.liner.findme.network.models.PhotoCard
import com.liner.findme.network.models.PhotoData
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

// @AssistedInject = HILT wrapper annotation

@HiltWorker
class PhotoCardWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val photoCardWorkerDependency: PhotoCardWorkerDependency
) : CoroutineWorker(context, params) {

    companion object {
        const val TAG = "photo_card_worker"
        const val PHOTO_URI = "photo_uri"
        const val PHOTO_CAPTION = "photo_caption"
        const val PHOTO_LATITUDE = "photo_latitude"
        const val PHOTO_LONGITUDE = "photo_longitude"
    }

    override suspend fun doWork(): Result {
        try {
            val photoUri = inputData.getString(PHOTO_URI)?.toUri() ?: throw Exception()
            val photoCaption = inputData.getString(PHOTO_CAPTION) ?: throw Exception()
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(applicationContext.contentResolver, photoUri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(applicationContext.contentResolver, photoUri)
            }
            val latitude = inputData.getDouble(PHOTO_LATITUDE, 0.toDouble())
            val longitude = inputData.getDouble(PHOTO_LONGITUDE, 0.toDouble())

            val isLandscape = bitmap.width >= bitmap.height
            val croppedBitmap = Bitmap.createBitmap(
                bitmap,
                if(isLandscape) (bitmap.width - bitmap.height)/2 else 0,
                if(isLandscape) 0 else (bitmap.height - bitmap.width)/2,
                if(isLandscape) bitmap.height else bitmap.width,
                if(isLandscape) bitmap.height else bitmap.width
            )
            val croppedResizedBitmap = Bitmap.createScaledBitmap(croppedBitmap, 1024, 1024, true)
//            Log.i("COMPRESS-crop", croppedBitmap.byteCount.toString())
//            Log.i("COMPRESS-resize", croppedResizedBitmap.byteCount.toString())
//            Log.i("COMPRESS-croptoarray", croppedBitmap.toByteArray().size.toString() )
//            Log.i("COMPRESS-resizetoarray", croppedResizedBitmap.toByteArray().size.toString() )

            val response = photoCardWorkerDependency.photoCardRepository.createPhotoCard(
                PhotoCard(
                    PhotoData(croppedResizedBitmap.toByteArray()),
                    GeoLocationData(latitude, longitude),
                    photoCaption
                )
            )

            return if (response?.isSuccessful == true) Result.success() else Result.failure()

        } catch (ex: Exception) {
            ex.printStackTrace()
            return Result.failure()
        }
    }

}