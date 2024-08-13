package com.liner.findme.ui.home.photo

import android.location.Location
import androidx.lifecycle.ViewModel
import com.liner.findme.repositories.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class PhotoViewModelImpl @Inject constructor(
    private val locationRepository: LocationRepository
): ViewModel(), PhotoViewModel {
    override val location: Flow<Location?> = locationRepository.lastLocation
}