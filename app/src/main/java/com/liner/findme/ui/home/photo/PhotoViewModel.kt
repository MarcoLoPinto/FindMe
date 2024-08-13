package com.liner.findme.ui.home.photo

import android.location.Location
import kotlinx.coroutines.flow.Flow

interface PhotoViewModel {
    val location: Flow<Location?>
}