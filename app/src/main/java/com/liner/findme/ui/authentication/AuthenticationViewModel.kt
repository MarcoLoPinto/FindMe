package com.liner.findme.ui.authentication

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.liner.findme.network.models.User
import kotlinx.coroutines.flow.Flow

interface AuthenticationViewModel {
    val isLogged: Flow<Boolean>
    fun signIn(task: Task<GoogleSignInAccount>, username: String?, nickname: String?): Flow<Result<Boolean>>
}