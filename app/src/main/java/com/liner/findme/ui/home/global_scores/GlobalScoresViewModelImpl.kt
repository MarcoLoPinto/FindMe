package com.liner.findme.ui.home.global_scores

import androidx.lifecycle.ViewModel
import com.liner.findme.network.models.User
import com.liner.findme.repositories.AuthenticationRepository
import com.liner.findme.repositories.PhotoCardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class GlobalScoresViewModelImpl @Inject constructor(
    private val photoCardRepository: PhotoCardRepository
) : ViewModel(), GlobalScoresViewModel {

    override fun getGlobalScores() = photoCardRepository.getGlobalScores()

}