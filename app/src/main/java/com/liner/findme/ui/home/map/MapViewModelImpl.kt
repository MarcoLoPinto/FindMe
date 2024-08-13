package com.liner.findme.ui.home.map

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liner.findme.network.models.PhotoCard
import com.liner.findme.repositories.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

@HiltViewModel
class MapViewModelImpl @Inject constructor(private val locationRepository: LocationRepository) : ViewModel(), MapViewModel{
    override val location: Flow<Location?> = locationRepository.lastLocation
    override fun guessLocation(photoCard: PhotoCard, selectedLocation: GeoPoint) = locationRepository.guessLocation(photoCard, selectedLocation)
}