package com.liner.findme.ui.home.photo

import android.location.Location
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

data class NewPhotoData(val uri: Uri, val location: Location, var caption: MutableState<String> = mutableStateOf(""))
