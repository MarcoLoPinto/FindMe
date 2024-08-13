package com.liner.findme.ui.authentication

import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.liner.findme.network.models.User
import com.liner.findme.repositories.AuthenticationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@HiltViewModel
class AuthenticationViewModelImpl @Inject constructor(
    private val authenticationRepository: AuthenticationRepository
) : ViewModel(), AuthenticationViewModel {

    override val isLogged: Flow<Boolean> = authenticationRepository.isLogged

    override fun signIn(task: Task<GoogleSignInAccount>, username: String?, nickname: String?) =
        authenticationRepository.signIn(task, username, nickname)

}