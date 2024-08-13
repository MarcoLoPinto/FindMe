package com.liner.findme.extensions

import com.google.firebase.auth.FirebaseAuth
import com.liner.findme.di.NetworkModule
import kotlinx.coroutines.tasks.await

suspend fun FirebaseAuth.getToken(): String {
    val token = this.currentUser?.getIdToken(false)?.await()?.token
    return NetworkModule.HEADER_AUTH_BEARER.plus(token)
}