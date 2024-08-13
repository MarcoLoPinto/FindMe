package com.liner.findme.ui.home.user_details


import android.net.Uri
import androidx.lifecycle.ViewModel
import com.liner.findme.network.models.PhotoData
import com.liner.findme.repositories.AuthenticationRepository
import com.liner.findme.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class UserDetailsViewModelImpl @Inject constructor(
    private val userRepository: UserRepository,
    private val authenticationRepository: AuthenticationRepository
) : ViewModel(), UserDetailsViewModel {

    override fun updateUserProfile(uri: Uri) =
        userRepository.updateUserProfile(uri)

    override fun userDetails() = authenticationRepository.userDetails()
    override fun signOut() = authenticationRepository.signOut()

}