package com.liner.findme.repositories

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.liner.findme.network.models.User
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

interface AuthenticationRepository {
    val isLogged: Flow<Boolean>
    fun signIn(task: Task<GoogleSignInAccount>, username: String?, nickname: String?): Flow<Result<Boolean>>
    fun signOut(): Flow<Result<Boolean>>
    fun userDetails(): Flow<User>
}