package com.liner.findme.repositories

import android.net.Uri
import com.liner.findme.network.models.PhotoData
import com.liner.findme.network.models.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun updateUserProfile(uri: Uri): Flow<User?>
}