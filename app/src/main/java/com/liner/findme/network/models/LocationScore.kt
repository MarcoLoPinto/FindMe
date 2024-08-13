package com.liner.findme.network.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LocationScore(val nickname: String, val username: String, val distance: Double) : Parcelable
