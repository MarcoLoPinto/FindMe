package com.liner.findme.ui.home.map

import android.location.Location
import com.liner.findme.network.models.PhotoCard
import kotlinx.coroutines.flow.Flow
import org.osmdroid.util.GeoPoint

interface MapViewModel {
    val location: Flow<Location?>

    fun guessLocation(photoCard: PhotoCard, selectedLocation: GeoPoint): Flow<Result<Boolean>>

}