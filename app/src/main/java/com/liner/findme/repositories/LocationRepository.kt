package com.liner.findme.repositories

import android.location.Location
import com.liner.findme.network.models.PhotoCard
import kotlinx.coroutines.flow.Flow
import org.osmdroid.util.GeoPoint

interface LocationRepository {
    val lastLocation: Flow<Location?>
    val isGPSEnabled: Flow<Boolean>
    fun guessLocation(photoCard: PhotoCard, selectedLocation: GeoPoint): Flow<Result<Boolean>>
}