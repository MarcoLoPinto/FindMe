package com.liner.findme.di

import com.liner.findme.repositories.PhotoCardRepository
import com.liner.findme.repositories.UserRepository
import com.liner.findme.worker.PhotoCardWorkerDependency
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class WorkerModule {

    @Singleton
    @Provides
    fun providePhotoCardWorkerDependency(photoCardRepository: PhotoCardRepository): PhotoCardWorkerDependency = PhotoCardWorkerDependency(photoCardRepository)

}