package com.liner.findme.di

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.liner.findme.BuildConfig
import com.liner.findme.network.services.AuthenticationService
import com.liner.findme.network.services.PhotoCardService
import com.liner.findme.network.services.UserService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.net.ConnectException
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    companion object {
        private const val REQUEST_TIMEOUT: Long = 30
        private const val BASE_URL: String = "http://192.168.1.16:3000/api/" // "https://findmeserver-liner.cyclic.app/api/"
        private const val HEADER_CONTENT_TYPE = "Content-Type"
        private const val HEADER_CONTENT_TYPE_VALUE = "application/json"
        const val HEADER_AUTH = "Authorization"
        const val HEADER_AUTH_BEARER = "Bearer "
    }

    @Singleton
    @Provides
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @Provides
    fun provideOkHttpClientBuilder(): OkHttpClient.Builder = OkHttpClient.Builder()
        .connectTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)

    @Provides
    fun provideOkHttpClient(
        builder: OkHttpClient.Builder,
        auth: FirebaseAuth
    ): OkHttpClient {
        return builder
            .addInterceptor { chain ->
                try{
                    val request = chain.request().newBuilder()
                    request.addHeader(HEADER_CONTENT_TYPE, HEADER_CONTENT_TYPE_VALUE)

                    /*val token = auth.currentUser?.getIdToken(false)?.result?.token
                    request.addHeader(HEADER_AUTH, HEADER_AUTH_BEARER.plus(token))*/

                    chain.proceed(request.build())

                } catch (ex: ConnectException) {
                    chain.proceed(chain.request().newBuilder().build())
                }

            }
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level =
                        if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
                }
            ).build()
    }


    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi).withNullSerialization())
            .build()

    // SERVICEs

    @Provides
    fun providePhotoCardApi(retrofit: Retrofit) = retrofit.create(PhotoCardService::class.java)

    @Provides
    fun provideAuthenticationApi(retrofit: Retrofit) = retrofit.create(AuthenticationService::class.java)

    @Provides
    fun provideUserApi(retrofit: Retrofit) = retrofit.create(UserService::class.java)

    // endregion
}