package com.liner.findme.ui.home.user_details

import android.net.Uri
import com.liner.findme.network.models.PhotoData
import com.liner.findme.network.models.User
import kotlinx.coroutines.flow.Flow

interface UserDetailsViewModel {
    fun updateUserProfile(uri: Uri): Flow<User?>
    fun userDetails(): Flow<User>
    fun signOut(): Flow<Result<Boolean>>
}