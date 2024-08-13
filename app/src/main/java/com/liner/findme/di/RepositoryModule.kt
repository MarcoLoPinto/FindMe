package com.liner.findme.di

import com.liner.findme.repositories.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {
    @Binds
    fun providePhotoCardsRepository(repositoryImpl: PhotoCardRepositoryImpl): PhotoCardRepository
    @Binds
    fun provideLocationRepository(repositoryImpl: LocationRepositoryImpl): LocationRepository
    @Binds
    fun provideAuthenticationRepository(repositoryImpl: AuthenticationRepositoryImpl): AuthenticationRepository
    @Binds
    fun provideUserRepository(repositoryImpl: UserRepositoryImpl): UserRepository
}