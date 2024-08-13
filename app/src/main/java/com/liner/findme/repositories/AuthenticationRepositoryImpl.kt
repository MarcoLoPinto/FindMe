package com.liner.findme.repositories

import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.liner.findme.extensions.getToken
import com.liner.findme.network.models.User
import com.liner.findme.network.services.AuthenticationService
import com.liner.findme.ui.MainActivity
import com.liner.findme.ui.authentication.AuthenticationViewModel
import com.squareup.moshi.JsonDataException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import java.net.ConnectException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthenticationRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val authenticationService: AuthenticationService,
    private val signInClient: GoogleSignInClient
) : AuthenticationRepository {

    override val isLogged: Flow<Boolean> = flow {
        val result = auth.currentUser?.getIdToken(false)?.await()
        if(result?.token.isNullOrEmpty()) {
            emit(false)
        } else {
            val isFirebaseAuthenticated = auth.currentUser != null
            val token = auth.getToken()
            val isServerAuthenticated = authenticationService.signIn(token, emptyMap()).isSuccessful
            emit(isFirebaseAuthenticated && isServerAuthenticated)
        }


    }.flowOn(Dispatchers.IO).catch {
        it.printStackTrace()
        emit(false)
    }

    override fun signIn(task: Task<GoogleSignInAccount>, username: String?, nickname: String?) =
        channelFlow<Result<Boolean>> {
            try {
                val account = task.result
                val credential: AuthCredential =
                    GoogleAuthProvider.getCredential(account.idToken, null)

                val signInResult = auth.signInWithCredential(credential).await()

                val token = signInResult.user?.getIdToken(false)?.result?.token

                if (token.isNullOrEmpty()) {
                    trySend(Result.failure(Exception("An error occurred")))
                } else {
                    val authenticationResult = authenticate(username, nickname)
                    trySend(authenticationResult)
                }

            } catch (ex: Exception) {
                ex.printStackTrace()
                trySend(Result.failure(ex))
            }

        }.flowOn(Dispatchers.IO)

    override fun signOut() = flow<Result<Boolean>> {
        auth.signOut()
        signInClient.signOut()
        emit(Result.success(true))
    }.flowOn(Dispatchers.IO)

    override fun userDetails() = channelFlow<User> {
        try {
            val userDetails = authenticationService.userDetails(auth.getToken()).body() ?: User("","")
            trySend(userDetails)
        } catch (ex: HttpException){
            ex.printStackTrace()
            trySend(User("",""))
        } catch (ex: JsonDataException){
            ex.printStackTrace()
            trySend(User("",""))
        } catch (ex: UnknownHostException) {
            ex.printStackTrace()
            trySend(User("",""))
        } catch (ex: ConnectException) {
            ex.printStackTrace()
            trySend(User("",""))
        }
    }.flowOn(Dispatchers.IO)


    // region private functions

    private suspend fun authenticate(username: String?, nickname: String?): Result<Boolean> {
        var result: Result<Boolean>

        try {
            val usernameAndNickname =
                if (username.isNullOrEmpty() || nickname.isNullOrEmpty()) emptyMap()
                else mapOf("username" to username, "nickname" to nickname)

            val response = this.authenticationService.signIn(auth.getToken(), usernameAndNickname)

            result = Result.success(response.isSuccessful)

        } catch (ex: HttpException) {
            result = Result.failure(ex)
        } catch (ex: UnknownHostException) {
            ex.printStackTrace()
            result = Result.failure(ex)
        } catch (ex: ConnectException) {
            ex.printStackTrace()
            result = Result.failure(ex)
        }

        return result

    }

    // endregion


}