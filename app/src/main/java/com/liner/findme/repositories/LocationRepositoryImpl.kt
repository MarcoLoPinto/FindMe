package com.liner.findme.repositories

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.liner.findme.extensions.getToken
import com.liner.findme.network.models.GeoLocationData
import com.liner.findme.network.models.GuessLocationData
import com.liner.findme.network.models.PhotoCard
import com.liner.findme.network.services.PhotoCardService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.asDeferred
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint
import retrofit2.HttpException
import java.net.ConnectException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepositoryImpl @Inject constructor(
    context: Context,
    private val photoCardApi: PhotoCardService,
    private val auth: FirebaseAuth
) : LocationRepository {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    override val lastLocation: Flow<Location?> = flow {
        val location = fusedLocationClient.lastLocation.asDeferred().await()
        emit(location)
    }.flowOn(Dispatchers.IO)

    override val isGPSEnabled: Flow<Boolean> = flow {
        val locationManager: LocationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGPSEnabled =
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) or locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            )
        emit(isGPSEnabled)
    }.flowOn(Dispatchers.IO)

    override fun guessLocation(photoCard: PhotoCard, selectedLocation: GeoPoint) = flow {
        val guessLocationData = GuessLocationData(
            GeoLocationData(
                selectedLocation.latitude,
                selectedLocation.longitude
            ),
            photoCard.id
        )

        val result: Result<Boolean> = try {
            val response = photoCardApi.guessLocation(auth.getToken(), guessLocationData)
            Result.success(response.isSuccessful)
        } catch (ex: HttpException) {
            Result.failure(ex)
        } catch (ex: UnknownHostException) {
            ex.printStackTrace()
            Result.failure(ex)
        } catch (ex: ConnectException) {
            ex.printStackTrace()
            Result.failure(ex)
        }

        emit(result)

    }.flowOn(Dispatchers.IO)

}